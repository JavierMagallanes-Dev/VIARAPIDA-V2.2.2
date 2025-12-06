package com.viarapida.app.data.model

import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

data class Route(
    // ============ CAMPOS EXISTENTES ============
    val id: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val price: Double = 0.0,
    val totalSeats: Int = 40,
    val occupiedSeats: List<Int> = emptyList(),

    // ============ NUEVOS CAMPOS ============
    val busType: BusType = BusType.STANDARD,
    val departureDate: String = "",                     // Formato: "2025-12-08"
    val arrivalTime: String = "",                       // Formato: "04:00 PM"
    val durationMinutes: Int = 480,                     // 8 horas = 480 minutos
    val rating: Double = 0.0,                           // 0.0 - 5.0
    val totalReviews: Int = 0,
    val amenities: List<String> = emptyList(),          // ["WiFi", "Baño", "TV", "AC", "USB"]
    val company: String = "ViaRapida",
    val busPlate: String = "",                          // "ABC-123"
    val isActive: Boolean = true,
    val imageUrl: String = ""                           // URL de imagen del bus (opcional)
) {
    companion object {
        fun fromFirestore(document: DocumentSnapshot): Route? {
            return try {
                Route(
                    // Campos existentes
                    id = document.id,
                    origin = document.getString("origin") ?: "",
                    destination = document.getString("destination") ?: "",
                    departureTime = document.getString("departureTime") ?: "",
                    price = document.getDouble("price") ?: 0.0,
                    totalSeats = document.getLong("totalSeats")?.toInt() ?: 40,
                    occupiedSeats = (document.get("occupiedSeats") as? List<*>)
                        ?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList(),

                    // Nuevos campos (con valores por defecto si no existen)
                    busType = BusType.fromString(document.getString("busType")),
                    departureDate = document.getString("departureDate") ?: "",
                    arrivalTime = document.getString("arrivalTime") ?: "",
                    durationMinutes = document.getLong("durationMinutes")?.toInt() ?: 480,
                    rating = document.getDouble("rating") ?: 0.0,
                    totalReviews = document.getLong("totalReviews")?.toInt() ?: 0,
                    amenities = (document.get("amenities") as? List<*>)
                        ?.mapNotNull { it as? String } ?: emptyList(),
                    company = document.getString("company") ?: "ViaRapida",
                    busPlate = document.getString("busPlate") ?: "",
                    isActive = document.getBoolean("isActive") ?: true,
                    imageUrl = document.getString("imageUrl") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            // Campos existentes
            "origin" to origin,
            "destination" to destination,
            "departureTime" to departureTime,
            "price" to price,
            "totalSeats" to totalSeats,
            "occupiedSeats" to occupiedSeats,

            // Nuevos campos
            "busType" to busType.name,
            "departureDate" to departureDate,
            "arrivalTime" to arrivalTime,
            "durationMinutes" to durationMinutes,
            "rating" to rating,
            "totalReviews" to totalReviews,
            "amenities" to amenities,
            "company" to company,
            "busPlate" to busPlate,
            "isActive" to isActive,
            "imageUrl" to imageUrl
        )
    }

    // ============ FUNCIONES EXISTENTES ============

    fun getAvailableSeats(): List<Int> {
        return (1..totalSeats).filter { it !in occupiedSeats }
    }

    fun getAvailableSeatsCount(): Int {
        return totalSeats - occupiedSeats.size
    }

    // ============ NUEVAS FUNCIONES ÚTILES ============

    /**
     * Obtiene la duración formateada (ej: "8h 30m")
     */
    fun getFormattedDuration(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return if (minutes > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${hours}h"
        }
    }

    /**
     * Obtiene el rating con formato (ej: "4.5 ⭐")
     */
    fun getFormattedRating(): String {
        return if (totalReviews > 0) {
            String.format("%.1f ⭐ (%d)", rating, totalReviews)
        } else {
            "Sin calificaciones"
        }
    }

    /**
     * Verifica si tiene una amenidad específica
     */
    fun hasAmenity(amenity: String): Boolean {
        return amenities.any { it.equals(amenity, ignoreCase = true) }
    }

    /**
     * Obtiene la fecha de salida formateada
     */
    fun getFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "PE"))
            val date = inputFormat.parse(departureDate)
            date?.let { outputFormat.format(it) } ?: departureDate
        } catch (e: Exception) {
            departureDate
        }
    }

    /**
     * Verifica si el bus está disponible para reservar
     */
    fun isAvailableForBooking(): Boolean {
        return isActive && getAvailableSeatsCount() > 0
    }

    /**
     * Obtiene el porcentaje de ocupación
     */
    fun getOccupancyPercentage(): Int {
        return ((occupiedSeats.size.toFloat() / totalSeats) * 100).toInt()
    }

    /**
     * Verifica si el bus está casi lleno (>80%)
     */
    fun isAlmostFull(): Boolean {
        return getOccupancyPercentage() >= 80
    }

    /**
     * Obtiene todas las amenidades como string
     */
    fun getAmenitiesString(): String {
        return amenities.joinToString(" • ")
    }
}