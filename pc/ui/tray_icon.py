"""
Bandeja del sistema (System Tray) para SpotiSkip en Windows.
Permite control rápido en segundo plano, notificaciones de anuncios
saltados y minimización sin ocupar espacio en la barra de tareas.
"""

from PyQt6.QtWidgets import QSystemTrayIcon, QMenu
from PyQt6.QtGui import QIcon, QPixmap, QPainter, QColor, QBrush, QPen
from PyQt6.QtCore import Qt, pyqtSignal, QObject


def create_default_icon(color_hex: str = "#00e5ff") -> QIcon:
    """Genera dinámicamente un icono vectorial de alta resolución para la bandeja."""
    size = 64
    pixmap = QPixmap(size, size)
    pixmap.fill(Qt.GlobalColor.transparent)

    painter = QPainter(pixmap)
    painter.setRenderHint(QPainter.RenderHint.Antialiasing)

    # Círculo exterior brillante
    pen = QPen(QColor(color_hex))
    pen.setWidth(4)
    painter.setPen(pen)
    painter.setBrush(QBrush(QColor("#0f172a")))
    painter.drawEllipse(4, 4, size - 8, size - 8)

    # Ondas de sonido / radar HUD
    painter.setPen(QPen(QColor(color_hex), 3))
    painter.drawArc(16, 20, 32, 24, 30 * 16, 120 * 16)
    painter.drawArc(22, 26, 20, 16, 30 * 16, 120 * 16)

    # Punto central
    painter.setBrush(QBrush(QColor(color_hex)))
    painter.setPen(Qt.PenStyle.NoPen)
    painter.drawEllipse(30, 38, 5, 5)

    painter.end()
    return QIcon(pixmap)


class SpotiSkipTray(QObject):
    toggle_window_requested = pyqtSignal()
    quit_requested = pyqtSignal()
    mode_changed_requested = pyqtSignal(str)
    launch_spotify_requested = pyqtSignal()

    def __init__(self, parent=None):
        super().__init__(parent)
        self.tray_icon = QSystemTrayIcon(parent)
        self.tray_icon.setIcon(create_default_icon("#00e5ff"))
        self.tray_icon.setToolTip("SpotiSkip Guardian - Protección Activa")

        self._build_menu()
        self.tray_icon.activated.connect(self._on_tray_activated)

    def _build_menu(self):
        menu = QMenu()
        menu.setStyleSheet("""
            QMenu {
                background-color: #111827;
                color: #f3f4f6;
                border: 1px solid #374151;
                border-radius: 8px;
                padding: 6px;
                font-family: 'Segoe UI', sans-serif;
                font-size: 13px;
            }
            QMenu::item {
                padding: 6px 20px;
                border-radius: 4px;
            }
            QMenu::item:selected {
                background-color: #1f2937;
                color: #00e5ff;
            }
            QMenu::separator {
                height: 1px;
                background-color: #374151;
                margin: 4px 8px;
            }
        """)

        action_open = menu.addAction("Abrir SpotiSkip")
        action_open.triggered.connect(self.toggle_window_requested.emit)

        action_launch_spot = menu.addAction("Lanzar Spotify")
        action_launch_spot.triggered.connect(self.launch_spotify_requested.emit)

        menu.addSeparator()

        self.action_restart_mode = menu.addAction("✓ Modo Reinicio (Bypass)")
        self.action_restart_mode.triggered.connect(lambda: self._set_mode("restart"))

        self.action_mute_mode = menu.addAction("  Modo Silenciador (Mute)")
        self.action_mute_mode.triggered.connect(lambda: self._set_mode("mute"))

        menu.addSeparator()

        action_quit = menu.addAction("Salir")
        action_quit.triggered.connect(self.quit_requested.emit)

        self.tray_icon.setContextMenu(menu)

    def _set_mode(self, mode: str):
        if mode == "restart":
            self.action_restart_mode.setText("✓ Modo Reinicio (Bypass)")
            self.action_mute_mode.setText("  Modo Silenciador (Mute)")
        else:
            self.action_restart_mode.setText("  Modo Reinicio (Bypass)")
            self.action_mute_mode.setText("✓ Modo Silenciador (Mute)")
        self.mode_changed_requested.emit(mode)

    def _on_tray_activated(self, reason):
        if reason == QSystemTrayIcon.ActivationReason.Trigger:
            self.toggle_window_requested.emit()

    def show(self):
        self.tray_icon.show()

    def show_notification(self, title: str, message: str):
        """Muestra una notificación en el centro de actividades de Windows."""
        self.tray_icon.showMessage(
            title,
            message,
            QSystemTrayIcon.MessageIcon.Information,
            3000
        )
