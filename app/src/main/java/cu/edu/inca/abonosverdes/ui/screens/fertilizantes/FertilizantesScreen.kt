package cu.edu.inca.abonosverdes.ui.screens.fertilizantes

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cu.edu.inca.abonosverdes.R
import androidx.compose.ui.res.stringResource
import cu.edu.inca.abonosverdes.data.local.entities.FertAbOrg
import cu.edu.inca.abonosverdes.ui.components.AbonarAppBar
import cu.edu.inca.abonosverdes.ui.screens.cultivos.DetailItem
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Busca y retorna el recurso de imagen asociado al nombre del fertilizante.
 *
 * @param nombre Nombre del fertilizante.
 * @return ID del recurso drawable.
 */
@Composable
fun getFertilizanteImageRes(nombre: String): Int {
    val context = LocalContext.current
    return remember(nombre) {
        val temp = Normalizer.normalize(nombre, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        val normalized = pattern.matcher(temp).replaceAll("")
            .replace('ñ', 'n')
            .replace('Ñ', 'n')
            .lowercase()
            .replace(" ", "")
            .filter { it.isLetterOrDigit() }

        val resId = context.resources.getIdentifier("fertilizante_$normalized", "drawable", context.packageName)
        if (resId != 0) {
            resId
        } else {
            val generalId = context.resources.getIdentifier("fertilizante_general", "drawable", context.packageName)
            if (generalId != 0) generalId else R.drawable.placeholder
        }
    }
}

/**
 * Obtiene la traducción del tipo de fertilizante.
 */
@Composable
fun getTranslatedFertType(tipo: String): String {
    return when (tipo) {
        "Fertilizantes minerales" -> stringResource(R.string.fert_type_mineral)
        "Organo-minerales" -> stringResource(R.string.fert_type_organo_mineral)
        "Abonos orgánicos" -> stringResource(R.string.fert_type_organic)
        "Abonos Verdes" -> stringResource(R.string.fert_type_green)
        else -> tipo
    }
}

/**
 * Obtiene el formato de visualización NPK según el tipo de fertilizante.
 */
@Composable
fun getNPKDisplay(fertilizante: FertAbOrg): String {
    val unitRes = if (fertilizante.tipo == "Abonos Verdes") R.string.kg_ha_unit_display else R.string.percent_unit_display
    val nStr = stringResource(unitRes, fertilizante.n ?: 0.0)
    val pStr = stringResource(unitRes, fertilizante.p ?: 0.0)
    val kStr = stringResource(unitRes, fertilizante.k ?: 0.0)
    return stringResource(R.string.npk_label, nStr, pStr, kStr)
}

/**
 * Pantalla que permite explorar el catálogo de fertilizantes y abonos orgánicos.
 * Ofrece información técnica sobre su composición y contenido nutricional.
 *
 * @param fertilizantesList Lista de fertilizantes a mostrar.
 * @param onOpenDrawer Callback para abrir el cajón de navegación.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FertilizantesScreen(
    fertilizantesList: List<FertAbOrg>,
    onOpenDrawer: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<FertAbOrg>()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val selectedFertilizante = navigator.currentDestination?.contentKey

    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val filteredFertilizantes = remember(searchQuery, fertilizantesList) {
        if (searchQuery.isBlank()) {
            fertilizantesList
        } else {
            fertilizantesList.filter {
                it.nomb.contains(searchQuery, ignoreCase = true) ||
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
                title = stringResource(R.string.fert_title),
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
                    val groupedFertilizantes = remember(filteredFertilizantes) {
                        filteredFertilizantes.groupBy { it.tipo }
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
                            placeholder = { Text(stringResource(R.string.search_fert)) },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        if (filteredFertilizantes.isEmpty() && searchQuery.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (filteredFertilizantes.isEmpty() && searchQuery.isNotEmpty()) {
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
                                groupedFertilizantes.forEach { (tipo, fertilizantes) ->
                                    item(key = tipo) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = getTranslatedFertType(tipo).uppercase(),
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
                                    items(fertilizantes, key = { it.id }) { fertilizante ->
                                        FertilizanteCard(
                                            fertilizante = fertilizante,
                                            onClick = {
                                                scope.launch {
                                                    navigator.navigateTo(
                                                        ListDetailPaneScaffoldRole.Detail,
                                                        fertilizante
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
                    selectedFertilizante?.let { fertilizante ->
                        FertilizanteDetail(
                            fertilizante = fertilizante,
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
                        Text(stringResource(R.string.select_fert), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        )
    }
}

/**
 * Tarjeta que muestra un resumen de un fertilizante o abono.
 *
 * @param fertilizante Objeto con los datos.
 * @param onClick Acción al hacer clic.
 */
@Composable
fun FertilizanteCard(fertilizante: FertAbOrg, onClick: () -> Unit) {
    val imageRes = getFertilizanteImageRes(fertilizante.nomb)

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
                    text = fertilizante.nomb.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = getNPKDisplay(fertilizante),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Presenta los detalles técnicos de un fertilizante o abono específico,
 * incluyendo su tipo, porcentaje de humedad, materia orgánica y contenido de NPK.
 *
 * @param fertilizante Objeto con los datos del fertilizante.
 * @param onBack Callback para regresar.
 */
@Composable
fun FertilizanteDetail(fertilizante: FertAbOrg, onBack: () -> Unit) {
    val imageRes = getFertilizanteImageRes(fertilizante.nomb)

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
                text = fertilizante.nomb,
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
                    DetailItem(stringResource(R.string.tipo_label), getTranslatedFertType(fertilizante.tipo))
                    fertilizante.humPercent?.let { DetailItem(stringResource(R.string.humedad_percent), stringResource(R.string.percent_unit_display, it)) }
                    fertilizante.relCN?.let { DetailItem(stringResource(R.string.rel_cn), it) }
                    fertilizante.mo?.let { DetailItem(stringResource(R.string.mat_org), stringResource(R.string.percent_unit_display, it)) }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val unitRes = if (fertilizante.tipo == "Abonos Verdes") R.string.kg_ha_unit_display else R.string.percent_unit_display
                    Text(text = stringResource(R.string.nutritional_content), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DetailItem(stringResource(R.string.nitrogeno), stringResource(unitRes, fertilizante.n ?: 0.0))
                    DetailItem(stringResource(R.string.fosforo_full), stringResource(unitRes, fertilizante.p ?: 0.0))
                    DetailItem(stringResource(R.string.potasio_full), stringResource(unitRes, fertilizante.k ?: 0.0))
                }
            }
        }
    }
}
