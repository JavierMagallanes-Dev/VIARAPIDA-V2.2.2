package com.viarapida.app.data.model

/**
 * Modelo para filtros de búsqueda de rutas
 */
data class SearchFilters(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val busTypes: List<BusType> = emptyList(),          // Vacío = todos los tipos
    val minRating: Double? = null,
    val departureTimeRanges: List<TimeRange> = emptyList(), // Vacío = todos los horarios
    val amenities: List<String> = emptyList(),          // Vacío = sin filtro de amenidades
    val sortBy: SortOption = SortOption.PRICE_ASC,
    val departureDate: String? = null                   // Formato: "2025-12-08"
) {
    /**
     * Verifica si hay algún filtro activo (excepto ordenamiento)
     */
    fun hasActiveFilters(): Boolean {
        return minPrice != null ||
                maxPrice != null ||
                busTypes.isNotEmpty() ||
                minRating != null ||
                departureTimeRanges.isNotEmpty() ||
                amenities.isNotEmpty() ||
                departureDate != null
    }

    /**
     * Cuenta cuántos filtros están activos
     */
    fun getActiveFiltersCount(): Int {
        var count = 0
        if (minPrice != null || maxPrice != null) count++
        if (busTypes.isNotEmpty()) count++
        if (minRating != null) count++
        if (departureTimeRanges.isNotEmpty()) count++
        if (amenities.isNotEmpty()) count++
        if (departureDate != null) count++
        return count
    }

    /**
     * Limpia todos los filtros
     */
    fun clear(): SearchFilters {
        return SearchFilters(sortBy = this.sortBy)
    }

    /**
     * Aplica el filtro a una lista de rutas
     */
    fun applyTo(routes: List<Route>): List<Route> {
        var filtered = routes

        // Filtrar por precio
        minPrice?.let { min ->
            filtered = filtered.filter { it.price >= min }
        }
        maxPrice?.let { max ->
            filtered = filtered.filter { it.price <= max }
        }

        // Filtrar por tipo de bus
        if (busTypes.isNotEmpty()) {
            filtered = filtered.filter { it.busType in busTypes }
        }

        // Filtrar por rating
        minRating?.let { min ->
            filtered = filtered.filter { it.rating >= min }
        }

        // Filtrar por horario
        if (departureTimeRanges.isNotEmpty()) {
            filtered = filtered.filter { route ->
                val hour = extractHourFromTime(route.departureTime)
                departureTimeRanges.any { it.containsHour(hour) }
            }
        }

        // Filtrar por amenidades (debe tener TODAS las seleccionadas)
        if (amenities.isNotEmpty()) {
            filtered = filtered.filter { route ->
                amenities.all { amenity -> route.hasAmenity(amenity) }
            }
        }

        // Filtrar por fecha
        departureDate?.let { date ->
            filtered = filtered.filter { it.departureDate == date }
        }

        // Aplicar ordenamiento
        filtered = applySorting(filtered, sortBy)

        return filtered
    }

    /**
     * Extrae la hora de un string de tiempo
     */
    private fun extractHourFromTime(time: String): Int {
        return try {
            val timePart = time.split(":")[0].trim()
            val hour = timePart.toIntOrNull() ?: 0
            val isPM = time.contains("PM", ignoreCase = true)

            when {
                hour == 12 && !isPM -> 0
                hour == 12 && isPM -> 12
                isPM -> hour + 12
                else -> hour
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Aplica el ordenamiento a la lista de rutas
     */
    private fun applySorting(routes: List<Route>, sortOption: SortOption): List<Route> {
        return when (sortOption) {
            SortOption.PRICE_ASC -> routes.sortedBy { it.price }
            SortOption.PRICE_DESC -> routes.sortedByDescending { it.price }
            SortOption.DEPARTURE_ASC -> routes.sortedBy { it.departureTime }
            SortOption.DEPARTURE_DESC -> routes.sortedByDescending { it.departureTime }
            SortOption.DURATION_ASC -> routes.sortedBy { it.durationMinutes }
            SortOption.DURATION_DESC -> routes.sortedByDescending { it.durationMinutes }
            SortOption.RATING_DESC -> routes.sortedByDescending { it.rating }
            SortOption.RATING_ASC -> routes.sortedBy { it.rating }
        }
    }

    companion object {
        /**
         * Filtros por defecto
         */
        fun default(): SearchFilters = SearchFilters()

        /**
         * Filtros para búsqueda rápida (solo lo esencial)
         */
        fun quickSearch(sortBy: SortOption = SortOption.PRICE_ASC): SearchFilters {
            return SearchFilters(sortBy = sortBy)
        }
    }
}