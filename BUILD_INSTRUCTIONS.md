# Instrucciones de Compilación - Zzz Timer Pro+

Este documento proporciona instrucciones detalladas para compilar y ejecutar la aplicación Zzz Timer Pro+ en Android Studio.

## Requisitos Previos

### Software Necesario

**Android Studio:** Se requiere Android Studio Arctic Fox (2020.3.1) o superior. Se recomienda la versión más reciente para obtener las últimas herramientas y optimizaciones.

**JDK:** Java Development Kit 17 o superior. Android Studio generalmente incluye el JDK necesario, pero puedes verificar la versión instalada en File > Project Structure > SDK Location.

**SDK de Android:** Debes tener instalado Android SDK con las siguientes configuraciones mínimas: SDK Platform Android 8.0 (API 26) como mínimo, SDK Platform Android 14 (API 34) como objetivo, Android SDK Build-Tools 34.0.0 o superior.

### Configuración del Sistema

**Espacio en Disco:** Al menos 4 GB de espacio libre para el proyecto y las dependencias.

**Memoria RAM:** Mínimo 8 GB recomendado para una experiencia fluida en Android Studio.

**Conexión a Internet:** Necesaria para descargar las dependencias de Gradle durante la primera compilación.

## Preparación del Proyecto

### Paso 1: Obtener el Código Fuente

Descarga o clona el proyecto completo en tu máquina local. Asegúrate de mantener la estructura de carpetas intacta.

### Paso 2: Agregar Archivos de Audio

Antes de compilar, debes agregar los archivos de audio necesarios. Navega a la carpeta `app/src/main/res/raw/`. Si la carpeta no existe, créala. Coloca los siguientes archivos de audio en formato MP3 u OGG: soft_rain.mp3, ocean_waves.mp3, night_forest.mp3, gentle_wind.mp3, white_noise.mp3, night_birds.mp3.

Consulta el archivo AUDIO_FILES_NEEDED.md para más detalles sobre las especificaciones de los archivos de audio.

### Paso 3: Agregar Iconos

Los iconos vectoriales deben agregarse a la carpeta `app/src/main/res/drawable/`. Consulta el archivo ICONS_NEEDED.md para la lista completa de iconos requeridos.

Para los iconos de launcher (ic_launcher.png e ic_launcher_round.png), debes crear versiones en múltiples resoluciones y colocarlas en las carpetas correspondientes: mipmap-mdpi, mipmap-hdpi, mipmap-xhdpi, mipmap-xxhdpi, mipmap-xxxhdpi.

## Compilación del Proyecto

### Paso 1: Abrir el Proyecto

Abre Android Studio. Selecciona "Open an Existing Project" o "File > Open". Navega hasta la carpeta raíz del proyecto ZzzTimerProPlus. Selecciona la carpeta y haz clic en "OK".

### Paso 2: Sincronizar con Gradle

Android Studio detectará automáticamente que es un proyecto Gradle y te pedirá sincronizar. Si no ocurre automáticamente, ve a File > Sync Project with Gradle Files.

Durante la primera sincronización, Gradle descargará todas las dependencias necesarias. Este proceso puede tardar varios minutos dependiendo de tu conexión a internet.

### Paso 3: Resolver Problemas de Sincronización

Si encuentras errores durante la sincronización, verifica lo siguiente: asegúrate de que tu versión de Android Studio sea compatible, confirma que tienes instalado el SDK de Android necesario, revisa que tu conexión a internet esté activa, intenta invalidar cachés con File > Invalidate Caches / Restart.

### Paso 4: Configurar el Dispositivo de Prueba

Puedes compilar y ejecutar la aplicación en un dispositivo físico o en un emulador.

**Para Dispositivo Físico:** Habilita las Opciones de Desarrollador en tu dispositivo Android (generalmente tocando 7 veces el número de compilación en Configuración > Acerca del teléfono). Activa la Depuración USB en Opciones de Desarrollador. Conecta tu dispositivo a la computadora mediante USB. Autoriza la conexión cuando tu dispositivo lo solicite.

**Para Emulador:** Ve a Tools > AVD Manager. Crea un nuevo dispositivo virtual si no tienes uno. Selecciona un dispositivo con API 26 o superior. Inicia el emulador.

### Paso 5: Compilar y Ejecutar

Una vez que el proyecto esté sincronizado y tengas un dispositivo configurado, selecciona tu dispositivo en el menú desplegable de la barra de herramientas. Haz clic en el botón Run (ícono de play verde) o presiona Shift + F10.

Android Studio compilará el proyecto y instalará la aplicación en tu dispositivo. La primera compilación puede tardar varios minutos.

## Variantes de Compilación

El proyecto incluye dos variantes de compilación principales.

**Debug:** Versión de desarrollo con información de depuración incluida. Se compila más rápido pero el APK es más grande. Incluye herramientas de depuración.

**Release:** Versión optimizada para distribución. Requiere configuración de firma. El APK es más pequeño y optimizado.

Para cambiar entre variantes, ve a Build > Select Build Variant y elige la variante deseada.

## Generar APK para Distribución

### Paso 1: Preparar la Firma

Para generar un APK de release, necesitas crear un keystore. Ve a Build > Generate Signed Bundle / APK. Selecciona APK y haz clic en Next. Si no tienes un keystore, haz clic en "Create new" y completa la información requerida. Guarda el keystore en un lugar seguro, lo necesitarás para futuras actualizaciones.

### Paso 2: Generar el APK

Selecciona tu keystore y completa las credenciales. Elige la variante "release". Marca "V1 (Jar Signature)" y "V2 (Full APK Signature)". Haz clic en Finish.

Android Studio generará el APK firmado. La ubicación del archivo se mostrará en una notificación cuando el proceso termine.

## Solución de Problemas Comunes

### Error de Sincronización de Gradle

Si Gradle no puede sincronizar, intenta limpiar el proyecto con Build > Clean Project, luego reconstruye con Build > Rebuild Project.

### Dependencias No Encontradas

Verifica que tu archivo build.gradle tenga acceso a los repositorios correctos (google() y mavenCentral()). Intenta actualizar las versiones de las dependencias si son muy antiguas.

### Errores de Compilación de Kotlin

Asegúrate de que la versión del plugin de Kotlin en build.gradle sea compatible con tu versión de Android Studio. Verifica que no haya errores de sintaxis en los archivos .kt.

### Problemas con Room Database

Si encuentras errores relacionados con Room, verifica que las anotaciones @Entity, @Dao y @Database estén correctamente configuradas. Asegúrate de que kapt esté habilitado en build.gradle.

### Errores de Recursos Faltantes

Si ves errores sobre recursos no encontrados (como @drawable/ic_timer), verifica que todos los archivos de iconos necesarios estén en su lugar. Consulta ICONS_NEEDED.md para la lista completa.

## Optimización del Rendimiento

### Habilitar Compilación Paralela

En tu archivo gradle.properties, agrega o verifica estas líneas:

```
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
```

### Aumentar Memoria de Gradle

Si experimentas lentitud durante la compilación, aumenta la memoria disponible para Gradle en gradle.properties:

```
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

## Pruebas

### Ejecutar Pruebas Unitarias

Para ejecutar las pruebas unitarias, haz clic derecho en la carpeta test y selecciona "Run Tests".

### Ejecutar Pruebas de Instrumentación

Las pruebas de instrumentación requieren un dispositivo o emulador. Haz clic derecho en la carpeta androidTest y selecciona "Run Tests".

## Recursos Adicionales

**Documentación Oficial de Android:** https://developer.android.com  
**Guías de Kotlin:** https://kotlinlang.org/docs/home.html  
**Material Design:** https://material.io/design

## Notas Finales

Recuerda que antes de distribuir la aplicación, debes agregar todos los archivos de audio e iconos necesarios. La aplicación no compilará correctamente si faltan recursos referenciados en el código.

Mantén tu keystore de firma en un lugar seguro y respaldado. Si lo pierdes, no podrás actualizar la aplicación en el futuro.

Para cualquier problema adicional no cubierto en esta guía, consulta la documentación oficial de Android Studio o contacta al desarrollador del proyecto.

---

**Creado por Felipe Plazas**
