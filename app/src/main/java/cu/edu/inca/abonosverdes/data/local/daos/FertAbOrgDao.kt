package cu.edu.inca.abonosverdes.data.local.daos

import androidx.room.*
import cu.edu.inca.abonosverdes.data.local.entities.FertAbOrg
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la entidad FertAbOrg (Fertilizantes y Abonos Orgánicos detallados).
 */
@Dao
interface FertAbOrgDao {
    /**
     * Obtiene todos los registros detallados de fertilizantes y abonos orgánicos.
     * @return Flow con la lista de [FertAbOrg].
     */
    @Query("SELECT * FROM fert_ab_org")
    fun getAll(): Flow<List<FertAbOrg>>

    /**
     * Retorna la cantidad de registros en la tabla fert_ab_org.
     */
    @Query("SELECT COUNT(*) FROM fert_ab_org")
    suspend fun count(): Int

    /**
     * Inserta una lista de registros de fertilizantes/abonos.
     * @param fertAbOrgs Lista a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fertAbOrgs: List<FertAbOrg>)

    /**
     * Limpia la tabla fert_ab_org.
     */
    @Query("DELETE FROM fert_ab_org")
    suspend fun deleteAll()
}
