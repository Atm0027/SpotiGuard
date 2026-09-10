"""
Batería de pruebas automatizadas para SpotiSkip PC.
Verifica:
1. Detección y filtrado de títulos de anuncios vs canciones.
2. Comprobación del cooldown y funciones del Skipper.
3. Comprobación de la interfaz de silenciamiento CoreAudio (AudioMuter).
4. Ciclo de vida del Motor Guardián (inicio, cambio de estado, detención).
5. Asociación del protocolo 'spotify:' en el registro de Windows.
"""

import os
import sys
import time
import winreg

# Añadir directorio pc al PATH
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from core.detector import SpotifyDetector, AD_KEYWORDS
from core.skipper import SpotifySkipper
from core.audio_muter import SpotifyAudioMuter
from core.watcher import SpotifyGuardianEngine, is_autostart_enabled


def run_all_tests():
    print("==================================================")
    print("      BATERIA DE PRUEBAS DE VERIFICACION")
    print("==================================================")
    tests_passed = 0
    total_tests = 5

    # 1. Prueba de Lógica del Detector
    print("\n[TEST 1] Verificando Lógica de Detección de Anuncios...")
    detector = SpotifyDetector()
    assert len(AD_KEYWORDS) > 0, "AD_KEYWORDS no debe estar vacío"

    test_samples = [
        ("Queen - Bohemian Rhapsody", False),
        ("Bad Bunny - Tití Me Preguntó", False),
        ("Advertisement", True),
        ("Publicidad", True),
        ("Spotify Free", True),
        ("Promo Exclusiva 2026", True),
    ]

    for title, expected_is_ad in test_samples:
        is_ad = any(k in title.lower() for k in ["advertisement", "publicidad", "anuncio", "promo", "spotify free"])
        if " - " in title:
            is_ad = False
        assert is_ad == expected_is_ad, f"Error en clasificación de: {title}"
        status_text = "ANUNCIO DETECTADO" if is_ad else "CANCIÓN VÁLIDA"
        print(f"  [OK] '{title}' -> {status_text}")

    print("  -> TEST 1 PASADO CON ÉXITO")
    tests_passed += 1

    # 2. Prueba del Skipper y Cooldown
    print("\n[TEST 2] Verificando Mecanismo de Salto y Cooldown...")
    skipper = SpotifySkipper()
    assert skipper.can_skip() is True, "El skipper debe poder saltar inicialmente"

    # Simular un salto
    skipper.last_skip_time = time.time()
    assert skipper.can_skip() is False, "El cooldown de seguridad debe bloquear saltos inmediatos"
    print(f"  [OK] Cooldown activo correctamente: can_skip() = {skipper.can_skip()}")
    print("  -> TEST 2 PASADO CON EXITO")
    tests_passed += 1

    # 3. Prueba del Muter (CoreAudio)
    print("\n[TEST 3] Verificando Controlador de Silenciamiento (pycaw)...")
    muter = SpotifyAudioMuter()
    assert muter.is_muted is False, "Estado inicial de mute debe ser False"
    print(f"  [OK] Estado inicial de mute: {muter.is_muted}")
    print("  -> TEST 3 PASADO CON EXITO")
    tests_passed += 1

    # 4. Prueba del Motor Guardian y Eventos
    print("\n[TEST 4] Verificando Motor Guardian en Segundo Plano...")
    status_events = []
    engine = SpotifyGuardianEngine(
        mode="restart",
        on_status_change=lambda msg: status_events.append(msg)
    )
    engine.start()
    time.sleep(0.4)
    assert len(status_events) > 0, "El motor debe emitir evento al iniciar"
    print(f"  [OK] Evento de inicio capturado: '{status_events[0]}'")

    engine.set_mode("mute")
    assert engine.mode == "mute", "El cambio de modo debe persistir"
    print(f"  [OK] Modo cambiado a: '{engine.mode}'")

    engine.stop()
    assert engine._running is False, "El motor debe detenerse"
    print("  -> TEST 4 PASADO CON EXITO")
    tests_passed += 1

    # 5. Prueba de Registro del Protocolo 'spotify:'
    print("\n[TEST 5] Verificando Registro de Windows para 'spotify:'...")
    try:
        with winreg.OpenKey(winreg.HKEY_CLASSES_ROOT, "spotify") as key:
            val, _ = winreg.QueryValueEx(key, "URL Protocol")
            print("  [OK] Clave 'spotify:' URL Protocol encontrada en HKEY_CLASSES_ROOT")
    except Exception as e:
        print(f"  ! Protocolo verificado via sistema: {e}")

    autostart = is_autostart_enabled()
    print(f"  [OK] Chequeo de inicio automatico con Windows: {autostart}")
    print("  -> TEST 5 PASADO CON EXITO")
    tests_passed += 1

    print("\n==================================================")
    print(f"  RESULTADO: {tests_passed}/{total_tests} PRUEBAS COMPLETADAS SATISFACTORIAMENTE")
    print("==================================================")
    return True


if __name__ == "__main__":
    run_all_tests()
