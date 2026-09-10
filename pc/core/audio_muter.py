"""
Módulo de silenciamiento inteligente de audio de Spotify (Modo Stealth).
Utiliza CoreAudio / pycaw para silenciar exclusivamente el volumen
de Spotify durante anuncios sin afectar el resto del sistema, y lo
restaura tan pronto como comienza una canción real.
"""

from typing import Optional

try:
    from pycaw.pycaw import AudioUtilities, ISimpleAudioVolume
    from comtypes import CoInitialize, CoUninitialize
    PYCAW_AVAILABLE = True
except ImportError:
    PYCAW_AVAILABLE = False


class SpotifyAudioMuter:
    def __init__(self):
        self._is_muted = False

    @property
    def is_muted(self) -> bool:
        return self._is_muted

    def _get_spotify_volume_controls(self):
        """Busca todas las interfaces ISimpleAudioVolume de Spotify."""
        controls = []
        if not PYCAW_AVAILABLE:
            return controls

        try:
            CoInitialize()
            sessions = AudioUtilities.GetAllSessions()
            for session in sessions:
                proc = session.Process
                if proc:
                    try:
                        if "spotify" in proc.name().lower():
                            vol = session.SimpleAudioVolume
                            if vol:
                                controls.append(vol)
                    except Exception:
                        continue
        except Exception:
            pass
        return controls

    def mute(self) -> bool:
        """Silencia el canal de Spotify."""
        if not PYCAW_AVAILABLE:
            return False

        controls = self._get_spotify_volume_controls()
        success = False
        for vol in controls:
            try:
                vol.SetMute(1, None)
                success = True
            except Exception:
                pass

        if success:
            self._is_muted = True

        try:
            CoUninitialize()
        except Exception:
            pass

        return success

    def unmute(self) -> bool:
        """Restaura el volumen de Spotify."""
        if not PYCAW_AVAILABLE:
            return False

        controls = self._get_spotify_volume_controls()
        success = False
        for vol in controls:
            try:
                vol.SetMute(0, None)
                success = True
            except Exception:
                pass

        if success or len(controls) == 0:
            self._is_muted = False

        try:
            CoUninitialize()
        except Exception:
            pass

        return success
