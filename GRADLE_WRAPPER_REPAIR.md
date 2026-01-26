gradle wrapper --gradle-version 8.6 --distribution-type all# Reparar Gradle Wrapper

## Problema
Error al ejecutar `gradlew.bat`:
```
Error: no se ha encontrado o cargado la clase principal org.gradle.wrapper.GradleWrapperMain
```
Esto indica que el wrapper está incompleto (falta `gradle/wrapper/gradle-wrapper.jar`) o fue removido.

## Solución

### Opción A: Regenerar Wrapper usando instalación local de Gradle
1. Instala Gradle (si no lo tienes) o usa el embebido de Android Studio.
2. Ejecuta en la raíz del proyecto:
```powershell
# Si tienes gradle en PATH
gradle wrapper --gradle-version 8.6 --distribution-type all
```
Esto crea/actualiza:
```
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.properties
gradle/wrapper/gradle-wrapper.jar
```

> Puedes usar otra versión (8.7, 8.8) pero asegúrate de compatibilidad con el plugin Android (8.x).

### Opción B: Usar Android Studio
1. Abrir proyecto.
2. Menú: Tools > Gradle > (o desde la ventana Gradle ejecutar tarea `wrapper`).
3. File > Sync Project with Gradle Files.

### Verificar
Después de regenerar:
```powershell
./gradlew.bat --version
```
Debe mostrar versión de Gradle y JVM sin errores.

### Ejecutar validación rápida
Una vez reparado el wrapper:
```powershell
powershell -ExecutionPolicy Bypass -File .\ValidateQuick.ps1
```

## Notas
- No versionar carpetas de caché locales (como `.gradle`), solo los archivos del wrapper.
- Si migras a Gradle 9 en el futuro, revisa deprecaciones mostradas ahora.

## Próximo Paso
Tras wrapper reparado, reintenta el build:
```powershell
./gradlew.bat clean :app:assembleDebug
```
