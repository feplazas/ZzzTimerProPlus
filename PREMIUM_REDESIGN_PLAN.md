# 🌙 Zzz Timer Pro+ Premium Redesign Plan

## 🎨 Nueva Paleta de Colores (Warm & Cozy Sleep Theme)

### Colores Principales
- **Deep Purple Night**: `#2D1B4E` (fondo principal oscuro)
- **Soft Lavender**: `#8B7BA8` (acentos suaves)
- **Warm Gold**: `#D4AF37` (premium accents)
- **Moonlight Blue**: `#4A5F7F` (secundario)
- **Gentle Pink**: `#E8B4B8` (toques cálidos)
- **Cloud White**: `#F5F3F7` (textos claros)

### Modo Claro
- **Cream Background**: `#FFF8F0`
- **Soft Purple**: `#9B8FB9`
- **Deep Plum**: `#4A2C5E`

## 🐛 Bugs a Arreglar (PRIORIDAD MÁXIMA)

### 1. Settings Crash
- [ ] Verificar binding de todos los elementos
- [ ] Añadir try-catch en inicialización
- [ ] Log detallado de errores

### 2. Sleep Tracking Crash  
- [ ] Verificar permisos en runtime
- [ ] Añadir manejo de errores en sensor init
- [ ] UI de feedback si faltan permisos

### 3. Vibración Constante en Timer
- [ ] Revisar TimerService - eliminar vibración continua
- [ ] Vibración solo al completar timer

### 4. Selector de Sonido Roto
- [ ] Fix sound picker en SavedTimersActivity
- [ ] Verificar permisos de audio

## 🎨 Rediseño UI/UX

### Iconos de Sonidos (SVG Premium)
1. **Soft Rain**: Gotas de lluvia suaves con nubes
2. **Ocean Waves**: Olas estilizadas
3. **Night Forest**: Árbol con luna
4. **Gentle Wind**: Líneas de viento curvas
5. **White Noise**: Ondas de sonido concéntricas
6. **Night Birds**: Silueta de búho/pájaro nocturno

### Breathing Exercises
- [ ] Texto en color claro (#FFFFFF con sombra)
- [ ] Bola de respiración en gradiente suave
- [ ] Animación más fluida

### Alarmas
- [ ] Aumentar tamaño de texto en días
- [ ] Usar abreviaturas si es necesario (Mon, Wed)
- [ ] Responsive text sizing

## 🌍 Sistema de Idioma

### Detección Automática
```kotlin
val systemLocale = Resources.getSystem().configuration.locales[0]
val appLanguage = if (systemLocale.language == "es") "es" else "en"
```

### Aplicación Consistente
- [ ] Eliminar selector manual de idioma
- [ ] Aplicar idioma en Application.onCreate()
- [ ] Forzar recreate de todas las activities

## 💎 Transparencia de Trial

### UI de Estado Premium
- [ ] Badge visible en MainActivity mostrando "TRIAL" o "PREMIUM"
- [ ] Contador de horas restantes en trial
- [ ] Lock icons en features premium
- [ ] Dialog explicativo al tocar feature bloqueada

### Features Durante Trial
- ✅ Timer hasta 15 min
- ✅ 2 sonidos (Soft Rain, Ocean Waves)
- ✅ 1 alarma
- ✅ Breathing exercises
- ❌ Timer > 15 min
- ❌ Todos los sonidos
- ❌ Sleep tracking
- ❌ Estadísticas detalladas
- ❌ Export data
- ❌ Widget

## 📋 Orden de Implementación

1. **CRÍTICO** - Fix crashes (Settings, Sleep Tracking)
2. **CRÍTICO** - Fix vibración en timer
3. **CRÍTICO** - Fix selector de sonido
4. **ALTO** - Nueva paleta de colores
5. **ALTO** - Iconos premium de sonidos
6. **ALTO** - Sistema de idioma automático
7. **MEDIO** - UI de trial transparency
8. **MEDIO** - Breathing exercises legible
9. **BAJO** - Alarmas text sizing
