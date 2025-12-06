package com.viarapida.app.data.model

/**
 * Tipos de buses disponibles en el sistema
 */
enum class BusType(
    val displayName: String,
    val description: String,
    val priceMultiplier: Double // Multiplicador de precio base
) {
    STANDARD(
        displayName = "Estándar",
        description = "Asientos reclinables básicos",
        priceMultiplier = 1.0
    ),
    SEMICAMA(
        displayName = "Semi Cama",
        description = "Asientos reclinables 140°",
        priceMultiplier = 1.3
    ),
    CAMA(
        displayName = "Cama",
        description = "Asientos reclinables 180°",
        priceMultiplier = 1.6
    ),
    VIP(
        displayName = "VIP",
        description = "Asientos premium con servicio exclusivo",
        priceMultiplier = 2.0
    );

    companion object {
        /**
         * Obtiene el tipo de bus desde un string
         */
        fun fromString(value: String?): BusType {
            return values().find {
                it.name.equals(value, ignoreCase = true)
            } ?: STANDARD
        }

        /**
         * Lista de todos los tipos para mostrar en UI
         */
        fun getAllTypes(): List<BusType> = values().toList()
    }
}