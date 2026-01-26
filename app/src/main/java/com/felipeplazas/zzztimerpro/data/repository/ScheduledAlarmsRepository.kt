package com.felipeplazas.zzztimerpro.data.repository

import android.content.Context
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.ScheduledAlarm
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestionar alarmas programadas.
 * Proporciona acceso centralizado a operaciones de alarmas.
 */
class ScheduledAlarmsRepository private constructor(private val db: AppDatabase) {

    /**
     * Obtiene todas las alarmas.
     * @return Flow de lista de alarmas
     */
    fun getAllAlarms(): Flow<List<ScheduledAlarm>> = db.scheduledAlarmDao().getAllAlarms()

    /**
     * Obtiene solo las alarmas habilitadas.
     * @return Flow de lista de alarmas activas
     */
    fun getEnabledAlarms(): Flow<List<ScheduledAlarm>> = db.scheduledAlarmDao().getEnabledAlarms()

    /**
     * Inserta una nueva alarma.
     * @param alarm Alarma a insertar
     * @return ID de la alarma insertada
     */
    suspend fun insertAlarm(alarm: ScheduledAlarm): Long = db.scheduledAlarmDao().insertAlarm(alarm)

    /**
     * Actualiza una alarma existente.
     * @param alarm Alarma con datos actualizados
     */
    suspend fun updateAlarm(alarm: ScheduledAlarm) = db.scheduledAlarmDao().updateAlarm(alarm)

    /**
     * Elimina una alarma.
     * @param alarm Alarma a eliminar
     */
    suspend fun deleteAlarm(alarm: ScheduledAlarm) = db.scheduledAlarmDao().deleteAlarm(alarm)

    /**
     * Habilita o deshabilita una alarma específica.
     * @param id ID de la alarma
     * @param enabled Estado deseado
     */
    suspend fun setEnabled(id: Long, enabled: Boolean) = db.scheduledAlarmDao().setAlarmEnabled(id, enabled)

    companion object {
        @Volatile
        private var INSTANCE: ScheduledAlarmsRepository? = null

        /**
         * Obtiene la instancia singleton del repositorio.
         * @param context Contexto de aplicación
         * @return Instancia del repositorio
         */
        fun getInstance(context: Context): ScheduledAlarmsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ScheduledAlarmsRepository(AppDatabase.getDatabase(context))
                INSTANCE = instance
                instance
            }
        }
    }
}

