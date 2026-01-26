package com.felipeplazas.zzztimerpro.data.repository

import android.content.Context
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.SavedTimer
import com.felipeplazas.zzztimerpro.data.local.SavedTimerDao
import com.felipeplazas.zzztimerpro.utils.LogExt
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestionar temporizadores guardados.
 * Proporciona una capa de abstracción entre la UI y Room Database.
 */
class SavedTimersRepository private constructor(private val dao: SavedTimerDao) {

    /**
     * Obtiene todos los temporizadores guardados ordenados por fecha de creación.
     * @return Flow de lista de temporizadores
     */
    fun getAllTimers(): Flow<List<SavedTimer>> = dao.getAllTimers()

    /**
     * Obtiene los temporizadores más usados.
     * @return Flow de lista de temporizadores ordenados por uso
     */
    fun getMostUsedTimers(): Flow<List<SavedTimer>> = dao.getMostUsedTimers()

    /**
     * Obtiene un temporizador específico por ID.
     * @param id ID del temporizador
     * @return Temporizador o null si no existe
     */
    suspend fun getTimerById(id: Long): SavedTimer? = dao.getTimerById(id)

    /**
     * Guarda un nuevo temporizador.
     * @param timer Temporizador a guardar
     * @return ID del temporizador insertado
     * @throws Exception si falla la inserción
     */
    suspend fun saveTimer(timer: SavedTimer): Long {
        return try {
            dao.insertTimer(timer)
        } catch (e: Exception) {
            LogExt.logStructured(
                tag = "REPO",
                phase = "SAVED_TIMERS",
                event = "insert_error",
                severity = LogExt.Severity.ERROR,
                metrics = mapOf("error" to (e.message ?: "unknown")),
                msg = e.javaClass.simpleName
            )
            throw e
        }
    }

    /**
     * Actualiza un temporizador existente.
     * @param timer Temporizador con datos actualizados
     */
    suspend fun updateTimer(timer: SavedTimer) {
        dao.updateTimer(timer)
    }

    /**
     * Elimina un temporizador.
     * @param timer Temporizador a eliminar
     */
    suspend fun deleteTimer(timer: SavedTimer) {
        dao.deleteTimer(timer)
    }

    /**
     * Elimina un temporizador por ID.
     * @param id ID del temporizador a eliminar
     */
    suspend fun deleteTimerById(id: Long) {
        dao.deleteTimerById(id)
    }

    /**
     * Marca un temporizador como usado, actualizando contador y timestamp.
     * @param id ID del temporizador
     * @param timestamp Timestamp del uso (por defecto ahora)
     */
    suspend fun markTimerUsed(id: Long, timestamp: Long = System.currentTimeMillis()) {
        dao.updateTimerUsage(id, timestamp)
    }

    companion object {
        @Volatile
        private var INSTANCE: SavedTimersRepository? = null

        /**
         * Obtiene la instancia singleton del repositorio.
         * @param context Contexto de aplicación
         * @return Instancia del repositorio
         */
        fun getInstance(context: Context): SavedTimersRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = SavedTimersRepository(db.savedTimerDao())
                INSTANCE = instance
                instance
            }
        }

        /**
         * Fábrica para pruebas que permite inyectar un DAO in-memory sin tocar el singleton.
         */
        fun createForTest(dao: SavedTimerDao): SavedTimersRepository = SavedTimersRepository(dao)
    }
}
