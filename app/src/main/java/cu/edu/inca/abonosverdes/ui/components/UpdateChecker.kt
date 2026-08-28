package cu.edu.inca.abonosverdes.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cu.edu.inca.abonosverdes.ui.viewmodel.UpdateViewModel

@Composable
fun UpdateChecker(
    viewModel: UpdateViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates()
    }

    when (val state = uiState) {
        is UpdateViewModel.UpdateUiState.NewVersionAvailable -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text(text = "Actualización disponible") },
                text = {
                    Text(
                        text = "Nueva versión disponible (${state.status.latestVersion}). ¿Deseas descargar e instalar la actualización?"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.startDownload(state.status) }) {
                        Text(text = "Actualizar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) {
                        Text(text = "Más tarde")
                    }
                }
            )
        }
        is UpdateViewModel.UpdateUiState.Downloading -> {
            AlertDialog(
                onDismissRequest = { /* No cerrar mientras descarga */ },
                title = { Text(text = "Descargando...") },
                text = {
                    CircularProgressIndicator()
                },
                confirmButton = {}
            )
        }
        else -> { /* Nada que mostrar */ }
    }
}
