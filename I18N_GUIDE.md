# Guía de Internacionalización (i18n)

## Principios
- Toda cadena visible en la UI debe estar en `strings.xml`.
- Evitar concatenar cadenas: usar placeholders `%1$s`, `%2$d`.
- Mantener simetría entre `values/` y `values-es/`.

## Placeholders
```xml
<string name="starting_timer">Starting %1$s…</string>
```
Traducción:
```xml
<string name="starting_timer">Iniciando %1$s…</string>
```

## Plurales (añadir si hace falta)
```xml
<plurals name="sessions_count">
  <item quantity="one">%1$d session</item>
  <item quantity="other">%1$d sessions</item>
</plurals>
```

## Buenas prácticas
- Usar `tools:ignore="MissingTranslation"` solo temporalmente.
- Reemplazar `...` por `…` (ellipsis) para consistencia tipográfica.
- Mantener nombres de claves en inglés en snake_case.

## Añadir nuevo idioma
1. Crear carpeta `values-<lang>` (ej: `values-fr`).
2. Copiar `strings.xml` base.
3. Traducir únicamente valores.
4. Ejecutar lint y corregir faltantes.

## Testing
- Cambiar idioma en Ajustes y verificar pantallas principales.
- Revisar truncamientos en textos largos.

## Recursos
- Evitar texto hardcoded en layouts.

