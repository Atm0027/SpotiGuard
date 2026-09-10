"""
Vigilante y orquestador en segundo plano para SpotiSkip en PC.
Monitorea la apertura de Spotify, gestiona el bucle de detección
en tiempo real y coordina el salto automático de anuncios o el muteo.
Incluye utilidades para autoarranque con Windows.
"""

import os
import sys
import time
import winreg
import threading
from typing import Callable, Optional
from .detector import SpotifyDetector
from .skipper import SpotifySkipper
from .audio_muter import SpotifyAudioMuter

APP_REG_KEY = r"Software\Microsoft\Windows\CurrentVersion\Run"
APP_NAME = "SpotiSkipGuardian"


class SpotifyGuardianEngine:
    def __init__(
        self,
        mode: str = "restart",  # "restart" o "mute"
        on_status_change: Optional[Callable[[str], None]] = None,
        on_track_change: Optional[Callable[[str], None]] = None,
        on_ad_detected: Optional[Callable[[str], None]] = None,
        on_ad_skipped: Optional[Callable[[str, int], None]] = None
    ):
        self.mode = mode
        self.on_status_change = on_status_change
        self.on_track_change = on_track_change
        self.on_ad_detected = on_ad_detected
        self.on_ad_skipped = on_ad_skipped

        self.detector = SpotifyDetector()
        self.skipper = SpotifySkipper()
        self.muter = SpotifyAudioMuter()

        self._running = False
        self._thread: Optional[threading.Thread] = None

        self.total_ads_skipped = 0
        self.estimated_time_saved_seconds = 0
        self._last_known_title = ""
        self._spotify_was_running = False

    def start(self):
        """Inicia el motor guardián en segundo plano."""
        if self._running:
            return
        self._running = True
        self._thread = threading.Thread(target=self._run_loop, daemon=True)
        self._thread.start()
        self._notify_status("Guardián iniciado. Esperando a Spotify...")

    def stop(self):
        """Detiene el motor guardián."""
        self._running = False
        if self.muter.is_muted:
            self.muter.unmute()
        self._notify_status("Guardián detenido.")

    def set_mode(self, mode: str):
        """Cambia el modo de operación ('restart' o 'mute')."""
        self.mode = mode
        if mode == "restart" and self.muter.is_muted:
            self.muter.unmute()
        self._notify_status(f"Modo cambiado a: {'Reinicio Rápido' if mode == 'restart' else 'Silenciador Furtivo'}")

    def _notify_status(self, msg: str):
        if self.on_status_change:
            try:
                self.on_status_change(msg)
            except Exception:
                pass

    def _notify_track(self, title: str):
        if self.on_track_change and title != self._last_known_title:
            self._last_known_title = title
            try:
                self.on_track_change(title)
            except Exception:
                pass

    def _run_loop(self):
        """Bucle principal de monitoreo reactivo."""
        while self._running:
            try:
                is_running, is_ad, title = self.detector.check_playback_state()

                # Transición de estado de Spotify (abierto / cerrado)
                if is_running and not self._spotify_was_running:
                    self._spotify_was_running = True
                    self._notify_status("Spotify DETECTADO. Protección activa.")
                elif not is_running and self._spotify_was_running:
                    self._spotify_was_running = False
                    self._notify_status("Spotify CERRADO. En espera...")
                    if self.muter.is_muted:
                        self.muter.unmute()

                if not is_running:
                    # Bajo consumo pero respuesta inmediata cuando Spotify se abre
                    time.sleep(0.7)
                    continue

                self._notify_track(title)

                if is_ad:
                    if self.on_ad_detected:
                        try:
                            self.on_ad_detected(title)
                        except Exception:
                            pass

                    if self.mode == "restart":
                        if self.skipper.can_skip():
                            self._notify_status("Anuncio detectado -> Saltando y reiniciando Spotify...")
                            success, result_msg = self.skipper.skip_and_restart()
                            if success:
                                self.total_ads_skipped += 1
                                self.estimated_time_saved_seconds += 30
                                if self.on_ad_skipped:
                                    try:
                                        self.on_ad_skipped(result_msg, self.total_ads_skipped)
                                    except Exception:
                                        pass
                                self._notify_status("Spotify restaurado. Reproduciendo música.")
                                time.sleep(1.5)
                                continue
                    elif self.mode == "mute":
                        if not self.muter.is_muted:
                            self.muter.mute()
                            self.total_ads_skipped += 1
                            self.estimated_time_saved_seconds += 30
                            self._notify_status(f"Anuncio silenciado: {title}")
                            if self.on_ad_skipped:
                                try:
                                    self.on_ad_skipped("Anuncio silenciado en segundo plano", self.total_ads_skipped)
                                except Exception:
                                    pass
                else:
                    # Es una canción real: asegurar que el audio no esté silenciado
                    if self.muter.is_muted:
                        self.muter.unmute()
                        self._notify_status("Canción detectada -> Audio restaurado.")

                time.sleep(0.7)

            except Exception as e:
                time.sleep(1.0)


# Utilidades de Auto-inicio con Windows
def is_autostart_enabled() -> bool:
    """Comprueba si SpotiSkip está registrado en el inicio de Windows."""
    try:
        with winreg.OpenKey(winreg.HKEY_CURRENT_USER, APP_REG_KEY, 0, winreg.KEY_READ) as key:
            winreg.QueryValueEx(key, APP_NAME)
            return True
    except FileNotFoundError:
        return False
    except Exception:
        return False


def set_autostart(enable: bool) -> bool:
    """Activa o desactiva el inicio automático con Windows."""
    try:
        with winreg.OpenKey(winreg.HKEY_CURRENT_USER, APP_REG_KEY, 0, winreg.KEY_SET_VALUE) as key:
            if enable:
                script_path = os.path.abspath(sys.argv[0])
                # Usar pythonw para arranque silencioso si es ejecutable o script
                if script_path.endswith(".py"):
                    python_exe = sys.executable.replace("python.exe", "pythonw.exe")
                    cmd = f'"{python_exe}" "{script_path}" --minimized'
                else:
                    cmd = f'"{script_path}" --minimized'
                winreg.SetValueEx(key, APP_NAME, 0, winreg.REG_SZ, cmd)
            else:
                try:
                    winreg.DeleteValue(key, APP_NAME)
                except FileNotFoundError:
                    pass
        return True
    except Exception:
        return False
