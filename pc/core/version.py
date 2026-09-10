"""Sistema de nombres y control de versiones heredado de la arquitectura de JARVIS.

Nomenclatura unificada:
- `versionName`: Nombre semántico deducido de Conventional Commits (p. ej. '1.0.0').
- `versionCode`: Entero estrictamente creciente obtenido de `git rev-list --count HEAD`.
- `etiqueta_git` (Tag): `v<versionName>-<versionCode>` (p. ej. 'v1.0.0-4').
- `titulo_release`: `SpotiGuard <versionName> (<versionCode>)` (p. ej. 'SpotiGuard 1.0.0 (4)').
- Artefactos:
    - Windows: `SpotiGuard-Windows-<versionName>-<versionCode>.zip`
    - Android: `SpotiGuard-<versionName>-<versionCode>.apk`
"""

from __future__ import annotations

import os
import re
import subprocess
from collections.abc import Iterable
from dataclasses import dataclass
from enum import IntEnum
from pathlib import Path

#: Etiqueta de publicación estilo JARVIS: `v<mayor>.<menor>.<parche>-<codigo>`
PATRON_ETIQUETA = re.compile(r"^v(\d+)\.(\d+)\.(\d+)-(\d+)$")

#: Cabecera de Conventional Commits: `tipo(ámbito)!: descripción`
PATRON_CABECERA = re.compile(r"^(?P<tipo>[a-zA-Z]+)(?:\((?P<ambito>[^)]*)\))?(?P<rompe>!)?:")

#: Marca de cambio rompedor (major) en cuerpo o pie
PATRON_ROMPEDOR = re.compile(r"^BREAKING[ -]CHANGE:", re.MULTILINE)

#: Patrón general para archivos compilados (Windows .zip/.exe y Android .apk)
PATRON_ARTEFACTO = re.compile(
    r"^(?:SpotiGuard|SpotiSkip)(?:-(?:Windows|Setup|Android))?-(?P<nombre>[0-9][\w.\-]*?)-(?P<codigo>\d+)\.(?P<ext>exe|zip|apk)$",
    re.IGNORECASE,
)

#: Versión base de inicio del proyecto
NOMBRE_INICIAL = "1.0.0"


class Salto(IntEnum):
    """Magnitud del incremento semántico según el commit."""

    NINGUNO = 0
    PARCHE = 1
    MENOR = 2
    MAYOR = 3


SALTOS_POR_TIPO: dict[str, Salto] = {
    "feat": Salto.MENOR,
    "fix": Salto.PARCHE,
    "perf": Salto.PARCHE,
    "revert": Salto.PARCHE,
}


@dataclass(frozen=True)
class VersionApp:
    nombre: str
    version_name: str
    version_code: int


def nombre_de_la_etiqueta(etiqueta: str) -> str | None:
    """Extrae `1.0.0` de `v1.0.0-4`. Devuelve `None` si no sigue el patrón."""
    encaje = PATRON_ETIQUETA.match(etiqueta.strip())
    if encaje is None:
        return None
    mayor, menor, parche, _codigo = encaje.groups()
    return f"{mayor}.{menor}.{parche}"


def clasificar_commit(mensaje: str) -> Salto:
    """Clasifica un commit individual según Conventional Commits."""
    texto = mensaje.strip()
    if not texto:
        return Salto.NINGUNO

    if PATRON_ROMPEDOR.search(texto):
        return Salto.MAYOR

    cabecera = texto.splitlines()[0]
    encaje = PATRON_CABECERA.match(cabecera)
    if encaje is None:
        return Salto.NINGUNO

    if encaje.group("rompe"):
        return Salto.MAYOR

    return SALTOS_POR_TIPO.get(encaje.group("tipo").lower(), Salto.NINGUNO)


def salto_de(mensajes: Iterable[str]) -> Salto:
    """Determina el salto mayor de un conjunto de commits."""
    mayor = Salto.NINGUNO
    for mensaje in mensajes:
        salto = clasificar_commit(mensaje)
        if salto > mayor:
            mayor = salto
            if mayor is Salto.MAYOR:
                break
    return mayor


def aplicar_salto(nombre: str, salto: Salto) -> str:
    """Aplica el incremento a un versionName 'X.Y.Z'."""
    partes = nombre.strip().split(".")
    if len(partes) != 3 or not all(p.isdigit() for p in partes):
        return NOMBRE_INICIAL
    mayor, menor, parche = (int(p) for p in partes)

    if salto is Salto.MAYOR:
        return f"{mayor + 1}.0.0"
    if salto is Salto.MENOR:
        return f"{mayor}.{menor + 1}.0"
    if salto is Salto.PARCHE:
        return f"{mayor}.{menor}.{parche + 1}"
    return f"{mayor}.{menor}.{parche}"


def siguiente_nombre(etiqueta_previa: str | None, mensajes: Iterable[str]) -> str:
    """Calcula el siguiente versionName semántico."""
    previo = nombre_de_la_etiqueta(etiqueta_previa or "")
    if previo is None:
        # Si la etiqueta era del estilo simple 'v1.0.0', extraer '1.0.0'
        if etiqueta_previa and re.match(r"^v?\d+\.\d+\.\d+$", etiqueta_previa.strip()):
            previo = etiqueta_previa.strip().lstrip("v")
        else:
            return NOMBRE_INICIAL
    return aplicar_salto(previo, salto_de(mensajes))


# ── Nombres y formatos estándar ──────────────────────────────────────────────


def formato_etiqueta(version_name: str, version_code: int) -> str:
    """Ej: 'v1.0.0-4'"""
    return f"v{version_name}-{version_code}"


def formato_titulo_release(version_name: str, version_code: int) -> str:
    """Ej: 'SpotiGuard {version_name} ({version_code})'"""
    return f"SpotiGuard {version_name} ({version_code})"


def formato_archivo_windows(version_name: str, version_code: int) -> str:
    """Ej: 'SpotiGuard-Windows-1.0.0-4.zip'"""
    return f"SpotiGuard-Windows-{version_name}-{version_code}.zip"


def formato_archivo_apk(version_name: str, version_code: int) -> str:
    """Ej: 'SpotiGuard-1.0.0-4.apk'"""
    return f"SpotiGuard-{version_name}-{version_code}.apk"


def parsear(cadena: str) -> VersionApp | None:
    """Parsea un nombre de archivo o etiqueta y extrae su versión y código."""
    texto = cadena.strip()
    # Intentar como etiqueta git v1.0.0-4
    m_tag = PATRON_ETIQUETA.match(texto)
    if m_tag:
        mayor, menor, parche, codigo = m_tag.groups()
        return VersionApp(nombre=texto, version_name=f"{mayor}.{menor}.{parche}", version_code=int(codigo))

    # Intentar como archivo SpotiGuard-Windows-1.0.0-4.zip o SpotiGuard-1.0.0-4.apk
    m_art = PATRON_ARTEFACTO.match(texto)
    if m_art:
        return VersionApp(
            nombre=texto,
            version_name=m_art.group("nombre"),
            version_code=int(m_art.group("codigo")),
        )

    return None


def hay_actualizacion(instalada: int, disponible: int) -> bool:
    """Compara códigos enteros de versión estrictamente crecientes."""
    return disponible > instalada


def mas_nueva(nombres: list[str]) -> VersionApp | None:
    """Obtiene la versión más reciente de una lista de nombres de artefactos."""
    candidatas = [v for v in (parsear(n) for n in nombres) if v is not None]
    if not candidatas:
        return None
    return max(candidatas, key=lambda v: v.version_code)


# ── Interfaz con el repositorio Git ──────────────────────────────────────────


def _ejecutar_git(*args: str, cwd: Path | None = None) -> str:
    try:
        res = subprocess.run(
            ["git", *args],
            capture_output=True,
            text=True,
            check=False,
            timeout=15,
            cwd=cwd,
            creationflags=0x08000000 if os.name == "nt" else 0,
        )
        return res.stdout.strip() if res.returncode == 0 else ""
    except Exception:
        return ""


def obtener_version_code(raiz: Path | None = None) -> int:
    """Calcula el código de compilación basado en el recuento de commits."""
    if raiz is None:
        raiz = Path(__file__).resolve().parent.parent.parent

    # Comprobar si existe archivo VERSION sellado en el paquete
    archivo_version = raiz / "VERSION"
    if archivo_version.is_file():
        try:
            return int(archivo_version.read_text(encoding="utf-8").strip())
        except ValueError:
            pass

    salida = _ejecutar_git("rev-list", "--count", "HEAD", cwd=raiz)
    if salida.isdigit():
        return int(salida)
    return 1


def obtener_version_name(raiz: Path | None = None) -> str:
    """Calcula el versionName semántico leyendo la última etiqueta y los commits posteriores."""
    if raiz is None:
        raiz = Path(__file__).resolve().parent.parent.parent

    # Obtener última etiqueta con formato v*.*.*-*
    ultima_etiqueta = _ejecutar_git("describe", "--tags", "--abbrev=0", "--match", "v*.*.*-*", cwd=raiz)
    if not ultima_etiqueta:
        # Si no hay etiqueta v*.*.*-*, buscar cualquier etiqueta v*.*.*
        ultima_etiqueta = _ejecutar_git("describe", "--tags", "--abbrev=0", "--match", "v*.*.*", cwd=raiz)

    # Obtener mensajes de commits posteriores a esa etiqueta
    rango = f"{ultima_etiqueta}..HEAD" if ultima_etiqueta else "HEAD"
    crudo = _ejecutar_git("log", rango, "--format=%B%x00", cwd=raiz)
    mensajes = [t.strip() for t in crudo.split("\0") if t.strip()]

    return siguiente_nombre(ultima_etiqueta or None, mensajes)


def obtener_info_version(raiz: Path | None = None) -> dict[str, str | int]:
    """Devuelve diccionario consolidado con todos los nombres de la versión actual."""
    nombre = obtener_version_name(raiz)
    codigo = obtener_version_code(raiz)
    return {
        "version_name": nombre,
        "version_code": codigo,
        "tag": formato_etiqueta(nombre, codigo),
        "titulo_release": formato_titulo_release(nombre, codigo),
        "archivo_windows": formato_archivo_windows(nombre, codigo),
        "archivo_apk": formato_archivo_apk(nombre, codigo),
    }


if __name__ == "__main__":
    import sys

    info = obtener_info_version()
    if len(sys.argv) > 1 and sys.argv[1] == "--tag":
        print(info["tag"])
    elif len(sys.argv) > 1 and sys.argv[1] == "--name":
        print(info["version_name"])
    elif len(sys.argv) > 1 and sys.argv[1] == "--code":
        print(info["version_code"])
    else:
        print(f"Version Name:    {info['version_name']}")
        print(f"Version Code:    {info['version_code']}")
        print(f"Git Tag:         {info['tag']}")
        print(f"Release Title:   {info['titulo_release']}")
        print(f"Windows Package: {info['archivo_windows']}")
        print(f"Android Package: {info['archivo_apk']}")
