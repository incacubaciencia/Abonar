package cu.edu.inca.abonosverdes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.edu.inca.abonosverdes.data.local.daos.*
import cu.edu.inca.abonosverdes.data.local.entities.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica de la pantalla de la calculadora.
 * Maneja el estado del formulario paso a paso, la validación de campos y la carga
 * automática de datos basados en la finca seleccionada.
 */
enum class YieldUnit(val resId: Int) {
    QUINTALES_HA(cu.edu.inca.abonosverdes.R.string.unit_q_ha),
    QUINTALES_CORDEL(cu.edu.inca.abonosverdes.R.string.unit_q_cordel),
    QUINTALES_BESANA(cu.edu.inca.abonosverdes.R.string.unit_q_besana),
    TONELADAS_HA(cu.edu.inca.abonosverdes.R.string.unit_t_ha),
    TONELADAS_CORDEL(cu.edu.inca.abonosverdes.R.string.unit_t_cordel),
    TONELADAS_BESANA(cu.edu.inca.abonosverdes.R.string.unit_t_besana)
}

data class SoilPhysicalCharacteristic(
    val resId: Int,
    val dbType: String,
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    cultivosDao: CultivosDao,
    private val suelosDao: SuelosDao,
    private val fertAbOrgDao: FertAbOrgDao,
    private val abonoOrganicoDao: AbonoOrganicoDao,
) : ViewModel() {

    private val soilPhysicalCharacteristics = listOf(
        SoilPhysicalCharacteristic(cu.edu.inca.abonosverdes.R.string.soil_char_red_yellow, "Ferralitico Rojo Lixiviado"),
        SoilPhysicalCharacteristic(cu.edu.inca.abonosverdes.R.string.soil_char_reddish_brown, "Fersialítico"),
        SoilPhysicalCharacteristic(cu.edu.inca.abonosverdes.R.string.soil_char_brown_gray, "Pardo"),
        SoilPhysicalCharacteristic(cu.edu.inca.abonosverdes.R.string.soil_char_black_loose, "Húmico"),
        SoilPhysicalCharacteristic(cu.edu.inca.abonosverdes.R.string.soil_char_black_compact, "Vértico"),
        SoilPhysicalCharacteristic(cu.edu.inca.abonosverdes.R.string.soil_char_gray_spots, "Gleysol"),
        SoilPhysicalCharacteristic(cu.edu.inca.abonosverdes.R.string.soil_char_red_very_dark, "Ferrálico"),
        SoilPhysicalCharacteristic(cu.edu.inca.abonosverdes.R.string.soil_char_white_spots, "Salino"),
    )

    // --- Form State ---
    private val _uiState = MutableStateFlow(CalculatorUiState())
    /**
     * Estado de la interfaz de usuario de la calculadora.
     */
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    // --- Data Sources ---
    /** Flujo de todos los cultivos disponibles ordenados alfabéticamente. */
    val allCultivos = cultivosDao.getAll().map { list ->
        list.sortedBy { it.nombre }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Flujo de nombres de fincas únicos ordenados alfabéticamente. */
    val allFincas = suelosDao.getUniqueFincas().map { list ->
        list.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Flujo de tipos de suelo únicos ordenados alfabéticamente. */
    val allTiposSuelo = suelosDao.getUniqueTiposSuelo().map { list ->
        list.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    /** Flujo de provincias únicas desde la tabla suelos ordenadas alfabéticamente. */
    val allProvincias = suelosDao.getUniqueProvincias().map { list ->
        list.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Flujo de municipios únicos filtrados por provincia desde la tabla suelos ordenados alfabéticamente. */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allMunicipios = _uiState.flatMapLatest { state ->
        val province = state.selectedProvincia
        if (province == null) {
            flowOf(emptyList())
        } else {
            suelosDao.getUniqueMunicipiosByProvincia(province).map { list ->
                list.sorted()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Flujo de características físicas del suelo filtradas por disponibilidad en la base de datos ordenadas alfabéticamente. */
    val availableSoilCharacteristics = allTiposSuelo.map { dbTypes ->
        soilPhysicalCharacteristics.asSequence().filter { char ->
            dbTypes.any { it.contains(char.dbType, ignoreCase = true) }
        }.sortedBy { it.dbType }.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Flujo de fuentes de nutrientes (excluyendo Abonos Verdes) ordenados por nombre. */
    val allFertAbOrg = fertAbOrgDao.getAll().map { list ->
        list.asSequence().filter { !it.tipo.contains("Abonos Verdes", ignoreCase = true) }
            .sortedBy { it.nomb }.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Flujo de Abonos Verdes únicamente ordenados por nombre. */
    val allAbonosVerdes = fertAbOrgDao.getAll().map { list ->
        list.asSequence().filter { it.tipo.contains("Abonos Verdes", ignoreCase = true) }
            .sortedBy { it.nomb }.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Form Actions ---
    /**
     * Acción ejecutada cuando se selecciona una finca.
     * Carga automáticamente los datos de suelo y abono asociados a esa finca si existen.
     *
     * @param finca Nombre de la finca seleccionada o null.
     */
    fun onFincaSelected(finca: String?) {
        viewModelScope.launch {
            val suelo = finca?.let { suelosDao.getByFinca(it) }
            val abono = finca?.let { abonoOrganicoDao.getByFinca(it) }
            
            _uiState.update { it.copy(
                selectedFinca = finca,
                autoLoadedSuelo = suelo,
                autoLoadedAbono = abono,
                // Reset conditional steps if finca is selected
                conoceTipoSuelo = if (finca != null) null else it.conoceTipoSuelo,
                calculationResult = null,
            ) }
        }
    }

    /**
     * Actualiza si el usuario conoce el tipo de suelo.
     * @param conoce True si lo conoce, false de lo contrario.
     */
    fun onConoceTipoSueloChanged(conoce: Boolean) {
        _uiState.update { it.copy(conoceTipoSuelo = conoce, selectedTipoSuelo = null, selectedSoilCharResId = null, calculationResult = null) }
    }

    /**
     * Selecciona un tipo de suelo específico.
     * @param tipo Nombre del tipo de suelo.
     */
    fun onTipoSueloSelected(tipo: String) {
        _uiState.update { it.copy(selectedTipoSuelo = tipo, calculationResult = null) }
    }

    /**
     * Selecciona la característica física del suelo.
     * @param resId ID del recurso de la cadena.
     */
    fun onSoilCharacteristicSelected(resId: Int) {
        _uiState.update { it.copy(selectedSoilCharResId = resId, calculationResult = null) }
    }

    /**
     * Actualiza si se realizó un análisis de fertilidad.
     * @param realizo True si se realizó, false de lo contrario.
     */
    fun onRealizoAnalisisFertilidadChanged(realizo: Boolean) {
        _uiState.update { it.copy(realizoAnalisisFertilidad = realizo, selectedProvincia = null, selectedMunicipio = null, calculationResult = null) }
    }

    /**
     * Selecciona la provincia para el análisis de fertilidad.
     */
    fun onProvinciaSelected(provincia: String) {
        _uiState.update { it.copy(selectedProvincia = provincia, selectedMunicipio = null, calculationResult = null) }
    }

    /**
     * Selecciona el municipio para el análisis de fertilidad.
     */
    fun onMunicipioSelected(municipio: String) {
        _uiState.update { it.copy(selectedMunicipio = municipio, calculationResult = null) }
    }

    /**
     * Selecciona un cultivo de la lista.
     */
    fun onCultivoSelected(cultivo: Cultivos) {
        _uiState.update { it.copy(selectedCultivo = cultivo, calculationResult = null) }
    }

    /**
     * Actualiza si el usuario conoce el rendimiento esperado.
     */
    fun onConoceRendimientoChanged(conoce: Boolean) {
        _uiState.update { it.copy(conoceRendimiento = conoce, rendimientoValue = "", selectedYieldUnit = YieldUnit.TONELADAS_HA, calculationResult = null) }
    }

    /**
     * Maneja el cambio en el valor numérico del rendimiento.
     * Valida que sea un número válido.
     */
    fun onRendimientoValueChanged(value: String) {
        if (value.all { (it.isDigit() || it == '.') }) {
            _uiState.update { it.copy(rendimientoValue = value, calculationResult = null) }
        }
    }

    /**
     * Selecciona la unidad de medida del rendimiento.
     */
    fun onUnidadRendimientoSelected(unit: YieldUnit) {
        _uiState.update { it.copy(selectedYieldUnit = unit, calculationResult = null) }
    }

    /**
     * Selecciona el nutriente disponible (fertilizante o abono).
     */
    fun onNutrienteDisponibleSelected(nutriente: FertAbOrg) {
        _uiState.update { it.copy(selectedNutrienteDisponible = nutriente, calculationResult = null) }
    }

    /**
     * Actualiza si el usuario utilizó abonos verdes.
     */
    fun onUtilizoAbonoVerdeChanged(utilizo: Boolean) {
        _uiState.update { it.copy(utilizoAbonoVerde = utilizo, selectedAbonoVerde = null, calculationResult = null) }
    }

    /**
     * Selecciona el abono verde empleado.
     */
    fun onAbonoVerdeSelected(abono: FertAbOrg) {
        _uiState.update { it.copy(selectedAbonoVerde = abono, calculationResult = null) }
    }

    /**
     * Resetea el formulario a su estado inicial.
     */
    fun resetForm() {
        _uiState.value = CalculatorUiState()
    }

    /**
     * Ejecuta el cálculo de la dosis de fertilizante basado en los datos introducidos.
     * Sigue un proceso secuencial de 6 pasos:
     * 1. Determinar Disponibilidad de Nutrientes del Suelo (N, P, K).
     * 2. Determinar y Normalizar el Rendimiento Esperado a T/ha.
     * 3. Cálculo Inicial de Dosis por Nutriente (utilizando coeficientes fijos).
     * 4. Evaluación de Resultados y Casos Especiales (identificación del nutriente base).
     * 5. Cálculo de la Dosis Final según el tipo de fertilizante (Mineral u Orgánico).
     * 6. Generación de Salida y Visualización de resultados.
     */
    fun calculateDosis() {
        val state = _uiState.value
        val cultivo = state.selectedCultivo ?: return
        val fertilizante = state.selectedNutrienteDisponible ?: return

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    // --- Paso 1: Determinar la Disponibilidad de Nutrientes del Suelo (N, P, K) ---
                    // Extrae MO, P y K de la base de datos (por finca o promedio de municipio/provincia o tipo de suelo).
                    val suelosList = if (state.selectedFinca != null) {
                        suelosDao.getAllByFinca(state.selectedFinca)
                    } else {
                        val searchTipo = if (state.conoceTipoSuelo == true) {
                            state.selectedTipoSuelo
                        } else {
                            state.selectedSoilCharResId?.let { resId ->
                                soilPhysicalCharacteristics.find { it.resId == resId }?.dbType
                            }
                        }

                        if (searchTipo != null) {
                            when {
                                state.selectedMunicipio != null -> {
                                    suelosDao.getAllByTipoSueloAndMunicipio(searchTipo, state.selectedMunicipio)
                                }
                                state.selectedProvincia != null -> {
                                    suelosDao.getAllByTipoSueloAndProvincia(searchTipo, state.selectedProvincia)
                                }
                                else -> {
                                    suelosDao.getAllByTipoSuelo(searchTipo)
                                }
                            }
                        } else {
                            emptyList()
                        }
                    }

                    val moBD = suelosList.asSequence().mapNotNull { it.moPercent }.average().takeIf { !it.isNaN() } ?: 0.0
                    val pBD = suelosList.asSequence().mapNotNull { it.p }.average().takeIf { !it.isNaN() } ?: 0.0
                    val kbdSuelo = suelosList.asSequence().mapNotNull { it.k }.average().takeIf { !it.isNaN() } ?: 0.0

                    // Conversión a disponibilidad real (kg/ha) usando fórmulas agronómicas actualizadas.
                    val dispN = moBD / 30.0
                    val dispP = pBD * 2.0
                    val dispK = kbdSuelo * 782.0

                    // --- Paso 2: Determinar y Normalizar el Rendimiento Esperado (a T/ha) ---
                    val rendimientoRaw = if (state.conoceRendimiento == true) {
                        state.rendimientoValue.toDoubleOrNull() ?: cultivo.rendimientoTH
                    } else {
                        cultivo.rendimientoTH
                    }

                    // Normalización obligatoria a Toneladas por Hectárea (T/ha) según la unidad seleccionada.
                    val rendimientoTHA = if (state.conoceRendimiento == true) {
                        when (state.selectedYieldUnit) {
                            YieldUnit.TONELADAS_HA -> rendimientoRaw
                            YieldUnit.TONELADAS_CORDEL -> rendimientoRaw / 24.0
                            YieldUnit.TONELADAS_BESANA -> rendimientoRaw / 3.86
                            YieldUnit.QUINTALES_HA -> rendimientoRaw / 22.0
                            YieldUnit.QUINTALES_CORDEL -> (rendimientoRaw / 22.0) / 24.0
                            YieldUnit.QUINTALES_BESANA -> (rendimientoRaw / 22.0) / 3.86
                        }
                    } else {
                        rendimientoRaw
                    }
                    //
                    // --- Paso 3: Cálculo Inicial de Dosis por Nutriente ---
                    // Coeficientes de aprovechamiento fijos: N=30, P=15, K=40.
                    val coefN = 30.0
                    val coefP = 15.0
                    val coefK = 40.0

                    fun calcDosisBase(nutCultivo: Double, rend: Double, coef: Double, dispSuelo: Double): Double {
                        return ((100.0 * nutCultivo * rend) / coef) - dispSuelo
                    }

                    var dosisN = calcDosisBase(cultivo.n, rendimientoTHA, coefN, dispN)
                    var dosisP = calcDosisBase(cultivo.p, rendimientoTHA, coefP, dispP)
                    var dosisK = calcDosisBase(cultivo.k, rendimientoTHA, coefK, dispK)

                    // Almacenar resultados parciales para visualización detallada.
                    val partials = PartialResults(
                        baseSoilValues = Triple(moBD, pBD, kbdSuelo),
                        dispNPK = Triple(dispN, dispP, dispK),
                        yieldRaw = rendimientoRaw,
                        yieldUnitResId = state.selectedYieldUnit.resId,
                        yieldTHA = rendimientoTHA,
                        cropRequirements = Triple(cultivo.n, cultivo.p, cultivo.k),
                        dosisBaseNPK = Triple(dosisN, dosisP, dosisK),
                        nutrientFertVal = 0.0,
                        nutrientFertName = ""
                    )

                    // --- Paso 4: Evaluación de Resultados y Casos Especiales ---
                    // Caso: El suelo está correctamente abastecido.
                    if (dosisN < 0 && dosisP < 0 && dosisK < 0) {
                        return@withContext CalculationResult(
                            messageResId = cu.edu.inca.abonosverdes.R.string.calc_result_not_needed,
                            isSuccess = true,
                            partialResults = partials
                        )
                    }

                    // Caso: Dosis = 0, se recalcula sin restar disponibilidad del suelo.
                    if (dosisN == 0.0) dosisN = (100.0 * cultivo.n * rendimientoTHA) / coefN
                    if (dosisP == 0.0) dosisP = (100.0 * cultivo.p * rendimientoTHA) / coefP
                    if (dosisK == 0.0) dosisK = (100.0 * cultivo.k * rendimientoTHA) / coefK

                    // Se selecciona el nutriente con el resultado mayor como base para el siguiente paso.
                    val results = mapOf("N" to dosisN, "P" to dosisP, "K" to dosisK)
                    val maxNutriente = results.maxBy { it.value }
                    val dosisMax = maxNutriente.value
                    val nutrienteBase = maxNutriente.key

                    // --- Paso 5: Cálculo de la Dosis a Aplicar según el Fertilizante ---
                    val nutFertVal = when (nutrienteBase) {
                        "N" -> fertilizante.n
                        "P" -> fertilizante.p
                        "K" -> fertilizante.k
                        else -> 0.0
                    } ?: 0.0

                    // Validación Crítica: El fertilizante debe contener el nutriente base.
                    if (nutFertVal <= 0.0) {
                        return@withContext CalculationResult(
                            messageResId = cu.edu.inca.abonosverdes.R.string.calc_result_missing_nutrient,
                            messageArgs = listOf(nutrienteBase),
                            isSuccess = false,
                            nutrienteFaltante = nutrienteBase,
                            partialResults = partials.copy(
                                nutrientFertVal = nutFertVal,
                                nutrientFertName = nutrienteBase
                            )
                        )
                    }

                    var finalMessageResId = cu.edu.inca.abonosverdes.R.string.calc_result_success
                    val finalMessageArgs: List<Any>
                    var dosisAMostrarKg = 0.0

                    if (state.utilizoAbonoVerde == true && state.selectedAbonoVerde != null) {
                        val greenManureNutVal = when (nutrienteBase) {
                            "N" -> state.selectedAbonoVerde.n
                            "P" -> state.selectedAbonoVerde.p
                            "K" -> state.selectedAbonoVerde.k
                            else -> 0.0
                        } ?: 0.0

                        val resultadoFinalDosis = dosisMax - greenManureNutVal

                        if (resultadoFinalDosis <= 0) {
                            finalMessageResId = cu.edu.inca.abonosverdes.R.string.calc_result_green_covers_all
                            finalMessageArgs = listOf(state.selectedAbonoVerde.nomb)
                        } else {
                            finalMessageResId = cu.edu.inca.abonosverdes.R.string.calc_result_green_complement
                            finalMessageArgs = listOf(state.selectedAbonoVerde.nomb)
                            
                            dosisAMostrarKg = if (fertilizante.tipo.contains("Mineral", ignoreCase = true)) {
                                (100.0 * resultadoFinalDosis) / nutFertVal
                            } else {
                                resultadoFinalDosis / (nutFertVal * 10.0)
                            }
                        }
                    } else {
                        // Caso normal (Sin abono verde)
                        val dosisAplicarTons = if (fertilizante.tipo.contains("Mineral", ignoreCase = true)) {
                            dosisMax / (nutFertVal * 10.0)
                        } else {
                            (100.0 * dosisMax) / nutFertVal
                        }
                        dosisAMostrarKg = dosisAplicarTons
                        finalMessageArgs = listOf(dosisAMostrarKg, fertilizante.nomb, cultivo.nombre)
                    }

                    CalculationResult(
                        messageResId = finalMessageResId,
                        messageArgs = finalMessageArgs,
                        isSuccess = true,
                        dosisAplicar = if (dosisAMostrarKg > 0) dosisAMostrarKg else null,
                        nutrienteBase = nutrienteBase,
                        partialResults = partials.copy(
                            nutrientFertVal = nutFertVal,
                            nutrientFertName = nutrienteBase,
                            avContribution = if (state.utilizoAbonoVerde == true) (when (nutrienteBase) {
                                "N" -> state.selectedAbonoVerde?.n
                                "P" -> state.selectedAbonoVerde?.p
                                "K" -> state.selectedAbonoVerde?.k
                                else -> 0.0
                            } ?: 0.0) else null,
                            avName = if (state.utilizoAbonoVerde == true) state.selectedAbonoVerde?.nomb else null,
                            remainingNeed = if (state.utilizoAbonoVerde == true) (dosisMax - (when (nutrienteBase) {
                                "N" -> state.selectedAbonoVerde?.n
                                "P" -> state.selectedAbonoVerde?.p
                                "K" -> state.selectedAbonoVerde?.k
                                else -> 0.0
                            } ?: 0.0)) else null
                        )
                    )
                }
                _uiState.update { it.copy(calculationResult = result) }
            } catch (_: Exception) {
                // Manejo de error silencioso o reporte a Firebase/Sentry en producción
            }
        }
    }

    // --- Validation ---
    /**
     * Determina si el botón de calcular debe estar habilitado basándose en si los pasos requeridos están completos.
     */
    val isCalculateEnabled: StateFlow<Boolean> = _uiState.map { state ->
        val fincaSeleccionada = state.selectedFinca != null
        
        val pasoCultivoComplete = state.selectedCultivo != null
        
        val pasoRendimientoComplete = when (state.conoceRendimiento) {
            false -> true
            true -> state.rendimientoValue.isNotEmpty()
            null -> false
        }

        val pasoNutrienteComplete = fincaSeleccionada || state.selectedNutrienteDisponible != null
        
        val pasoAbonoVerdeComplete = state.utilizoAbonoVerde == false || 
                                    (state.utilizoAbonoVerde == true && state.selectedAbonoVerde != null)

        val pasoSueloComplete = fincaSeleccionada || (state.conoceTipoSuelo == true && state.selectedTipoSuelo != null) || 
                           (state.conoceTipoSuelo == false && state.selectedSoilCharResId != null)
        
        val pasoFertilidadComplete = fincaSeleccionada || state.realizoAnalisisFertilidad != null

        pasoCultivoComplete && pasoRendimientoComplete && pasoNutrienteComplete && pasoAbonoVerdeComplete && pasoSueloComplete && pasoFertilidadComplete
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = false)

    /**
     * Controla la visibilidad de los pasos del formulario basándose en la progresión del usuario.
     */
    val visibleSteps: StateFlow<VisibleSteps> = _uiState.map { state ->
        val fincaSeleccionada = state.selectedFinca != null
        
        // Paso 2: Tipo de Suelo (Si no hay finca)
        val step2Visible = !fincaSeleccionada
        val step2Complete = fincaSeleccionada || when (state.conoceTipoSuelo) {
            true -> state.selectedTipoSuelo != null
            false -> state.selectedSoilCharResId != null
            null -> false
        }
        
        // Paso 3: Análisis de Fertilidad (Si no hay finca, tras suelo)
        val step3Visible = !fincaSeleccionada && step2Complete
        val step3Complete = fincaSeleccionada || state.realizoAnalisisFertilidad != null
        
        // Paso 4: Tipo de Cultivo (Tras Fertilidad o Finca)
        val step4Visible = fincaSeleccionada || step3Complete
        val step4Complete = state.selectedCultivo != null
        
        // Paso 5: Rendimiento de la Finca (Tras Cultivo)
        val step5Visible = step4Complete
        val step5Complete = when (state.conoceRendimiento) {
            false -> true
            true -> state.rendimientoValue.isNotEmpty()
            null -> false
        }
        
        // Paso 6: Tipo de Nutrientes Disponibles (Tras Rendimiento)
        val step6Visible = step5Complete
        val step6Complete = state.selectedNutrienteDisponible != null
        
        // Paso 7: Abono Verde (Tras Nutrientes)
        val step7Visible = step6Complete

        VisibleSteps(
            step2 = step2Visible,
            step3 = step3Visible,
            step4 = step4Visible,
            step5 = step5Visible,
            step6 = step6Visible,
            step7 = step7Visible
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VisibleSteps())
}

/**
 * Representa la visibilidad de cada paso en el flujo de la calculadora.
 */
data class VisibleSteps(
    val step2: Boolean = false,
    val step3: Boolean = false,
    val step4: Boolean = false,
    val step5: Boolean = false,
    val step6: Boolean = false,
    val step7: Boolean = false,
)

/**
 * Estado que contiene todos los valores seleccionados en el formulario de la calculadora.
 */
data class CalculatorUiState(
    val selectedFinca: String? = null,
    val autoLoadedSuelo: Suelos? = null,
    val autoLoadedAbono: AbonoOrganico? = null,

    val conoceTipoSuelo: Boolean? = null,
    val selectedTipoSuelo: String? = null,
    val selectedSoilCharResId: Int? = null,

    val realizoAnalisisFertilidad: Boolean? = null,
    val selectedProvincia: String? = null,
    val selectedMunicipio: String? = null,

    val selectedCultivo: Cultivos? = null,

    val conoceRendimiento: Boolean? = null,
    val rendimientoValue: String = "",
    val selectedYieldUnit: YieldUnit = YieldUnit.TONELADAS_HA,

    val selectedNutrienteDisponible: FertAbOrg? = null,

    val utilizoAbonoVerde: Boolean? = null,
    val selectedAbonoVerde: FertAbOrg? = null,

    val calculationResult: CalculationResult? = null,
)

data class CalculationResult(
    val messageResId: Int,
    val messageArgs: List<Any> = emptyList(),
    val isSuccess: Boolean,
    val dosisAplicar: Double? = null,
    val nutrienteBase: String? = null,
    val nutrienteFaltante: String? = null,
    val partialResults: PartialResults? = null,
)

data class PartialResults(
    val baseSoilValues: Triple<Double, Double, Double>,
    val dispNPK: Triple<Double, Double, Double>,
    val yieldRaw: Double,
    val yieldUnitResId: Int,
    val yieldTHA: Double,
    val cropRequirements: Triple<Double, Double, Double>,
    val dosisBaseNPK: Triple<Double, Double, Double>,
    val nutrientFertVal: Double,
    val nutrientFertName: String,
    val avContribution: Double? = null,
    val avName: String? = null,
    val remainingNeed: Double? = null,
)
