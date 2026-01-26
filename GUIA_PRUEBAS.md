# 🧪 GUÍA DE PRUEBAS - Zzz Timer Pro+

## Objetivo: Verificar que el timer funciona correctamente en todas las situaciones

---

## ✅ PRUEBAS BÁSICAS (HACER PRIMERO)

### Test 1: Timer básico ⏱️
**Objetivo:** Verificar que el timer cuenta correctamente

**Pasos:**
1. Abre la app
2. Selecciona 5 minutos
3. Presiona "Iniciar Timer"
4. Observa la cuenta regresiva
5. Espera al menos 1 minuto

**Resultado esperado:**
- ✅ El contador disminuye cada segundo
- ✅ La notificación se actualiza
- ✅ La UI muestra el tiempo correctamente

**¿Pasó?** ⬜ Sí / ⬜ No

---

### Test 2: Timer en background (Home) 🏠
**Objetivo:** Verificar que el timer continúa cuando presionas Home

**Pasos:**
1. Inicia un timer de 10 minutos
2. Anota el tiempo mostrado (ejemplo: 9:45)
3. Presiona el botón HOME
4. Espera 2 minutos en la pantalla de inicio
5. Abre la app de nuevo
6. Verifica el tiempo

**Resultado esperado:**
- ✅ El timer continuó contando en background
- ✅ El tiempo mostrado es ~2 minutos menos (ejemplo: 7:45)
- ✅ La notificación estuvo visible todo el tiempo

**¿Pasó?** ⬜ Sí / ⬜ No

---

### Test 3: Cerrar desde Recientes 🔄
**Objetivo:** Verificar que el timer NO se detiene al cerrar la app

**Pasos:**
1. Inicia un timer de 15 minutos
2. Anota el tiempo (ejemplo: 14:30)
3. Presiona el botón de Recientes (□)
4. Desliza la app hacia arriba para cerrarla
5. Espera 2 minutos
6. Verifica que la notificación sigue visible
7. Abre la app desde el launcher
8. Verifica el tiempo

**Resultado esperado:**
- ✅ La notificación permaneció visible
- ✅ El timer continuó contando (~12:30)
- ✅ Al reabrir, se restaura el estado correcto

**¿Pasó?** ⬜ Sí / ⬜ No

**Si FALLA este test:** El TimerService no está configurado correctamente como Foreground Service.

---

### Test 4: Pausar y Reanudar ⏸️▶️
**Objetivo:** Verificar los botones Pausar/Reanudar

**Pasos:**
1. Inicia un timer de 10 minutos
2. Espera a que llegue a 9:00
3. Presiona "Pausar"
4. Espera 30 segundos
5. Verifica que sigue en 9:00
6. Presiona "Reanudar"
7. Verifica que continúa contando

**Resultado esperado:**
- ✅ El timer se pausa correctamente
- ✅ El tiempo no cambia mientras está pausado
- ✅ Se reanuda desde el mismo punto

**¿Pasó?** ⬜ Sí / ⬜ No

---

### Test 5: Detener Timer 🛑
**Objetivo:** Verificar que se puede cancelar el timer

**Pasos:**
1. Inicia un timer de 10 minutos
2. Espera a que llegue a 7:00
3. Presiona "Detener Timer"
4. Verifica que la notificación desaparece
5. Verifica que vuelves a la pantalla principal

**Resultado esperado:**
- ✅ El timer se detiene inmediatamente
- ✅ La notificación desaparece
- ✅ Puedes iniciar un nuevo timer

**¿Pasó?** ⬜ Sí / ⬜ No

---

## 🔥 PRUEBAS AVANZADAS

### Test 6: Volumen Fade Out 🔉
**Objetivo:** Verificar que el volumen disminuye gradualmente

**Pasos:**
1. Reproduce música o un sonido ambiente
2. Ajusta el volumen a nivel medio-alto
3. Inicia un timer con fade de 5 minutos
4. Anota el volumen inicial
5. Observa cuando entren los últimos 5 minutos
6. Verifica que el volumen disminuye gradualmente

**Resultado esperado:**
- ✅ El volumen comienza a bajar en los últimos 5 min
- ✅ La disminución es gradual (no abrupta)
- ✅ Al finalizar el timer, el volumen está en 0

**¿Pasó?** ⬜ Sí / ⬜ No

---

### Test 7: Completar Timer ✅
**Objetivo:** Verificar la notificación de completado

**Pasos:**
1. Inicia un timer de 1 minuto (para prueba rápida)
2. Espera a que termine completamente (0:00)
3. Observa qué pasa

**Resultado esperado:**
- ✅ Vibración al completar
- ✅ Notificación de "Timer Complete!"
- ✅ Volumen en 0
- ✅ Se puede cerrar la notificación

**¿Pasó?** ⬜ Sí / ⬜ No

---

### Test 8: Llamada entrante durante timer 📞
**Objetivo:** Verificar que el timer no interfiere con llamadas

**Pasos:**
1. Inicia un timer
2. Recibe una llamada (o simula con otra app)
3. Contesta la llamada
4. Termina la llamada
5. Verifica el timer

**Resultado esperado:**
- ✅ La llamada entra normalmente
- ✅ El timer continúa en background
- ✅ Al terminar la llamada, el timer sigue funcionando

**¿Pasó?** ⬜ Sí / ⬜ No

---

### Test 9: Persistencia después de reinicio 🔄
**Objetivo:** Verificar que el timer sobrevive al reinicio (START_STICKY)

**Pasos:**
1. Inicia un timer de 20 minutos
2. Espera a que llegue a ~18:00
3. **Fuerza el cierre de la app** (Ajustes → Apps → Zzz Timer → Forzar detención)
4. Espera 1 minuto
5. Abre la app de nuevo

**Resultado esperado (con START_STICKY):**
- ⚠️ El servicio se reinició
- ⚠️ El timer se restauró desde la persistencia
- ⚠️ Continúa desde ~17:00

**Resultado actual (si no hay receiver de BOOT_COMPLETED):**
- ❌ El timer se detuvo
- ℹ️ Esto es normal si no hay AlarmManager configurado

**¿Pasó?** ⬜ Sí / ⬜ No

**Nota:** Para que funcione después de reinicio completo del teléfono, necesitarías un BroadcastReceiver para BOOT_COMPLETED.

---

### Test 10: Múltiples apps en background 📱
**Objetivo:** Verificar que el timer funciona con varias apps abiertas

**Pasos:**
1. Inicia un timer de 10 minutos
2. Abre YouTube y reproduce un video
3. Presiona Home
4. Abre Instagram y navega
5. Presiona Home
6. Abre Chrome y navega
7. Espera 3 minutos
8. Abre Zzz Timer de nuevo

**Resultado esperado:**
- ✅ El timer continuó contando
- ✅ El fade de volumen afectó a YouTube (si estaba configurado)
- ✅ No hubo crashes

**¿Pasó?** ⬜ Sí / ⬜ No

---

## 🔋 PRUEBAS DE BATERÍA Y OPTIMIZACIÓN

### Test 11: Modo de ahorro de batería ⚡
**Objetivo:** Verificar funcionamiento con ahorro de batería

**Pasos:**
1. Activa "Ahorro de batería" en Ajustes
2. Inicia un timer de 15 minutos
3. Cierra la app
4. Espera 5 minutos
5. Verifica la notificación
6. Abre la app

**Resultado esperado:**
- ✅ El timer continúa (puede tener ligero retraso)
- ✅ La notificación permanece

**Resultado en algunos teléfonos:**
- ⚠️ El timer puede pausarse o detenerse
- ℹ️ Esto depende del fabricante (Xiaomi, Huawei son muy agresivos)

**¿Pasó?** ⬜ Sí / ⬜ No

---

### Test 12: Optimización de batería por app 🔋
**Objetivo:** Desactivar optimización para mejor rendimiento

**Pasos:**
1. Ve a Ajustes → Apps → Zzz Timer Pro+
2. Batería → Optimización de batería
3. Cambia a "No optimizar"
4. Inicia un timer de 30 minutos
5. Cierra la app y bloquea el teléfono
6. Espera 10 minutos
7. Desbloquea y verifica

**Resultado esperado:**
- ✅ El timer funcionó perfectamente
- ✅ Sin pausas ni retrasos

**¿Pasó?** ⬜ Sí / ⬜ No

---

## 📊 REGISTRO DE RESULTADOS

### Resumen de pruebas:

| Test | Descripción | Pasó | Notas |
|------|-------------|------|-------|
| 1 | Timer básico | ⬜ | |
| 2 | Background (Home) | ⬜ | |
| 3 | Cerrar desde Recientes | ⬜ | |
| 4 | Pausar/Reanudar | ⬜ | |
| 5 | Detener | ⬜ | |
| 6 | Fade de volumen | ⬜ | |
| 7 | Completar timer | ⬜ | |
| 8 | Llamada entrante | ⬜ | |
| 9 | Forzar cierre | ⬜ | |
| 10 | Múltiples apps | ⬜ | |
| 11 | Ahorro batería | ⬜ | |
| 12 | Sin optimización | ⬜ | |

---

## 🐛 QUÉ HACER SI UN TEST FALLA

### Si falla Test 1 (Timer básico):
- **Problema:** Bug en el contador
- **Revisar:** `TimerService.kt` - función `startCountdownLoop()`

### Si falla Test 2 (Background Home):
- **Problema:** El servicio se pausa incorrectamente
- **Revisar:** Lifecycle de `TimerActivity`

### Si falla Test 3 (Cerrar Recientes):
- **Problema:** `onTaskRemoved()` no funciona
- **Revisar:** 
  - `TimerService.kt` - función `onTaskRemoved()`
  - Verificar que `startForeground()` se llama
  - Verificar que la notificación tiene `setOngoing(true)`

### Si falla Test 6 (Fade volumen):
- **Problema:** `startVolumeFade()` no se ejecuta
- **Revisar:** 
  - Condición `remainingMillis <= fadeDurationMillis`
  - Permisos de audio

### Si falla Test 9 (Forzar cierre):
- **Normal:** Sin AlarmManager, el timer no se reinicia
- **Solución:** Implementar AlarmManager para programar reinicio

---

## ✅ CRITERIOS DE ÉXITO

**Mínimo aceptable:**
- Tests 1-5: Deben pasar ✅
- Test 6: Debe pasar ✅
- Test 7: Debe pasar ✅

**Ideal:**
- Tests 1-8: Todos pasan ✅
- Tests 9-10: Pasan con comportamiento esperado ⚠️
- Tests 11-12: Pasan con optimización desactivada ✅

---

## 📝 NOTAS IMPORTANTES

### Sobre el Test 3 (Cerrar desde Recientes):

Este es el TEST MÁS IMPORTANTE porque replica exactamente lo que viste en el logcat.

**Si pasa:** Tu app está bien configurada como Foreground Service ✅

**Si falla:** Necesitas revisar:
1. `onTaskRemoved()` en `TimerService.kt`
2. `startForeground()` se llama correctamente
3. La notificación tiene las propiedades correctas

---

## 🎯 PRÓXIMOS PASOS

1. **Ejecuta los Tests 1-5** primero
2. **Si todos pasan**, ejecuta Tests 6-8
3. **Si Test 3 falla**, revisa `TimerService.kt`
4. **Comparte los resultados** conmigo si necesitas ayuda

---

**Fecha de creación:** 14 de Noviembre, 2025  
**Versión de pruebas:** 1.0  
**App:** Zzz Timer Pro+

