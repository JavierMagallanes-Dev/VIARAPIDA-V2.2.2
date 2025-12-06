package com.viarapida.app.ui.screens.purchase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.viarapida.app.data.model.Route
import com.viarapida.app.data.model.Ticket
import com.viarapida.app.data.model.User
import com.viarapida.app.data.remote.FirebaseClient
import com.viarapida.app.data.repository.AuthRepository
import com.viarapida.app.data.repository.RouteRepository
import com.viarapida.app.data.repository.TicketRepository
import com.viarapida.app.di.AppModule
import com.viarapida.app.ui.utils.Constants
import com.viarapida.app.ui.utils.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val authRepository: AuthRepository = AppModule.provideAuthRepository(),
    private val routeRepository: RouteRepository = AppModule.provideRouteRepository(),
    private val ticketRepository: TicketRepository = AppModule.provideTicketRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState

    companion object {
        private const val TAG = "PurchaseViewModel"
    }

    fun initialize(routeId: String, seatNumber: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            Log.d(TAG, "Inicializando compra - Ruta: $routeId, Asiento: $seatNumber")

            // Cargar usuario actual
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(currentUser = user)

                        // Cargar ruta
                        loadRoute(routeId, seatNumber)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Usuario no autenticado"
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Error cargando usuario: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar usuario"
                    )
                }
        }
    }

    private suspend fun loadRoute(routeId: String, seatNumber: Int) {
        routeRepository.getRouteById(routeId)
            .onSuccess { route ->
                if (route != null) {
                    // Verificar que el asiento siga disponible
                    if (seatNumber in route.occupiedSeats) {
                        Log.w(TAG, "Asiento ya ocupado: $seatNumber")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "El asiento seleccionado ya no está disponible"
                        )
                    } else {
                        Log.d(TAG, "Ruta cargada correctamente")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            route = route,
                            seatNumber = seatNumber
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ruta no encontrada"
                    )
                }
            }
            .onFailure { error ->
                Log.e(TAG, "Error cargando ruta: ${error.message}", error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Error al cargar la ruta"
                )
            }
    }

    fun onPassengerNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            passengerName = name,
            passengerNameError = ""
        )
    }

    fun onPassengerDNIChange(dni: String) {
        _uiState.value = _uiState.value.copy(
            passengerDNI = dni,
            passengerDNIError = ""
        )
    }

    fun confirmPurchase() {
        val currentState = _uiState.value

        // Validar nombre del pasajero
        val nameValidation = Validators.validateName(currentState.passengerName)
        if (!nameValidation.isValid) {
            _uiState.value = currentState.copy(passengerNameError = nameValidation.errorMessage)
            return
        }

        // Validar DNI
        val dniValidation = Validators.validateDNI(currentState.passengerDNI)
        if (!dniValidation.isValid) {
            _uiState.value = currentState.copy(passengerDNIError = dniValidation.errorMessage)
            return
        }

        val route = currentState.route
        val user = currentState.currentUser
        val seatNumber = currentState.seatNumber

        if (route == null || user == null || seatNumber == null) {
            _uiState.value = currentState.copy(error = "Datos incompletos para la compra")
            return
        }

        // Procesar compra
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = "")

            Log.d(TAG, "Procesando compra para asiento: $seatNumber")

            try {
                // Verificar nuevamente que el asiento esté disponible
                routeRepository.getRouteById(route.id)
                    .onSuccess { updatedRoute ->
                        if (updatedRoute != null && seatNumber in updatedRoute.occupiedSeats) {
                            Log.w(TAG, "Asiento ocupado durante la compra: $seatNumber")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "El asiento fue ocupado por otro usuario. Por favor selecciona otro."
                            )
                            return@onSuccess
                        }

                        // Crear ticket
                        val ticket = Ticket(
                            userId = user.id,
                            userName = user.name,
                            routeId = route.id,
                            passengerName = currentState.passengerName,
                            passengerDNI = currentState.passengerDNI,
                            seatNumber = seatNumber,
                            origin = route.origin,
                            destination = route.destination,
                            departureTime = route.departureTime,
                            price = route.price,
                            purchaseDate = Timestamp.now(),
                            status = Constants.STATUS_ACTIVE
                        )

                        // Guardar ticket
                        ticketRepository.createTicket(ticket)
                            .onSuccess { ticketId ->
                                Log.d(TAG, "Ticket creado: $ticketId")

                                // Actualizar asientos ocupados
                                val updatedOccupiedSeats = route.occupiedSeats + seatNumber
                                routeRepository.updateOccupiedSeats(route.id, updatedOccupiedSeats)
                                    .onSuccess {
                                        Log.d(TAG, "Asientos actualizados correctamente")
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            purchaseSuccess = true,
                                            ticketId = ticketId
                                        )
                                    }
                                    .onFailure { error ->
                                        Log.e(TAG, "Error actualizando asientos: ${error.message}", error)
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            error = "Error al actualizar asientos: ${error.message}"
                                        )
                                    }
                            }
                            .onFailure { error ->
                                Log.e(TAG, "Error creando ticket: ${error.message}", error)
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Error al crear el ticket: ${error.message}"
                                )
                            }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Error verificando disponibilidad: ${error.message}", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error al verificar disponibilidad: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error en compra: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al procesar la compra: ${e.message}"
                )
            }
        }
    }
}

data class PurchaseUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val route: Route? = null,
    val seatNumber: Int? = null,
    val passengerName: String = "",
    val passengerDNI: String = "",
    val passengerNameError: String = "",
    val passengerDNIError: String = "",
    val error: String = "",
    val purchaseSuccess: Boolean = false,
    val ticketId: String? = null
)