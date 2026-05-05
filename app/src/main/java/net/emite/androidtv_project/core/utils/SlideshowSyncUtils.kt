package net.emite.androidtv_project.core.utils

import net.emite.androidtv_project.domain.model.SlideshowItem
import java.util.Calendar

/**
 * Utilidades para el cálculo de sincronización determinística de slideshow.
 *
 * El principio fundamental es que todos los dispositivos utilicen la hora del sistema
 * como fuente de verdad compartida, calculando la posición actual del ciclo de forma
 * independiente al momento en que se inició la aplicación.
 */
object SlideshowSyncUtils {

    /**
     * Obtiene los segundos transcurridos desde la medianoche (00:00:00) del día actual.
     * Este valor es el mismo en todos los dispositivos sincronizados con la misma hora.
     *
     * @return Número de segundos desde la medianoche.
     */
    fun getSecondsSinceMidnight(): Long {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        return (hours * 3600L) + (minutes * 60L) + seconds
    }

    /**
     * Obtiene la hora actual formateada como HH:mm:ss para uso en logs.
     *
     * @return String con la hora actual en formato legible.
     */
    fun getCurrentTimeString(): String {
        val calendar = Calendar.getInstance()
        return String.format(
            "%02d:%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )
    }

    /**
     * Calcula la duración total (en segundos) de todos los ítems del ciclo.
     *
     * @param items Lista de ítems activos del slideshow.
     * @return Suma de las duraciones de todos los ítems en segundos.
     */
    fun calculateTotalCycleDuration(items: List<SlideshowItem>): Long {
        return items.sumOf { it.durationSeconds.toLong() }
    }

    /**
     * Resultado del cálculo de sincronización para un ítem determinado.
     *
     * @param item El ítem que debe estar reproduciéndose en el instante actual.
     * @param itemIndex El índice del ítem en la lista activa (base 0).
     * @param remainingSeconds Segundos que quedan para que este ítem termine.
     * @param slotStartSeconds Segundo del ciclo en el que empieza este ítem.
     * @param slotEndSeconds Segundo del ciclo en el que termina este ítem.
     */
    data class SyncResult(
        val item: SlideshowItem,
        val itemIndex: Int,
        val remainingSeconds: Long,
        val slotStartSeconds: Long,
        val slotEndSeconds: Long
    )

    /**
     * Calcula de forma determinística qué ítem del slideshow debe estar
     * reproduciéndose en el instante actual, basándose en la hora del sistema.
     *
     * El algoritmo usa la medianoche (00:00:00) como punto de referencia fijo,
     * garantizando que múltiples dispositivos con la misma hora del sistema
     * calcularán el mismo resultado independientemente de cuándo arrancaron.
     *
     * @param items Lista de ítems activos del slideshow (ya filtrados por día/hora).
     *              Deben estar ordenados correctamente (por 'orden').
     * @return Un [SyncResult] con el ítem a mostrar y el tiempo restante,
     *         o null si la lista está vacía o la duración total es 0.
     */
    fun findCurrentSynchronizedItem(items: List<SlideshowItem>): SyncResult? {
        if (items.isEmpty()) return null

        val totalDuration = calculateTotalCycleDuration(items)
        if (totalDuration <= 0L) return null

        val secondsSinceMidnight = getSecondsSinceMidnight()
        val positionInCycle = secondsSinceMidnight % totalDuration

        var cumulativeSeconds = 0L
        for ((index, item) in items.withIndex()) {
            val slotStart = cumulativeSeconds
            val slotEnd = cumulativeSeconds + item.durationSeconds.toLong()

            if (positionInCycle < slotEnd) {
                val remainingSeconds = slotEnd - positionInCycle
                return SyncResult(
                    item = item,
                    itemIndex = index,
                    remainingSeconds = remainingSeconds,
                    slotStartSeconds = slotStart,
                    slotEndSeconds = slotEnd
                )
            }
            cumulativeSeconds = slotEnd
        }

        // Fallback: si por redondeo no se encontró ninguno, devuelve el último
        val lastItem = items.last()
        return SyncResult(
            item = lastItem,
            itemIndex = items.lastIndex,
            remainingSeconds = lastItem.durationSeconds.toLong(),
            slotStartSeconds = totalDuration - lastItem.durationSeconds.toLong(),
            slotEndSeconds = totalDuration
        )
    }
}
