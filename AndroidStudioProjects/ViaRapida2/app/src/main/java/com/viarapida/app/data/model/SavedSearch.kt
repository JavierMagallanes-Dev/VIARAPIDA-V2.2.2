package com.viarapida.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Modelo para búsquedas guardadas del usuario
 */
data class SavedSearch(
    val id: String = "",
    val userId: String = "",
    val origin: String = "",
    val destination: String = "",
    val name: String = "",                              // "Viaje a Lima", "Trabajo semanal", etc.
    val filters: SearchFilters? = null,                 // Filtros asociados (opcional)
    val createdAt: Timestamp = Timestamp.now(),
    val lastUsed: Timestamp = Timestamp.now(),
    val useCount: Int = 0,                              // Contador de usos
    val icon: String = "🔍"                             // Emoji representativo (opcional)
) {
    companion object {
        fun fromFirestore(document: DocumentSnapshot): SavedSearch? {
            return try {
                SavedSearch(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    origin = document.getString("origin") ?: "",
                    destination = document.getString("destination") ?: "",
                    name = document.getString("name") ?: "",
                    // filters se guardará como Map, aquí lo reconstruimos
                    filters = null, // Por ahora null, se puede implementar deserialización
                    createdAt = document.getTimestamp("createdAt") ?: Timestamp.now(),
                    lastUsed = document.getTimestamp("lastUsed") ?: Timestamp.now(),
                    useCount = document.getLong("useCount")?.toInt() ?: 0,
                    icon = document.getString("icon") ?: "🔍"
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "origin" to origin,
            "destination" to destination,
            "name" to name,
            "createdAt" to createdAt,
            "lastUsed" to lastUsed,
            "useCount" to useCount,
            "icon" to icon
        )
    }

    /**
     * Obtiene el nombre para mostrar
     */
    fun getDisplayName(): String {
        return name.ifBlank { "$origin → $destination" }
    }

    /**
     * Obtiene la fecha formateada de último uso
     */
    fun getFormattedLastUsed(): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM", Locale("es", "PE"))
            sdf.format(lastUsed.toDate())
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Verifica si la búsqueda es reciente (últimos 7 días)
     */
    fun isRecent(): Boolean {
        val now = Timestamp.now().seconds
        val daysDiff = (now - lastUsed.seconds) / (60 * 60 * 24)
        return daysDiff <= 7
    }

    /**
     * Obtiene el texto de frecuencia de uso
     */
    fun getUsageText(): String {
        return when {
            useCount == 0 -> "Sin usar"
            useCount == 1 -> "Usado 1 vez"
            useCount < 5 -> "Usado $useCount veces"
            useCount < 10 -> "Uso frecuente ($useCount veces)"
            else -> "Muy usado ($useCount veces)"
        }
    }
}