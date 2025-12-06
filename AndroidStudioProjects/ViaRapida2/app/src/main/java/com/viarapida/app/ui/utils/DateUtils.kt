package com.viarapida.app.ui.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    private const val DATE_FORMAT_API = "yyyy-MM-dd"
    private const val DATE_FORMAT_SHORT = "dd/MM"

    private val spanishLocale = Locale("es", "PE")

    /**
     * Obtiene la fecha actual
     */
    fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }

    /**
     * Obtiene la fecha actual en formato String (yyyy-MM-dd)
     */
    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT_API, Locale.getDefault())
        return sdf.format(getCurrentDate())
    }

    /**
     * Formatea una fecha para mostrar (ej: "15 Dic 2025")
     */
    fun formatDateForDisplay(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Seleccionar fecha"

        return try {
            val inputFormat = SimpleDateFormat(DATE_FORMAT_API, Locale.getDefault())
            val outputFormat = SimpleDateFormat(DATE_FORMAT_DISPLAY, spanishLocale)
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Formatea fecha corta (ej: "15/12")
     */
    fun formatDateShort(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat(DATE_FORMAT_API, Locale.getDefault())
            val outputFormat = SimpleDateFormat(DATE_FORMAT_SHORT, Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Convierte Date a String (yyyy-MM-dd)
     */
    fun dateToString(date: Date): String {
        val sdf = SimpleDateFormat(DATE_FORMAT_API, Locale.getDefault())
        return sdf.format(date)
    }

    /**
     * Convierte String (yyyy-MM-dd) a Date
     */
    fun stringToDate(dateString: String): Date? {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT_API, Locale.getDefault())
            sdf.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Agrega días a una fecha
     */
    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    /**
     * Agrega días a una fecha en formato String
     */
    fun addDaysToString(dateString: String, days: Int): String {
        val date = stringToDate(dateString) ?: return dateString
        val newDate = addDays(date, days)
        return dateToString(newDate)
    }

    /**
     * Obtiene rango de fechas (±N días)
     */
    fun getDateRange(centerDate: String, rangeDays: Int = 3): List<String> {
        val center = stringToDate(centerDate) ?: return emptyList()
        val dates = mutableListOf<String>()

        for (i in -rangeDays..rangeDays) {
            val date = addDays(center, i)
            dates.add(dateToString(date))
        }

        return dates
    }

    /**
     * Verifica si una fecha es hoy
     */
    fun isToday(dateString: String): Boolean {
        return dateString == getCurrentDateString()
    }

    /**
     * Verifica si una fecha es mañana
     */
    fun isTomorrow(dateString: String): Boolean {
        val tomorrow = addDaysToString(getCurrentDateString(), 1)
        return dateString == tomorrow
    }

    /**
     * Verifica si una fecha está en el pasado
     */
    fun isPastDate(dateString: String): Boolean {
        val date = stringToDate(dateString) ?: return false
        val today = getCurrentDate()

        val calendar = Calendar.getInstance()
        calendar.time = today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return date.before(calendar.time)
    }

    /**
     * Obtiene el nombre del día de la semana
     */
    fun getDayOfWeek(dateString: String): String {
        return try {
            val date = stringToDate(dateString) ?: return ""
            val calendar = Calendar.getInstance()
            calendar.time = date

            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Dom"
                Calendar.MONDAY -> "Lun"
                Calendar.TUESDAY -> "Mar"
                Calendar.WEDNESDAY -> "Mié"
                Calendar.THURSDAY -> "Jue"
                Calendar.FRIDAY -> "Vie"
                Calendar.SATURDAY -> "Sáb"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Obtiene label amigable para fecha (Hoy, Mañana, fecha)
     */
    fun getFriendlyDateLabel(dateString: String): String {
        return when {
            isToday(dateString) -> "Hoy"
            isTomorrow(dateString) -> "Mañana"
            else -> {
                val dayOfWeek = getDayOfWeek(dateString)
                val shortDate = formatDateShort(dateString)
                "$dayOfWeek $shortDate"
            }
        }
    }

    /**
     * Calcula días entre dos fechas
     */
    fun daysBetween(startDate: String, endDate: String): Int {
        return try {
            val start = stringToDate(startDate) ?: return 0
            val end = stringToDate(endDate) ?: return 0

            val diff = end.time - start.time
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Obtiene los próximos N días desde hoy
     */
    fun getUpcomingDates(days: Int = 30): List<String> {
        val dates = mutableListOf<String>()
        val today = getCurrentDate()

        for (i in 0 until days) {
            val date = addDays(today, i)
            dates.add(dateToString(date))
        }

        return dates
    }

    /**
     * Formatea rango de fechas para mostrar
     */
    fun formatDateRangeForDisplay(startDate: String?, endDate: String?): String {
        if (startDate == null && endDate == null) return "Cualquier fecha"
        if (startDate == null) return "Hasta ${formatDateForDisplay(endDate)}"
        if (endDate == null) return "Desde ${formatDateForDisplay(startDate)}"

        return "${formatDateForDisplay(startDate)} - ${formatDateForDisplay(endDate)}"
    }
}