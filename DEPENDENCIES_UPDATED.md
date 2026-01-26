# Actualización de Dependencias

## Fecha: 2025-11-15

### Cambios Principales

#### Migración a KSP
- **Antes**: Room con kapt (compilación lenta)
- **Ahora**: Room con KSP (compilación ~2x más rápida)
- Plugin KSP: `com.google.devtools.ksp:1.9.20-1.0.14`

#### Dependencias Actualizadas

**AndroidX Core**
- core-ktx: 1.12.0 → 1.17.0
- appcompat: 1.6.1 → 1.7.1
- material: 1.10.0 → 1.13.0
- constraintlayout: 2.1.4 → 2.2.1

**Lifecycle**
- 2.6.2 → 2.9.4 (viewmodel, livedata, runtime)

**Navigation**
- 2.7.5 → 2.9.6 (fragment-ktx, ui-ktx)

**Room**
- 2.6.1 → 2.8.3 (runtime, ktx, compiler, testing)

**Coroutines**
- 1.7.3 → 1.10.2

**DataStore**
- 1.1.1 → 1.1.7

**Billing**
- 6.0.1 → 8.1.0 (breaking changes menores, revisar si se usa query asíncrono)

**WorkManager**
- 2.9.0 → 2.11.0

**Play Services**
- location: 21.0.1 → 21.3.0

**Health Connect**
- 1.1.0-alpha07 → 1.1.0 (estable)

**Firebase**
- BOM: 32.7.0 → 34.5.0 (COMENTADO - requiere google-services.json y plugin)
- Ver `FIREBASE_FIX.md` para instrucciones de habilitación

**Gson**
- 2.10.1 → 2.13.2

**Lottie**
- 6.1.0 → 6.7.1

**Testing**
- junit-ext: 1.1.5 → 1.3.0
- espresso: 3.5.1 → 3.7.0

**Detekt**
- 1.23.6 → 1.23.8

### Mejoras de Build

- `task clean` → `tasks.register('clean')` (sintaxis moderna)
- `buildDir` → `layout.buildDirectory` (deprecación resuelta)
- `configurations.all` → `configurations.configureEach` (performance)

### Pruebas Adicionales

- Test migración 1→2 en `Migration12InstrumentedTest.kt`

### Notas

- Compilación debería ser más rápida con KSP.
- Billing 8.x requiere verificar flujos de compra si se añaden nuevas features.
- Health Connect ahora es estable (no alpha).

### Próximos pasos opcionales

- [ ] Migrar a Kotlin 2.0 cuando sea estable.
- [ ] Considerar Compose para futuras pantallas.

