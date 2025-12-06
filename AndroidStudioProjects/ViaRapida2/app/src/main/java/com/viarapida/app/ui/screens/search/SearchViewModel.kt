package com.viarapida.app.ui.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viarapida.app.data.model.Route
import com.viarapida.app.data.model.SearchFilters
import com.viarapida.app.data.model.SortOption
import com.viarapida.app.data.repository.RouteRepository
import com.viarapida.app.di.AppModule
import com.viarapida.app.ui.utils.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val routeRepository: RouteRepository = AppModule.provideRouteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    companion object {
        private const val TAG = "SearchViewModel"
    }

    fun onOriginChange(origin: String) {
        _uiState.update { it.copy(origin = origin, originError = "") }
    }

    fun onDestinationChange(destination: String) {
        _uiState.update { it.copy(destination = destination, destinationError = "") }
    }

    fun onDateSelected(date: String) {
        val updatedFilters = _uiState.value.filters.copy(departureDate = date)
        _uiState.update { it.copy(filters = updatedFilters) }

        // Si ya hay resultados, aplicar filtros inmediatamente
        if (_uiState.value.hasSearched) {
            applyFiltersToCurrentResults()
        }
    }

    fun onClearDate() {
        val updatedFilters = _uiState.value.filters.copy(departureDate = null)
        _uiState.update { it.copy(filters = updatedFilters) }

        if (_uiState.value.hasSearched) {
            applyFiltersToCurrentResults()
        }
    }

    fun onFiltersChanged(filters: SearchFilters) {
        _uiState.update { it.copy(filters = filters) }
        // Si ya hay resultados, aplicar filtros inmediatamente
        if (_uiState.value.hasSearched) {
            applyFiltersToCurrentResults()
        }
    }

    fun clearFilters() {
        _uiState.update { it.copy(filters = SearchFilters.default()) }
        if (_uiState.value.hasSearched) {
            applyFiltersToCurrentResults()
        }
    }

    fun searchRoutes() {
        val currentState = _uiState.value

        // Validar origen y destino
        val validation = Validators.validateOriginDestination(
            currentState.origin,
            currentState.destination
        )

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    originError = if (currentState.origin.isBlank()) validation.errorMessage else "",
                    destinationError = if (currentState.destination.isBlank() ||
                        currentState.origin == currentState.destination) validation.errorMessage else ""
                )
            }
            return
        }

        // Buscar rutas
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "") }

            Log.d(TAG, "Buscando rutas: ${currentState.origin} -> ${currentState.destination}")

            routeRepository.searchRoutes(currentState.origin, currentState.destination)
                .onSuccess { routes ->
                    Log.d(TAG, "Rutas encontradas: ${routes.size}")

                    // Guardar resultados sin filtrar
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allRoutes = routes,
                            hasSearched = true
                        )
                    }

                    // Aplicar filtros
                    applyFiltersToCurrentResults()
                }
                .onFailure { error ->
                    Log.e(TAG, "Error buscando rutas: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al buscar rutas"
                        )
                    }
                }
        }
    }

    private fun applyFiltersToCurrentResults() {
        val currentState = _uiState.value
        val filteredRoutes = currentState.filters.applyTo(currentState.allRoutes)

        Log.d(TAG, "Aplicando filtros. Resultados: ${filteredRoutes.size}/${currentState.allRoutes.size}")

        _uiState.update { it.copy(routes = filteredRoutes) }
    }

    fun toggleFilterSheet() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }
}

data class SearchUiState(
    val origin: String = "",
    val destination: String = "",
    val originError: String = "",
    val destinationError: String = "",
    val isLoading: Boolean = false,
    val allRoutes: List<Route> = emptyList(),      // Resultados sin filtrar
    val routes: List<Route> = emptyList(),          // Resultados filtrados
    val hasSearched: Boolean = false,
    val error: String = "",
    val filters: SearchFilters = SearchFilters.default(),
    val showFilters: Boolean = false
)