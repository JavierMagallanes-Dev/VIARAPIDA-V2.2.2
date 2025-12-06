package com.viarapida.app.data.model

/**
 * Rangos de horario para filtrar búsquedas
 */
enum class TimeRange(
    val displayName: String,
    val startHour: Int,
    val endHour: Int,
    val icon: String
) {
    MORNING(
        displayName = "Mañana",
        startHour = 6,
        endHour = 12,
        icon = "☀️"
    ),
    AFTERNOON(
        displayName = "Tarde",
        startHour = 12,
        endHour = 18,
        icon = "🌤️"
    ),
    EVENING(
        displayName = "Noche",
        startHour = 18,
        endHour = 24,
        icon = "🌙"
    ),
    LATE_NIGHT(
        displayName = "Madrugada",
        startHour = 0,
        endHour = 6,
        icon = "🌃"
    );

    /**
     * Verifica si una hora está dentro del rango
     */
    fun containsHour(hour: Int): Boolean {
        return if (startHour < endHour) {
            hour in startHour until endHour
        } else {
            // Para casos como 23:00 - 02:00
            hour >= startHour || hour < endHour
        }
    }

    /**
     * Obtiene el rango de tiempo formateado
     */
    fun getTimeRangeFormatted(): String {
        return "$icon $displayName (${formatHour(startHour)} - ${formatHour(endHour)})"
    }

    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12 AM"
            hour < 12 -> "$hour AM"
            hour == 12 -> "12 PM"
            else -> "${hour - 12} PM"
        }
    }

    companion object {
        /**
         * Obtiene el rango de tiempo basado en una hora
         */
        fun fromHour(hour: Int): TimeRange {
            return values().find { it.containsHour(hour) } ?: MORNING
        }

        fun getAllRanges(): List<TimeRange> = values().toList()
    }
}