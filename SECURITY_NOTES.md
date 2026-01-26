# SECURITY NOTES

## Licenciamiento y Compras
- BillingClient v6: compras one-time.
- Estado de licencia debe guardarse en almacenamiento interno o DataStore encriptado (futuro: EncryptedSharedPreferences).

## Recomendaciones
- [ ] Validar recibos en backend (opcional para evitar spoofing).
- [ ] Evitar exponer lógica de desbloqueo en strings fáciles de localizar.
- [ ] ProGuard/R8 mantiene ofuscación en release.

## Datos de Sueño
- No guardar datos sensibles fuera de Room.
- Exportación CSV: limpiar delimitadores potencialmente peligrosos.

## Permisos
- Solicitar solo cuando se necesitan (post-notifications, exact alarm).
- Evitar solicitar micrófono si no se implementa análisis de audio todavía.

## Networking Futuro
- Usar HTTPS obligatorio (OkHttp con certificate pinning si sensitivo).

