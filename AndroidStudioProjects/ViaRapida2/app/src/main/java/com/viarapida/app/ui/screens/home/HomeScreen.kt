package com.viarapida.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viarapida.app.ui.components.CustomButton
import com.viarapida.app.ui.components.LoadingDialog

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToMyTickets: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            onNavigateToLogin()
        }
    }

    if (uiState.isLoading) {
        LoadingDialog(message = "Cargando...")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Text(
            text = "🚌",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Título
        Text(
            text = "ViaRapida",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bienvenida
        uiState.user?.let { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Bienvenido, ${user.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buscar Rutas
        CustomButton(
            text = "🔍 Buscar Rutas",
            onClick = onNavigateToSearch
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mis Pasajes
        CustomButton(
            text = "🎫 Mis Pasajes",
            onClick = onNavigateToMyTickets
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Panel Admin (solo si es admin)
        if (uiState.user?.isAdmin == true) {
            CustomButton(
                text = "⚙️ Panel Administrador",
                onClick = onNavigateToAdmin
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Cerrar Sesión
        CustomButton(
            text = "🚪 Cerrar Sesión",
            onClick = { viewModel.logout() }
        )
    }
}