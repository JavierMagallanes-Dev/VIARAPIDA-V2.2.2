package com.viarapida.app.data.repository

import com.viarapida.app.data.model.Route

interface RouteRepository {
    // ============ FUNCIONES BÁSICAS ============
    suspend fun getAllRoutes(): Result<List<Route>>
    suspend fun searchRoutes(origin: String, destination: String): Result<List<Route>>
    suspend fun getRouteById(routeId: String): Result<Route?>
    suspend fun createRoute(route: Route): Result<String>
    suspend fun updateRoute(routeId: String, route: Route): Result<Unit>
    suspend fun updateOccupiedSeats(routeId: String, occupiedSeats: List<Int>): Result<Unit>

    // ============ INICIALIZACIÓN ============
    suspend fun createInitialRoutes(): Result<Unit>

    // ============ IMPORTACIÓN MASIVA ============
    /**
     * 🚀 Importa 100+ rutas automáticamente para los próximos 30 días
     * Incluye:
     * - Lima ↔ Ayacucho (diario)
     * - Ayacucho ↔ Huancayo (cada 3 días)
     * - Ayacucho → Cusco (semanal)
     * - Lima → Arequipa (cada 5 días)
     */
    suspend fun importMassiveRoutes(): Result<Unit>
}