package cu.edu.inca.abonosverdes.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.compose.ui.res.stringResource
import cu.edu.inca.abonosverdes.R
import cu.edu.inca.abonosverdes.ui.components.AbonarAppBar
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de inicio de la aplicación.
 * Muestra un mensaje de bienvenida, un acceso directo a la calculadora y el estado
 * actual de la sincronización de datos con la nube.
 *
 * @param onNavigateToCalculator Callback para navegar a la pantalla de la calculadora.
 * @param onOpenDrawer Callback para abrir el cajón de navegación.
 */
@Composable
fun HomeScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToGuia: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    
    // Evitamos llamar a WorkManager en el modo de vista previa (Preview) 
    // para prevenir la excepción IllegalStateException ya que WorkManager 
    // no está inicializado en este entorno.
    val workInfos = if (LocalInspectionMode.current) {
        remember { mutableStateOf<List<WorkInfo>?>(null) }
    } else {
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData("SyncWorker")
            .observeAsState()
    }
    
    val syncStatus = workInfos.value?.firstOrNull()

    HomeScreenContent(
        onNavigateToCalculator = onNavigateToCalculator,
        onNavigateToGuia = onNavigateToGuia,
        onOpenDrawer = onOpenDrawer,
        syncStatus = syncStatus
    )
}

/**
 * Versión sin estado de la pantalla de inicio, ideal para previsualizaciones y pruebas.
 * 
 * @param syncStatus Estado actual de la sincronización.
 * @param onNavigateToCalculator Callback para navegar a la calculadora.
 * @param onNavigateToGuia Callback para navegar a la guía de usuario.
 * @param onOpenDrawer Callback para abrir el cajón de navegación.
 */
@Composable
fun HomeScreenContent(
    syncStatus: WorkInfo?,
    onNavigateToCalculator: () -> Unit,
    onNavigateToGuia: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    Scaffold(
        topBar = {
            AbonarAppBar(
                title = stringResource(R.string.home_title),
                onOpenDrawer = onOpenDrawer
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.welcome_message),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                InfoCard(text = stringResource(R.string.home_tool_description))
            }
            item {
                DashboardCard(
                    title = stringResource(R.string.calculator_title),
                    description = stringResource(R.string.calculator_desc),
                    icon = Icons.Rounded.Calculate,
                    onClick = onNavigateToCalculator
                )
            }
            item {
                DashboardCard(
                    title = stringResource(R.string.guia_card_title),
                    description = stringResource(R.string.guia_card_desc),
                    icon = Icons.AutoMirrored.Rounded.Help,
                    onClick = onNavigateToGuia
                )
            }
            item {
                SyncStatusCard(syncStatus)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Tarjeta informativa para el panel principal.
 *
 * @param text Mensaje a mostrar en la tarjeta.
 */
@Composable
fun InfoCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * Tarjeta interactiva para el panel principal.
 *
 * @param title Título de la tarjeta.
 * @param description Breve descripción de la funcionalidad.
 * @param icon Icono representativo.
 * @param onClick Acción al presionar la tarjeta.
 */
@Composable
fun DashboardCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Muestra el estado actual del trabajador de sincronización.
 * Indica si hay una tarea en curso o la hora del último intento exitoso.
 *
 * @param workInfo Información sobre el estado de la tarea en segundo plano.
 */
@Composable
fun SyncStatusCard(workInfo: WorkInfo?) {
    val statusText = when (workInfo?.state) {
        WorkInfo.State.RUNNING -> stringResource(R.string.sync_running)
        WorkInfo.State.ENQUEUED -> stringResource(R.string.sync_enqueued)
        WorkInfo.State.SUCCEEDED, WorkInfo.State.FAILED -> {
            val locale = LocalConfiguration.current.locales[0]
            val date = SimpleDateFormat("HH:mm", locale).format(Date())
            stringResource(R.string.sync_last_attempt, date)
        }
        else -> stringResource(R.string.sync_unknown)
    }

    val icon = if (workInfo?.state == WorkInfo.State.RUNNING) Icons.Rounded.Sync else Icons.Rounded.CloudDone
    val color = if (workInfo?.state == WorkInfo.State.RUNNING) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.sync_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(statusText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        onNavigateToCalculator = {},
        onNavigateToGuia = {},
        onOpenDrawer = {},
        syncStatus = null
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardCardPreview() {
    DashboardCard(
        title = stringResource(R.string.test_dashboard_title),
        description = stringResource(R.string.test_dashboard_desc),
        icon = Icons.Rounded.Calculate,
        onClick = {}
    )
}
