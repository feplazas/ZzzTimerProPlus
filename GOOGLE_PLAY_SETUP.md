# Configuración de Google Play Console para Zzz Timer Pro+

## Guía Paso a Paso para Configurar el Sistema de Licencias

---

## 1. Preparación Inicial

### Requisitos Previos

- Cuenta de desarrollador de Google Play activa ($25 USD de registro único)
- Aplicación compilada y firmada (APK o AAB)
- Keystore de producción configurado
- Acceso a Google Play Console

---

## 2. Crear la Aplicación en Google Play Console

### Pasos

1. Inicia sesión en [Google Play Console](https://play.google.com/console)
2. Haz clic en "Crear aplicación"
3. Completa la información básica:
   - **Nombre de la aplicación**: Zzz Timer Pro+
   - **Idioma predeterminado**: Español (o Inglés)
   - **Tipo de aplicación**: Aplicación
   - **Categoría**: Salud y bienestar
   - **Gratuita o de pago**: Gratuita (con compras dentro de la aplicación)

4. Acepta las declaraciones de contenido y políticas
5. Haz clic en "Crear aplicación"

---

## 3. Configurar el Producto In-App

### Acceder a la Sección de Productos

1. En el menú lateral, ve a **Monetización** → **Productos dentro de la aplicación**
2. Haz clic en "Crear producto"

### Configuración del Producto

#### Información Básica

- **ID del producto**: `zzz_timer_pro_license`
  - ⚠️ **Importante**: Este ID debe coincidir exactamente con el definido en `LicenseManager.kt`
  - No se puede cambiar después de crear el producto

- **Nombre**: Licencia Completa Zzz Timer Pro+
  - Este nombre aparecerá en la interfaz de compra de Google Play

- **Descripción**: 
  ```
  Desbloquea todas las funciones premium de Zzz Timer Pro+:
  • Temporizador hasta 120 minutos
  • Acceso a 6 sonidos ambientales
  • Estadísticas detalladas con gráficos
  • Widget para pantalla de inicio
  • Exportación de datos
  
  Pago único, sin suscripciones.
  ```

#### Tipo de Producto

- Selecciona: **Producto gestionado** (Managed product)
- No es una suscripción

#### Estado

- **Estado**: Activo
- Asegúrate de activar el producto antes de publicar la aplicación

### Configurar Precios

1. Haz clic en "Establecer precio"
2. Selecciona el precio base:
   - **Precio**: $0.99 USD
3. Google Play ajustará automáticamente los precios para otros países
4. Revisa los precios sugeridos y ajusta si es necesario
5. Haz clic en "Aplicar precios"

### Guardar el Producto

1. Revisa toda la información
2. Haz clic en "Guardar"
3. Verifica que el estado sea "Activo"

---

## 4. Configurar Cuentas de Prueba

### Agregar Testers de Licencia

1. Ve a **Configuración** → **Configuración de cuenta** → **Acceso a licencias**
2. En la sección "Testers de licencia", haz clic en "Agregar testers"
3. Agrega las direcciones de correo electrónico de Google de los testers:
   ```
   tester1@gmail.com
   tester2@gmail.com
   ```
4. Los testers pueden realizar compras sin cargos reales

### Configurar Respuestas de Prueba

1. Ve a **Configuración** → **Configuración de cuenta** → **Testers con licencia**
2. Configura respuestas de prueba para diferentes escenarios:
   - **android.test.purchased**: Compra exitosa
   - **android.test.canceled**: Compra cancelada
   - **android.test.refunded**: Compra reembolsada
   - **android.test.item_unavailable**: Producto no disponible

---

## 5. Subir la Aplicación

### Preparar el APK/AAB

1. En Android Studio, genera el bundle firmado:
   - **Build** → **Generate Signed Bundle / APK**
   - Selecciona **Android App Bundle**
   - Usa tu keystore de producción
   - Selecciona build variant: **release**

2. Verifica que el archivo se generó correctamente:
   - Ubicación: `app/release/app-release.aab`

### Subir a Google Play Console

1. En Google Play Console, ve a **Versión** → **Producción**
2. Haz clic en "Crear nueva versión"
3. Sube el archivo AAB
4. Completa los detalles de la versión:
   - **Nombre de la versión**: 1.0.0
   - **Notas de la versión** (en español e inglés):
     ```
     Versión inicial de Zzz Timer Pro+
     
     Características:
     • Temporizador inteligente con desvanecimiento de volumen
     • 6 sonidos ambientales relajantes
     • Estadísticas detalladas
     • Soporte multiidioma (inglés/español)
     • Período de prueba gratuito de 48 horas
     • Widget para pantalla de inicio
     ```

5. Haz clic en "Guardar" y luego "Revisar versión"

---

## 6. Completar la Ficha de Play Store

### Descripción de la Aplicación

#### Descripción Breve (80 caracteres)
```
Temporizador para dormir con sonidos relajantes y desvanecimiento de volumen
```

#### Descripción Completa (4000 caracteres)
```
Zzz Timer Pro+ es la aplicación definitiva para ayudarte a dormir mejor. Con un temporizador inteligente que reduce gradualmente el volumen de tu música o podcasts, y una biblioteca de sonidos ambientales relajantes, podrás conciliar el sueño de forma natural.

🌟 CARACTERÍSTICAS PRINCIPALES

⏱️ Temporizador Inteligente
• Duración configurable de 5 a 120 minutos
• Desvanecimiento gradual del volumen en los últimos 5 minutos
• Activación automática del modo No Molestar
• Notificaciones personalizadas

🎵 Sonidos Ambientales
• Lluvia Suave
• Olas del Mar
• Bosque Nocturno
• Viento Suave
• Ruido Blanco
• Pájaros Nocturnos

📊 Estadísticas Detalladas
• Seguimiento de uso del temporizador
• Gráficos semanales
• Tiempo total de uso
• Sonido más utilizado
• Duración promedio de sesiones

🌍 Multiidioma
• Soporte completo para inglés y español
• Cambio de idioma en tiempo real
• Todas las interfaces localizadas

🎁 PERÍODO DE PRUEBA GRATUITO
Disfruta de 48 horas de acceso completo a todas las funciones premium. Después del período de prueba, puedes adquirir la licencia completa por un pago único de $0.99 USD o continuar con la versión gratuita.

💎 VERSIÓN PREMIUM
• Temporizador hasta 120 minutos (vs 15 minutos en versión gratuita)
• Acceso a todos los 6 sonidos ambientales (vs 1 en versión gratuita)
• Estadísticas detalladas con gráficos
• Exportación de datos
• Widget para pantalla de inicio

✨ DISEÑO ELEGANTE
Interfaz moderna con Material Design 3, colores relajantes y navegación intuitiva.

🔒 PRIVACIDAD
Tus datos se almacenan localmente en tu dispositivo. No recopilamos información personal.

📱 REQUISITOS
Android 8.0 o superior

Creado por Felipe Plazas
```

### Capturas de Pantalla

Necesitarás al menos 2 capturas de pantalla por cada tipo de dispositivo:

1. **Teléfono** (obligatorio):
   - Pantalla principal con temporizador
   - Pantalla de sonidos ambientales
   - Pantalla de estadísticas
   - Pantalla de configuración
   - Pantalla de licencia/trial

2. **Tablet de 7 pulgadas** (opcional)
3. **Tablet de 10 pulgadas** (opcional)

**Especificaciones**:
- Formato: PNG o JPEG
- Dimensiones mínimas: 320px
- Dimensiones máximas: 3840px
- Relación de aspecto: 16:9 o 9:16

### Ícono de la Aplicación

- **Formato**: PNG de 32 bits
- **Dimensiones**: 512 x 512 px
- Sin transparencias
- Debe representar la marca de la aplicación

### Gráfico de Funciones

- **Formato**: PNG o JPEG
- **Dimensiones**: 1024 x 500 px
- Banner promocional que aparece en Play Store

### Categoría y Etiquetas

- **Categoría**: Salud y bienestar
- **Etiquetas**: temporizador, dormir, sonidos relajantes, meditación, sueño

### Información de Contacto

- **Correo electrónico**: [tu_email@example.com]
- **Sitio web** (opcional): [tu_sitio_web]
- **Política de privacidad**: [URL de tu política de privacidad]

---

## 7. Completar el Cuestionario de Contenido

### Clasificación de Contenido

1. Ve a **Política** → **Clasificación de la aplicación**
2. Completa el cuestionario:
   - **Categoría**: Utilidades
   - **Violencia**: No
   - **Contenido sexual**: No
   - **Lenguaje**: No
   - **Drogas**: No
   - **Discriminación**: No

3. Envía el cuestionario
4. Espera la clasificación (generalmente instantánea)

### Público Objetivo

1. Ve a **Política** → **Público objetivo y contenido**
2. Selecciona:
   - **Público objetivo**: Mayores de 13 años
   - **Interés de los niños**: No está diseñada específicamente para niños

---

## 8. Configurar Datos de Seguridad

### Declaración de Datos

1. Ve a **Política** → **Datos de seguridad**
2. Completa la declaración:

**¿Recopila o comparte datos de usuario?**
- No, esta aplicación no recopila datos de usuario

**Prácticas de seguridad**:
- Los datos se cifran en tránsito: No aplica
- Los usuarios pueden solicitar que se borren los datos: Sí (datos locales)
- Se han realizado evaluaciones de seguridad independientes: No

3. Envía la declaración

---

## 9. Configurar Precios y Distribución

### Países de Distribución

1. Ve a **Versión** → **Países y regiones**
2. Selecciona los países donde quieres distribuir la aplicación
   - Recomendado: Todos los países disponibles
3. Guarda los cambios

### Contenido para Adultos

- Selecciona: No contiene contenido para adultos

### Anuncios

- Selecciona: No contiene anuncios

---

## 10. Publicar la Aplicación

### Revisión Final

1. Ve a **Panel de control**
2. Revisa todos los elementos pendientes
3. Completa cualquier sección faltante

### Enviar para Revisión

1. Una vez que todos los elementos estén completos
2. Haz clic en **Enviar para revisión**
3. Espera la aprobación de Google Play (puede tardar de 1 a 7 días)

### Después de la Aprobación

1. La aplicación estará disponible en Google Play Store
2. Los usuarios podrán descargarla
3. Las compras in-app estarán activas

---

## 11. Monitoreo Post-Lanzamiento

### Verificar Compras

1. Ve a **Monetización** → **Informes de compras**
2. Revisa las transacciones
3. Verifica que las compras se procesen correctamente

### Responder a Reseñas

1. Ve a **Calidad** → **Reseñas**
2. Responde a las reseñas de los usuarios
3. Mantén una comunicación activa

### Actualizar la Aplicación

Para subir actualizaciones:

1. Incrementa el `versionCode` y `versionName` en `build.gradle`
2. Genera un nuevo AAB firmado
3. Sube a una nueva versión en Google Play Console
4. Completa las notas de la versión
5. Envía para revisión

---

## 12. Solución de Problemas Comunes

### Error: "El producto no está disponible"

**Causa**: El producto in-app no está activado o la aplicación no está firmada correctamente.

**Solución**:
1. Verifica que el producto esté en estado "Activo"
2. Asegúrate de que la aplicación esté firmada con el keystore de producción
3. Espera hasta 24 horas después de crear el producto

### Error: "La compra no se puede completar"

**Causa**: Problemas de conexión o configuración de Google Play.

**Solución**:
1. Verifica la conexión a internet
2. Actualiza Google Play Services
3. Limpia el caché de Google Play Store

### Error: "El ID del producto no coincide"

**Causa**: El ID en el código no coincide con el ID en Google Play Console.

**Solución**:
1. Verifica que `PRODUCT_ID` en `LicenseManager.kt` sea exactamente `zzz_timer_pro_license`
2. Verifica que el producto en Google Play Console tenga el mismo ID

---

## 13. Recursos Adicionales

### Documentación Oficial

- [Google Play Billing Library](https://developer.android.com/google/play/billing)
- [Guía de Publicación](https://support.google.com/googleplay/android-developer/answer/9859152)
- [Políticas de Contenido](https://support.google.com/googleplay/android-developer/topic/9858052)

### Soporte

- [Centro de Ayuda de Google Play Console](https://support.google.com/googleplay/android-developer)
- [Foro de Desarrolladores](https://groups.google.com/g/android-developers)

---

## Checklist Final

Antes de publicar, asegúrate de haber completado:

- [ ] Aplicación creada en Google Play Console
- [ ] Producto in-app configurado con ID correcto
- [ ] Precio establecido ($0.99 USD)
- [ ] Cuentas de prueba configuradas
- [ ] AAB firmado subido
- [ ] Descripción de la aplicación completa
- [ ] Capturas de pantalla subidas (mínimo 2)
- [ ] Ícono de 512x512 subido
- [ ] Clasificación de contenido completada
- [ ] Público objetivo definido
- [ ] Datos de seguridad declarados
- [ ] Países de distribución seleccionados
- [ ] Política de privacidad publicada (si aplica)
- [ ] Revisión final completada
- [ ] Aplicación enviada para revisión

---

**Creado por Felipe Plazas**  
**Última actualización: Noviembre 2025**
