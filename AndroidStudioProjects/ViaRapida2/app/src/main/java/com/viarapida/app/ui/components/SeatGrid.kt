package com.viarapida.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.viarapida.app.ui.theme.SeatAvailable
import com.viarapida.app.ui.theme.SeatOccupied
import com.viarapida.app.ui.theme.SeatSelected
import com.viarapida.app.ui.utils.Constants

@Composable
fun SeatGrid(
    totalSeats: Int = Constants.TOTAL_SEATS,
    occupiedSeats: List<Int>,
    selectedSeat: Int?,
    onSeatSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Leyenda
        SeatLegend()

        Spacer(modifier = Modifier.height(16.dp))

        // Grid de asientos usando Rows normales
        val seatsPerRow = Constants.SEATS_PER_ROW
        val rows = (totalSeats + seatsPerRow - 1) / seatsPerRow // Redondear hacia arriba

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (rowIndex in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (colIndex in 0 until seatsPerRow) {
                        val seatNumber = rowIndex * seatsPerRow + colIndex + 1

                        if (seatNumber <= totalSeats) {
                            val isOccupied = seatNumber in occupiedSeats
                            val isSelected = seatNumber == selectedSeat

                            SeatItem(
                                seatNumber = seatNumber,
                                isOccupied = isOccupied,
                                isSelected = isSelected,
                                onClick = {
                                    if (!isOccupied) {
                                        onSeatSelected(seatNumber)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Espacio vacío para mantener el grid alineado
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatItem(
    seatNumber: Int,
    isOccupied: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isOccupied -> SeatOccupied
        isSelected -> SeatSelected
        else -> SeatAvailable
    }

    Box(
        modifier = modifier
            .height(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !isOccupied) { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💺",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = seatNumber.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SeatLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = SeatAvailable,
            label = "Disponible"
        )
        LegendItem(
            color = SeatOccupied,
            label = "Ocupado"
        )
        LegendItem(
            color = SeatSelected,
            label = "Seleccionado"
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}