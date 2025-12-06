package com.viarapida.app.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.viarapida.app.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilters: SearchFilters,
    onFiltersChanged: (SearchFilters) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filters by remember { mutableStateOf(currentFilters) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    TextButton(onClick = {
                        filters = SearchFilters.default()
                    }) {
                        Text("Limpiar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contador de filtros activos
            if (filters.hasActiveFilters()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${filters.getActiveFiltersCount()} filtros activos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 1. Rango de Precio
            PriceRangeFilter(
                minPrice = filters.minPrice,
                maxPrice = filters.maxPrice,
                onPriceRangeChanged = { min, max ->
                    filters = filters.copy(minPrice = min, maxPrice = max)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Tipo de Bus
            BusTypeFilter(
                selectedTypes = filters.busTypes,
                onTypesChanged = { types ->
                    filters = filters.copy(busTypes = types)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Calificación Mínima
            RatingFilter(
                minRating = filters.minRating,
                onRatingChanged = { rating ->
                    filters = filters.copy(minRating = rating)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Horario de Salida
            TimeRangeFilter(
                selectedRanges = filters.departureTimeRanges,
                onRangesChanged = { ranges ->
                    filters = filters.copy(departureTimeRanges = ranges)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Amenidades
            AmenitiesFilter(
                selectedAmenities = filters.amenities,
                onAmenitiesChanged = { amenities ->
                    filters = filters.copy(amenities = amenities)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Ordenar Por
            SortByFilter(
                currentSort = filters.sortBy,
                onSortChanged = { sort ->
                    filters = filters.copy(sortBy = sort)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Aplicar
            Button(
                onClick = {
                    onFiltersChanged(filters)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aplicar Filtros")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PriceRangeFilter(
    minPrice: Double?,
    maxPrice: Double?,
    onPriceRangeChanged: (Double?, Double?) -> Unit
) {
    Column {
        Text(
            text = "💰 Rango de Precio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = minPrice?.toString() ?: "",
                onValueChange = { value ->
                    val price = value.toDoubleOrNull()
                    onPriceRangeChanged(price, maxPrice)
                },
                label = { Text("Mínimo") },
                prefix = { Text("S/ ") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = maxPrice?.toString() ?: "",
                onValueChange = { value ->
                    val price = value.toDoubleOrNull()
                    onPriceRangeChanged(minPrice, price)
                },
                label = { Text("Máximo") },
                prefix = { Text("S/ ") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BusTypeFilter(
    selectedTypes: List<BusType>,
    onTypesChanged: (List<BusType>) -> Unit
) {
    Column {
        Text(
            text = "🚌 Tipo de Bus",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Alternativa a FlowRow usando Column + Row
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val types = BusType.getAllTypes()
            types.chunked(2).forEach { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTypes.forEach { type ->
                        FilterChip(
                            selected = type in selectedTypes,
                            onClick = {
                                val newTypes = if (type in selectedTypes) {
                                    selectedTypes - type
                                } else {
                                    selectedTypes + type
                                }
                                onTypesChanged(newTypes)
                            },
                            label = { Text(type.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Relleno si hay número impar de elementos
                    if (rowTypes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingFilter(
    minRating: Double?,
    onRatingChanged: (Double?) -> Unit
) {
    Column {
        Text(
            text = "⭐ Calificación Mínima",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(null, 3.0, 3.5, 4.0, 4.5).forEach { rating ->
                FilterChip(
                    selected = minRating == rating,
                    onClick = { onRatingChanged(rating) },
                    label = {
                        Text(rating?.toString()?.plus("+ ⭐") ?: "Todas")
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TimeRangeFilter(
    selectedRanges: List<TimeRange>,
    onRangesChanged: (List<TimeRange>) -> Unit
) {
    Column {
        Text(
            text = "🕐 Horario de Salida",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val ranges = TimeRange.getAllRanges()
            ranges.chunked(2).forEach { rowRanges ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowRanges.forEach { range ->
                        FilterChip(
                            selected = range in selectedRanges,
                            onClick = {
                                val newRanges = if (range in selectedRanges) {
                                    selectedRanges - range
                                } else {
                                    selectedRanges + range
                                }
                                onRangesChanged(newRanges)
                            },
                            label = { Text("${range.icon} ${range.displayName}") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowRanges.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AmenitiesFilter(
    selectedAmenities: List<String>,
    onAmenitiesChanged: (List<String>) -> Unit
) {
    val availableAmenities = listOf("WiFi", "Baño", "TV", "AC", "USB", "Mantas", "Comida")

    Column {
        Text(
            text = "✨ Amenidades",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableAmenities.chunked(3).forEach { rowAmenities ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowAmenities.forEach { amenity ->
                        FilterChip(
                            selected = amenity in selectedAmenities,
                            onClick = {
                                val newAmenities = if (amenity in selectedAmenities) {
                                    selectedAmenities - amenity
                                } else {
                                    selectedAmenities + amenity
                                }
                                onAmenitiesChanged(newAmenities)
                            },
                            label = { Text(amenity) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Relleno para la última fila
                    repeat(3 - rowAmenities.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SortByFilter(
    currentSort: SortOption,
    onSortChanged: (SortOption) -> Unit
) {
    Column {
        Text(
            text = "📊 Ordenar Por",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortOption.getAllOptions().forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSort == option,
                        onClick = { onSortChanged(option) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}