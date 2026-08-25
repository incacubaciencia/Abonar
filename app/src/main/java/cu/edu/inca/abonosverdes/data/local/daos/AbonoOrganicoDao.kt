package cu.edu.inca.abonosverdes.data.local.daos

import androidx.room.*
import cu.edu.inca.abonosverdes.data.local.entities.AbonoOrganico
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad AbonoOrganico.
 * Permite realizar operaciones CRUD sobre los datos de abonos orgánicos.
 */
@Dao
interface AbonoOrganicoDao {
    /**
     * Obtiene todos los registros de abonos orgánicos.
     * @return Un Flow con la lista de objetos [AbonoOrganico].
     */
    @Query("SELECT * FROM abono_organico")
    fun getAll(): Flow<List<AbonoOrganico>>

    /**
     * Obtiene los nombres únicos de las fincas que tienen abono orgánico registrado.
     * @return Un Flow con la lista de nombres de fincas.
     */
    @Query("SELECT DISTINCT finca FROM abono_organico WHERE finca IS NOT NULL")
    fun getUniqueFincasConAbono(): Flow<List<String>>

    /**
     * Recupera la información de abono orgánico para una finca específica.
     * @param fincaName Nombre de la finca.
     * @return El objeto [AbonoOrganico] encontrado o null.
     */
    @Query("SELECT * FROM abono_organico WHERE finca = :fincaName LIMIT 1")
    suspend fun getByFinca(fincaName: String): AbonoOrganico?

    /**
     * Cuenta cuántos registros de abono orgánico hay en total.
     * @return El número de registros.
     */
    @Query("SELECT COUNT(*) FROM abono_organico")
    suspend fun count(): Int

    /**
     * Inserta una lista de abonos orgánicos, reemplazando en caso de conflicto.
     * @param abonos Lista de abonos a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(abonos: List<AbonoOrganico>)

    /**
     * Elimina todos los registros de la tabla abono_organico.
     */
    @Query("DELETE FROM abono_organico")
    suspend fun deleteAll()
}
