package cu.edu.inca.abonosverdes.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.compose.ui.res.stringResource
import cu.edu.inca.abonosverdes.BuildConfig
import cu.edu.inca.abonosverdes.R
import cu.edu.inca.abonosverdes.ui.screens.calculadora.CalculatorScreen
import cu.edu.inca.abonosverdes.ui.screens.cultivos.CultivosScreen
import cu.edu.inca.abonosverdes.ui.screens.fertilizantes.FertilizantesScreen
import cu.edu.inca.abonosverdes.ui.screens.fincas.FincasScreen
import cu.edu.inca.abonosverdes.ui.screens.home.HomeScreen
import cu.edu.inca.abonosverdes.ui.screens.onboarding.OnboardingScreen
import cu.edu.inca.abonosverdes.ui.screens.splash.SplashScreen
import cu.edu.inca.abonosverdes.ui.screens.guia.GuiaScreen
import cu.edu.inca.abonosverdes.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbonarNavHost(
    showOnboarding: Boolean,
    onOnboardingFinished: () -> Unit
) {
    val navKeySaver = listSaver<MutableState<List<NavKey>>, Destination>(
        save = { state -> state.value.map { it as Destination } },
        restore = { list -> mutableStateOf(list.map { it as NavKey }) }
    )
    var backStackState = rememberSaveable(saver = navKeySaver) { mutableStateOf(listOf<NavKey>(Destination.Splash)) }
    var backStack by backStackState
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()

    val cultivos by viewModel.cultivos.collectAsStateWithLifecycle()
    val suelos by viewModel.suelos.collectAsStateWithLifecycle()
    val fertilizantes by viewModel.fertAbOrg.collectAsStateWithLifecycle()

    val currentDestination = backStack.lastOrNull()

    val myEntryProvider = entryProvider<NavKey> {
        entry(Destination.Splash as NavKey) {
            SplashScreen(onTimeout = {
                backStack = if (showOnboarding) listOf(Destination.Onboarding) else listOf(Destination.Home)
            })
        }
        entry(Destination.Onboarding as NavKey) {
            OnboardingScreen(onFinished = {
                onOnboardingFinished()
                backStack = listOf(Destination.Home)
            })
        }
        entry(Destination.Home as NavKey) {
            HomeScreen(
                onNavigateToCalculator = { backStack = backStack + Destination.Calculadora },
                onNavigateToGuia = { backStack = backStack + Destination.Guia },
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
        entry(Destination.Cultivos as NavKey) {
            CultivosScreen(
                cultivosList = cultivos,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
        entry(Destination.Fincas as NavKey) {
            FincasScreen(
                suelosList = suelos,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
        entry(Destination.Fertilizantes as NavKey) {
            FertilizantesScreen(
                fertilizantesList = fertilizantes,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
        entry(Destination.Calculadora as NavKey) {
            CalculatorScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
        entry(Destination.Guia as NavKey) {
            GuiaScreen(onOpenDrawer = { scope.launch { drawerState.open() } })
        }
    }

    if (currentDestination is Destination.Splash || currentDestination is Destination.Onboarding) {
        NavDisplay(
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack = backStack.dropLast(1) },
            entryProvider = myEntryProvider
        )
    } else {
        val configuration = LocalConfiguration.current
        val drawerWidth = configuration.screenWidthDp.dp * 0.75f

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(drawerWidth)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.cultivo_general),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(10.dp),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                        )
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.nombre),
                            contentDescription = stringResource(R.string.logo_desc),
                            modifier = Modifier
                                .size(300.dp)
                                .align(Alignment.Center)
                        )
                        Text(
                            text = stringResource(R.string.version_label, BuildConfig.VERSION_NAME),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_inicio)) },
                        selected = currentDestination == Destination.Home,
                        onClick = { backStack = listOf(Destination.Home); scope.launch { drawerState.close() } }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Rounded.Agriculture, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_cultivos)) },
                        selected = currentDestination == Destination.Cultivos,
                        onClick = { backStack = listOf(Destination.Cultivos); scope.launch { drawerState.close() } }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Rounded.Landscape, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_fincas)) },
                        selected = currentDestination == Destination.Fincas,
                        onClick = { backStack = listOf(Destination.Fincas); scope.launch { drawerState.close() } }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Rounded.Science, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_fertilizantes)) },
                        selected = currentDestination == Destination.Fertilizantes,
                        onClick = { backStack = listOf(Destination.Fertilizantes); scope.launch { drawerState.close() } }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Rounded.Calculate, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_calculadora)) },
                        selected = currentDestination == Destination.Calculadora,
                        onClick = { backStack = listOf(Destination.Calculadora); scope.launch { drawerState.close() } }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Rounded.Help, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_guia)) },
                        selected = currentDestination == Destination.Guia,
                        onClick = { backStack = listOf(Destination.Guia); scope.launch { drawerState.close() } }
                    )
                }
            }
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack = backStack.dropLast(1) },
                entryProvider = myEntryProvider
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, onOpenDrawer: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Rounded.Menu, contentDescription = stringResource(R.string.menu_desc))
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
            Text(text = stringResource(R.string.placeholder_message, title), modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaceholderScreenPreview() {
    PlaceholderScreen(title = stringResource(R.string.nav_inicio), onOpenDrawer = {})
}
