package cu.edu.inca.abonosverdes.ui.screens.cultivos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cu.edu.inca.abonosverdes.R
import androidx.compose.ui.res.stringResource
import cu.edu.inca.abonosverdes.data.local.entities.Cultivos
import cu.edu.inca.abonosverdes.ui.components.AbonarAppBar
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Busca y retorna el recurso de imagen (ID de drawable) asociado al nombre del cultivo.
 * Realiza una normalización del nombre para que coincida con las convenciones de nombres de recursos de Android.
 *
 * @param nombre Nombre del cultivo.
 * @return ID del recurso drawable.
 */
@Composable
fun getCultivoImageRes(nombre: String): Int {
    val context = LocalContext.current
    return remember(nombre) {
        val temp = Normalizer.normalize(nombre, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        val normalized = pattern.matcher(temp).replaceAll("")
            .replace('ñ', 'n')
            .replace('Ñ', 'n')
            .lowercase()
            .trim()
            .replace(' ', '_')

        val resId = context.resources.getIdentifier("cultivo_$normalized", "drawable", context.packageName)
        if (resId != 0) {
            resId
        } else {
            val generalId = context.resources.getIdentifier("cultivo_general", "drawable", context.packageName)
            if (generalId != 0) generalId else R.drawable.placeholder
        }
    }
}

/**
 * Pantalla que muestra el catálogo de cultivos disponibles.
 * Organiza los cultivos por tipo y permite ver detalles específicos de cada uno
 * mediante un patrón de lista-detalle adaptativo.
 *
 * @param cultivosList Lista de cultivos a mostrar.
 * @param onOpenDrawer Callback para el menú de navegación.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CultivosScreen(
    cultivosList: List<Cultivos>,
    onOpenDrawer: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Cultivos>()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val selectedCultivo = navigator.currentDestination?.contentKey

    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val filteredCultivos = remember(searchQuery, cultivosList) {
        if (searchQuery.isBlank()) {
            cultivosList
        } else {
            cultivosList.filter {
                it.nombre.contains(searchQuery, ignoreCase = true) ||
                it.tipo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    BackHandler(navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
        }
    }

    Scaffold(
        topBar = {
            AbonarAppBar(
                title = stringResource(R.string.cultivos_title),
                onOpenDrawer = onOpenDrawer
            )
        }
    ) { paddingValues ->
        ListDetailPaneScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    val groupedCultivos = remember(filteredCultivos) {
                        filteredCultivos.groupBy { it.tipo }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester)
                            .focusable()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            placeholder = { Text(stringResource(R.string.search_cultivo)) },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        if (filteredCultivos.isEmpty() && searchQuery.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (filteredCultivos.isEmpty() && searchQuery.isNotEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.no_results), style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                groupedCultivos.forEach { (tipo, cultivos) ->
                                    item(key = tipo) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = tipo.uppercase(),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = TextDecoration.Underline
                                                ),
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                textAlign = TextAlign.Center
                                            )
                                            HorizontalDivider(
                                                thickness = 1.dp,
                                                color = Color.Gray.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                    items(cultivos, key = { it.id }) { cultivo ->
                                        CultivoCard(
                                            cultivo = cultivo,
                                            onClick = {
                                                scope.launch {
                                                    navigator.navigateTo(
                                                        ListDetailPaneScaffoldRole.Detail,
                                                        cultivo
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            detailPane = {
                AnimatedPane {
                    selectedCultivo?.let { cultivo ->
                        CultivoDetail(
                            cultivo = cultivo,
                            onBack = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            }
                        )
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.select_cultivo_detail), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        )
    }
}

/**
 * Tarjeta individual que muestra un resumen de un cultivo.
 *
 * @param cultivo Los datos del cultivo.
 * @param onClick Acción al hacer clic.
 */
@Composable
fun CultivoCard(cultivo: Cultivos, onClick: () -> Unit) {
    val imageRes = getCultivoImageRes(cultivo.nombre)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageRes,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.placeholder)
            )
            
            // Overlay gradient for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = cultivo.nombre.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    textAlign = TextAlign.Center
                )
                
                val phRange = if (cultivo.phMin != null && cultivo.phMax != null) {
                    "${cultivo.phMin} - ${cultivo.phMax}"
                } else if (cultivo.phMin != null) {
                    "> ${cultivo.phMin}"
                } else if (cultivo.phMax != null) {
                    "< ${cultivo.phMax}"
                } else {
                    "-"
                }

                Text(
                    text = stringResource(R.string.rend_prefix, cultivo.rendimientoTH) + " | " + stringResource(R.string.ph_prefix, phRange),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.npk_label, cultivo.n, cultivo.p, cultivo.k),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Muestra la información detallada de un cultivo, incluyendo requerimientos nutricionales (N, P, K)
 * y rangos de pH óptimos.
 *
 * @param cultivo Los datos del cultivo.
 * @param onBack Callback para volver a la lista.
 */
@Composable
fun CultivoDetail(cultivo: Cultivos, onBack: () -> Unit) {
    val imageRes = getCultivoImageRes(cultivo.nombre)

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = imageRes,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(10.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back_desc),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = cultivo.nombre,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.tech_info), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DetailItem(stringResource(R.string.tipo_label), cultivo.tipo)
                    DetailItem(stringResource(R.string.yield_expected), "${cultivo.rendimientoTH} ${stringResource(R.string.unit_t_ha_short)}")
                    DetailItem(stringResource(R.string.ph_min), cultivo.phMin?.toString() ?: "-")
                    DetailItem(stringResource(R.string.ph_max), cultivo.phMax?.toString() ?: "-")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.nutritional_req), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DetailItem(stringResource(R.string.nitrogeno), "${cultivo.n} ${stringResource(R.string.unit_kg_t)}")
                    DetailItem(stringResource(R.string.fosforo_full), "${cultivo.p} ${stringResource(R.string.unit_kg_t)}")
                    DetailItem(stringResource(R.string.potasio_full), "${cultivo.k} ${stringResource(R.string.unit_kg_t)}")
                }
            }
        }
    }
}

/**
 * Componente simple para mostrar un par etiqueta-valor en una fila.
 *
 * @param label Etiqueta descriptiva.
 * @param value Valor asociado.
 */
@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun CultivosScreenPreview() {
    CultivosScreen(
        cultivosList = listOf(
            Cultivos(1, "Frutales", "Aguacate", 10.0, 6.0, 8.0, 6.0, 1.0, 10.0),
            Cultivos(2, "Frutales", "Cafeto", 2.5, null, null, 5.0, 0.45, 6.0),
            Cultivos(14, "Granos", "Maíz", 2.0, 6.0, 7.0, 15.0, 3.0, 4.0)
        ),
        onOpenDrawer = {}
    )
}
