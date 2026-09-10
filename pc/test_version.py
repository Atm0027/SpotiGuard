"""Batería de pruebas unitarias para el sistema de nomenclatura de versiones estilo JARVIS."""

import unittest
from pc.core.version import (
    Salto,
    VersionApp,
    clasificar_commit,
    salto_de,
    aplicar_salto,
    siguiente_nombre,
    formato_etiqueta,
    formato_titulo_release,
    formato_archivo_windows,
    formato_archivo_apk,
    parsear,
    hay_actualizacion,
    mas_nueva,
)


class TestVersionSemantica(unittest.TestCase):
    def test_clasificar_commit_feat(self):
        self.assertEqual(clasificar_commit("feat: añadir soporte para Android 14"), Salto.MENOR)
        self.assertEqual(clasificar_commit("feat(core): nueva heurística de detección"), Salto.MENOR)

    def test_clasificar_commit_fix(self):
        self.assertEqual(clasificar_commit("fix: corregir error en reconexión"), Salto.PARCHE)
        self.assertEqual(clasificar_commit("perf: optimizar tiempo de inicio"), Salto.PARCHE)
        self.assertEqual(clasificar_commit("revert: deshacer commit anterior"), Salto.PARCHE)

    def test_clasificar_commit_breaking(self):
        self.assertEqual(clasificar_commit("feat!: cambio de arquitectura incompatible"), Salto.MAYOR)
        self.assertEqual(
            clasificar_commit("refactor: reescribir motor\n\nBREAKING CHANGE: la API cambia"),
            Salto.MAYOR,
        )

    def test_clasificar_commit_no_relevante(self):
        self.assertEqual(clasificar_commit("docs: actualizar README"), Salto.NINGUNO)
        self.assertEqual(clasificar_commit("chore: limpieza de archivos"), Salto.NINGUNO)
        self.assertEqual(clasificar_commit("test: añadir tests"), Salto.NINGUNO)

    def test_salto_de_lote(self):
        mensajes = [
            "docs: actualizar README",
            "fix: resolver bug de volumen",
            "chore: formato de código",
        ]
        self.assertEqual(salto_de(mensajes), Salto.PARCHE)

        mensajes_con_feat = [
            "docs: actualizar README",
            "fix: resolver bug",
            "feat: nueva interfaz HUD",
        ]
        self.assertEqual(salto_de(mensajes_con_feat), Salto.MENOR)

    def test_aplicar_salto(self):
        self.assertEqual(aplicar_salto("1.0.0", Salto.PARCHE), "1.0.1")
        self.assertEqual(aplicar_salto("1.0.0", Salto.MENOR), "1.1.0")
        self.assertEqual(aplicar_salto("1.0.0", Salto.MAYOR), "2.0.0")
        self.assertEqual(aplicar_salto("1.0.0", Salto.NINGUNO), "1.0.0")

    def test_siguiente_nombre(self):
        self.assertEqual(siguiente_nombre("v1.0.0-3", ["fix: corrección"]), "1.0.1")
        self.assertEqual(siguiente_nombre("v1.0.0-3", ["feat: nueva funcionalidad"]), "1.1.0")
        self.assertEqual(siguiente_nombre("v1.0.0-3", ["docs: solo documentación"]), "1.0.0")


class TestFormatosYNombres(unittest.TestCase):
    def test_formatos_estandar(self):
        self.assertEqual(formato_etiqueta("1.0.0", 4), "v1.0.0-4")
        self.assertEqual(formato_titulo_release("1.0.0", 4), "SpotiGuard 1.0.0 (4)")
        self.assertEqual(formato_archivo_windows("1.0.0", 4), "SpotiGuard-Windows-1.0.0-4.zip")
        self.assertEqual(formato_archivo_apk("1.0.0", 4), "SpotiGuard-1.0.0-4.apk")

    def test_parsear_etiqueta(self):
        v = parsear("v1.0.0-4")
        self.assertIsNotNone(v)
        self.assertEqual(v.version_name, "1.0.0")
        self.assertEqual(v.version_code, 4)

    def test_parsear_archivo_windows(self):
        v = parsear("SpotiGuard-Windows-1.0.0-4.zip")
        self.assertIsNotNone(v)
        self.assertEqual(v.version_name, "1.0.0")
        self.assertEqual(v.version_code, 4)

    def test_parsear_archivo_apk(self):
        v = parsear("SpotiGuard-1.0.0-4.apk")
        self.assertIsNotNone(v)
        self.assertEqual(v.version_name, "1.0.0")
        self.assertEqual(v.version_code, 4)

    def test_hay_actualizacion(self):
        self.assertTrue(hay_actualizacion(instalada=3, disponible=4))
        self.assertFalse(hay_actualizacion(instalada=4, disponible=4))
        self.assertFalse(hay_actualizacion(instalada=5, disponible=4))

    def test_mas_nueva(self):
        archivos = [
            "SpotiGuard-Windows-1.0.0-2.zip",
            "SpotiGuard-Windows-1.0.0-4.zip",
            "SpotiGuard-Windows-1.0.0-1.zip",
            "invalido.zip",
        ]
        top = mas_nueva(archivos)
        self.assertIsNotNone(top)
        self.assertEqual(top.version_code, 4)


if __name__ == "__main__":
    unittest.main()
