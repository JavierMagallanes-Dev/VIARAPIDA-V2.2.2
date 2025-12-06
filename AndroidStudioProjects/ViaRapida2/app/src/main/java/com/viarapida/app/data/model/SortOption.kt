package com.viarapida.app.data.model

/**
 * Opciones de ordenamiento para resultados de búsqueda
 */
enum class SortOption(val displayName: String) {
    PRICE_ASC("Precio: Menor a Mayor"),
    PRICE_DESC("Precio: Mayor a Menor"),
    DEPARTURE_ASC("Salida: Más Temprano"),
    DEPARTURE_DESC("Salida: Más Tarde"),
    DURATION_ASC("Duración: Más Corto"),
    DURATION_DESC("Duración: Más Largo"),
    RATING_DESC("Mejor Calificación"),
    RATING_ASC("Menor Calificación");

    companion object {
        fun getDefault(): SortOption = PRICE_ASC

        fun getAllOptions(): List<SortOption> = values().toList()
    }
}