package com.viarapida.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Modelo para alertas de precio
 */
data class PriceAlert(
    val id: String = "",
    val userId: String = "",
    val origin: String = "",
    val destination: String = "",
    val targetPrice: Double = 0.0,                      // Precio objetivo
    val currentLowestPrice: Double = 0.0,               // Precio más bajo actual
    val isActive: Boolean = true,
    val notificationSent: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp? = null,                   // Fecha de expiración (opcional)
    val lastChecked: Timestamp? = null                  // Última vez que se verificó el precio
) {
    companion object {
        fun fromFirestore(document: DocumentSnapshot): PriceAlert? {
            return try {
                PriceAlert(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    origin = document.getString("origin") ?: "",
                    destination = document.getString("destination") ?: "",
                    targetPrice = document.getDouble("targetPrice") ?: 0.0,
                    currentLowestPrice = document.getDouble("currentLowestPrice") ?: 0.0,
                    isActive = document.getBoolean("isActive") ?: true,
                    notificationSent = document.getBoolean("notificationSent") ?: false,
                    createdAt = document.getTimestamp("createdAt") ?: Timestamp.now(),
                    expiresAt = document.getTimestamp("expiresAt"),
                    lastChecked = document.getTimestamp("lastChecked")
                )
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Crea una alerta con expiración de 30 días
         */
        fun create(
            userId: String,
            origin: String,
            destination: String,
            targetPrice: Double
        ): PriceAlert {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 30)
            val expiresAt = Timestamp(calendar.time)

            return PriceAlert(
                userId = userId,
                origin = origin,
                destination = destination,
                targetPrice = targetPrice,
                expiresAt = expiresAt
            )
        }
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf(
            "userId" to userId,
            "origin" to origin,
            "destination" to destination,
            "targetPrice" to targetPrice,
            "currentLowestPrice" to currentLowestPrice,
            "isActive" to isActive,
            "notificationSent" to notificationSent,
            "createdAt" to createdAt
        )

        expiresAt?.let { map["expiresAt"] = it }
        lastChecked?.let { map["lastChecked"] = it }

        return map
    }

    /**
     * Verifica si la alerta ha expirado
     */
    fun isExpired(): Boolean {
        expiresAt?.let {
            return Timestamp.now().seconds > it.seconds
        }
        return false
    }

    /**
     * Verifica si el precio objetivo se ha alcanzado
     */
    fun isPriceReached(): Boolean {
        return currentLowestPrice > 0 && currentLowestPrice <= targetPrice
    }

    /**
     * Obtiene el porcentaje de diferencia con el precio objetivo
     */
    fun getPriceDifferencePercentage(): Double {
        return if (currentLowestPrice > 0) {
            ((currentLowestPrice - targetPrice) / targetPrice) * 100
        } else {
            0.0
        }
    }

    /**
     * Obtiene el texto del estado de la alerta
     */
    fun getStatusText(): String {
        return when {
            isExpired() -> "⏰ Expirada"
            notificationSent -> "✅ Notificada"
            isPriceReached() -> "🎯 Precio alcanzado"
            isActive -> "🔔 Activa"
            else -> "⏸️ Pausada"
        }
    }

    /**
     * Obtiene el texto de ahorro potencial
     */
    fun getSavingsText(): String {
        val savings = currentLowestPrice - targetPrice
        return if (savings > 0) {
            "Ahorra S/ ${String.format("%.2f", savings)}"
        } else {
            "Objetivo: S/ ${String.format("%.2f", targetPrice)}"
        }
    }

    /**
     * Obtiene la fecha de expiración formateada
     */
    fun getFormattedExpiryDate(): String {
        return try {
            expiresAt?.let {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es", "PE"))
                sdf.format(it.toDate())
            } ?: "Sin expiración"
        } catch (e: Exception) {
            "Sin expiración"
        }
    }

    /**
     * Obtiene los días restantes hasta la expiración
     */
    fun getDaysUntilExpiry(): Int {
        expiresAt?.let {
            val now = Timestamp.now().seconds
            val diff = it.seconds - now
            return (diff / (60 * 60 * 24)).toInt()
        }
        return -1
    }

    /**
     * Verifica si debe enviarse notificación
     */
    fun shouldNotify(): Boolean {
        return isActive &&
                !notificationSent &&
                !isExpired() &&
                isPriceReached()
    }
}