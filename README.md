# Zzz Timer Pro+

Creado por Felipe Plazas

Aplicación Android de temporizador para dormir con fade de volumen, alarmas programadas, sonidos ambientales, estadísticas y sistema de licencia con prueba. Incluye soporte multiidioma (inglés/español), DataStore para ajustes (tema/idioma) y base de datos Room con migraciones seguras.

## ⚠️ IMPORTANTE: Solución de Errores de Build

**Si encuentras errores al compilar**, consulta `BUILD_FIX_GUIDE.md` para instrucciones detalladas.

**Errores comunes resueltos:**
- ✅ Recursos duplicados (strings)
- ✅ Warning de AndroidManifest
- ✅ Todos los errores de compilación corregidos

**El proyecto está listo para compilar desde Android Studio.**

## Requisitos
- Android Studio (Giraffe o superior recomendado)
- SDK: min 26, target 34
- Java/Kotlin: JDK 17

## Compilación

### Opción A: Android Studio
1. Abrir el proyecto en Android Studio
2. Sincronizar Gradle (Sync Now)
3. Build > Make Project
4. Run en dispositivo o emulador

### Opción B: Gradle wrapper (si está presente)
```powershell
# En PowerShell dentro de la carpeta del proyecto
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```
Salida: `app/build/outputs/apk/debug/app-debug.apk`

Si no tienes el wrapper, compila desde Android Studio o añade los archivos gradlew/gradlew.bat.

## Permisos y configuración en el dispositivo
- Notificaciones (Android 13+): se solicita al iniciar la app. Si lo bloqueas, ve a Ajustes del sistema > Apps > Zzz Timer Pro+ > Notificaciones.
- Alarmas exactas (Android 12+): recomendado para puntualidad de alarmas. Desde Ajustes de la app, usa el acceso directo “Permiso de alarma exacta” para abrir la pantalla del sistema.
- Vibración: necesaria para feedback al finalizar el temporizador.
- No molestar: opcional, para silenciar durante el temporizador (pueden requerirse pasos manuales según fabricante).

## Documentación ampliada
- Guía i18n: `I18N_GUIDE.md`
- Rendimiento: `PERFORMANCE_TODO.md`
- Accesibilidad: `ACCESSIBILITY_TODO.md`
- Seguridad/licencias: `SECURITY_NOTES.md`
- Roadmap: `ROADMAP.md`

## Pruebas rápidas
```powershell
# Unit tests
./gradlew.bat test
# Instrumented tests (emulador/dispositivo)
./gradlew.bat connectedAndroidTest
# Detekt (análisis estático)
./gradlew.bat detekt
```

## Validación rápida (script)
Ejecuta validación integral (build + tests + detekt opcional):
```powershell
powershell -ExecutionPolicy Bypass -File .\ValidateQuick.ps1
# Con tests instrumentados
powershell -ExecutionPolicy Bypass -File .\ValidateQuick.ps1 -IncludeInstrumented
# Omitir detekt
powershell -ExecutionPolicy Bypass -File .\ValidateQuick.ps1 -SkipDetekt
```

## Internacionalización
Ver guía completa en `I18N_GUIDE.md`. Para añadir idioma nuevo copiar `strings.xml` base y traducir valores.

## Próximos pasos
Consulta `ROADMAP.md` para mejoras planificadas.

## Tema
- Ajustes > Tema: Claro, Oscuro o Sistema.
- Se persiste en DataStore y se aplica en vivo.

## Base de datos y migraciones
- Room Database con migraciones 1→2 y 2→3 (índices). No se usa fallback destructivo.
- Pruebas instrumentadas para DAOs y migración 2→3.

## Módulos Clave
- TimerService: servicio foreground con restauración tras kill y fade de volumen.
- AlarmScheduler: programación/cancelación de alarmas con reschedule tras reboot.
- Repositorios: `SavedTimersRepository`, `ScheduledAlarmsRepository`.
- Ajustes: DataStore (`SettingsRepository`) para idioma/tema.

## Recursos necesarios
- Archivos de audio en `app/src/main/res/raw/` (ver `AUDIO_FILES_NEEDED.md`).
- Iconos en `app/src/main/res/drawable/` (ver `ICONS_NEEDED.md`).

## Problemas comunes
- Si las alarmas no disparan: concede “Alarmas exactas” en sistema y desactiva optimizaciones de batería para la app.
- Si no ves notificaciones (Android 13+): concede permiso Notificaciones.
- Fabricantes con gestión agresiva: considera añadir la app a excepciones (dontkillmyapp.com para guía).

## Licencia y Premium
- Prueba gratuita limitada; compra única para desbloquear todo.

## Contacto
- Soporte y sugerencias: ver datos del desarrollador.
