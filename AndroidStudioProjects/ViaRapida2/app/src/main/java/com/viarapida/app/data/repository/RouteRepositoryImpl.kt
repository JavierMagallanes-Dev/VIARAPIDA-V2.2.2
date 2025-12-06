package com.viarapida.app.data.repository

import android.util.Log
import com.viarapida.app.data.model.BusType
import com.viarapida.app.data.model.Route
import com.viarapida.app.data.remote.FirebaseClient
import kotlinx.coroutines.tasks.await

class RouteRepositoryImpl : RouteRepository {

    private val firestore = FirebaseClient.firestore

    private companion object {
        const val TAG = "RouteRepository"
        const val ROUTES_COLLECTION = "routes"
    }

    override suspend fun getAllRoutes(): Result<List<Route>> {
        return try {
            Log.d(TAG, "Obteniendo todas las rutas")

            val snapshot = firestore.collection(ROUTES_COLLECTION)
                .get()
                .await()

            val routes = snapshot.documents.mapNotNull { doc ->
                Route.fromFirestore(doc)
            }

            Log.d(TAG, "Rutas obtenidas: ${routes.size}")
            Result.success(routes)

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo rutas: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun searchRoutes(origin: String, destination: String): Result<List<Route>> {
        return try {
            Log.d(TAG, "Buscando rutas: $origin -> $destination")

            val snapshot = firestore.collection(ROUTES_COLLECTION)
                .whereEqualTo("origin", origin)
                .whereEqualTo("destination", destination)
                .get()
                .await()

            val routes = snapshot.documents.mapNotNull { doc ->
                Route.fromFirestore(doc)
            }

            Log.d(TAG, "Rutas encontradas: ${routes.size}")
            Result.success(routes)

        } catch (e: Exception) {
            Log.e(TAG, "Error buscando rutas: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getRouteById(routeId: String): Result<Route?> {
        return try {
            Log.d(TAG, "Obteniendo ruta: $routeId")

            val doc = firestore.collection(ROUTES_COLLECTION)
                .document(routeId)
                .get()
                .await()

            val route = Route.fromFirestore(doc)
            Log.d(TAG, "Ruta obtenida: ${route?.origin} -> ${route?.destination}")

            Result.success(route)

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo ruta: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun createRoute(route: Route): Result<String> {
        return try {
            Log.d(TAG, "Creando ruta: ${route.origin} -> ${route.destination}")

            val docRef = firestore.collection(ROUTES_COLLECTION)
                .add(route.toMap())
                .await()

            Log.d(TAG, "Ruta creada con ID: ${docRef.id}")
            Result.success(docRef.id)

        } catch (e: Exception) {
            Log.e(TAG, "Error creando ruta: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateRoute(routeId: String, route: Route): Result<Unit> {
        return try {
            Log.d(TAG, "Actualizando ruta: $routeId")

            firestore.collection(ROUTES_COLLECTION)
                .document(routeId)
                .set(route.toMap())
                .await()

            Log.d(TAG, "Ruta actualizada exitosamente")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando ruta: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateOccupiedSeats(routeId: String, occupiedSeats: List<Int>): Result<Unit> {
        return try {
            Log.d(TAG, "Actualizando asientos ocupados para ruta: $routeId")

            firestore.collection(ROUTES_COLLECTION)
                .document(routeId)
                .update("occupiedSeats", occupiedSeats)
                .await()

            Log.d(TAG, "Asientos actualizados: ${occupiedSeats.size} ocupados")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando asientos: ${e.message}", e)
            Result.failure(e)
        }
    }
    override suspend fun createInitialRoutes(): Result<Unit> {
        return try {
            Log.d(TAG, "Creando rutas iniciales con nuevos campos")

            val initialRoutes = listOf(
                // Lima - Ayacucho
                Route(
                    origin = "Lima",
                    destination = "Ayacucho",
                    departureTime = "09:00 PM",
                    arrivalTime = "05:00 AM",
                    departureDate = "2025-12-10",
                    durationMinutes = 480, // 8 horas
                    price = 50.0,
                    busType = BusType.SEMICAMA,
                    rating = 4.5,
                    totalReviews = 120,
                    amenities = listOf("WiFi", "Baño", "TV", "AC"),
                    company = "ViaRapida Express",
                    busPlate = "AYA-123",
                    totalSeats = 40,
                    occupiedSeats = emptyList(),
                    isActive = true
                ),
                Route(
                    origin = "Lima",
                    destination = "Ayacucho",
                    departureTime = "10:00 PM",
                    arrivalTime = "06:00 AM",
                    departureDate = "2025-12-10",
                    durationMinutes = 480,
                    price = 65.0,
                    busType = BusType.CAMA,
                    rating = 4.8,
                    totalReviews = 89,
                    amenities = listOf("WiFi", "Baño", "TV", "AC", "USB"),
                    company = "ViaRapida VIP",
                    busPlate = "AYA-456",
                    totalSeats = 32,
                    occupiedSeats = emptyList(),
                    isActive = true
                ),
                Route(
                    origin = "Lima",
                    destination = "Ayacucho",
                    departureTime = "08:00 PM",
                    arrivalTime = "04:00 AM",
                    departureDate = "2025-12-10",
                    durationMinutes = 480,
                    price = 40.0,
                    busType = BusType.STANDARD,
                    rating = 4.0,
                    totalReviews = 156,
                    amenities = listOf("Baño"),
                    company = "ViaRapida Económico",
                    busPlate = "AYA-789",
                    totalSeats = 48,
                    occupiedSeats = emptyList(),
                    isActive = true
                ),

                // Ayacucho - Lima
                Route(
                    origin = "Ayacucho",
                    destination = "Lima",
                    departureTime = "08:00 AM",
                    arrivalTime = "04:00 PM",
                    departureDate = "2025-12-11",
                    durationMinutes = 480,
                    price = 50.0,
                    busType = BusType.SEMICAMA,
                    rating = 4.6,
                    totalReviews = 98,
                    amenities = listOf("WiFi", "Baño", "TV", "AC"),
                    company = "ViaRapida Express",
                    busPlate = "LIM-123",
                    totalSeats = 40,
                    occupiedSeats = emptyList(),
                    isActive = true
                ),
                Route(
                    origin = "Ayacucho",
                    destination = "Lima",
                    departureTime = "10:00 PM",
                    arrivalTime = "06:00 AM",
                    departureDate = "2025-12-11",
                    durationMinutes = 480,
                    price = 45.0,
                    busType = BusType.STANDARD,
                    rating = 4.2,
                    totalReviews = 134,
                    amenities = listOf("Baño", "TV"),
                    company = "ViaRapida",
                    busPlate = "LIM-456",
                    totalSeats = 40,
                    occupiedSeats = emptyList(),
                    isActive = true
                ),

                // Ayacucho - Huancayo
                Route(
                    origin = "Ayacucho",
                    destination = "Huancayo",
                    departureTime = "06:00 AM",
                    arrivalTime = "12:00 PM",
                    departureDate = "2025-12-12",
                    durationMinutes = 360, // 6 horas
                    price = 30.0,
                    busType = BusType.STANDARD,
                    rating = 4.3,
                    totalReviews = 67,
                    amenities = listOf("Baño"),
                    company = "ViaRapida Regional",
                    busPlate = "HYO-123",
                    totalSeats = 40,
                    occupiedSeats = emptyList(),
                    isActive = true
                ),
                Route(
                    origin = "Ayacucho",
                    destination = "Huancayo",
                    departureTime = "02:00 PM",
                    arrivalTime = "08:00 PM",
                    departureDate = "2025-12-12",
                    durationMinutes = 360,
                    price = 35.0,
                    busType = BusType.SEMICAMA,
                    rating = 4.4,
                    totalReviews = 45,
                    amenities = listOf("WiFi", "Baño", "AC"),
                    company = "ViaRapida Confort",
                    busPlate = "HYO-456",
                    totalSeats = 36,
                    occupiedSeats = emptyList(),
                    isActive = true
                ),

                // Huancayo - Ayacucho
                Route(
                    origin = "Huancayo",
                    destination = "Ayacucho",
                    departureTime = "07:00 AM",
                    arrivalTime = "01:00 PM",
                    departureDate = "2025-12-13",
                    durationMinutes = 360,
                    price = 30.0,
                    busType = BusType.STANDARD,
                    rating = 4.1,
                    totalReviews = 72,
                    amenities = listOf("Baño"),
                    company = "ViaRapida Regional",
                    busPlate = "AYA-987",
                    totalSeats = 40,
                    occupiedSeats = emptyList(),
                    isActive = true
                ),

                // Ayacucho - Cusco
                Route(
                    origin = "Ayacucho",
                    destination = "Cusco",
                    departureTime = "09:00 PM",
                    arrivalTime = "09:00 AM",
                    departureDate = "2025-12-14",
                    durationMinutes = 720, // 12 horas
                    price = 80.0,
                    busType = BusType.CAMA,
                    rating = 4.7,
                    totalReviews = 103,
                    amenities = listOf("WiFi", "Baño", "TV", "AC", "USB", "Mantas"),
                    company = "ViaRapida Imperial",
                    busPlate = "CUZ-123",
                    totalSeats = 28,
                    occupiedSeats = emptyList(),
                    isActive = true
                ),

                // Lima - Arequipa (vía Ayacucho)
                Route(
                    origin = "Lima",
                    destination = "Arequipa",
                    departureTime = "06:00 PM",
                    arrivalTime = "10:00 AM",
                    departureDate = "2025-12-15",
                    durationMinutes = 960, // 16 horas
                    price = 100.0,
                    busType = BusType.CAMA,
                    rating = 4.9,
                    totalReviews = 187,
                    amenities = listOf("WiFi", "Baño", "TV", "AC", "USB", "Comida"),
                    company = "ViaRapida Premium",
                    busPlate = "AQP-123",
                    totalSeats = 24,
                    occupiedSeats = emptyList(),
                    isActive = true
                )
            )

            initialRoutes.forEach { route ->
                firestore.collection(ROUTES_COLLECTION)
                    .add(route.toMap())
                    .await()
            }

            Log.d(TAG, "✅ Rutas iniciales creadas: ${initialRoutes.size}")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando rutas iniciales: ${e.message}", e)
            Result.failure(e)
        }
    }
}
