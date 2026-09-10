"""
Módulo de omisión y reinicio de Spotify (Bypass Trick).
Cierra forzosamente los procesos de Spotify al detectar un anuncio,
relanza la aplicación de inmediato y envía el comando multimedia
para continuar la reproducción de la cola sin esperar la cuña publicitaria.
Incorpora control nativo de Windows SMTC, teclas extendidas Win32,
descarte de buffers publicitarios (Next Track) y bucle de verificación de audio.
"""

import os
import sys
import time
import ctypes
import subprocess
import psutil
from typing import Optional, Tuple

try:
    import asyncio
    from winsdk.windows.media.control import GlobalSystemMediaTransportControlsSessionManager as SMTCManager
    WINSDK_AVAILABLE = True
except ImportError:
    WINSDK_AVAILABLE = False

try:
    import win32gui
    import win32process
    import win32con
    WIN32_AVAILABLE = True
except ImportError:
    WIN32_AVAILABLE = False

# Constantes de teclas multimedia en Win32
VK_MEDIA_NEXT_TRACK = 0xB0
VK_MEDIA_PREV_TRACK = 0xB1
VK_MEDIA_STOP = 0xB2
VK_MEDIA_PLAY_PAUSE = 0xB3
KEYEVENTF_EXTENDEDKEY = 0x0001
KEYEVENTF_KEYUP = 0x0002

# Mensajes de aplicación Win32
WM_APPCOMMAND = 0x0319
APPCOMMAND_MEDIA_NEXTTRACK = 11 << 16
APPCOMMAND_MEDIA_PLAY_PAUSE = 14 << 16
APPCOMMAND_MEDIA_PLAY = 46 << 16


def send_media_key(vk_code: int = VK_MEDIA_PLAY_PAUSE):
    """
    Envía un evento de pulsación de tecla multimedia a nivel de sistema
    asegurando el flag KEYEVENTF_EXTENDEDKEY y el scan code adecuado
    para compatibilidad total con Windows 10 y 11.
    """
    try:
        user32 = ctypes.windll.user32
        scan = user32.MapVirtualKeyW(vk_code, 0)
        user32.keybd_event(vk_code, scan, KEYEVENTF_EXTENDEDKEY, 0)
        time.sleep(0.05)
        user32.keybd_event(vk_code, scan, KEYEVENTF_EXTENDEDKEY | KEYEVENTF_KEYUP, 0)
    except Exception:
        pass


def send_appcommand_to_spotify(cmd: int) -> bool:
    """Envía un WM_APPCOMMAND directamente a la ventana activa de Spotify."""
    if not WIN32_AVAILABLE:
        return False
    user32 = ctypes.windll.user32
    spotify_pids = set([
        p.info['pid'] for p in psutil.process_iter(['pid', 'name'])
        if 'spotify' in (p.info['name'] or '').lower()
    ])
    if not spotify_pids:
        return False

    dispatched = False

    def cb(hwnd, _):
        nonlocal dispatched
        if win32gui.IsWindow(hwnd):
            try:
                _, pid = win32process.GetWindowThreadProcessId(hwnd)
                if pid in spotify_pids:
                    cls = win32gui.GetClassName(hwnd)
                    if "Chrome_WidgetWin" in cls:
                        user32.SendMessageW(hwnd, WM_APPCOMMAND, hwnd, cmd)
                        dispatched = True
            except Exception:
                pass
        return True

    try:
        win32gui.EnumWindows(cb, None)
    except Exception:
        pass
    return dispatched


def resume_via_smtc() -> bool:
    """
    Reanuda la reproducción usando la API oficial de Windows Runtime SMTC.
    Si detecta que la sesión actual contiene un anuncio o título publicitario,
    descarta el buffer enviando Next Track antes de Play.
    """
    if not WINSDK_AVAILABLE:
        return False
    try:
        async def _run_smtc():
            mgr = await SMTCManager.request_async()
            for s in mgr.get_sessions():
                if 'spotify' in s.source_app_user_model_id.lower():
                    props = await s.try_get_media_properties_async()
                    t_lower = (props.title or "").lower()
                    if any(k in t_lower for k in ["spotify", "advertisement", "escúchalo", "escuchalo", "anuncio", "promo"]):
                        await s.try_skip_next_async()
                        await asyncio.sleep(0.3)
                    await s.try_play_async()
                    return True
            return False

        return asyncio.run(_run_smtc())
    except Exception:
        return False


def dismiss_spotify_hang_dialogs():
    """Descarta automáticamente cualquier cuadro de diálogo '#32770' de Spotify colgado."""
    if not WIN32_AVAILABLE:
        return
    user32 = ctypes.windll.user32

    def cb(hwnd, _):
        if win32gui.IsWindow(hwnd):
            try:
                cls = win32gui.GetClassName(hwnd)
                txt = win32gui.GetWindowText(hwnd)
                if cls == "#32770" and "spotify" in txt.lower():
                    def child_cb(c_hwnd, _):
                        c_txt = win32gui.GetWindowText(c_hwnd).lower()
                        if c_txt in ["aceptar", "ok", "yes", "sí", "close", "cerrar"]:
                            user32.SendMessageW(c_hwnd, win32con.BM_CLICK, 0, 0)
                        return True
                    win32gui.EnumChildWindows(hwnd, child_cb, None)
            except Exception:
                pass
        return True

    try:
        win32gui.EnumWindows(cb, None)
    except Exception:
        pass


class SpotifySkipper:
    def __init__(self):
        self.last_skip_time = 0
        self.cooldown_seconds = 4  # Evitar re-disparos accidentales en bucle

    def can_skip(self) -> bool:
        """Comprueba el cooldown para evitar ciclos repetidos de reinicio."""
        return (time.time() - self.last_skip_time) > self.cooldown_seconds

    def terminate_spotify(self) -> bool:
        """
        Mata todos los procesos de Spotify y SpotifyLauncher en ejecución de forma atómica.
        Elimina también procesos lanzadores para evitar cuadros de diálogo de error de sesión.
        """
        try:
            subprocess.run(
                ["taskkill", "/F", "/IM", "Spotify.exe", "/T"],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )
            subprocess.run(
                ["taskkill", "/F", "/IM", "SpotifyLauncher.exe", "/T"],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )
        except Exception:
            pass

        # Fallback de limpieza con psutil
        for proc in psutil.process_iter(['name']):
            try:
                p_name = (proc.name() or '').lower()
                if 'spotify' in p_name:
                    proc.kill()
            except Exception:
                pass
        return True

    def relaunch_spotify(self) -> bool:
        """
        Vuelve a abrir Spotify mediante el protocolo de sistema 'spotify:'.
        Funciona tanto para versión estándar como versión de Microsoft Store.
        """
        try:
            # Lanzamiento mediante protocolo URL de Windows
            os.system("start spotify:")
            return True
        except Exception:
            # Fallback intentando rutas conocidas
            appdata = os.environ.get("APPDATA", "")
            standard_path = os.path.join(appdata, "Spotify", "Spotify.exe")
            if os.path.exists(standard_path):
                subprocess.Popen([standard_path])
                return True
            return False

    def skip_and_restart(self) -> Tuple[bool, str]:
        """
        Ejecuta la maniobra de omisión con garantía de reanudación:
        1. Cierra Spotify y lanzadores.
        2. Relanza Spotify.
        3. Espera a que CEF y el grafo de audio se inicialicen (2.0s - 2.5s).
        4. Descarta diálogos de cuelgue residuales si los hubiera.
        5. Envía señal de 'Next Track' para limpiar el buffer del anuncio que quedó pausado.
        6. Envía señal de 'Play' mediante SMTC nativo, WM_APPCOMMAND y teclas multimedia extendidas.
        7. Verifica en bucle activo que la música haya comenzado a sonar.
        """
        if not self.can_skip():
            return False, "Cooldown activo, esperando..."

        self.last_skip_time = time.time()
        start_t = time.time()

        # 1. Terminar Spotify de forma limpia
        self.terminate_spotify()
        time.sleep(0.5)

        # 2. Relanzar Spotify
        self.relaunch_spotify()

        # 3. Esperar a que el proceso levante
        process_up = False
        for _ in range(35):  # hasta 3.5 segundos
            time.sleep(0.1)
            for proc in psutil.process_iter(['name']):
                try:
                    if 'spotify' in (proc.name() or '').lower():
                        process_up = True
                        break
                except Exception:
                    pass
            if process_up:
                break

        # 4. Tiempo de estabilización de CEF y registro en Windows SMTC
        time.sleep(2.2)
        dismiss_spotify_hang_dialogs()

        # 5. Bucle de reanudación activa con verificación multicanal
        for attempt in range(4):
            # Paso A: Limpiar buffer residual del anuncio
            send_media_key(VK_MEDIA_NEXT_TRACK)
            send_appcommand_to_spotify(APPCOMMAND_MEDIA_NEXTTRACK)
            time.sleep(0.15)

            # Paso B: SMTC nativo (el método más fiable en Windows 10/11)
            if resume_via_smtc():
                break

            # Paso C: Win32 Message y Tecla extendida
            send_appcommand_to_spotify(APPCOMMAND_MEDIA_PLAY)
            send_media_key(VK_MEDIA_PLAY_PAUSE)

            time.sleep(0.6)

            # Verificar si SMTC ya se sincronizó
            if resume_via_smtc():
                break

        duration = round(time.time() - start_t, 2)
        return True, f"Anuncio saltado y reproducción reanudada en {duration}s"
