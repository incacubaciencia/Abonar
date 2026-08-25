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
        Suelos(50, "Mayabeque", "San Jose", "Las Papas", "Ferralitico Rojo Lixiviado", 7.7, 3.37, 10.5, 4.5, 0.65, 0.05, 455.0)
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
