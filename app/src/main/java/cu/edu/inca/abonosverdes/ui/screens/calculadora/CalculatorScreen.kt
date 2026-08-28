package cu.edu.inca.abonosverdes.ui.screens.calculadora

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cu.edu.inca.abonosverdes.R
import cu.edu.inca.abonosverdes.data.local.entities.Cultivos
import cu.edu.inca.abonosverdes.data.local.entities.FertAbOrg
import cu.edu.inca.abonosverdes.ui.components.AbonarAppBar
import androidx.compose.ui.tooling.preview.Preview
import cu.edu.inca.abonosverdes.ui.theme.AbonarTheme
import cu.edu.inca.abonosverdes.ui.viewmodel.CalculatorUiState
import cu.edu.inca.abonosverdes.ui.viewmodel.VisibleSteps
import cu.edu.inca.abonosverdes.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de la calculadora de abonos.
 * Presenta un formulario paso a paso donde el usuario introduce datos sobre su finca,
 * suelo, cultivo y fertilizantes disponibles para obtener una recomendación.
 *
 * @param onOpenDrawer Función para abrir el menú lateral de navegación.
 * @param viewModel ViewModel que gestiona el estado y la lógica de la calculadora.
 */
@Composable
fun CalculatorScreen(
    onOpenDrawer: () -> Unit,
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val visibleSteps by viewModel.visibleSteps.collectAsStateWithLifecycle()
    val isCalculateEnabled by viewModel.isCalculateEnabled.collectAsStateWithLifecycle()

    val allFincas by viewModel.allFincas.collectAsStateWithLifecycle()
    val allTiposSuelo by viewModel.allTiposSuelo.collectAsStateWithLifecycle()
    val availableSoilCharacteristics by viewModel.availableSoilCharacteristics.collectAsStateWithLifecycle()
    val allAbonosVerdes by viewModel.allAbonosVerdes.collectAsStateWithLifecycle()
    val allProvincias by viewModel.allProvincias.collectAsStateWithLifecycle()
    val allMunicipios by viewModel.allMunicipios.collectAsStateWithLifecycle()
    val allCultivos by viewModel.allCultivos.collectAsStateWithLifecycle()
    val allFertAbOrg by viewModel.allFertAbOrg.collectAsStateWithLifecycle()

    val yieldUnits = cu.edu.inca.abonosverdes.ui.viewmodel.YieldUnit.entries
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll al resultado cuando aparezca
    LaunchedEffect(uiState.calculationResult) {
        if (uiState.calculationResult != null) {
            scope.launch {
                // El resultado es el último item, scroll a una posición alta para asegurar visibilidad
                listState.animateScrollToItem(index = 20) 
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetForm()
        }
    }

    CalculatorContent(
        onOpenDrawer = onOpenDrawer,
        uiState = uiState,
        visibleSteps = visibleSteps,
        isCalculateEnabled = isCalculateEnabled,
        allFincas = allFincas,
        allTiposSuelo = allTiposSuelo,
        availableSoilCharacteristics = availableSoilCharacteristics,
        allProvincias = allProvincias,
        allMunicipios = allMunicipios,
        allCultivos = allCultivos,
        allFertAbOrg = allFertAbOrg,
        onFincaSelected = viewModel::onFincaSelected,
        onConoceTipoSueloChanged = viewModel::onConoceTipoSueloChanged,
        onTipoSueloSelected = viewModel::onTipoSueloSelected,
        onSoilCharacteristicSelected = viewModel::onSoilCharacteristicSelected,
        onRealizoAnalisisFertilidadChanged = viewModel::onRealizoAnalisisFertilidadChanged,
        onProvinciaSelected = viewModel::onProvinciaSelected,
        onMunicipioSelected = viewModel::onMunicipioSelected,
        onCultivoSelected = viewModel::onCultivoSelected,
        onConoceRendimientoChanged = viewModel::onConoceRendimientoChanged,
        onRendimientoValueChanged = viewModel::onRendimientoValueChanged,
        onUnidadRendimientoSelected = viewModel::onUnidadRendimientoSelected,
        onNutrienteDisponibleSelected = viewModel::onNutrienteDisponibleSelected,
        onUtilizoAbonoVerdeChanged = viewModel::onUtilizoAbonoVerdeChanged,
        onAbonoVerdeSelected = viewModel::onAbonoVerdeSelected,
        onCalculateClick = viewModel::calculateDosis,
        onResetForm = viewModel::resetForm,
        yieldUnits = yieldUnits,
        allAbonosVerdes = allAbonosVerdes,
        listState = listState
    )
}

@Composable
fun CalculatorContent(
    onOpenDrawer: () -> Unit,
    uiState: CalculatorUiState,
    visibleSteps: VisibleSteps,
    isCalculateEnabled: Boolean,
    allFincas: List<String>,
    allTiposSuelo: List<String>,
    availableSoilCharacteristics: List<cu.edu.inca.abonosverdes.ui.viewmodel.SoilPhysicalCharacteristic>,
    allProvincias: List<String>,
    allMunicipios: List<String>,
    allCultivos: List<Cultivos>,
    allFertAbOrg: List<FertAbOrg>,
    allAbonosVerdes: List<FertAbOrg>,
    onFincaSelected: (String?) -> Unit,
    onConoceTipoSueloChanged: (Boolean) -> Unit,
    onTipoSueloSelected: (String) -> Unit,
    onSoilCharacteristicSelected: (Int) -> Unit,
    onRealizoAnalisisFertilidadChanged: (Boolean) -> Unit,
    onProvinciaSelected: (String) -> Unit,
    onMunicipioSelected: (String) -> Unit,
    onCultivoSelected: (Cultivos) -> Unit,
    onConoceRendimientoChanged: (Boolean) -> Unit,
    onRendimientoValueChanged: (String) -> Unit,
    onUnidadRendimientoSelected: (cu.edu.inca.abonosverdes.ui.viewmodel.YieldUnit) -> Unit,
    onNutrienteDisponibleSelected: (FertAbOrg) -> Unit,
    onUtilizoAbonoVerdeChanged: (Boolean) -> Unit,
    onAbonoVerdeSelected: (FertAbOrg) -> Unit,
    onCalculateClick: () -> Unit,
    onResetForm: () -> Unit,
    yieldUnits: List<cu.edu.inca.abonosverdes.ui.viewmodel.YieldUnit>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Scaffold(
        topBar = {
            AbonarAppBar(
                title = stringResource(R.string.nav_calculadora),
                onOpenDrawer = onOpenDrawer
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Selección de Finca
            item {
                StepContainer(title = stringResource(R.string.calc_step_finca)) {
                    SearchableDropdown(
                        label = stringResource(R.string.calc_finca_optional),
                        options = allFincas,
                        selectedOption = uiState.selectedFinca ?: "",
                        onOptionSelected = { onFincaSelected(it.ifEmpty { null }) },
                    )
                    if (uiState.selectedFinca != null) {
                        InfoCard(text = stringResource(R.string.calc_finca_auto_loaded, uiState.selectedFinca))
                    }
                }
            }

            // Tipo de Suelo
            item {
                AnimatedVisibility(visible = visibleSteps.step2) {
                    StepContainer(title = stringResource(R.string.calc_step_tipo_suelo)) {
                        Text(stringResource(R.string.calc_conoce_tipo_suelo), style = MaterialTheme.typography.bodyLarge)
                        Row(Modifier.selectableGroup()) {
                            RadioButtonWithLabel(stringResource(R.string.si), uiState.conoceTipoSuelo == true) { onConoceTipoSueloChanged(true) }
                            RadioButtonWithLabel(stringResource(R.string.no), uiState.conoceTipoSuelo == false) { onConoceTipoSueloChanged(false) }
                        }

                        AnimatedVisibility(visible = uiState.conoceTipoSuelo == true) {
                            SearchableDropdown(
                                label = stringResource(R.string.tipo_suelo),
                                options = allTiposSuelo,
                                selectedOption = uiState.selectedTipoSuelo ?: "",
                                onOptionSelected = { onTipoSueloSelected(it) }
                            )
                        }

                        AnimatedVisibility(visible = uiState.conoceTipoSuelo == false) {
                            SoilCharacteristicDropdown(
                                label = stringResource(R.string.calc_color_suelo_ques),
                                options = availableSoilCharacteristics,
                                selectedOptionResId = uiState.selectedSoilCharResId,
                                onOptionSelected = { onSoilCharacteristicSelected(it.resId) }
                            )
                        }
                    }
                }
            }

            // Análisis de Fertilidad
            item {
                AnimatedVisibility(visible = visibleSteps.step3) {
                    StepContainer(title = stringResource(R.string.calc_step_fertilidad)) {
                        Text(stringResource(R.string.calc_realizo_analisis_fert), style = MaterialTheme.typography.bodyLarge)
                        Row(Modifier.selectableGroup()) {
                            RadioButtonWithLabel(stringResource(R.string.si), uiState.realizoAnalisisFertilidad == true) { onRealizoAnalisisFertilidadChanged(true) }
                            RadioButtonWithLabel(stringResource(R.string.no), uiState.realizoAnalisisFertilidad == false) { onRealizoAnalisisFertilidadChanged(false) }
                        }

                        AnimatedVisibility(visible = uiState.realizoAnalisisFertilidad == true) {
                            if (uiState.selectedFinca == null) {
                                InfoCard(text = stringResource(R.string.calc_fert_select_finca_error))
                            } else {
                                InfoCard(text = stringResource(R.string.calc_fert_info_auto))
                            }
                        }

                        AnimatedVisibility(visible = uiState.realizoAnalisisFertilidad == false) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                SearchableDropdown(
                                    label = stringResource(R.string.calc_provincia),
                                    options = allProvincias,
                                    selectedOption = uiState.selectedProvincia ?: "",
                                    onOptionSelected = { onProvinciaSelected(it) }
                                )
                                SearchableDropdown(
                                    label = stringResource(R.string.calc_municipio),
                                    options = allMunicipios,
                                    selectedOption = uiState.selectedMunicipio ?: "",
                                    onOptionSelected = { onMunicipioSelected(it) }
                                )
                            }
                        }
                    }
                }
            }

            // Tipo de Cultivo
            item {
                AnimatedVisibility(visible = visibleSteps.step4) {
                    StepContainer(title = stringResource(R.string.calc_step_cultivo)) {
                        Text(stringResource(R.string.calc_cultivo_ques), style = MaterialTheme.typography.bodyLarge)
                        CultivoDropdown(
                            label = stringResource(R.string.cultivo_label),
                            options = allCultivos,
                            selectedOption = uiState.selectedCultivo,
                            onOptionSelected = { onCultivoSelected(it) }
                        )
                    }
                }
            }

            // Rendimiento de la Finca
            item {
                AnimatedVisibility(visible = visibleSteps.step5) {
                    StepContainer(title = stringResource(R.string.calc_step_rendimiento)) {
                        Text(stringResource(R.string.calc_conoce_rendimiento), style = MaterialTheme.typography.bodyLarge)
                        Row(Modifier.selectableGroup()) {
                            RadioButtonWithLabel(stringResource(R.string.si), uiState.conoceRendimiento == true) { onConoceRendimientoChanged(true) }
                            RadioButtonWithLabel(stringResource(R.string.no), uiState.conoceRendimiento == false) { onConoceRendimientoChanged(false) }
                        }

                        AnimatedVisibility(visible = uiState.conoceRendimiento == true) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.rendimientoValue,
                                    onValueChange = { onRendimientoValueChanged(it) },
                                    label = { Text(stringResource(R.string.calc_rendimiento_value)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                YieldUnitDropdown(
                                    options = yieldUnits,
                                    selectedOption = uiState.selectedYieldUnit,
                                    onOptionSelected = { onUnidadRendimientoSelected(it) },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Tipo de Nutrientes Disponibles
            item {
                AnimatedVisibility(visible = visibleSteps.step6) {
                    StepContainer(title = stringResource(R.string.calc_step_nutrientes)) {
                        Text(stringResource(R.string.calc_nutrientes_ques), style = MaterialTheme.typography.bodyLarge)
                        FertilizanteDropdown(
                            label = stringResource(R.string.calc_select_nutrient_type),
                            options = allFertAbOrg,
                            selectedOption = uiState.selectedNutrienteDisponible,
                            onOptionSelected = { onNutrienteDisponibleSelected(it) }
                        )
                    }
                }
            }

            // Abono Verde
            item {
                AnimatedVisibility(visible = visibleSteps.step7) {
                    StepContainer(title = stringResource(R.string.calc_step_abono_verde)) {
                        Text(stringResource(R.string.calc_utilizo_abono_verde_ques), style = MaterialTheme.typography.bodyLarge)
                        Row(Modifier.selectableGroup()) {
                            RadioButtonWithLabel(stringResource(R.string.si), uiState.utilizoAbonoVerde == true) { onUtilizoAbonoVerdeChanged(true) }
                            RadioButtonWithLabel(stringResource(R.string.no), uiState.utilizoAbonoVerde == false) { onUtilizoAbonoVerdeChanged(false) }
                        }

                        AnimatedVisibility(visible = uiState.utilizoAbonoVerde == true) {
                            FertilizanteDropdown(
                                label = stringResource(R.string.calc_select_abono_verde),
                                options = allAbonosVerdes,
                                selectedOption = uiState.selectedAbonoVerde,
                                onOptionSelected = { onAbonoVerdeSelected(it) }
                            )
                        }
                    }
                }
            }

            // BOTÓN CALCULAR
            item {
                Button(
                    onClick = onCalculateClick,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = isCalculateEnabled,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(R.string.btn_calcular), style = MaterialTheme.typography.titleMedium)
                }
            }

            // RESULTADOS
            uiState.calculationResult?.let { result ->
                item {
                    ResultSection(result, onResetForm)
                }
            }
        }
    }
}

/**
 * Sección que muestra los resultados detallados del cálculo agronómico.
 * Presenta tres bloques: Datos de Entrada, Cálculos Intermedios y Recomendación Final.
 *
 * @param result Objeto con los datos y el mensaje del cálculo realizado.
 * @param onResetForm Función para resetear el formulario.
 */
@Composable
fun ResultSection(
    result: cu.edu.inca.abonosverdes.ui.viewmodel.CalculationResult,
    onResetForm: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (result.isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = if (result.isSuccess) stringResource(R.string.calc_report_title) else stringResource(R.string.calc_validation_alert),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (result.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            
            /* result.partialResults?.let { partials ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.calc_input_data), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    
                    ResultRow(stringResource(R.string.mo_base), String.format(Locale.US, "%.5f", partials.baseSoilValues.first))
                    ResultRow(stringResource(R.string.p_base), String.format(Locale.US, "%.5f", partials.baseSoilValues.second))
                    ResultRow(stringResource(R.string.k_base), String.format(Locale.US, "%.5f", partials.baseSoilValues.third))
                    
                    ResultRow(stringResource(R.string.yield_input), "${String.format(Locale.US, "%.5f", partials.yieldRaw)} ${stringResource(partials.yieldUnitResId)}")
                    
                    ResultRow(stringResource(R.string.req_n), String.format(Locale.US, "%.5f", partials.cropRequirements.first))
                    ResultRow(stringResource(R.string.req_p), String.format(Locale.US, "%.5f", partials.cropRequirements.second))
                    ResultRow(stringResource(R.string.req_k), String.format(Locale.US, "%.5f", partials.cropRequirements.third))
                    
                    if (partials.nutrientFertName.isNotEmpty()) {
                        ResultRow(stringResource(R.string.fert_nutrient_prefix, partials.nutrientFertName), stringResource(R.string.percent_unit, partials.nutrientFertVal))
                    }

                    partials.avName?.let { _ ->
                        ResultRow(stringResource(R.string.av_base_value, partials.nutrientFertName), String.format(Locale.US, "%.5f", partials.avContribution ?: 0.0))
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stringResource(R.string.calc_intermediate_results), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    
                    ResultRow(stringResource(R.string.soil_disp_n), stringResource(R.string.kg_ha_unit, partials.dispNPK.first))
                    ResultRow(stringResource(R.string.soil_disp_p), stringResource(R.string.kg_ha_unit, partials.dispNPK.second))
                    ResultRow(stringResource(R.string.soil_disp_k), stringResource(R.string.kg_ha_unit, partials.dispNPK.third))
                    
                    ResultRow(stringResource(R.string.yield_norm), stringResource(R.string.t_ha_unit, partials.yieldTHA))
                    
                    ResultRow(stringResource(R.string.base_dose_n), stringResource(R.string.kg_ha_unit, partials.dosisBaseNPK.first))
                    ResultRow(stringResource(R.string.base_dose_p), stringResource(R.string.kg_ha_unit, partials.dosisBaseNPK.second))
                    ResultRow(stringResource(R.string.base_dose_k), stringResource(R.string.kg_ha_unit, partials.dosisBaseNPK.third))
                    
                    result.nutrienteBase?.let {
                        ResultRow(stringResource(R.string.limiting_nutrient), it)
                    }

                    partials.remainingNeed?.let { need ->
                        ResultRow(stringResource(R.string.remaining_nutrient_need), stringResource(R.string.kg_ha_unit, need))
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            } */

            Text(text = stringResource(R.string.calc_final_recommendation), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = stringResource(result.messageResId, *result.messageArgs.toTypedArray()),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onResetForm,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.calc_reset))
            }
        }
    }
}


/**
 * Contenedor visual para cada paso del formulario.
 * Incluye un título estilizado y una línea divisoria al final.
 *
 * @param title Título del paso.
 * @param content Contenido composable del paso.
 */
@Composable
fun StepContainer(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/**
 * Componente de RadioButton con una etiqueta de texto al lado.
 *
 * @param label Texto que acompaña al botón.
 * @param selected Indica si el botón está seleccionado.
 * @param onClick Función que se ejecuta al hacer clic.
 */
@Composable
fun RadioButtonWithLabel(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .selectable(selected = selected, onClick = onClick)
            .padding(end = 16.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
    }
}

/**
 * Menú desplegable con capacidad de búsqueda (solo lectura en este caso, actúa como selector).
 *
 * @param label Etiqueta del campo.
 * @param options Lista de opciones disponibles.
 * @param selectedOption Opción actualmente seleccionada.
 * @param onOptionSelected Callback cuando se elige una opción.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilCharacteristicDropdown(
    label: String,
    options: List<cu.edu.inca.abonosverdes.ui.viewmodel.SoilPhysicalCharacteristic>,
    selectedOptionResId: Int?,
    onOptionSelected: (cu.edu.inca.abonosverdes.ui.viewmodel.SoilPhysicalCharacteristic) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOptionResId?.let { stringResource(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.resId)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoDropdown(
    label: String,
    options: List<Cultivos>,
    selectedOption: Cultivos?,
    onOptionSelected: (Cultivos) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.nombre) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizanteDropdown(
    label: String,
    options: List<FertAbOrg>,
    selectedOption: FertAbOrg?,
    onOptionSelected: (FertAbOrg) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption?.nomb ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.nomb) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YieldUnitDropdown(
    options: List<cu.edu.inca.abonosverdes.ui.viewmodel.YieldUnit>,
    selectedOption: cu.edu.inca.abonosverdes.ui.viewmodel.YieldUnit,
    onOptionSelected: (cu.edu.inca.abonosverdes.ui.viewmodel.YieldUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = stringResource(selectedOption.resId),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            textStyle = MaterialTheme.typography.bodySmall
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.resId), style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Tarjeta informativa utilizada para mostrar mensajes de carga automática o ayuda.
 *
 * @param text Mensaje a mostrar.
 */
@Composable
fun InfoCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(text = text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    AbonarTheme {
        CalculatorContent(
            onOpenDrawer = {},
            uiState = CalculatorUiState(
                conoceTipoSuelo = true,
                selectedTipoSuelo = "Ferralítico Rojo",
                realizoAnalisisFertilidad = false,
                selectedProvincia = "Mayabeque",
                selectedMunicipio = "San José de las Lajas",
                selectedCultivo = Cultivos(1, "Tubérculo", "Papa", 20.0, 5.5, 7.5, 100.0, 50.0, 150.0),
                conoceRendimiento = true,
                rendimientoValue = "15.5",
                selectedYieldUnit = cu.edu.inca.abonosverdes.ui.viewmodel.YieldUnit.TONELADAS_HA,
                selectedNutrienteDisponible = FertAbOrg(1, "Orgánico", "Compost", 40.0, "15:1", 50.0, 1.5, 1.0, 1.2),
                calculationResult = cu.edu.inca.abonosverdes.ui.viewmodel.CalculationResult(
                    messageResId = R.string.calc_result_success,
                    messageArgs = listOf(450.25, "Compost", "Papa"),
                    isSuccess = true,
                    dosisAplicar = 450.25,
                    nutrienteBase = "K",
                    partialResults = cu.edu.inca.abonosverdes.ui.viewmodel.PartialResults(
                        baseSoilValues = Triple(2.5, 15.0, 0.45),
                        dispNPK = Triple(25000.0, 60000000.0, 351900000.0),
                        yieldRaw = 15.5,
                        yieldUnitResId = R.string.unit_t_ha,
                        yieldTHA = 15.5,
                        cropRequirements = Triple(100.0, 50.0, 150.0),
                        dosisBaseNPK = Triple(51666.67, 103333.33, 37500.0),
                        nutrientFertVal = 1.2,
                        nutrientFertName = "K"
                    )
                )
            ),
            visibleSteps = VisibleSteps(
                step2 = true,
                step3 = true,
                step4 = true,
                step5 = true,
                step6 = true
            ),
            isCalculateEnabled = true,
            allFincas = listOf("Finca La Esperanza", "Finca El Progreso"),
            allTiposSuelo = listOf("Ferralítico Rojo", "Pardo con Carbonatos"),
            availableSoilCharacteristics = listOf(
                cu.edu.inca.abonosverdes.ui.viewmodel.SoilPhysicalCharacteristic(R.string.soil_char_red_yellow, "Ferralítico"),
                cu.edu.inca.abonosverdes.ui.viewmodel.SoilPhysicalCharacteristic(R.string.soil_char_brown_gray, "Pardo")
            ),
            allProvincias = listOf("Mayabeque", "Artemisa"),
            allMunicipios = listOf("San José de las Lajas", "Güines", "Jaruco"),
            allCultivos = listOf(
                Cultivos(1, "Tubérculo", "Papa", 20.0, 5.5, 7.5, 100.0, 50.0, 150.0),
                Cultivos(2, "Cereal", "Maíz", 5.0, 6.0, 7.0, 120.0, 60.0, 80.0)
            ),
            allFertAbOrg = listOf(
                FertAbOrg(1, "Orgánico", "Compost", 40.0, "15:1", 50.0, 1.5, 1.0, 1.2),
                FertAbOrg(2, "Mineral", "Urea", 0.0, null, 0.0, 46.0, 0.0, 0.0)
            ),
            onFincaSelected = {},
            onConoceTipoSueloChanged = {},
            onTipoSueloSelected = {},
            onSoilCharacteristicSelected = {},
            onRealizoAnalisisFertilidadChanged = {},
            onProvinciaSelected = {},
            onMunicipioSelected = {},
            onCultivoSelected = {},
            onConoceRendimientoChanged = {},
            onRendimientoValueChanged = {},
            onUnidadRendimientoSelected = {},
            onNutrienteDisponibleSelected = {},
            onUtilizoAbonoVerdeChanged = {},
            onAbonoVerdeSelected = {},
            onCalculateClick = {},
            onResetForm = {},
            yieldUnits = cu.edu.inca.abonosverdes.ui.viewmodel.YieldUnit.entries,
            allAbonosVerdes = listOf(
                FertAbOrg(24, "Abonos Verdes", "Crotalaria", null, null, null, 255.0, 21.0, 92.0)
            ),
            listState = rememberLazyListState()
        )
    }
}
