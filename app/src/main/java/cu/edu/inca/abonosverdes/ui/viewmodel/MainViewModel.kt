package cu.edu.inca.abonosverdes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.edu.inca.abonosverdes.data.local.daos.CultivosDao
import cu.edu.inca.abonosverdes.data.local.daos.FertAbOrgDao
import cu.edu.inca.abonosverdes.data.local.daos.SuelosDao
import cu.edu.inca.abonosverdes.data.local.entities.Cultivos
import cu.edu.inca.abonosverdes.data.local.entities.FertAbOrg
import cu.edu.inca.abonosverdes.data.local.entities.Suelos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel principal de la aplicación.
 * Proporciona flujos de datos básicos de cultivos, suelos y fertilizantes para ser utilizados
 * en diferentes partes de la UI que no requieren una lógica de estado compleja.
 *
 * @param cultivosDao DAO para acceder a los datos de cultivos.
 * @param suelosDao DAO para acceder a los datos de suelos.
 * @param fertAbOrgDao DAO para acceder a los datos de fertilizantes y abonos.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    cultivosDao: CultivosDao,
    suelosDao: SuelosDao,
    fertAbOrgDao: FertAbOrgDao
) : ViewModel() {

    /**
     * Flujo de estado que contiene la lista completa de cultivos disponibles.
     */
    val cultivos: StateFlow<List<Cultivos>> = cultivosDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Flujo de estado que contiene la lista completa de registros de suelos.
     */
    val suelos: StateFlow<List<Suelos>> = suelosDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Flujo de estado que contiene la lista de fertilizantes y abonos detallados.
     */
    val fertAbOrg: StateFlow<List<FertAbOrg>> = fertAbOrgDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
