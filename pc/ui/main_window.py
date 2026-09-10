"""
Ventana principal de SpotiSkip con interfaz HUD futurista (Dark/Cyan).
Muestra telemetría en tiempo real, estadísticas de tiempo ahorrado,
control de modos (Reinicio vs Mute) y registro de actividad en vivo.
"""

import os
import sys
import time
from datetime import datetime
from PyQt6.QtWidgets import (
    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QRadioButton, QButtonGroup,
    QCheckBox, QTextEdit, QFrame, QSizePolicy
)
from PyQt6.QtCore import Qt, pyqtSignal, pyqtSlot, QTimer
from PyQt6.QtGui import QFont, QIcon, QColor, QPalette
from .tray_icon import create_default_icon


STYLE_SHEET = """
QMainWindow {
    background-color: #0b0f19;
}

QWidget {
    font-family: 'Segoe UI', Arial, sans-serif;
    color: #e2e8f0;
}

/* Tarjetas y Contenedores */
QFrame.Card {
    background-color: #131b2e;
    border: 1px solid #1e293b;
    border-radius: 12px;
    padding: 14px;
}

QFrame.HeaderCard {
    background: qlineargradient(x1:0, y1:0, x2:1, y2:0, stop:0 #111827, stop:1 #0e273c);
    border: 1px solid #0284c7;
    border-radius: 12px;
}

/* Botones */
QPushButton {
    background-color: #1e293b;
    color: #38bdf8;
    border: 1px solid #0284c7;
    border-radius: 8px;
    padding: 8px 16px;
    font-weight: 600;
    font-size: 13px;
}

QPushButton:hover {
    background-color: #0284c7;
    color: #ffffff;
}

QPushButton:pressed {
    background-color: #0369a1;
}

QPushButton.ActionPrimary {
    background-color: #0284c7;
    color: #ffffff;
    border: 1px solid #38bdf8;
}

QPushButton.ActionPrimary:hover {
    background-color: #0369a1;
}

/* Radio Buttons y Checkboxes */
QRadioButton {
    font-size: 13px;
    spacing: 8px;
    color: #cbd5e1;
}

QRadioButton::indicator {
    width: 16px;
    height: 16px;
    border-radius: 8px;
    border: 2px solid #64748b;
    background-color: #0f172a;
}

QRadioButton::indicator:checked {
    border-color: #00e5ff;
    background-color: #00e5ff;
}

QCheckBox {
    font-size: 13px;
    spacing: 8px;
    color: #94a3b8;
}

QCheckBox::indicator {
    width: 16px;
    height: 16px;
    border-radius: 4px;
    border: 2px solid #64748b;
    background-color: #0f172a;
}

QCheckBox::indicator:checked {
    border-color: #00e5ff;
    background-color: #00e5ff;
}

/* Consola de logs */
QTextEdit {
    background-color: #080c14;
    color: #38bdf8;
    border: 1px solid #1e293b;
    border-radius: 8px;
    font-family: 'Consolas', 'Courier New', monospace;
    font-size: 12px;
    padding: 8px;
}
"""


class MainWindow(QMainWindow):
    mode_changed = pyqtSignal(str)
    autostart_toggled = pyqtSignal(bool)
    launch_spotify_requested = pyqtSignal()
    toggle_engine_requested = pyqtSignal()

    def __init__(self):
        super().__init__()
        self.setWindowTitle("SpotiSkip - Spotify Autonomous Guardian")
        self.setWindowIcon(create_default_icon("#00e5ff"))
        self.resize(650, 720)
        self.setMinimumSize(580, 650)
        self.setStyleSheet(STYLE_SHEET)

        self.minimize_to_tray_on_close = True
        self._init_ui()

    def _init_ui(self):
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        main_layout = QVBoxLayout(central_widget)
        main_layout.setContentsMargins(20, 20, 20, 20)
        main_layout.setSpacing(16)

        # 1. HEADER HUD
        header_frame = QFrame()
        header_frame.setProperty("class", "HeaderCard")
        header_layout = QHBoxLayout(header_frame)
        header_layout.setContentsMargins(16, 12, 16, 12)

        title_vbox = QVBoxLayout()
        app_title = QLabel("⚡ SPOTISKIP GUARDIAN")
        app_title.setStyleSheet("font-size: 18px; font-weight: 800; color: #00e5ff; letter-spacing: 1px;")
        app_subtitle = QLabel("Sistema autónomo de omisión y protección de Spotify")
        app_subtitle.setStyleSheet("font-size: 12px; color: #94a3b8;")
        title_vbox.addWidget(app_title)
        title_vbox.addWidget(app_subtitle)

        header_layout.addLayout(title_vbox)
        header_layout.addStretch()

        # Status badge
        self.status_badge = QLabel("EN ESPERA")
        self.status_badge.setStyleSheet("""
            background-color: #1e293b;
            color: #fbbf24;
            border: 1px solid #d97706;
            border-radius: 12px;
            padding: 4px 14px;
            font-size: 11px;
            font-weight: bold;
        """)
        header_layout.addWidget(self.status_badge)

        main_layout.addWidget(header_frame)

        # 2. PISTA EN REPRODUCCIÓN (LIVE HUD)
        track_frame = QFrame()
        track_frame.setProperty("class", "Card")
        track_layout = QVBoxLayout(track_frame)

        track_header = QLabel("📡 REPRODUCCIÓN DETECTADA")
        track_header.setStyleSheet("font-size: 11px; font-weight: 700; color: #64748b; letter-spacing: 1px;")
        self.lbl_track_title = QLabel("Spotify no detectado")
        self.lbl_track_title.setStyleSheet("font-size: 15px; font-weight: 600; color: #f8fafc;")
        self.lbl_track_title.setWordWrap(True)

        track_layout.addWidget(track_header)
        track_layout.addWidget(self.lbl_track_title)
        main_layout.addWidget(track_frame)

        # 3. CONTADORES / ESTADÍSTICAS
        stats_layout = QHBoxLayout()
        stats_layout.setSpacing(12)

        # Contador de saltos
        card_skips = QFrame()
        card_skips.setProperty("class", "Card")
        card_skips_vbox = QVBoxLayout(card_skips)
        lbl_skips_title = QLabel("ANUNCIOS EVITADOS")
        lbl_skips_title.setStyleSheet("font-size: 11px; color: #94a3b8; font-weight: 600;")
        self.lbl_skips_count = QLabel("0")
        self.lbl_skips_count.setStyleSheet("font-size: 26px; font-weight: 800; color: #00e5ff;")
        card_skips_vbox.addWidget(lbl_skips_title)
        card_skips_vbox.addWidget(self.lbl_skips_count)
        stats_layout.addWidget(card_skips)

        # Contador de tiempo ahorrado
        card_time = QFrame()
        card_time.setProperty("class", "Card")
        card_time_vbox = QVBoxLayout(card_time)
        lbl_time_title = QLabel("TIEMPO AHORRADO")
        lbl_time_title.setStyleSheet("font-size: 11px; color: #94a3b8; font-weight: 600;")
        self.lbl_time_saved = QLabel("0 s")
        self.lbl_time_saved.setStyleSheet("font-size: 26px; font-weight: 800; color: #10b981;")
        card_time_vbox.addWidget(lbl_time_title)
        card_time_vbox.addWidget(self.lbl_time_saved)
        stats_layout.addWidget(card_time)

        main_layout.addLayout(stats_layout)

        # 4. CONFIGURACIÓN DE PROTECCIÓN
        settings_frame = QFrame()
        settings_frame.setProperty("class", "Card")
        settings_layout = QVBoxLayout(settings_frame)
        settings_layout.setSpacing(10)

        lbl_settings_header = QLabel("⚙️ CONFIGURACIÓN DE OPERACIÓN")
        lbl_settings_header.setStyleSheet("font-size: 11px; font-weight: 700; color: #64748b; letter-spacing: 1px;")
        settings_layout.addWidget(lbl_settings_header)

        # Selector de modo
        mode_layout = QHBoxLayout()
        self.radio_restart = QRadioButton("Reinicio Rápido (Cierra y reabre Spotify en 1s)")
        self.radio_restart.setChecked(True)
        self.radio_restart.setToolTip("El método clásico: mata Spotify, lo relanza y envía Play para saltar la cola.")
        self.radio_restart.toggled.connect(self._on_mode_toggled)

        self.radio_mute = QRadioButton("Silenciador Furtivo (Mute)")
        self.radio_mute.setToolTip("Silencia exclusivamente a Spotify con CoreAudio durante el anuncio y lo desmutea en la canción.")
        self.radio_mute.toggled.connect(self._on_mode_toggled)

        self.mode_group = QButtonGroup(self)
        self.mode_group.addButton(self.radio_restart)
        self.mode_group.addButton(self.radio_mute)

        mode_layout.addWidget(self.radio_restart)
        mode_layout.addWidget(self.radio_mute)
        settings_layout.addLayout(mode_layout)

        # Checkboxes
        opts_layout = QHBoxLayout()
        self.chk_autostart = QCheckBox("Iniciar con Windows (Siempre activo en segundo plano)")
        self.chk_autostart.toggled.connect(self.autostart_toggled.emit)

        self.chk_tray = QCheckBox("Minimizar a la bandeja al cerrar")
        self.chk_tray.setChecked(True)
        self.chk_tray.toggled.connect(self._on_tray_toggle)

        opts_layout.addWidget(self.chk_autostart)
        opts_layout.addWidget(self.chk_tray)
        settings_layout.addLayout(opts_layout)

        # Botones de acción rápida
        btn_layout = QHBoxLayout()
        self.btn_launch = QPushButton("🚀 Lanzar Spotify")
        self.btn_launch.setProperty("class", "ActionPrimary")
        self.btn_launch.clicked.connect(self.launch_spotify_requested.emit)

        self.btn_toggle_guard = QPushButton("⏸️ Pausar Guardián")
        self.btn_toggle_guard.clicked.connect(self.toggle_engine_requested.emit)

        btn_layout.addWidget(self.btn_launch)
        btn_layout.addWidget(self.btn_toggle_guard)
        settings_layout.addLayout(btn_layout)

        main_layout.addWidget(settings_frame)

        # 5. REGISTRO DE ACTIVIDAD EN VIVO (HUD TERMINAL)
        lbl_log_header = QLabel("📋 REGISTRO DE EVENTOS")
        lbl_log_header.setStyleSheet("font-size: 11px; font-weight: 700; color: #64748b; letter-spacing: 1px;")
        main_layout.addWidget(lbl_log_header)

        self.log_console = QTextEdit()
        self.log_console.setReadOnly(True)
        main_layout.addWidget(self.log_console)

        self.append_log("SpotiSkip inicializado. Motor listo.")

    def _on_mode_toggled(self):
        if self.radio_restart.isChecked():
            self.mode_changed.emit("restart")
            self.append_log("Modo establecido en: REINICIO RÁPIDO (Cerrar + Relanzar)")
        else:
            self.mode_changed.emit("mute")
            self.append_log("Modo establecido en: SILENCIADOR FURTIVO (Mute)")

    def _on_tray_toggle(self, checked: bool):
        self.minimize_to_tray_on_close = checked

    def set_mode(self, mode: str):
        if mode == "restart":
            self.radio_restart.setChecked(True)
        else:
            self.radio_mute.setChecked(True)

    @pyqtSlot(str)
    def update_track_title(self, title: str):
        self.lbl_track_title.setText(title)

    @pyqtSlot(str)
    def update_status(self, message: str):
        self.append_log(message)
        msg_upper = message.upper()
        if "DETECTADO" in msg_upper or "RESTAURADO" in msg_upper:
            self.status_badge.setText("● ACTIVO")
            self.status_badge.setStyleSheet("""
                background-color: #064e3b;
                color: #34d399;
                border: 1px solid #059669;
                border-radius: 12px;
                padding: 4px 14px;
                font-size: 11px;
                font-weight: bold;
            """)
        elif "ANUNCIO" in msg_upper or "SALTANDO" in msg_upper:
            self.status_badge.setText("⚡ BYPASS")
            self.status_badge.setStyleSheet("""
                background-color: #881337;
                color: #fb7185;
                border: 1px solid #e11d48;
                border-radius: 12px;
                padding: 4px 14px;
                font-size: 11px;
                font-weight: bold;
            """)
        elif "CERRADO" in msg_upper or "ESPERA" in msg_upper:
            self.status_badge.setText("○ EN ESPERA")
            self.status_badge.setStyleSheet("""
                background-color: #1e293b;
                color: #fbbf24;
                border: 1px solid #d97706;
                border-radius: 12px;
                padding: 4px 14px;
                font-size: 11px;
                font-weight: bold;
            """)

    @pyqtSlot(int, int)
    def update_stats(self, count: int, saved_seconds: int):
        self.lbl_skips_count.setText(str(count))
        minutes = saved_seconds // 60
        seconds = saved_seconds % 60
        if minutes > 0:
            self.lbl_time_saved.setText(f"{minutes} min {seconds:02d} s")
        else:
            self.lbl_time_saved.setText(f"{seconds} s")

    def append_log(self, text: str):
        now_str = datetime.now().strftime("%H:%M:%S")
        self.log_console.append(f"[{now_str}] {text}")

    def closeEvent(self, event):
        """Si la casilla de minimizar a bandeja está activa, oculta la ventana."""
        if self.minimize_to_tray_on_close:
            event.ignore()
            self.hide()
        else:
            event.accept()
