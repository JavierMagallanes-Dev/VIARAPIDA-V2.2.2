package com.viarapida.app.data.repository

import android.util.Log
import com.viarapida.app.data.model.BusType
import com.viarapida.app.data.model.Route
import com.viarapida.app.data.remote.FirebaseClient
import com.viarapida.app.utils.RouteDataImporter
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

    // ============ NUEVAS FUNCIONES ============

    /**
     * 🚀 IMPORTACIÓN MASIVA DE RUTAS
     * Usa RouteDataImporter para crear 100+ rutas automáticamente
     */
    override suspend fun importMassiveRoutes(): Result<Unit> {
        return try {
            Log.d(TAG, "🚀 Iniciando importación masiva de rutas...")

            val importer = RouteDataImporter()
            val result = importer.importAllRoutes()

            result.onSuccess {
                Log.d(TAG, "✅ Importación masiva completada exitosamente")
            }.onFailure { error ->
                Log.e(TAG, "❌ Error en importación masiva: ${error.message}", error)
                return Result.failure(error)
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "💥 Error general en importación masiva: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * 🗑️ LIMPIAR TODAS LAS RUTAS
     * Útil para reiniciar la base de datos
     */
    suspend fun deleteAllRoutes(): Result<Unit> {
        return try {
            Log.d(TAG, "🗑️ Eliminando todas las rutas...")

            val snapshot = firestore.collection(ROUTES_COLLECTION)
                .get()
                .await()

            var deletedCount = 0
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
                deletedCount++
            }

            Log.d(TAG, "✅ Rutas eliminadas: $deletedCount")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando rutas: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * 📊 OBTENER ESTADÍSTICAS DE RUTAS
     */
    suspend fun getRoutesStats(): Result<RouteStats> {
        return try {
            Log.d(TAG, "📊 Obteniendo estadísticas de rutas...")

            val snapshot = firestore.collection(ROUTES_COLLECTION)
                .get()
                .await()

            val routes = snapshot.documents.mapNotNull { Route.fromFirestore(it) }

            val stats = RouteStats(
                totalRoutes = routes.size,
                activeRoutes = routes.count { it.isActive },
                totalSeats = routes.sumOf { it.totalSeats },
                occupiedSeats = routes.sumOf { it.occupiedSeats.size },
                routesByOrigin = routes.groupBy { it.origin }
                    .mapValues { it.value.size },
                routesByBusType = routes.groupBy { it.busType.name }
                    .mapValues { it.value.size },
                averagePrice = routes.map { it.price }.average(),
                averageOccupancy = (routes.sumOf { it.occupiedSeats.size }.toDouble() /
                        routes.sumOf { it.totalSeats }) * 100
            )

            Log.d(TAG, "✅ Estadísticas obtenidas: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo estadísticas: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============ FUNCIÓN ORIGINAL (MANTENER PARA COMPATIBILIDAD) ============

    override suspend fun createInitialRoutes(): Result<Unit> {
        return try {
            Log.d(TAG, "Creando rutas iniciales (método legacy)")

            // Verificar si ya existen rutas
            val existingRoutes = firestore.collection(ROUTES_COLLECTION)
                .limit(1)
                .get()
                .await()

            if (!existingRoutes.isEmpty) {
                Log.d(TAG, "⚠️ Ya existen rutas en la base de datos, saltando creación")
                return Result.success(Unit)
            }

            val initialRoutes = listOf(
                // Lima - Ayacucho
                Route(
                    origin = "Lima",
                    destination = "Ayacucho",
                    departureTime = "09:00 PM",
                    arrivalTime = "05:00 AM",
                    departureDate = "2025-12-10",
                    durationMinutes = 480,
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

/**
 * 📊 Modelo de estadísticas de rutas
 */
data class RouteStats(
    val totalRoutes: Int,
    val activeRoutes: Int,
    val totalSeats: Int,
    val occupiedSeats: Int,
    val routesByOrigin: Map<String, Int>,
    val routesByBusType: Map<String, Int>,
    val averagePrice: Double,
    val averageOccupancy: Double
)