package com.viarapida.app.ui.screens.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viarapida.app.ui.components.CustomButton
import com.viarapida.app.ui.components.CustomTextField
import com.viarapida.app.ui.components.LoadingDialog
import com.viarapida.app.ui.components.RouteCard
import com.viarapida.app.ui.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSeatSelection: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingDialog(message = "Buscando rutas...")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Rutas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Formulario de búsqueda
            SearchForm(
                origin = uiState.origin,
                destination = uiState.destination,
                originError = uiState.originError,
                destinationError = uiState.destinationError,
                onOriginChange = { viewModel.onOriginChange(it) },
                onDestinationChange = { viewModel.onDestinationChange(it) },
                onSearch = { viewModel.searchRoutes() },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error general
            if (uiState.error.isNotEmpty()) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Resultados
            if (uiState.hasSearched) {
                if (uiState.routes.isEmpty()) {
                    // No hay resultados
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔍",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron rutas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    // Lista de rutas
                    Text(
                        text = "Rutas Disponibles (${uiState.routes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.routes) { route ->
                            RouteCard(
                                route = route,
                                onSelectRoute = {
                                    onNavigateToSeatSelection(route.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchForm(
    origin: String,
    destination: String,
    originError: String,
    destinationError: String,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onSearch: () -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Origen
        Text(
            text = "Origen",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Dropdown simulado con TextField
        CustomTextField(
            value = origin,
            onValueChange = onOriginChange,
            label = "Selecciona origen",
            isError = originError.isNotEmpty(),
            errorMessage = originError,
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sugerencias de origen
        Text(
            text = "Ciudades: ${Constants.CITIES.joinToString(", ")}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Destino
        Text(
            text = "Destino",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        CustomTextField(
            value = destination,
            onValueChange = onDestinationChange,
            label = "Selecciona destino",
            isError = destinationError.isNotEmpty(),
            errorMessage = destinationError,
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sugerencias de destino
        Text(
            text = "Ciudades: ${Constants.CITIES.joinToString(", ")}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de búsqueda
        CustomButton(
            text = "🔍 Buscar",
            onClick = onSearch,
            enabled = enabled
        )
    }
}