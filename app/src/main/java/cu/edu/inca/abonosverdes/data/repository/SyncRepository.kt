package cu.edu.inca.abonosverdes.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.squareup.moshi.Moshi
import cu.edu.inca.abonosverdes.data.local.daos.*
import cu.edu.inca.abonosverdes.data.local.entities.*
import cu.edu.inca.abonosverdes.data.remote.ApiService
import cu.edu.inca.abonosverdes.data.remote.models.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: cu.edu.inca.abonosverdes.data.local.AppDatabase,
    private val databaseVersionDao: DatabaseVersionDao,
    private val abonoOrganicoDao: AbonoOrganicoDao,
    private val cultivosDao: CultivosDao,
    private val fertAbOrgDao: FertAbOrgDao,
    private val suelosDao: SuelosDao,
    private val moshi: Moshi
) {

    suspend fun sync(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentLocalVersion = databaseVersionDao.getVersion()?.currentVersion ?: 0
            Log.d("SyncRepository", "Starting sync. Current local version: $currentLocalVersion")
            
            try {
                val response = apiService.getDbUpdates(currentLocalVersion)
                if (currentLocalVersion < response.minimumRequiredVersion) {
                    clearAllData()
                }
                response.updates.sortedBy { it.version }.forEach { update ->
                    applyUpdate(update)
                    databaseVersionDao.updateVersion(DatabaseVersion(currentVersion = update.version))
                }
            } catch (e: Exception) {
                Log.e("SyncRepository", "API sync failed", e)
            }
            
            if (cultivosDao.count() == 0) {
                prePopulateSampleData()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SyncRepository", "Critical sync failure", e)
            Result.failure(e)
        }
    }

    private suspend fun prePopulateSampleData() {
        database.clearAndPopulate(
            suelos = getInitialSuelos(),
            cultivos = getInitialCultivos(),
            fertilizantes = getInitialFertilizantes(),
            abonos = getInitialAbonos()
        )
        databaseVersionDao.updateVersion(DatabaseVersion(currentVersion = 1))
    }

    private fun yearToDate(year: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, 0, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun getInitialAbonos(): List<AbonoOrganico> = listOf(
        AbonoOrganico(1, yearToDate(2018), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 21.5, 1.77, 0.49, 0.44),
        AbonoOrganico(2, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 20.2, 1.85, 0.21, 0.21),
        AbonoOrganico(3, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 21.8, 1.98, 0.21, 0.19),
        AbonoOrganico(4, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 22.8, 1.64, 0.24, 0.19),
        AbonoOrganico(5, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 20.8, 1.66, 0.23, 0.2),
        AbonoOrganico(6, yearToDate(2019), "nan", "nan", "Estiercol", 0.0, 0.0, 18.63, 0.96, 0.81, 0.31),
        AbonoOrganico(7, yearToDate(2019), "nan", "nan", "Carboncillo", 0.0, 0.0, 6.09, 0.0, 0.06, 0.12),
        AbonoOrganico(8, yearToDate(2019), "nan", "nan", "Cachaza", 0.0, 0.0, 21.16, 0.61, 1.18, 0.1),
        AbonoOrganico(9, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 18.4, 1.13, 1.0, 0.38),
        AbonoOrganico(10, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 19.5, 1.78, 0.15, 0.21),
        AbonoOrganico(11, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 20.2, 1.85, 0.21, 0.21),
        AbonoOrganico(12, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 21.8, 1.98, 0.21, 0.19),
        AbonoOrganico(13, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 22.8, 1.64, 0.24, 0.19),
        AbonoOrganico(14, yearToDate(2019), "Centro Hab.", "ECOFEL", "Humus", 0.0, 0.0, 20.8, 1.66, 0.23, 0.2),
        AbonoOrganico(15, yearToDate(2019), "Tapaste", "Fca El Mulato", "Cachaza", 0.0, 0.0, 34.6, 1.61, 1.8, 0.2)
    )

    private fun getInitialCultivos(): List<Cultivos> = listOf(
        Cultivos(1, "Frutales", "Aguacate", 10.0, 6.0, 8.0, 6.0, 1.0, 10.0),
        Cultivos(2, "Frutales", "Cafeto", 2.5, null, null, 5.0, 0.45, 6.0),
        Cultivos(3, "Frutales", "Mango", 1.0, null, null, 5.5, 0.2, 6.5),
        Cultivos(4, "Frutales", "Plátano", 25.0, 6.5, 7.5, 12.6, 3.2, 41.4),
        Cultivos(5, "Frutales", "Piña", 50.0, 5.0, 6.0, 2.75, 0.75, 6.87),
        Cultivos(6, "Frutales", "Papaya", 10.0, 7.0, null, 1.8, 0.2, 2.1),
        Cultivos(7, "Hortalizas", "Ajo", 10.0, null, null, 10.0, 3.0, 11.0),
        Cultivos(8, "Hortalizas", "Cebolla", 10.0, 6.0, 7.0, 2.0, 0.3, 2.4),
        Cultivos(9, "Hortalizas", "Pepino", 15.0, 6.0, 8.0, 1.7, 0.5, 2.7),
        Cultivos(10, "Hortalizas", "Pimiento", 16.0, 6.0, 6.5, 4.0, 1.0, 8.0),
        Cultivos(11, "Hortalizas", "Tomate", 20.0, 6.0, 7.0, 2.75, 0.75, 4.0),
        Cultivos(12, "Granos", "Arroz", 5.0, 6.0, 6.5, 13.0, 2.5, 3.0),
        Cultivos(13, "Granos", "Frijol", 1.2, 5.3, 6.0, 32.0, 4.0, 18.0),
        Cultivos(14, "Granos", "Maíz", 2.0, 6.0, 7.0, 15.0, 3.0, 4.0),
        Cultivos(15, "Granos", "Maní", 2.0, 5.0, 6.0, 44.0, 3.0, 11.0),
        Cultivos(16, "Granos", "Soja", 3.0, null, null, 66.0, 6.0, 19.0),
        Cultivos(17, "Raíces y Tubérculos", "Boniato", 25.0, 5.0, 7.0, 3.5, 1.0, 5.5),
        Cultivos(18, "Raíces y Tubérculos", "Papa", 20.0, 4.8, 5.4, 4.12, 0.6, 8.44),
        Cultivos(19, "Raíces y Tubérculos", "Yuca", 25.0, 6.0, 8.0, 1.71, 0.3, 7.43),
        Cultivos(20, "Raíces y Tubérculos", "Malanga", 20.0, null, null, 1.2, 0.4, 3.6)
    )

    private fun getInitialFertilizantes(): List<FertAbOrg> = listOf(
        FertAbOrg(1, "Fertilizantes minerales", "Urea", null, null, null, 46.0, null, null),
        FertAbOrg(2, "Fertilizantes minerales", "Nitrato de amonio", null, null, null, 33.5, null, null),
        FertAbOrg(3, "Fertilizantes minerales", "Sulfato de amonio", null, null, null, 20.5, null, null),
        FertAbOrg(4, "Fertilizantes minerales", "Roca fosfórica", null, null, null, null, 36.0, null),
        FertAbOrg(5, "Fertilizantes minerales", "Superfosfato simple", null, null, null, null, 20.0, null),
        FertAbOrg(6, "Fertilizantes minerales", "Superfosfato triple", null, null, null, null, 46.0, null),
        FertAbOrg(7, "Fertilizantes minerales", "Fosfato diamónico (DAP)", null, null, null, 20.0, 52.0, null),
        FertAbOrg(8, "Fertilizantes minerales", "Cloruro de potasio", null, null, null, null, null, 60.0),
        FertAbOrg(9, "Fertilizantes minerales", "Nitrato de potasio", null, null, null, 13.0, null, 46.0),
        FertAbOrg(10, "Fertilizantes minerales", "Sulfato de potasio", null, null, null, null, null, 50.0),
        FertAbOrg(11, "Organo-minerales", "Nerea-Plus", null, null, null, 5.0, 2.0, 2.0),
        FertAbOrg(12, "Organo-minerales", "Agromena", null, null, null, 2.0, 7.0, 2.0),
        FertAbOrg(13, "Abonos orgánicos", "Estiércol vacuno", 80.0, "20/1", 11.5, 0.33, 0.23, 0.72),
        FertAbOrg(14, "Abonos orgánicos", "Estiércol ovino/caprino", 61.6, "15/1", 21.12, 0.82, 0.21, 0.84),
        FertAbOrg(15, "Abonos orgánicos", "Estiércol caballo", 67.4, "30/1", 17.93, 0.34, 0.13, 0.35),
        FertAbOrg(16, "Abonos orgánicos", "Estiércol porcino", 72.8, "19/1", 15.0, 0.45, 0.2, 0.6),
        FertAbOrg(17, "Abonos orgánicos", "Gallinaza", 75.0, "22/1", 15.54, 0.7, 1.03, 0.49),
        FertAbOrg(18, "Abonos orgánicos", "Compost", 75.0, "10/1", 13.25, 0.5, 0.26, 0.53),
        FertAbOrg(19, "Abonos orgánicos", "Humus de lombriz", 61.3, "10/1", 14.6, 0.83, 0.42, 0.25),
        FertAbOrg(20, "Abonos orgánicos", "Cachaza cruda", 54.5, "15/1", 28.9, 1.11, 1.11, 0.15),
        FertAbOrg(21, "Abonos orgánicos", "Guano de murciélago", 23.0, "8/1", 13.2, 0.96, 12.0, 0.4),
        FertAbOrg(22, "Abonos orgánicos", "Turba", 70.0, "42/1", 14.4, 0.2, 0.17, 0.12),
        FertAbOrg(23, "Abonos orgánicos", "Estiércol bocashi", null, null, null, null, null, null),
        FertAbOrg(id = 24, tipo = "Abonos Verdes", nomb = "Crotalaria", humPercent = null, relCN = null, null, n = 255.00, p = 21.00, k = 92.00),
        FertAbOrg(id = 25, tipo = "Abonos Verdes", nomb = "Canavalia", humPercent = null, relCN = null, null, n = 153.00, p = 11.00, k = 44.00),
        FertAbOrg(id = 26, tipo = "Abonos Verdes", nomb = "Frijol Terciopelo", humPercent = null, relCN = null, null, n = 149.00, p = 8.00, k = 44.00),
        FertAbOrg(id = 27, tipo = "Abonos Verdes", nomb = "Frijol Caballero", humPercent = null, relCN = null, null, n = 121.00, p = 10.00, k = 52.00),
        FertAbOrg(id = 28, tipo = "Abonos Verdes", nomb = "Gandul", humPercent = null, relCN = null, null, n = 135.00, p = 13.00, k = 67.00)
    )

    private fun getInitialSuelos(): List<Suelos> = mutableListOf<Suelos>().apply {
        addAll(getInitialSuelosChunk1())
        addAll(getInitialSuelosChunk2())
        addAll(getInitialSuelosChunk3())
        addAll(getInitialSuelosChunk4())
        addAll(getInitialSuelosChunk5())
        addAll(getInitialSuelosChunk6())
        addAll(getInitialSuelosChunk7())
        addAll(getInitialSuelosChunk8())
        addAll(getInitialSuelosChunk9())
        addAll(getInitialSuelosChunk10())
        addAll(getInitialSuelosChunk11())
        addAll(getInitialSuelosChunk12())
        addAll(getInitialSuelosChunk13())
        addAll(getInitialSuelosChunk14())
        addAll(getInitialSuelosChunk15())
        addAll(getInitialSuelosChunk16())
        addAll(getInitialSuelosChunk17())
        addAll(getInitialSuelosChunk18())
    }

    private fun getInitialSuelosChunk1(): List<Suelos> = listOf(
        Suelos(1, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.9, 3.06, 11.5, 2.5, 0.58, 0.02, 174.0),
        Suelos(2, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.8, 2.99, 13.5, 2.5, 0.77, 0.0, 167.0),
        Suelos(3, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.8, 3.02, 12.0, 2.5, 0.63, 0.0, 182.0),
        Suelos(4, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.7, 2.76, 14.5, 2.5, 0.63, 0.03, 222.0),
        Suelos(5, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.5, 4.24, 22.0, 4.0, 0.54, 0.13, 2419.0),
        Suelos(6, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.7, 3.7, 23.0, 1.0, 0.33, 0.09, 2510.0),
        Suelos(7, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.5, 4.14, 21.0, 3.0, 0.29, 0.11, 2138.0),
        Suelos(8, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.6, 4.04, 21.0, 4.5, 0.49, 0.11, 2080.0),
        Suelos(9, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.6, 4.31, 22.0, 3.5, 0.3, 0.14, 2399.0),
        Suelos(10, "Mayabeque", "San Jose", "Area Central", "Ferralitico Rojo Lixiviado", 7.6, 4.17, 22.5, 3.0, 0.36, 0.1, 2224.0)
    )

    private fun getInitialSuelosChunk2(): List<Suelos> = listOf(
        Suelos(11, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.8, 3.02, 33.5, 10.5, 0.47, 0.1, 63.0),
        Suelos(12, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.7, 2.92, 30.0, 7.0, 0.44, 0.15, 54.0),
        Suelos(13, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.6, 3.32, 42.0, 4.5, 0.93, 0.27, 242.0),
        Suelos(14, "Mayabeque", "San Jose", "Fca Rosa Mondrejo", "Ferralitico Rojo Lixiviado", 7.7, 3.18, 9.0, 2.0, 0.37, 0.03, 88.0),
        Suelos(15, "Mayabeque", "San Jose", "Fca Rosa Mondrejo", "Ferralitico Rojo Lixiviado", 7.0, 2.85, 7.5, 1.5, 0.2, 0.0, 24.0),
        Suelos(16, "Mayabeque", "San Jose", "Fca Rosa Mondrejo", "Ferralitico Rojo Lixiviado", 6.8, 3.12, 9.5, 4.0, 0.44, 0.0, 137.0),
        Suelos(17, "Mayabeque", "San Jose", "Fca Rosa Mondrejo", "Ferralitico Rojo Lixiviado", 6.5, 2.76, 5.5, 3.0, 0.28, 0.0, 22.0),
        Suelos(18, "Mayabeque", "San Jose", "Fca Rosa Mondrejo", "Ferralitico Rojo Lixiviado", 6.2, 2.89, 6.5, 2.5, 0.26, 0.02, 9.0),
        Suelos(19, "Mayabeque", "San Jose", "Fca Rosa Mondrejo", "Ferralitico Rojo Lixiviado", 6.2, 3.05, 5.5, 3.5, 0.3, 0.0, 6.0)
    )

    private fun getInitialSuelosChunk3(): List<Suelos> = listOf(
        Suelos(20, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 7.9, 3.22, 59.5, 4.5, 0.82, 0.38, 117.0),
        Suelos(21, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 7.9, 2.92, 60.0, 5.0, 0.75, 0.28, 214.0),
        Suelos(22, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 8.0, 3.02, 53.0, 11.0, 0.74, 0.27, 173.0),
        Suelos(23, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 7.9, 3.02, 43.5, 16.0, 0.82, 0.27, 89.0),
        Suelos(24, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 8.0, 2.73, 42.5, 17.5, 0.78, 0.26, 124.0),
        Suelos(25, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 8.0, 2.89, 58.0, 6.5, 0.88, 0.29, 204.0),
        Suelos(26, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 8.1, 2.89, 52.0, 10.5, 0.89, 0.3, 87.0),
        Suelos(27, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 8.1, 2.79, 54.0, 8.5, 0.84, 0.32, 147.0),
        Suelos(28, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 8.0, 2.59, 53.5, 10.0, 0.88, 0.27, 205.0),
        Suelos(29, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 8.0, 2.73, 56.0, 8.5, 0.84, 0.26, 161.0),
        Suelos(30, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 8.2, 2.76, 43.5, 21.0, 0.9, 0.23, 120.0),
        Suelos(31, "Mayabeque", "Jaruco", "Los Angeles", "Pardo con carbonatos", 8.1, 3.88, 58.0, 4.5, 0.87, 0.21, 138.0)
    )

    private fun getInitialSuelosChunk4(): List<Suelos> = listOf(
        Suelos(32, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.7, 3.39, 13.5, 9.0, 0.33, 0.05, 102.0),
        Suelos(33, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.6, 3.29, 24.0, 11.5, 0.62, 0.12, 124.0),
        Suelos(34, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.6, 3.19, 15.0, 10.0, 0.45, 0.1, 190.0),
        Suelos(35, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.7, 2.99, 20.5, 8.5, 0.52, 0.13, 97.0),
        Suelos(36, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.8, 3.23, 33.5, 8.0, 0.58, 0.12, 150.0),
        Suelos(37, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.8, 3.19, 21.5, 8.0, 0.52, 0.12, 125.0),
        Suelos(38, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 8.0, 3.13, 27.0, 10.5, 0.52, 0.09, 141.0),
        Suelos(39, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.9, 3.19, 21.5, 11.0, 0.54, 0.06, 74.0),
        Suelos(40, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.5, 3.26, 23.0, 8.5, 0.52, 0.08, 119.0),
        Suelos(41, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.6, 3.9, 18.5, 9.0, 0.53, 0.11, 242.0),
        Suelos(42, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.7, 3.29, 19.5, 8.0, 0.51, 0.08, 176.0),
        Suelos(43, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.7, 3.02, 15.0, 7.0, 0.53, 0.04, 105.0),
        Suelos(44, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.5, 2.89, 13.5, 9.5, 0.51, 0.05, 62.0),
        Suelos(45, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.8, 3.13, 32.0, 9.5, 0.63, 0.11, 165.0)
    )

    private fun getInitialSuelosChunk5(): List<Suelos> = listOf(
        Suelos(46, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.9, 3.54, 11.5, 5.0, 0.53, 0.06, 470.0),
        Suelos(47, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.9, 3.23, 10.5, 4.5, 0.53, 0.06, 372.0),
        Suelos(48, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.9, 3.3, 12.5, 4.0, 0.6, 0.08, 484.0),
        Suelos(49, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.8, 3.23, 11.5, 2.0, 0.48, 0.03, 510.0),
        Suelos(50, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.7, 3.37, 10.5, 4.5, 0.65, 0.05, 455.0),
        Suelos(51, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.7, 3.34, 10.00, 4.00, 0.56, 0.04, 346.00),
        Suelos(52, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.7, 3.41, 12.5, 9.5, 0.67, 0.06, 484.00),
        Suelos(53, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.7, 3.41, 11.5, 4.00, 0.68, 0.05, 490.00),
        Suelos(54, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.7, 3.54, 11.5, 2.00, 0.69, 0.04, 492.00),
        Suelos(55, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.6, 3.44, 10.00, 3.00, 0.77, 0.05, 404.00),
        Suelos(56, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.7, 3.48, 9.5, 4.5, 0.8, 0.07, 524.00),
        Suelos(57, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.6, 3.3, 10.5, 4.00, 0.74, 0.05, 495.00),
        Suelos(58, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.6, 3.44, 11.00, 3.00, 0.96, 0.06, 467.00)
    )

    private fun getInitialSuelosChunk6(): List<Suelos> = listOf (
        Suelos(59, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.7, 3.23, 10.5, 2.00, 0.74, 0.06, 372.00),
        Suelos(60, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.6, 3.41, 9.00, 6.00, 0.73, 0.05, 533.00),
        Suelos(61, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 7.6, 3.34, 9.5, 3.5, 0.73, 0.06, 507.00),
        Suelos(62, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 7.9, 3.03, 39.00, 2.00, 0.77, 0.00, 24.00),
        Suelos(63, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 8.00, 2.86, 36.00, 3.00, 0.63, 0.00, 14.00),
        Suelos(64, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 7.6, 2.73, 34.00, 5.00, 0.63, 0.03, 0.00),
        Suelos(65, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 7.3, 3.84, 48.00, 3.5, 0.76, 0.11, 83.00),
        Suelos(66, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 7.7, 3.5, 47.5, 4.00, 0.69, 0.12, 10.00),
        Suelos(67, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 7.4, 4.78, 42.5, 4.5, 0.74, 0.07, 1.00),
        Suelos(68, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 7.5, 4.93, 48.00, 1.2, 0.69, 0.08, 0.00),
        Suelos(69, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 7.3, 4.54, 48.5, 6.5, 1.06, 0.14, 55.00),
        Suelos(70, "Mayabeque", "Jaruco", "Popo", "Pardo con carbonatos", 7.7, 3.36, 37.5, 2.5, 1.06, 0.16, 14.00),
        Suelos(71, "Mayabeque", "Nueva Paz", "Bagay", "Fersialítico", 7.8, 3.2, 21.00, 12.5, 0.71, 0.11, 60.00)
    )

    private fun getInitialSuelosChunk7(): List<Suelos> = listOf (
        Suelos(72, "Mayabeque", "Nueva Paz", "Bagay", "Fersialítico", 7.8, 3.06, 23.5, 9.00, 0.72, 0.14, 44.00),
        Suelos(73, "Mayabeque", "Area Central", "Area de dpto MAS", "Ferralitico Rojo Lixiviado", 7.6, 3.41, 11.5, 4.00, 0.71, 0.05, 529.00),
        Suelos(74, "Mayabeque", "Area Central", "Area de dpto MAS", "Ferralitico Rojo Lixiviado", 7.7, 3.51, 12.00, 4.00, 0.47, 0.06, 552.00),
        Suelos(75, "Mayabeque", "Area Central", "Area de dpto MAS", "Ferralitico Rojo Lixiviado", 7.8, 3.23, 11.00, 3.00, 0.5, 0.06, 548.00),
        Suelos(76, "Mayabeque", "Area Central", "Area de dpto MAS", "Ferralitico Rojo Lixiviado", 7.8, 3.27, 12.5, 4.5, 0.4, 0.03, 506.00),
        Suelos(77, "Mayabeque", "Area Central", "Area de dpto MAS", "Ferralitico Rojo Lixiviado", 7.8, 3.2, 10.5, 6.5, 0.53, 0.06, 534.00),
        Suelos(78, "Mayabeque", "Area Central", "Area de dpto MAS", "Ferralitico Rojo Lixiviado", 7.8, 3.3, 9.5, 6.00, 0.51, 0.09, 520.00),
        Suelos(79, "Mayabeque", "Area Central", "Area de dpto MAS", "Ferralitico Rojo Lixiviado", 7.4, 3.99, 19.00, 3.5, 0.95, 0.12, 492.00),
        Suelos(80, "Mayabeque", "Area Central", "Area de dpto MAS", "Ferralitico Rojo Lixiviado", 7.6, 4.3, 17.5, 5.00, 0.98, 0.06, 459.00),
        Suelos(81, "Mayabeque", "Area Central", "Area de dpto MAS", "Ferralitico Rojo Lixiviado", 7.6, 4.4, 21.5, 1.5, 0.96, 0.1, 364.00),
        Suelos(82, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.4, 4.13, 15.5, 4.5, 0.7, 0.09, 984.00),
        Suelos(83, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.8, 3.37, 14.00, 1.5, 0.58, 0.09, 775.00),
        Suelos(84, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.8, 3.48, 9.5, 4.00, 0.37, 0.12, 631.00)
    )

    private fun getInitialSuelosChunk8(): List<Suelos> = listOf (
        Suelos(85, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.9, 3.48, 9.5, 5.00, 0.59, 0.09, 640.00),
        Suelos(86, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.00, 3.61, 10.5, 3.00, 0.92, 0.14, 645.00),
        Suelos(87, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.00, 3.45, 13.5, 3.00, 0.91, 0.15, 1040.00),
        Suelos(88, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.1, 3.34, 9.5, 4.00, 0.91, 0.11, 798.00),
        Suelos(89, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 3.16, 10.5, 2.5, 0.92, 0.12, 766.00),
        Suelos(90, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 3.23, 11.00, 3.5, 0.91, 0.1, 738.00),
        Suelos(91, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 3.13, 10.00, 3.00, 0.76, 0.09, 677.00),
        Suelos(92, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.1, 3.51, 15.00, 2.5, 0.58, 0.06, 775.00),
        Suelos(93, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 3.09, 10.00, 3.00, 0.54, 0.06, 761.00),
        Suelos(94, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.1, 3.2, 10.5, 3.5, 0.59, 0.07, 659.00),
        Suelos(95, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 3.3, 11.00, 2.5, 0.52, 0.08, 617.00),
        Suelos(96, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 3.23, 9.5, 4.00, 0.58, 0.05, 618.00),
        Suelos(97, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 2.82, 14.5, 2.5, 0.91, 0.07, 780.00)
    )

    private fun getInitialSuelosChunk9(): List<Suelos> = listOf (
        Suelos(98, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.1, 2.78, 12.5, 2.00, 0.76, 0.07, 766.00),
        Suelos(99, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 2.49, 10.5, 1.00, 0.75, 0.08, 645.00),
        Suelos(100, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 2.47, 10.5, 1.00, 0.68, 0.06, 655.00),
        Suelos(101, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 2.54, 11.00, 2.00, 0.82, 0.08, 659.00),
        Suelos(102, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 2.61, 13.5, 1.5, 0.88, 0.08, 794.00),
        Suelos(103, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 2.54, 10.5, 4.00, 0.8, 0.08, 715.00),
        Suelos(104, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 2.44, 9.5, 2.00, 0.91, 0.08, 650.00),
        Suelos(105, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.3, 2.5, 10.00, 2.5, 0.81, 0.09, 622.00),
        Suelos(106, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.2, 2.37, 10.5, 2.00, 0.8, 0.09, 603.00),
        Suelos(107, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.6, 3.29, 24.00, 11.5, 0.62, 0.12, 124.00),
        Suelos(108, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.6, 3.19, 15.00, 10.00, 0.45, 0.1, 190.00),
        Suelos(109, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.7, 2.99, 20.5, 8.5, 0.52, 0.13, 97.00),
        Suelos(110, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.8, 3.23, 33.5, 8.00, 0.58, 0.12, 150.00)
    )

    private fun getInitialSuelosChunk10(): List<Suelos> = listOf (
        Suelos(111, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.8, 3.19, 21.5, 8.00, 0.52, 0.12, 125.00),
        Suelos(112, "Mayabeque", "Güines", "Fca Vasallo", "Ferrálico", 8.00, 3.13, 27.00, 10.5, 0.52, 0.09, 141.00),
        Suelos(113, "Mayabeque", "Güines", "Fca Vasallo", "Ferrálico", 7.9, 3.19, 21.5, 11.00, 0.54, 0.06, 74.00),
        Suelos(114, "Mayabeque", "Güines", "Fca Vasallo", "Ferrálico", 7.6, 3.29, 24.00, 11.5, 0.62, 0.12, 124.00),
        Suelos(115, "Mayabeque", "Güines", "Fca Vasallo", "Ferrálico", 7.6, 3.19, 15.00, 10.00, 0.45, 0.1, 190.00),
        Suelos(116, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.6, 3.66, 30.00, 3.00, 0.34, 0.05, 36.00),
        Suelos(117, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.6, 4.00, 29.00, 4.5, 0.35, 0.05, 28.00),
        Suelos(118, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.8, 4.55, 49.00, 5.00, 0.57, 0.18, 33.00),
        Suelos(119, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.5, 3.55, 35.5, 4.00, 0.43, 0.13, 20.00),
        Suelos(120, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.4, 3.97, 38.00, 2.00, 0.62, 0.07, 39.00),
        Suelos(121, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.8, 3.97, 50.00, 2.00, 0.92, 0.1, 33.00),
        Suelos(122, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.6, 3.46, 36.5, 2.00, 0.48, 0.08, 128.00),
        Suelos(123, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.5, 3.59, 45.5, 5.5, 0.67, 0.12, 107.00),
        Suelos(124, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.8, 3.59, 52.00, 2.5, 0.7, 0.1, 79.00)
    )

    private fun getInitialSuelosChunk11(): List<Suelos> = listOf (
        Suelos(125, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.3, 3.3, 30.00, 9.5, 0.42, 0.05, 83.00),
        Suelos(126, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.3, 3.59, 54.5, 4.00, 0.58, 0.2, 28.00),
        Suelos(127, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.3, 3.14, 53.5, 6.5, 0.69, 0.22, 18.00),
        Suelos(128, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.7, 3.76, 41.00, 1.5, 0.47, 0.08, 33.00),
        Suelos(129, "Mayabeque", "Jaruco", "Fca San Miguel", "Pardo con carbonatos", 7.8, 4.31, 38.5, 3.5, 0.51, 0.08, 24.00),
        Suelos(130, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.7, 3.23, 14.5, 7.00, 0.2, 0.07, 125.00),
        Suelos(131, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.7, 3.38, 21.00, 4.00, 0.2, 0.07, 120.00),
        Suelos(132, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.7, 3.09, 14.00, 4.00, 0.18, 0.04, 120.00),
        Suelos(132, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.7, 3.09, 14.00, 4.00, 0.18, 0.04, 120.00),
        Suelos(133, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.8, 3.12, 8.5, 7.00, 0.13, 0.02, 98.00),
        Suelos(134, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.7, 3.59, 8.5, 6.00, 0.17, 0.04, 97.00),
        Suelos(135, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.6, 3.3, 11.5, 9.5, 0.27, 0.07, 108.00),
        Suelos(136, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.5, 2.94, 17.5, 2.5, 0.23, 0.07, 98.00),
        Suelos(137, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.7, 3.45, 13.5, 8.00, 0.25, 0.08, 102.00),
        Suelos(138, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.6, 3.01, 14.00, 5.5, 0.24, 0.06, 94.00)
    )

    private fun getInitialSuelosChunk12(): List<Suelos> = listOf (
        Suelos(139, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.7, 3.63, 15.00, 1.5, 0.25, 0.05, 94.00),
        Suelos(140, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.6, 3.27, 9.00, 2.5, 0.14, 0.03, 101.00),
        Suelos(141, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.5, 3.09, 14.5, 6.00, 0.2, 0.07, 95.00),
        Suelos(142, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.5, 3.52, 12.5, 3.00, 0.25, 0.07, 90.00),
        Suelos(143, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.4, 3.45, 8.5, 4.00, 0.15, 0.04, 93.00),
        Suelos(144, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.3, 3.27, 6.5, 3.5, 0.14, 0.03, 109.00),
        Suelos(145, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.5, 3.27, 17.5, 5.5, 0.25, 0.08, 109.00),
        Suelos(146, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.3, 3.71, 12.5, 3.00, 0.64, 0.05, 607.00),
        Suelos(147, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.2, 3.4, 12.00, 3.5, 0.44, 0.05, 678.00),
        Suelos(148, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.2, 3.54, 9.5, 6.5, 0.46, 0.05, 605.00),
        Suelos(149, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.1, 3.86, 11.00, 6.5, 0.37, 0.04, 587.00),
        Suelos(150, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.2, 3.25, 12.00, 6.5, 0.43, 0.08, 564.00),
        Suelos(151, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.1, 3.36, 11.5, 5.00, 0.36, 0.06, 629.00)
    )

    private fun getInitialSuelosChunk13(): List<Suelos> = listOf (
        Suelos(152, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.1, 3.61, 11.00, 4.00, 0.41, 0.05, 579.00),
        Suelos(153, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.3, 3.36, 13.00, 2.00, 0.6, 0.05, 542.00),
        Suelos(154, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.2, 3.82, 12.5, 3.5, 0.5, 0.05, 700.00),
        Suelos(155, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.4, 3.47, 12.5, 3.00, 0.51, 0.05, 508.00),
        Suelos(156, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.3, 3.33, 12.00, 4.00, 0.42, 0.05, 440.00),
        Suelos(157, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.3, 3.15, 11.00, 5.00, 0.38, 0.04, 586.00),
        Suelos(158, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.4, 4.1, 11.5, 4.5, 0.51, 0.05, 679.00),
        Suelos(159, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.2, 3.47, 11.00, 3.5, 0.53, 0.05, 476.00),
        Suelos(160, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.00, 3.25, 11.00, 4.00, 0.56, 0.06, 507.00),
        Suelos(161, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 6.00, 3.61, 13.00, 4.00, 0.49, 0.05, 527.00),
        Suelos(162, "Mayabeque", "San Jose", "Fca El mulato", "Ferralitico Rojo Lixiviado", 5.7, 4.53, 204.00, 4.00, 0.23, 0.12, 204.00),
        Suelos(163, "Mayabeque", "San Jose", "Fca El mulato", "Ferralitico Rojo Lixiviado", 5.4, 4.77, 169.00, 5.00, 0.27, 0.13, 169.00),
        Suelos(164, "Mayabeque", "San Jose", "Fca El mulato", "Ferralitico Rojo Lixiviado", 5.4, 5.13, 178.00, 3.00, 0.29, 0.13, 178.00)
    )

    private fun getInitialSuelosChunk14(): List<Suelos> = listOf (
        Suelos(165, "Mayabeque", "San Jose", "Fca El mulato", "Ferralitico Rojo Lixiviado", 5.8, 5.48, 271.00, 2.00, 0.39, 0.12, 271.00),
        Suelos(166, "Mayabeque", "San Jose", "Fca El mulato", "Ferralitico Rojo Lixiviado", 5.5, 3.46, 143.00, 3.00, 0.2, 0.13, 143.00),
        Suelos(167, "Mayabeque", "San Jose", "Fca El mulato", "Ferralitico Rojo Lixiviado", 5.4, 5.01, 180.00, 1.5, 0.25, 0.13, 180.00),
        Suelos(168, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.1, 4.2, 43.5, 4.00, 0.613333, 0.173333, 893.00),
        Suelos(169, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.00, 4.1, 46.5, 6.00, 0.613333, 0.173333, 1100.00),
        Suelos(170, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 6.8, 3.93, 42.00, 6.00, 0.613333, 0.173333, 1356.00),
        Suelos(171, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.5, 3.72, 41.5, 3.5, 0.613333, 0.173333, 714.00),
        Suelos(172, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.1, 4.21, 41.1, 1.5, 0.613333, 0.173333, 377.00),
        Suelos(173, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.00, 4.31, 41.5, 4.5, 0.613333, 0.173333, 354.00),
        Suelos(174, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.00, 3.34, 39.00, 4.5, 0.613333, 0.173333, 458.00),
        Suelos(175, "Mayabeque", "San Jose", "Fca La Chiveria", "Fersialítico", 7.1, 3.72, 37.5, 6.5, 0.613333, 0.173333, 403.00),
        Suelos(176, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.8, 1.5, 11.5, 3.5, 0.203125, 0.055625, 123.00),
        Suelos(177, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.8, 3.00, 16.00, 7.5, 0.203125, 0.055625, 134.00)
    )

    private fun getInitialSuelosChunk15(): List<Suelos> = listOf (
        Suelos(178, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.5, 2.67, 17.5, 2.5, 0.203125, 0.055625, 141.00),
        Suelos(179, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.4, 2.35, 15.00, 5.00, 0.203125, 0.055625, 121.00),
        Suelos(180, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.3, 2.35, 18.00, 8.00, 0.203125, 0.055625, 141.00),
        Suelos(181, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.2, 2.58, 13.00, 5.00, 0.203125, 0.055625, 132.00),
        Suelos(182, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.5, 2.67, 17.00, 4.00, 0.203125, 0.055625, 173.00),
        Suelos(183, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.7, 2.38, 19.00, 2.5, 0.203125, 0.055625, 161.00),
        Suelos(184, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.8, 2.51, 21.00, 2.5, 0.203125, 0.055625, 144.00),
        Suelos(185, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.7, 2.35, 19.5, 1.00, 0.203125, 0.055625, 125.00),
        Suelos(186, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.1, 3.45, 18.00, 5.00, 0.203125, 0.055625, 214.00),
        Suelos(187, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.7, 3.61, 35.00, 2.00, 0.203125, 0.055625, 146.00),
        Suelos(188, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.6, 3.38, 31.5, 5.00, 0.203125, 0.055625, 132.00),
        Suelos(189, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 6.8, 3.38, 32.5, 10.00, 0.203125, 0.055625, 101.00),
        Suelos(190, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.1, 2.95, 25.00, 6.00, 0.203125, 0.055625, 159.00)
    )

    private fun getInitialSuelosChunk16(): List<Suelos> = listOf (
        Suelos(191, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.1, 2.95, 23.00, 2.00, 0.203125, 0.055625, 132.00),
        Suelos(192, "Mayabeque", "Nueva Paz", "Sta Catalina,Los Palos", "Ferralitico Rojo Lixiviado", 7.1, 3.48, 25.00, 4.5, 0.203125, 0.055625, 184.00),
        Suelos(193, "Mayabeque", "Jaruco", "Fca Cabrales", "Pardo con carbonatos", 7.3, 6.26, 26.00, 1.5, 0.776, 0.077, 198.00),
        Suelos(194, "Mayabeque", "Jaruco", "Fca Cabrales", "Pardo con carbonatos", 7.2, 6.83, 25.5, 3.5, 0.776, 0.077, 45.00),
        Suelos(195, "Mayabeque", "Jaruco", "Fca Cabrales", "Pardo con carbonatos", 7.2, 5.83, 23.00, 4.00, 0.776, 0.077, 47.00),
        Suelos(196, "Mayabeque", "Jaruco", "Fca Cabrales", "Pardo con carbonatos", 7.3, 4.81, 21.00, 4.00, 0.776, 0.077, 40.00),
        Suelos(197, "Mayabeque", "Jaruco", "Fca Cabrales", "Pardo con carbonatos", 7.4, 5.37, 21.00, 2.5, 0.776, 0.077, 47.00),
        Suelos(198, "Mayabeque", "Jaruco", "Fca Cabrales", "Pardo con carbonatos", 7.3, 4.34, 28.5, 2.5, 0.776, 0.077, 40.00),
        Suelos(199, "Mayabeque", "Jaruco", "Fca Cabrales", "Pardo con carbonatos", 7.3, 3.78, 19.5, 2.00, 0.776, 0.077, 42.00),
        Suelos(200, "Mayabeque", "Jaruco", "Fca Cabrales", "Pardo con carbonatos", 7.3, 4.04, 17.5, 3.5, 0.776, 0.077, 53.00),
        Suelos(201, "Mayabeque", "Jaruco", "Fca Cabrales", "Pardo con carbonatos", 7.1, 3.71, 21.00, 3.5, 0.776, 0.077, 45.00),
        Suelos(202, "Mayabeque", "San Jose", "Fca. El Lucero", "Pardo con carbonatos", 6.8, 3.88, 39.00, 2.00, 0.776, 0.077, 125.00),
        Suelos(203, "Mayabeque", "San Jose", "Fca. El Lucero", "Pardo con carbonatos", 6.3, 4.08, 32.5, 10.00, 0.776, 0.077, 78.00)
    )

    private fun getInitialSuelosChunk17(): List<Suelos> = listOf (
        Suelos(204, "Mayabeque", "San Jose", "Fca. El Lucero", "Pardo con carbonatos", 6.7, 2.35, 35.9, 18.00, 0.776, 0.077, 191.00),
        Suelos(205, "Mayabeque", "San Jose", "Fca. El Lucero", "Pardo con carbonatos", 7.00, 2.58, 39.00, 6.00, 0.776, 0.077, 191.00),
        Suelos(206, "Artemisa", "Güira", "San Miguel", "Ferralitico Rojo Lixiviado", 6.7, 4.57, 12.00, 5.5, 0.553571, 0.107857, 217.00),
        Suelos(207, "Artemisa", "Güira", "San Miguel", "Ferralitico Rojo Lixiviado", 6.8, 3.45, 13.5, 5.00, 0.553571, 0.107857, 260.00),
        Suelos(208, "Artemisa", "Güira", "San Miguel", "Ferralitico Rojo Lixiviado", 6.8, 3.21, 11.5, 4.00, 0.553571, 0.107857, 231.00),
        Suelos(209, "Artemisa", "Güira", "San Miguel", "Ferralitico Rojo Lixiviado", 6.8, 3.31, 9.00, 3.5, 0.553571, 0.107857, 184.00),
        Suelos(210, "Artemisa", "Güira", "San Miguel", "Ferralitico Rojo Lixiviado", 6.8, 2.88, 9.00, 4.00, 0.553571, 0.107857, 186.00),
        Suelos(211, "Artemisa", "Güira", "San Miguel", "Ferralitico Rojo Lixiviado", 6.6, 3.38, 13.00, 2.00, 0.553571, 0.107857, 155.00),
        Suelos(212, "Artemisa", "Güira", "San Miguel", "Ferralitico Rojo Lixiviado", 6.6, 3.35, 11.5, 1.5, 0.553571, 0.107857, 186.00),
        Suelos(213, "Artemisa", "Güira", "San Miguel", "Ferralitico Rojo Lixiviado", 6.8, 3.25, 10.5, 2.5, 0.553571, 0.107857, 101.00),
        Suelos(214, "Artemisa", "Güira", "San Miguel", "Ferralitico Rojo Lixiviado", 6.6, 3.58, 10.5, 3.5, 0.553571, 0.107857, 130.00),
        Suelos(215, "Artemisa", "Güira", "Gavilan", "Ferralitico Rojo Lixiviado", 7.8, 3.1, 11.5, 5.5, 0.711111, 0.0911111, 146.25),
        Suelos(216, "Artemisa", "Güira", "Fca Rebeca", "Ferralitico Rojo Lixiviado", 7.00, 1.79, 12.00, 4.00, 0.711111, 0.0911111, 146.25),
        Suelos(217, "Artemisa", "Güira", "Fca Rebeca", "Ferralitico Rojo Lixiviado", 7.5, 2.76, 11.6, 6.4, 0.711111, 0.0911111, 146.25)
    )

    private fun getInitialSuelosChunk18(): List<Suelos> = listOf (
        Suelos(218, "Artemisa", "Güira", "Fca Rebeca", "Ferralitico Rojo Lixiviado", 7.5, 2.72, 10.00, 6.5, 0.711111, 0.0911111, 146.25),
        Suelos(219, "Mayabeque", "Jaruco", "Fca La Pastora", "Fersialítico", 9.00, 7.5, 7.5, 9.00, 0.711111, 0.0911111, 146.25),
        Suelos(220, "Mayabeque", "Jaruco", "Fca La Pastora", "Fersialítico", 6.5, 9.00, 9.00, 6.5, 0.711111, 0.0911111, 146.25),
        Suelos(221, "Mayabeque", "San Jose", "Fca de Kety", "Ferralitico Rojo Lixiviado", 7.00, 4.14, 10.00, 7.5, 0.42, 0.101429, 146.25),
        Suelos(222, "Mayabeque", "San Jose", "Fca La Campana", "Ferralitico Rojo Lixiviado", 7.00, 3.71, 9.5, 8.5, 0.42, 0.101429, 146.25),
        Suelos(223, "Mayabeque", "San Jose", "Fca La Campana", "Ferralitico Rojo Lixiviado", 7.3, 4.49, 6.5, 6.00, 0.42, 0.101429, 146.25),
        Suelos(224, "Mayabeque", "San Jose", "Fca La Campana", "Ferralitico Rojo Lixiviado", 7.2, 3.88, 11.5, 9.5, 0.42, 0.101429, 146.25),
        Suelos(225, "Mayabeque", "San Jose", "Fca La Campana", "Ferralitico Rojo Lixiviado", 7.5, 2.47, 9.00, 5.00, 0.42, 0.101429, 146.25),
        Suelos(226, "Mayabeque", "San Jose", "Fca La Campana", "Ferralitico Rojo Lixiviado", 7.7, 3.7, 8.00, 4.00, 0.42, 0.101429, 146.25),
        Suelos(227, "Mayabeque", "San Jose", "Fca La Campana", "Ferralitico Rojo Lixiviado", 7.7, 5.89, 12.5, 8.5, 0.42, 0.101429, 146.25)
    )

    private suspend fun clearAllData() {
        abonoOrganicoDao.deleteAll()
        cultivosDao.deleteAll()
        fertAbOrgDao.deleteAll()
        suelosDao.deleteAll()
    }

    private suspend fun applyUpdate(update: UpdateVersion) {
        update.changes.forEach { (table, items) ->
            when (table) {
                "abono_organico" -> abonoOrganicoDao.insertAll(items.mapNotNull { moshi.adapter(AbonoOrganicoDto::class.java).fromJsonValue(it)?.toEntity() })
                "cultivos" -> cultivosDao.insertAll(items.mapNotNull { moshi.adapter(CultivosDto::class.java).fromJsonValue(it)?.toEntity() })
                "fert_ab_org" -> fertAbOrgDao.insertAll(items.mapNotNull { moshi.adapter(FertAbOrgDto::class.java).fromJsonValue(it)?.toEntity() })
                "suelos" -> suelosDao.insertAll(items.mapNotNull { moshi.adapter(SuelosDto::class.java).fromJsonValue(it)?.toEntity() })
            }
        }
    }
}
