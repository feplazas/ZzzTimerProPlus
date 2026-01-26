# 🔧 FIXES CRÍTICOS - Implementación Paso a Paso

## 1. ❌ Settings Crash - PARCIALMENTE RESUELTO
- [x] Añadido try-catch en onCreate
- [ ] Verificar que todos los elementos del layout existen
- [ ] Añadir logs para debugging

## 2. ❌ Sleep Tracking Crash - PENDIENTE
**Causa probable**: Falta de permisos o sensores no disponibles
**Fix**:
```kotlin
// En SleepTrackingActivity.kt
private fun checkPermissionsAndStart() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
        // Request permissions
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BODY_SENSORS
        ), PERMISSION_REQUEST_CODE)
    } else {
        startTracking()
    }
}
```

## 3. ❌ Vibración Constante - INVESTIGAR
**Hipótesis**:
1. ¿Hay vibración configurada en notificaciones?
2. ¿El switch de vibración está activado y causando vibración continua?
3. ¿Hay vibración háptica en los botones?

**Acción**: Revisar:
- NotificationCompat.Builder - quitar vibration pattern
- Switches - quitar performHapticFeedback()
- TimerActivity - verificar que no haya vibración en UI

## 4. ❌ Selector de Sonido Roto - PENDIENTE
**Ubicación**: SavedTimersActivity.kt
**Fix**: Verificar que el dialog de selección de sonido se muestre correctamente

## 5. 🎨 Paleta de Colores Premium - PENDIENTE
**Archivo**: res/values/colors.xml
**Nueva paleta**:
```xml
<!-- Dark Theme (Default) -->
<color name="deep_purple_night">#2D1B4E</color>
<color name="soft_lavender">#8B7BA8</color>
<color name="warm_gold">#D4AF37</color>
<color name="moonlight_blue">#4A5F7F</color>
<color name="gentle_pink">#E8B4B8</color>
<color name="cloud_white">#F5F3F7</color>

<!-- Light Theme -->
<color name="cream_background">#FFF8F0</color>
<color name="soft_purple">#9B8FB9</color>
<color name="deep_plum">#4A2C5E</color>
```

## 6. 🎨 Iconos de Sonidos - PENDIENTE
Crear 6 iconos SVG premium:
1. ic_sound_rain.xml - Gotas de lluvia
2. ic_sound_ocean.xml - Olas
3. ic_sound_forest.xml - Árbol con luna
4. ic_sound_wind.xml - Líneas curvas
5. ic_sound_white_noise.xml - Ondas concéntricas
6. ic_sound_birds.xml - Silueta de búho

## 7. 🌍 Sistema de Idioma Automático - PENDIENTE
**Archivo**: ZzzTimerApplication.kt
```kotlin
override fun onCreate() {
    super.onCreate()
    // Detectar idioma del sistema
    val systemLocale = Resources.getSystem().configuration.locales[0]
    val appLanguage = if (systemLocale.language == "es") "es" else "en"
    localeManager.setLocaleFromCode(this, appLanguage)
}
```

## 8. 💎 UI de Trial Transparency - PENDIENTE
**Componentes**:
- Badge en MainActivity mostrando "TRIAL" o "PREMIUM"
- Contador de horas restantes
- Lock icons en features premium
- Dialog explicativo

## 9. 🎨 Breathing Exercises Legible - PENDIENTE
**Archivo**: activity_breathing.xml
**Fix**: Cambiar color de texto a blanco con sombra

## 10. 🎨 Alarmas Text Sizing - PENDIENTE
**Archivo**: item_alarm.xml o similar
**Fix**: Usar autoSizeTextType o abreviaturas

## PRIORIDAD DE IMPLEMENTACIÓN:
1. Vibración constante (CRÍTICO - afecta UX inmediatamente)
2. Sleep Tracking crash (CRÍTICO - feature no funciona)
3. Selector de sonido (ALTO - feature no funciona)
4. Paleta de colores (ALTO - mejora percepción premium)
5. Iconos de sonidos (ALTO - mejora estética)
6. Sistema de idioma (MEDIO - mejora UX)
7. Breathing exercises (MEDIO - legibilidad)
8. Trial transparency (MEDIO - claridad de features)
9. Alarmas text (BAJO - cosmético)
