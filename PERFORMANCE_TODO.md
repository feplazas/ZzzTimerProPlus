# PERFORMANCE TODO

## Observaciones Iniciales
- MediaPlayer: liberar siempre en onDestroy / al finalizar fade.
- Vibración: usar VibratorManager solo cuando necesario; evitar loops largos.
- Room: índices ya añadidos (saved_timers y scheduled_alarms). Revisar consultas futuras.

## Acciones
- [ ] Implementar fade usando Coroutine + ajustar volumen incremental (verificar no bloquea UI).
- [ ] Añadir cache en repositorio de temporizadores (in-memory Map para accesos frecuentes).
- [ ] Usar Dispatchers.IO en todas las operaciones Room explícitas.
- [ ] Añadir medición simple con `System.nanoTime()` en cálculos SleepScore si se expanden.
- [ ] Considerar WorkManager para tareas de backup pesado.
- [ ] Revisar tamaños de audio (stream vs load en memoria si >5MB).

## Futuro
- [ ] Integrar Benchmark tests (Jetpack Benchmark) para SleepScoreCalculator.

