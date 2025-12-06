package com.viarapida.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.viarapida.app.ui.utils.DateUtils

/**
 * Selector de fecha con rango de ±3 días
 */
@Composable
fun DateRangeSelector(
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    onClearDate: () -> Unit,
    modifier: Modifier = Modifier,
    showClearButton: Boolean = true
) {
    val centerDate = selectedDate ?: DateUtils.getCurrentDateString()
    val dateRange = remember(centerDate) {
        DateUtils.getDateRange(centerDate, rangeDays = 3)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedDate != null) {
                            DateUtils.formatDateForDisplay(selectedDate)
                        } else {
                            "Selecciona una fecha"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showClearButton && selectedDate != null) {
                    IconButton(
                        onClick = onClearDate,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar fecha",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista horizontal de fechas
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dateRange) { date ->
                    DateChip(
                        date = date,
                        isSelected = date == selectedDate,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateChip(
    date: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isPast = DateUtils.isPastDate(date)
    val isToday = DateUtils.isToday(date)
    val isTomorrow = DateUtils.isTomorrow(date)

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .width(80.dp)
            .height(90.dp)
            .clickable(enabled = !isPast) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        border = if (isToday && !isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Día de la semana
            Text(
                text = DateUtils.getDayOfWeek(date),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Día del mes
            Text(
                text = date.split("-").getOrNull(2) ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Label especial
            if (isToday) {
                Text(
                    text = "Hoy",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) textColor else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else if (isTomorrow) {
                Text(
                    text = "Mañana",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            } else {
                // Mes abreviado
                Text(
                    text = date.split("-").getOrNull(1)?.let {
                        when (it) {
                            "01" -> "Ene"
                            "02" -> "Feb"
                            "03" -> "Mar"
                            "04" -> "Abr"
                            "05" -> "May"
                            "06" -> "Jun"
                            "07" -> "Jul"
                            "08" -> "Ago"
                            "09" -> "Sep"
                            "10" -> "Oct"
                            "11" -> "Nov"
                            "12" -> "Dic"
                            else -> it
                        }
                    } ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Calendario mensual completo (para modal/dialog)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.let {
            DateUtils.stringToDate(it)?.time
        } ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.util.Date(millis)
                        val dateString = DateUtils.dateToString(date)
                        onDateSelected(dateString)
                    }
                    onDismiss()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Seleccionar fecha",
                    modifier = Modifier.padding(16.dp)
                )
            },
            headline = {
                Text(
                    text = datePickerState.selectedDateMillis?.let { millis ->
                        DateUtils.formatDateForDisplay(
                            DateUtils.dateToString(java.util.Date(millis))
                        )
                    } ?: "Selecciona una fecha",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        )
    }
}

/**
 * Quick date selector - Hoy, Mañana, Esta Semana
 */
@Composable
fun QuickDateSelector(
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "⚡ Accesos Rápidos",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickDateButton(
                    text = "Hoy",
                    icon = "📅",
                    onClick = {
                        onDateSelected(DateUtils.getCurrentDateString())
                    },
                    modifier = Modifier.weight(1f)
                )

                QuickDateButton(
                    text = "Mañana",
                    icon = "☀️",
                    onClick = {
                        onDateSelected(
                            DateUtils.addDaysToString(DateUtils.getCurrentDateString(), 1)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                QuickDateButton(
                    text = "En 7 días",
                    icon = "📆",
                    onClick = {
                        onDateSelected(
                            DateUtils.addDaysToString(DateUtils.getCurrentDateString(), 7)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickDateButton(
    text: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, style = MaterialTheme.typography.labelSmall)
            Text(text = text, style = MaterialTheme.typography.labelSmall)
        }
    }
}