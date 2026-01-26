# Sistema de Licencias - Zzz Timer Pro+

## Descripción General

Zzz Timer Pro+ implementa un sistema de licencias con período de prueba gratuito de 48 horas y pago único mediante Google Play Billing. Este documento describe la arquitectura, implementación y funcionamiento del sistema.

---

## Arquitectura del Sistema

### Componentes Principales

#### 1. **LicenseManager**
- **Ubicación**: `app/src/main/java/com/felipeplazas/zzztimerpro/license/LicenseManager.kt`
- **Responsabilidades**:
  - Gestión del período de prueba de 48 horas
  - Integración con Google Play Billing Library
  - Verificación del estado de licencia
  - Procesamiento de compras y restauraciones
  - Persistencia de datos de licencia

#### 2. **TrialActivity**
- **Ubicación**: `app/src/main/java/com/felipeplazas/zzztimerpro/ui/trial/TrialActivity.kt`
- **Responsabilidades**:
  - Mostrar estado del período de prueba
  - Interfaz de compra de licencia
  - Restauración de compras previas
  - Navegación a versión gratuita

#### 3. **BaseActivity (Extensión)**
- **Ubicación**: `app/src/main/java/com/felipeplazas/zzztimerpro/ui/BaseActivity.kt`
- **Métodos Agregados**:
  - `isPremium()`: Verifica si el usuario tiene acceso premium
  - `checkPremiumAccess()`: Verifica y redirige si no tiene premium
  - `requirePremiumAccess()`: Requiere premium o cierra la actividad

---

## Período de Prueba

### Características

- **Duración**: 48 horas (172,800,000 milisegundos)
- **Inicio**: Automático al primer lanzamiento de la aplicación
- **Almacenamiento**: SharedPreferences
- **Clave**: `PREF_TRIAL_START_TIME`

### Flujo de Funcionamiento

1. **Primera Ejecución**:
   - Se registra el timestamp actual como inicio del trial
   - El usuario tiene acceso completo a todas las funciones

2. **Durante el Trial**:
   - Se calcula el tiempo restante en cada verificación
   - Se muestra el countdown en TrialActivity
   - Todas las funciones premium están disponibles

3. **Después del Trial**:
   - Las funciones premium se bloquean
   - Se muestra mensaje de actualización
   - El usuario puede comprar o usar versión gratuita

---

## Sistema de Compras

### Configuración de Google Play Billing

#### Dependencias
```gradle
implementation 'com.android.billingclient:billing:6.0.1'
implementation 'com.android.billingclient:billing-ktx:6.0.1'
```

#### Permisos
```xml
<uses-permission android:name="com.android.vending.BILLING" />
```

### Producto In-App

- **Product ID**: `zzz_timer_pro_license`
- **Tipo**: Compra única (One-time purchase)
- **Precio**: $0.99 USD
- **Descripción**: Licencia completa de Zzz Timer Pro+

### Proceso de Compra

1. **Inicialización**:
   ```kotlin
   licenseManager.initializeBillingClient()
   ```

2. **Lanzar Compra**:
   ```kotlin
   licenseManager.launchPurchase(activity) { success, message ->
       // Manejar resultado
   }
   ```

3. **Verificación**:
   - La compra se verifica automáticamente
   - Se guarda el estado en SharedPreferences
   - Se actualiza la UI

4. **Restauración**:
   ```kotlin
   licenseManager.queryPurchases()
   ```

---

## Restricciones de la Versión Gratuita

### Funciones Limitadas

| Función | Versión Gratuita | Versión Premium |
|---------|------------------|-----------------|
| Duración del Temporizador | Máximo 15 minutos | Hasta 120 minutos |
| Sonidos Ambientales | 1 sonido | 6 sonidos |
| Estadísticas | Básicas | Detalladas con gráficos |
| Exportación de Datos | No disponible | Disponible |
| Widget | No disponible | Disponible |

### Implementación de Restricciones

#### MainActivity
```kotlin
// Limitar duración del temporizador
if (!licenseManager.isPremium() && selectedDuration > 15) {
    Toast.makeText(this, R.string.timer_limit_free_version, Toast.LENGTH_SHORT).show()
    checkPremiumAccess()
    return
}
```

#### AmbientSoundsActivity
```kotlin
// Limitar sonidos disponibles
val sounds = if (licenseManager.isPremium()) {
    allSounds
} else {
    allSounds.take(1) // Solo primer sonido
}
```

#### StatisticsActivity
```kotlin
// Ocultar gráficos detallados
if (!licenseManager.isPremium()) {
    binding.chartWeekly.visibility = View.GONE
}
```

---

## Integración en Actividades

### Verificación de Licencia

Todas las actividades que requieren funciones premium deben:

1. **Crear instancia de LicenseManager**:
   ```kotlin
   private lateinit var licenseManager: LicenseManager
   
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       licenseManager = LicenseManager(this)
       // ...
   }
   ```

2. **Verificar estado antes de acciones premium**:
   ```kotlin
   if (!licenseManager.isPremium()) {
       Toast.makeText(this, R.string.feature_locked, Toast.LENGTH_SHORT).show()
       checkPremiumAccess()
       return
   }
   ```

3. **Actualizar UI según estado**:
   ```kotlin
   override fun onResume() {
       super.onResume()
       updateLicenseStatus()
   }
   ```

---

## Interfaz de Usuario

### TrialActivity

#### Elementos Principales

1. **Card de Estado del Trial**:
   - Muestra tiempo restante en formato "XXh XXm"
   - Cambia de color según estado (activo/expirado)
   - Actualización cada minuto

2. **Card de Funciones Premium**:
   - Lista de 5 funciones principales
   - Iconos de verificación
   - Descripción clara de cada función

3. **Card de Precio**:
   - Muestra "Pago único"
   - Precio destacado: $0.99 USD
   - Diseño atractivo con colores de marca

4. **Botones de Acción**:
   - **Comprar Licencia**: Botón principal (Material Button)
   - **Restaurar Compra**: Botón secundario (Outlined Button)
   - **Continuar con Versión Gratuita**: Botón de texto (solo visible si trial expiró)

### SettingsActivity

#### Sección de Licencia

- Muestra estado actual de la licencia
- Colores dinámicos según estado:
  - Verde (accent): Premium activo
  - Azul (primary): Trial activo
  - Rojo (error): Trial expirado
- Click para abrir TrialActivity

---

## Recursos Multiidioma

### Strings Agregados

#### Inglés (`values/strings.xml`)
- `license_section`: "License"
- `trial_title`: "Free Trial"
- `purchase_license`: "Purchase License - $0.99"
- Y 20+ strings más...

#### Español (`values-es/strings.xml`)
- `license_section`: "Licencia"
- `trial_title`: "Prueba Gratuita"
- `purchase_license`: "Comprar Licencia - $0.99"
- Traducciones completas de todos los strings

---

## Persistencia de Datos

### SharedPreferences

**Archivo**: `license_prefs`

**Claves Utilizadas**:

| Clave | Tipo | Descripción |
|-------|------|-------------|
| `PREF_TRIAL_START_TIME` | Long | Timestamp de inicio del trial |
| `PREF_LICENSE_PURCHASED` | Boolean | Estado de compra de licencia |
| `PREF_PURCHASE_TOKEN` | String | Token de compra de Google Play |

### Métodos de Acceso

```kotlin
// Guardar inicio de trial
prefs.edit().putLong(PREF_TRIAL_START_TIME, System.currentTimeMillis()).apply()

// Verificar compra
val isPurchased = prefs.getBoolean(PREF_LICENSE_PURCHASED, false)

// Guardar token de compra
prefs.edit().putString(PREF_PURCHASE_TOKEN, token).apply()
```

---

## Seguridad

### Medidas Implementadas

1. **Validación de Compras**:
   - Verificación mediante Google Play Billing
   - Tokens de compra seguros
   - No se confía solo en SharedPreferences

2. **Protección de Trial**:
   - Timestamp almacenado localmente
   - Verificación en cada lanzamiento
   - No se puede resetear fácilmente

3. **Ofuscación**:
   - ProGuard configurado para release
   - Nombres de clases y métodos ofuscados
   - Strings sensibles protegidos

### Limitaciones Conocidas

⚠️ **Importante**: El sistema actual almacena el estado de trial en SharedPreferences local. Para mayor seguridad en producción, se recomienda:

1. Implementar validación server-side
2. Usar Google Play Developer API para verificar compras
3. Implementar detección de root/modificación
4. Agregar ofuscación adicional con R8

---

## Testing

### Modo de Prueba

Para probar el sistema de licencias en desarrollo:

1. **Usar Cuentas de Prueba**:
   - Configurar en Google Play Console
   - Agregar emails de testers
   - Las compras son gratuitas para testers

2. **Simular Trial Expirado**:
   ```kotlin
   // En LicenseManager, cambiar temporalmente:
   private const val TRIAL_DURATION_MS = 60000L // 1 minuto para testing
   ```

3. **Resetear Trial**:
   ```kotlin
   // Limpiar SharedPreferences
   getSharedPreferences("license_prefs", MODE_PRIVATE)
       .edit()
       .clear()
       .apply()
   ```

### Casos de Prueba

- [ ] Primera instalación inicia trial correctamente
- [ ] Countdown se actualiza cada minuto
- [ ] Trial expira después de 48 horas
- [ ] Compra desbloquea todas las funciones
- [ ] Restauración recupera compra previa
- [ ] Restricciones se aplican correctamente en versión gratuita
- [ ] Cambio de idioma mantiene estado de licencia
- [ ] Reinstalación mantiene compra (con restauración)

---

## Configuración para Producción

### Google Play Console

1. **Crear Producto In-App**:
   - ID: `zzz_timer_pro_license`
   - Tipo: Compra gestionada (Managed product)
   - Precio: $0.99 USD
   - Estado: Activo

2. **Configurar Licencias de Prueba**:
   - Agregar cuentas de prueba
   - Configurar respuestas de prueba
   - Validar flujo de compra

3. **Configurar Claves de API**:
   - Generar clave de licencia
   - Configurar en el proyecto
   - Implementar validación server-side (recomendado)

### Pasos de Publicación

1. Compilar APK/AAB en modo release
2. Firmar con keystore de producción
3. Subir a Google Play Console
4. Configurar producto in-app
5. Probar con cuentas de prueba
6. Publicar en producción

---

## Mantenimiento

### Actualización de Precios

Para cambiar el precio:

1. **Google Play Console**:
   - Modificar precio del producto in-app
   - Los cambios se reflejan automáticamente

2. **Código (opcional)**:
   - Actualizar string `purchase_license` si incluye precio
   - Recomendado: Obtener precio dinámicamente de Billing Library

### Agregar Nuevas Restricciones

Para limitar una nueva función:

```kotlin
// En la actividad correspondiente
if (!licenseManager.isPremium()) {
    Toast.makeText(this, R.string.feature_locked, Toast.LENGTH_SHORT).show()
    checkPremiumAccess()
    return
}
// Código de la función premium
```

### Modificar Duración del Trial

```kotlin
// En LicenseManager.kt
private const val TRIAL_DURATION_MS = 172800000L // Cambiar valor (en milisegundos)
```

---

## Solución de Problemas

### Problema: Compra no se reconoce

**Solución**:
1. Verificar conexión a internet
2. Usar botón "Restaurar Compra"
3. Verificar que la cuenta de Google Play es la misma
4. Limpiar caché de Google Play Store

### Problema: Trial no inicia

**Solución**:
1. Verificar permisos de almacenamiento
2. Revisar logs de SharedPreferences
3. Reinstalar aplicación

### Problema: Billing no se conecta

**Solución**:
1. Verificar que Google Play Services está actualizado
2. Verificar que el producto in-app está configurado
3. Verificar que la app está firmada correctamente
4. Revisar logs de BillingClient

---

## Notas Adicionales

### Consideraciones Legales

- Incluir términos de servicio
- Política de privacidad
- Política de reembolsos
- Cumplir con GDPR/CCPA si aplica

### Mejoras Futuras

1. **Suscripciones**: Agregar opción de suscripción mensual/anual
2. **Niveles de Premium**: Crear tiers (Basic, Pro, Ultimate)
3. **Ofertas Especiales**: Implementar descuentos temporales
4. **Programa de Referidos**: Recompensas por invitar amigos
5. **Validación Server-Side**: Mayor seguridad con backend propio

---

## Contacto y Soporte

**Desarrollador**: Felipe Plazas  
**Aplicación**: Zzz Timer Pro+  
**Versión del Sistema de Licencias**: 1.0.0  

Para reportar problemas o sugerencias relacionadas con el sistema de licencias, contactar al desarrollador.

---

*Última actualización: Noviembre 2025*
