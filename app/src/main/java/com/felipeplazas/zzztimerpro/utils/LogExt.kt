package com.felipeplazas.zzztimerpro.utils

import android.util.Log
import java.util.Locale

/**
 * Utilidad para logging estructurado consistente en toda la app.
 * Formato base:
 * TAG | phase=FASE | event=EVENTO | cid=CORRELATION_ID | k1=v1 | k2=v2 | thread=main/bg | ts=epoch_ms | rel=ms_desde_inicio | severity=LEVEL | msg=Descripcion
 */
object LogExt {
    private const val GLOBAL_TAG_PREFIX = "ZZZ" // Prefijo común para facilitar filtrado
    private val appStartEpoch = System.currentTimeMillis()

    enum class Severity { DEBUG, INFO, WARN, ERROR, FATAL }

    @Volatile
    private var enabled = true // Se podría desactivar en release si se requiere

    fun setEnabled(value: Boolean) { enabled = value }

    fun logStructured(
        tag: String,
        phase: String,
        event: String,
        severity: Severity = Severity.INFO,
        correlationId: String? = null,
        metrics: Map<String, Any?> = emptyMap(),
        msg: String = ""
    ) {
        if (!enabled) return
        val epoch = System.currentTimeMillis()
        val rel = epoch - appStartEpoch
        val threadName = Thread.currentThread().name.lowercase(Locale.getDefault())
        val threadType = if (threadName.contains("main")) "main" else "bg"

        val metricsStr = buildString {
            for ((k, v) in metrics) {
                append(" | ")
                append(k)
                append("=")
                append(v)
            }
        }

        val core = "$tag | phase=$phase | event=$event" +
                (correlationId?.let { " | cid=$it" } ?: "") +
                metricsStr +
                " | thread=$threadType | ts=$epoch | rel=${rel} | severity=$severity" +
                (if (msg.isNotBlank()) " | msg=$msg" else "")

        when (severity) {
            Severity.DEBUG -> Log.d("${GLOBAL_TAG_PREFIX}_$tag", core)
            Severity.INFO -> Log.i("${GLOBAL_TAG_PREFIX}_$tag", core)
            Severity.WARN -> Log.w("${GLOBAL_TAG_PREFIX}_$tag", core)
            Severity.ERROR -> Log.e("${GLOBAL_TAG_PREFIX}_$tag", core)
            Severity.FATAL -> Log.wtf("${GLOBAL_TAG_PREFIX}_$tag", core)
        }
    }
}

