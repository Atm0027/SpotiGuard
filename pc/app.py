"""
Punto de entrada principal para SpotiSkip en PC.
Integra la interfaz HUD, el icono de la bandeja del sistema,
el vigilante de procesos y el motor de omisión de anuncios.
"""

import os
import sys
import json
import argparse
import ctypes
from PyQt6.QtWidgets import QApplication
from PyQt6.QtCore import QObject, pyqtSignal

# Importes locales
from core.watcher import SpotifyGuardianEngine, is_autostart_enabled, set_autostart
from ui.main_window import MainWindow
from ui.tray_icon import SpotiSkipTray

ERROR_ALREADY_EXISTS = 183


def check_single_instance():
    """Garantiza que sólo una instancia de la aplicación se ejecute a la vez en Windows."""
    try:
        kernel32 = ctypes.windll.kernel32
        mutex_name = "Global\\SpotiGuard_SingleInstance_App_Mutex"
        mutex = kernel32.CreateMutexW(None, False, mutex_name)
        if kernel32.GetLastError() == ERROR_ALREADY_EXISTS:
            try:
                import win32gui
                hwnd = win32gui.FindWindow(None, "SpotiSkip - Spotify Autonomous Guardian")
                if hwnd:
                    win32gui.ShowWindow(hwnd, 9)  # SW_RESTORE
                    win32gui.SetForegroundWindow(hwnd)
            except Exception:
                pass
            return None
        return mutex
    except Exception:
        return True


CONFIG_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "config.json")


def load_config() -> dict:
    defaults = {
        "mode": "restart",
        "minimize_to_tray_on_close": True
    }
    if os.path.exists(CONFIG_FILE):
        try:
            with open(CONFIG_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)
                defaults.update(data)
        except Exception:
            pass
    return defaults


def save_config(cfg: dict):
    try:
        with open(CONFIG_FILE, "w", encoding="utf-8") as f:
            json.dump(cfg, f, indent=4)
    except Exception:
        pass


class BridgeSignals(QObject):
    """Puente de señales seguras entre hilos de fondo y la interfaz PyQt6."""
    status_changed = pyqtSignal(str)
    track_changed = pyqtSignal(str)
    ad_detected = pyqtSignal(str)
    ad_skipped = pyqtSignal(str, int)


def main():
    mutex = check_single_instance()
    if mutex is None:
        # Instancia duplicada: ya está corriendo otra instancia
        sys.exit(0)

    parser = argparse.ArgumentParser(description="SpotiSkip - Spotify Autonomous Guardian")
    parser.add_argument("--minimized", action="store_true", help="Iniciar directamente minimizado en la bandeja")
    args = parser.parse_args()

    app = QApplication(sys.argv)
    app.setQuitOnLastWindowClosed(False)

    config = load_config()

    # Señalizador para cruzar del hilo de detección a la UI
    signals = BridgeSignals()

    # 1. Crear motor Guardián
    engine = SpotifyGuardianEngine(
        mode=config.get("mode", "restart"),
        on_status_change=signals.status_changed.emit,
        on_track_change=signals.track_changed.emit,
        on_ad_detected=signals.ad_detected.emit,
        on_ad_skipped=lambda msg, count: signals.ad_skipped.emit(msg, count)
    )

    # 2. Crear Ventana Principal
    window = MainWindow()
    window.set_mode(engine.mode)
    window.chk_autostart.setChecked(is_autostart_enabled())
    window.chk_tray.setChecked(config.get("minimize_to_tray_on_close", True))

    # 3. Crear Bandeja del Sistema
    tray = SpotiSkipTray(window)

    # Conexiones de UI hacia Motor
    def on_mode_change(new_mode: str):
        engine.set_mode(new_mode)
        config["mode"] = new_mode
        save_config(config)
        window.set_mode(new_mode)
        tray._set_mode(new_mode)

    window.mode_changed.connect(on_mode_change)
    tray.mode_changed_requested.connect(on_mode_change)

    def on_autostart_toggle(enabled: bool):
        set_autostart(enabled)
        window.append_log(f"Inicio automático con Windows: {'ACTIVADO' if enabled else 'DESACTIVADO'}")

    window.autostart_toggled.connect(on_autostart_toggle)

    def launch_spotify():
        window.append_log("Lanzando Spotify a petición del usuario...")
        engine.skipper.relaunch_spotify()

    window.launch_spotify_requested.connect(launch_spotify)
    tray.launch_spotify_requested.connect(launch_spotify)

    def toggle_engine():
        if engine._running:
            engine.stop()
            window.btn_toggle_guard.setText("▶️ Reanudar Guardián")
        else:
            engine.start()
            window.btn_toggle_guard.setText("⏸️ Pausar Guardián")

    window.toggle_engine_requested.connect(toggle_engine)

    # Conexiones de Bandeja
    def toggle_window():
        if window.isVisible():
            window.hide()
        else:
            window.showNormal()
            window.activateWindow()

    tray.toggle_window_requested.connect(toggle_window)

    def quit_all():
        engine.stop()
        config["minimize_to_tray_on_close"] = window.chk_tray.isChecked()
        save_config(config)
        app.quit()

    tray.quit_requested.connect(quit_all)

    # Conexiones de Señales Seguras hacia UI
    signals.status_changed.connect(window.update_status)
    signals.track_changed.connect(window.update_track_title)
    signals.ad_skipped.connect(
        lambda msg, count: (
            window.update_stats(count, engine.estimated_time_saved_seconds),
            tray.show_notification("⚡ SpotiSkip", msg)
        )
    )

    # Iniciar motor
    engine.start()
    tray.show()

    if not args.minimized:
        window.show()
    else:
        tray.show_notification("SpotiSkip Activo", "Guardián de Spotify ejecutándose en la bandeja.")

    sys.exit(app.exec())


if __name__ == "__main__":
    main()
