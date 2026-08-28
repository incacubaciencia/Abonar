package cu.edu.inca.abonosverdes.ui.screens.guia

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cu.edu.inca.abonosverdes.R
import cu.edu.inca.abonosverdes.ui.components.AbonarAppBar
import cu.edu.inca.abonosverdes.ui.theme.AbonarTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla de Guía de Usuario interactiva.
 * Explica paso a paso cómo usar la aplicación y permite exportar un manual en PDF.
 *
 * @param onOpenDrawer Callback para abrir el cajón de navegación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaScreen(
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val successMessage = stringResource(R.string.export_success)
    val errorMessage = stringResource(R.string.export_error)

    // Launcher para guardar el archivo PDF usando el Storage Access Framework
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            scope.launch {
                val success = exportPdf(context, it)
                if (success) {
                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AbonarAppBar(
                title = stringResource(R.string.guia_title),
                onOpenDrawer = onOpenDrawer
            )
        },
        floatingActionButton = {
            val filename = stringResource(R.string.export_pdf_filename)
            ExtendedFloatingActionButton(
                onClick = { exportLauncher.launch(filename) },
                icon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                text = { Text(stringResource(R.string.export_pdf)) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.como_usar),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Sección Módulos Base
            item {
                GuideSection(
                    title = stringResource(R.string.section_base),
                    icon = Icons.Rounded.Dashboard
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BaseModuleItem(
                            title = stringResource(R.string.reg_fincas_title),
                            description = stringResource(R.string.reg_fincas_desc),
                            imageRes = R.drawable.guia_fincas
                        )
                        BaseModuleItem(
                            title = stringResource(R.string.reg_cultivos_title),
                            description = stringResource(R.string.reg_cultivos_desc),
                            imageRes = R.drawable.guia_cultivos
                        )
                        BaseModuleItem(
                            title = stringResource(R.string.fert_title),
                            description = stringResource(R.string.reg_fertilizantes_desc),
                            imageRes = R.drawable.guia_fertilizantes
                        )
                    }
                }
            }

            // Sección Calculadora (Paso a paso detallado)
            item {
                GuideSection(
                    title = stringResource(R.string.section_calculator),
                    icon = Icons.Rounded.Calculate
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CalculatorStepItem(
                            title = stringResource(R.string.step_1_title),
                            description = stringResource(R.string.step_1_desc),
                            imageRes = R.drawable.guia_calc_finca
                        )
                        CalculatorStepItem(
                            title = stringResource(R.string.step_2_title),
                            description = stringResource(R.string.step_2_desc),
                            imageRes = R.drawable.guia_calc_cultivo
                        )
                        CalculatorStepItem(
                            title = stringResource(R.string.step_3_title),
                            description = stringResource(R.string.step_3_desc),
                            imageRes = R.drawable.guia_calc_rend
                        )
                        CalculatorStepItem(
                            title = stringResource(R.string.step_4_title),
                            description = stringResource(R.string.step_4_desc),
                            imageRes = R.drawable.guia_calc_fert
                        )
                        CalculatorStepItem(
                            title = stringResource(R.string.step_5_title),
                            description = stringResource(R.string.step_5_desc),
                            imageRes = R.drawable.guia_calc_res
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Espacio para el FAB
            }
        }
    }
}

@Composable
fun GuideSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun BaseModuleItem(title: String, description: String, imageRes: Int) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        GuideImage(imageRes = imageRes, contentDescription = title)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
    }
}

@Composable
fun CalculatorStepItem(title: String, description: String, imageRes: Int) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        GuideImage(imageRes = imageRes, contentDescription = title)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = description, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun GuideImage(imageRes: Int, contentDescription: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(MaterialTheme.shapes.medium),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Función que maneja la lógica de extraer el PDF de assets y guardarlo.
 */
suspend fun exportPdf(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
    try {
        context.assets.open("guia_usuario.pdf").use { inputStream ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
                true
            } ?: false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Preview(showBackground = true)
@Composable
fun GuiaScreenPreview() {
    AbonarTheme {
        GuiaScreen(onOpenDrawer = {})
    }
}
