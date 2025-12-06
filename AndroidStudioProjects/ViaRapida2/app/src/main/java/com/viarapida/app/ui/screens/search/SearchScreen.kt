package com.viarapida.app.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viarapida.app.ui.components.*
import com.viarapida.app.ui.theme.GradientEnd
import com.viarapida.app.ui.theme.GradientStart
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

    // Bottom Sheet de Filtros
    if (uiState.showFilters) {
        FilterBottomSheet(
            currentFilters = uiState.filters,
            onFiltersChanged = { filters ->
                viewModel.onFiltersChanged(filters)
            },
            onDismiss = { viewModel.toggleFilterSheet() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Buscar Rutas",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón de filtros con badge
                    BadgedBox(
                        badge = {
                            if (uiState.filters.hasActiveFilters()) {
                                Badge {
                                    Text("${uiState.filters.getActiveFiltersCount()}")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { viewModel.toggleFilterSheet() }) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filtros"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Header con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(
                            text = "Encuentra tu ruta",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selecciona origen y destino",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
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

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Fecha
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        QuickDateSelector(
                            onDateSelected = { date ->
                                viewModel.onDateSelected(date)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        DateRangeSelector(
                            selectedDate = uiState.filters.departureDate,
                            onDateSelected = { date ->
                                viewModel.onDateSelected(date)
                            },
                            onClearDate = {
                                viewModel.onClearDate()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chips de filtros activos
                AnimatedVisibility(
                    visible = uiState.filters.hasActiveFilters(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    ActiveFiltersChips(
                        filters = uiState.filters,
                        onClearFilters = { viewModel.clearFilters() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Error general
                AnimatedVisibility(
                    visible = uiState.error.isNotEmpty(),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Resultados
                AnimatedVisibility(
                    visible = uiState.hasSearched,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    if (uiState.routes.isEmpty()) {
                        // Sin resultados
                        NoResultsCard()
                    } else {
                        // Lista de rutas
                        RoutesList(
                            routes = uiState.routes,
                            allRoutesCount = uiState.allRoutes.size,
                            onNavigateToSeatSelection = onNavigateToSeatSelection
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveFiltersChips(
    filters: com.viarapida.app.data.model.SearchFilters,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎯 ${filters.getActiveFiltersCount()} filtros activos",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )

                    // Mostrar fecha si está seleccionada
                    filters.departureDate?.let { date ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📅 ${com.viarapida.app.ui.utils.DateUtils.formatDateForDisplay(date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                TextButton(onClick = onClearFilters) {
                    Text("Limpiar todo")
                }
            }
        }
    }
}

@Composable
private fun NoResultsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No se encontraron rutas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Intenta ajustar los filtros o buscar otras ciudades",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RoutesList(
    routes: List<com.viarapida.app.data.model.Route>,
    allRoutesCount: Int,
    onNavigateToSeatSelection: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rutas Disponibles",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "${routes.size}/$allRoutesCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(routes) { route ->
                RouteCard(
                    route = route,
                    onSelectRoute = {
                        onNavigateToSeatSelection(route.id)
                    }
                )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            DropdownTextField(
                value = origin,
                onValueChange = onOriginChange,
                label = "Origen",
                placeholder = "Selecciona ciudad de origen",
                options = Constants.CITIES,
                leadingIcon = Icons.Default.LocationOn,
                isError = originError.isNotEmpty(),
                errorMessage = originError,
                enabled = enabled
            )

            Spacer(modifier = Modifier.height(16.dp))

            DropdownTextField(
                value = destination,
                onValueChange = onDestinationChange,
                label = "Destino",
                placeholder = "Selecciona ciudad de destino",
                options = Constants.CITIES,
                leadingIcon = Icons.Default.LocationOn,
                isError = destinationError.isNotEmpty(),
                errorMessage = destinationError,
                enabled = enabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "✈️ Ciudades disponibles: ${Constants.CITIES.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                text = "Buscar Rutas",
                onClick = onSearch,
                enabled = enabled,
                icon = Icons.Default.Search
            )
        }
    }
}