"""
Detector de estado y anuncios de Spotify en Windows.
Combina la inspección de títulos de ventana (Win32 API) con
la telemetría de sesiones de audio activas (pycaw CoreAudio).
"""

import sys
import psutil
import win32gui
import win32process
from typing import Optional, Tuple, List

try:
    from pycaw.pycaw import AudioUtilities, ISimpleAudioVolume
    from comtypes import CoInitialize, CoUninitialize
    PYCAW_AVAILABLE = True
except ImportError:
    PYCAW_AVAILABLE = False

try:
    import asyncio
    from winsdk.windows.media.control import GlobalSystemMediaTransportControlsSessionManager as SMTCManager
    WINSDK_AVAILABLE = True
except ImportError:
    WINSDK_AVAILABLE = False


AD_KEYWORDS = [
    "advertisement",
    "publicidad",
    "anuncio",
    "anuncios",
    "spotify free",
    "spotify",
    "promo",
    "werbung",
    "publicité",
    "publicite",
    "publicidade",
    "pubblicità",
    "pubblicita",
    "escúchalo ahora",
    "escuchalo ahora",
    "escucha sin límites",
    "escucha sin limites",
    "hazte premium",
    "prueba premium",
    "descubre premium",
    "audio publicitario",
    "vídeo publicitario",
    "video publicitario",
    "patrocinado",
    "sponsored"
]


class SpotifyDetector:
    def __init__(self):
        self._last_title = ""
        self._is_active = False

    @staticmethod
    def is_spotify_running() -> bool:
        """Verifica si algún proceso de Spotify está en ejecución."""
        for proc in psutil.process_iter(['name']):
            try:
                name = proc.info['name']
                if name and 'spotify' in name.lower():
                    return True
            except (psutil.NoSuchProcess, psutil.AccessDenied):
                continue
        return False

    @staticmethod
    def get_spotify_pids() -> List[int]:
        """Obtiene la lista de PIDs asociados a Spotify."""
        pids = []
        for proc in psutil.process_iter(['pid', 'name']):
            try:
                name = proc.info['name']
                if name and 'spotify' in name.lower():
                    pids.append(proc.info['pid'])
            except (psutil.NoSuchProcess, psutil.AccessDenied):
                continue
        return pids

    def get_spotify_window_title(self) -> Optional[str]:
        """
        Encuentra la ventana principal de Spotify y obtiene su título.
        """
        spotify_pids = set(self.get_spotify_pids())
        if not spotify_pids:
            return None

        found_titles = []

        def enum_windows_callback(hwnd, _):
            if win32gui.IsWindow(hwnd):
                try:
                    _, pid = win32process.GetWindowThreadProcessId(hwnd)
                    if pid in spotify_pids:
                        title = win32gui.GetWindowText(hwnd).strip()
                        if title:
                            # Ignorar ventanas internas auxiliares de CEF
                            if title not in ["Angle", "Default IME", "MSCTFIME UI"]:
                                found_titles.append(title)
                except Exception:
                    pass
            return True

        try:
            win32gui.EnumWindows(enum_windows_callback, None)
        except Exception:
            pass

        if not found_titles:
            return None

        # Priorizar títulos que contengan ' - ' (canciones) o palabras de anuncios
        for t in found_titles:
            if " - " in t:
                return t
        for t in found_titles:
            if any(k in t.lower() for k in AD_KEYWORDS):
                return t

        # Si hay algún título representativo, devolver el primero no vacío
        return found_titles[0]

    def is_spotify_playing_audio(self) -> bool:
        """
        Consulta pycaw para comprobar si Spotify tiene una sesión de audio activa.
        AudioSessionState: 0 = Inactive, 1 = Active, 2 = Expired.
        """
        if not PYCAW_AVAILABLE:
            return True  # Si pycaw no está disponible, asumimos activo

        try:
            CoInitialize()
            sessions = AudioUtilities.GetAllSessions()
            for session in sessions:
                proc = session.Process
                if proc:
                    try:
                        p_name = proc.name().lower()
                        if "spotify" in p_name:
                            # 1 significa AudioSessionStateActive
                            if session.State == 1:
                                return True
                    except Exception:
                        continue
        except Exception:
            pass
        finally:
            try:
                CoUninitialize()
            except Exception:
                pass

        return False

    def get_smtc_playback_info(self) -> Optional[Tuple[str, str, int]]:
        """
        Consulta SMTC (System Media Transport Controls) para Spotify.
        Retorna (title, artist, playback_status_int) o None.
        PlaybackStatus: 4 = Playing, 5 = Paused, 3 = Stopped.
        """
        if not WINSDK_AVAILABLE:
            return None
        try:
            async def _query():
                mgr = await SMTCManager.request_async()
                for s in mgr.get_sessions():
                    if 'spotify' in s.source_app_user_model_id.lower():
                        props = await s.try_get_media_properties_async()
                        info = s.get_playback_info()
                        status = info.playback_status.value if info and info.playback_status else 0
                        return props.title or "", props.artist or "", status
                return None
            return asyncio.run(_query())
        except Exception:
            return None

    def check_playback_state(self) -> Tuple[bool, bool, str]:
        """
        Evalúa el estado actual combinando SMTC nativo, títulos Win32 y pycaw.
        Retorna:
            (is_running: bool, is_ad: bool, current_title: str)
        """
        if not self.is_spotify_running():
            return False, False, "Spotify no está abierto"

        # 1. Inspección de alta precisión mediante Windows SMTC
        smtc_data = self.get_smtc_playback_info()
        if smtc_data:
            s_title, s_artist, s_status = smtc_data
            s_title_lower = s_title.lower()
            s_artist_lower = s_artist.lower()

            # Comprobar si coincide con palabras clave de publicidad
            is_ad_by_kw = any(k in s_title_lower for k in AD_KEYWORDS) or any(k in s_artist_lower for k in ["spotify", "publicidad", "advertisement"])

            # Si está reproduciendo activamente (PlaybackStatus == 4)
            if s_status == 4:
                if is_ad_by_kw or (not s_artist and s_title_lower in ["spotify", "spotify free", "escúchalo ahora", "escuchalo ahora"]):
                    return True, True, f"Anuncio detectado: {s_title or 'Spotify Promo'}"
                if s_artist and s_title:
                    return True, False, f"{s_artist} - {s_title}"
            elif s_status == 5:
                # Pausado: si es anuncio pausado, indicarlo
                if is_ad_by_kw:
                    return True, True, f"Anuncio pausado: {s_title}"
                if s_artist and s_title:
                    return True, False, f"{s_artist} - {s_title} (Pausa)"

        # 2. Inspección secundaria mediante títulos de ventana Win32 + pycaw
        title = self.get_spotify_window_title()
        is_playing = self.is_spotify_playing_audio()

        if not title:
            if is_playing:
                return True, False, "Reproduciendo (Segundo plano)"
            return True, False, "Spotify en espera"

        title_lower = title.lower()

        # Si el título tiene el formato típico 'Artista - Canción' o 'Canción - Artista', es música
        if " - " in title:
            return True, False, title

        # Si contiene palabras explícitas o frases de anuncios
        if any(ad_kw in title_lower for ad_kw in AD_KEYWORDS):
            return True, True, title

        # Si el título es exactamente "Spotify" o "Spotify Free"
        if title_lower in ["spotify", "spotify free"]:
            if is_playing:
                return True, True, f"Anuncio detectado ({title})"
            return True, False, "Spotify en espera"

        # Si no hay guión y está sonando audio
        if is_playing:
            return True, True, f"Posible anuncio: {title}"

        return True, False, title
