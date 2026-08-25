package cu.edu.inca.abonosverdes.ui.screens.fincas

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cu.edu.inca.abonosverdes.R
import cu.edu.inca.abonosverdes.data.local.entities.Suelos
import cu.edu.inca.abonosverdes.ui.components.AbonarAppBar
import kotlinx.coroutines.launch

/**
 * Pantalla que lista las fincas registradas y sus detalles de suelo.
 * Utiliza un diseño adaptativo de lista-detalle para tablets y pantallas grandes.
 *
 * @param suelosList Lista de objetos [Suelos] a mostrar.
 * @param onOpenDrawer Callback para abrir el menú lateral.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FincasScreen(
    suelosList: List<Suelos>,
    onOpenDrawer: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Suelos>()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val selectedSuelo = navigator.currentDestination?.contentKey

    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val filteredSuelos = remember(searchQuery, suelosList) {
        if (searchQuery.isBlank()) {
            suelosList
        } else {
            suelosList.filter {
                it.finca?.contains(searchQuery, ignoreCase = true) == true ||
                it.provincia?.contains(searchQuery, ignoreCase = true) == true ||
                it.municipio?.contains(searchQuery, ignoreCase = true) == true ||
                it.tipoSuelo?.contains(searchQuery, ignoreCase = true) == true
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
                title = stringResource(R.string.fincas_title),
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
                            placeholder = { Text(stringResource(R.string.search_finca)) },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        if (filteredSuelos.isEmpty() && searchQuery.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (filteredSuelos.isEmpty() && searchQuery.isNotEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.no_results), style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredSuelos, key = { it.id }) { suelo ->
                                    FincaCard(
                                        suelo = suelo,
                                        onClick = {
                                            scope.launch {
                                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, suelo)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            detailPane = {
                AnimatedPane {
                    selectedSuelo?.let { suelo ->
                        SueloDetail(
                            suelo = suelo,
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
                        Text(stringResource(R.string.select_finca), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        )
    }
}

/**
 * Representación visual de una finca en la lista.
 * Muestra el nombre de la finca, municipio y tipo de suelo sobre una imagen de fondo.
 *
 * @param suelo Objeto con la información del suelo de la finca.
 * @param onClick Acción al seleccionar la tarjeta.
 */
@Composable
fun FincaCard(
    suelo: Suelos,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = R.drawable.placeholder,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder)
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
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = (suelo.finca ?: "").uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )
                Text(
                    text = stringResource(
                        R.string.finca_location_format,
                        suelo.provincia ?: "",
                        suelo.municipio ?: "",
                        suelo.tipoSuelo ?: ""
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Muestra los detalles técnicos completos del suelo de una finca seleccionada.
 *
 * @param suelo Datos del suelo.
 * @param onBack Acción para regresar a la lista (en móviles).
 */
@Composable
fun SueloDetail(
    suelo: Suelos,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = R.drawable.placeholder,
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
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    RoundedCornerShape(12.dp)
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = (suelo.finca ?: "").uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Text(
                        text = stringResource(R.string.provincia_label, suelo.provincia ?: ""),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = stringResource(R.string.municipio_label, suelo.municipio ?: ""),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = stringResource(R.string.tech_info),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailItem(stringResource(R.string.tipo_suelo), suelo.tipoSuelo ?: "")
                        DetailItem(stringResource(R.string.ph_label), suelo.ph?.toString() ?: "-")
                        DetailItem(stringResource(R.string.mo_percent), suelo.moPercent?.toString() ?: "-")
                        DetailItem(stringResource(R.string.calcio), suelo.ca?.toString() ?: "-")
                        DetailItem(stringResource(R.string.magnesio), suelo.mg?.toString() ?: "-")
                        DetailItem(stringResource(R.string.potasio), suelo.k?.toString() ?: "-")
                        DetailItem(stringResource(R.string.sodio), suelo.na?.toString() ?: "-")
                        DetailItem(stringResource(R.string.fosforo), suelo.p?.toString() ?: "-")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FincasScreenPreview() {
    FincasScreen(
        suelosList = listOf(
            Suelos(1, "Mayabeque", "San Jose", "Finca La Esperanza", "Ferralítico", 6.2, 2.5, 10.0, 3.0, 0.5, 0.1, 15.0),
            Suelos(2, "Artemisa", "Güira", "Finca El Progreso", "Pardo con Carbonatos", 7.1, 3.2, 15.0, 5.0, 0.8, 0.2, 20.0)
        ),
        onOpenDrawer = {}
    )
}
