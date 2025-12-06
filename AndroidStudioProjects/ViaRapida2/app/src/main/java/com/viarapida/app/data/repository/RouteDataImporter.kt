package com.viarapida.app.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.viarapida.app.data.model.BusType
import com.viarapida.app.data.model.Route
import com.viarapida.app.data.remote.FirebaseClient
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * ⚡ IMPORTADOR AUTOMÁTICO DE RUTAS
 *
 * Uso:
 * 1. Agregar en RouteRepository: suspend fun importMassiveRoutes(): Result<Unit>
 * 2. Implementar en RouteRepositoryImpl usando esta función
 * 3. Llamar una sola vez desde SplashViewModel o AdminScreen
 */
class RouteDataImporter {

    private val firestore = FirebaseClient.firestore

    companion object {
        private const val TAG = "RouteDataImporter"
        private const val ROUTES_COLLECTION = "routes"
    }

    /**
     * ⬇️ IMPORTA 90+ RUTAS AUTOMÁTICAMENTE
     */
    suspend fun importAllRoutes(): Result<Unit> {
        return try {
            Log.d(TAG, "🚀 Iniciando importación masiva de rutas...")

            val routes = generateAllRoutes()
            var successCount = 0
            var errorCount = 0

            routes.forEach { route ->
                try {
                    firestore.collection(ROUTES_COLLECTION)
                        .add(route.toMap())
                        .await()
                    successCount++
                    Log.d(TAG, "✅ Ruta importada: ${route.origin} → ${route.destination} (${route.departureDate})")
                } catch (e: Exception) {
                    errorCount++
                    Log.e(TAG, "❌ Error en ruta: ${route.origin} → ${route.destination}", e)
                }
            }

            Log.d(TAG, "🎉 Importación completada: $successCount exitosas, $errorCount errores")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "💥 Error fatal en importación: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * 🔧 GENERA TODAS LAS RUTAS (30 días)
     */
    private fun generateAllRoutes(): List<Route> {
        val routes = mutableListOf<Route>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = Calendar.getInstance().apply {
            set(2025, Calendar.DECEMBER, 6) // 6 de diciembre 2025
        }

        // ===== RUTAS DIARIAS (6-31 Dic + 1-5 Ene) =====
        for (day in 0..30) {
            val currentDate = startDate.clone() as Calendar
            currentDate.add(Calendar.DAY_OF_YEAR, day)
            val dateStr = sdf.format(currentDate.time)

            // Precios dinámicos (más caros en fechas especiales)
            val priceMultiplier = when {
                day in 18..19 -> 1.2  // 24-25 Dic (Navidad)
                day == 25 -> 1.4      // 31 Dic (Año Nuevo)
                day == 26 -> 1.5      // 1 Ene (Año Nuevo)
                day in 14..17 -> 1.1  // Semana Navidad
                else -> 1.0
            }

            // Lima → Ayacucho (3 servicios diarios)
            routes.add(createRoute(
                origin = "Lima",
                destination = "Ayacucho",
                departureTime = "09:00 PM",
                arrivalTime = "05:00 AM",
                date = dateStr,
                duration = 480,
                basePrice = 50.0,
                priceMultiplier = priceMultiplier,
                busType = BusType.SEMICAMA,
                rating = 4.5,
                reviews = 120,
                amenities = listOf("WiFi", "Baño", "TV", "AC"),
                company = "ViaRapida Express",
                plate = "AYA-${100 + day}",
                seats = 40
            ))

            routes.add(createRoute(
                origin = "Lima",
                destination = "Ayacucho",
                departureTime = "10:00 PM",
                arrivalTime = "06:00 AM",
                date = dateStr,
                duration = 480,
                basePrice = 65.0,
                priceMultiplier = priceMultiplier,
                busType = BusType.CAMA,
                rating = 4.8,
                reviews = 89,
                amenities = listOf("WiFi", "Baño", "TV", "AC", "USB"),
                company = "ViaRapida VIP",
                plate = "AYA-${400 + day}",
                seats = 32
            ))

            if (day % 3 == 0) { // Económico cada 3 días
                routes.add(createRoute(
                    origin = "Lima",
                    destination = "Ayacucho",
                    departureTime = "08:00 PM",
                    arrivalTime = "04:00 AM",
                    date = dateStr,
                    duration = 480,
                    basePrice = 40.0,
                    priceMultiplier = priceMultiplier,
                    busType = BusType.STANDARD,
                    rating = 4.0,
                    reviews = 156,
                    amenities = listOf("Baño"),
                    company = "ViaRapida Económico",
                    plate = "AYA-${700 + day}",
                    seats = 48
                ))
            }

            // Ayacucho → Lima (2 servicios diarios)
            routes.add(createRoute(
                origin = "Ayacucho",
                destination = "Lima",
                departureTime = "08:00 AM",
                arrivalTime = "04:00 PM",
                date = dateStr,
                duration = 480,
                basePrice = 50.0,
                priceMultiplier = priceMultiplier,
                busType = BusType.SEMICAMA,
                rating = 4.6,
                reviews = 98,
                amenities = listOf("WiFi", "Baño", "TV", "AC"),
                company = "ViaRapida Express",
                plate = "LIM-${100 + day}",
                seats = 40
            ))

            if (day % 2 == 0) { // Nocturno día por medio
                routes.add(createRoute(
                    origin = "Ayacucho",
                    destination = "Lima",
                    departureTime = "10:00 PM",
                    arrivalTime = "06:00 AM",
                    date = dateStr,
                    duration = 480,
                    basePrice = 45.0,
                    priceMultiplier = priceMultiplier,
                    busType = BusType.STANDARD,
                    rating = 4.2,
                    reviews = 134,
                    amenities = listOf("Baño", "TV"),
                    company = "ViaRapida",
                    plate = "LIM-${400 + day}",
                    seats = 40
                ))
            }
        }

        // ===== RUTAS ESPECIALES (algunos días específicos) =====

        // Ayacucho ↔ Huancayo (cada 3 días)
        for (day in listOf(4, 7, 10, 13, 16, 19, 22, 25, 28)) {
            val currentDate = startDate.clone() as Calendar
            currentDate.add(Calendar.DAY_OF_YEAR, day)
            val dateStr = sdf.format(currentDate.time)

            routes.add(createRoute(
                origin = "Ayacucho",
                destination = "Huancayo",
                departureTime = "06:00 AM",
                arrivalTime = "12:00 PM",
                date = dateStr,
                duration = 360,
                basePrice = 30.0,
                busType = BusType.STANDARD,
                rating = 4.3,
                reviews = 67,
                amenities = listOf("Baño"),
                company = "ViaRapida Regional",
                plate = "HYO-${day}",
                seats = 40
            ))

            routes.add(createRoute(
                origin = "Ayacucho",
                destination = "Huancayo",
                departureTime = "02:00 PM",
                arrivalTime = "08:00 PM",
                date = dateStr,
                duration = 360,
                basePrice = 35.0,
                busType = BusType.SEMICAMA,
                rating = 4.4,
                reviews = 45,
                amenities = listOf("WiFi", "Baño", "AC"),
                company = "ViaRapida Confort",
                plate = "HYO-${100 + day}",
                seats = 36
            ))

            routes.add(createRoute(
                origin = "Huancayo",
                destination = "Ayacucho",
                departureTime = "07:00 AM",
                arrivalTime = "01:00 PM",
                date = dateStr,
                duration = 360,
                basePrice = 30.0,
                busType = BusType.STANDARD,
                rating = 4.1,
                reviews = 72,
                amenities = listOf("Baño"),
                company = "ViaRapida Regional",
                plate = "AYA-${900 + day}",
                seats = 40
            ))
        }

        // Ayacucho → Cusco (cada semana)
        for (day in listOf(7, 14, 21, 28)) {
            val currentDate = startDate.clone() as Calendar
            currentDate.add(Calendar.DAY_OF_YEAR, day)
            val dateStr = sdf.format(currentDate.time)

            routes.add(createRoute(
                origin = "Ayacucho",
                destination = "Cusco",
                departureTime = "09:00 PM",
                arrivalTime = "09:00 AM",
                date = dateStr,
                duration = 720,
                basePrice = 80.0,
                busType = BusType.CAMA,
                rating = 4.7,
                reviews = 103,
                amenities = listOf("WiFi", "Baño", "TV", "AC", "USB", "Mantas"),
                company = "ViaRapida Imperial",
                plate = "CUZ-${day}",
                seats = 28
            ))
        }

        // Lima → Arequipa (cada 5 días)
        for (day in listOf(8, 13, 18, 23, 28)) {
            val currentDate = startDate.clone() as Calendar
            currentDate.add(Calendar.DAY_OF_YEAR, day)
            val dateStr = sdf.format(currentDate.time)

            routes.add(createRoute(
                origin = "Lima",
                destination = "Arequipa",
                departureTime = "06:00 PM",
                arrivalTime = "10:00 AM",
                date = dateStr,
                duration = 960,
                basePrice = 100.0,
                busType = BusType.CAMA,
                rating = 4.9,
                reviews = 187,
                amenities = listOf("WiFi", "Baño", "TV", "AC", "USB", "Comida"),
                company = "ViaRapida Premium",
                plate = "AQP-${day}",
                seats = 24
            ))
        }

        Log.d(TAG, "📊 Rutas generadas: ${routes.size}")
        return routes
    }

    /**
     * 🏗️ CONSTRUCTOR DE RUTAS
     */
    private fun createRoute(
        origin: String,
        destination: String,
        departureTime: String,
        arrivalTime: String,
        date: String,
        duration: Int,
        basePrice: Double,
        priceMultiplier: Double = 1.0,
        busType: BusType,
        rating: Double,
        reviews: Int,
        amenities: List<String>,
        company: String,
        plate: String,
        seats: Int
    ): Route {
        return Route(
            origin = origin,
            destination = destination,
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            departureDate = date,
            durationMinutes = duration,
            price = basePrice * priceMultiplier,
            busType = busType,
            rating = rating,
            totalReviews = reviews,
            amenities = amenities,
            company = company,
            busPlate = plate,
            totalSeats = seats,
            occupiedSeats = emptyList(),
            isActive = true,
            imageUrl = ""
        )
    }
}