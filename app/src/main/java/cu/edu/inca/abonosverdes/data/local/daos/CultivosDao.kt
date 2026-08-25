package cu.edu.inca.abonosverdes.data.local.daos

import androidx.room.*
import cu.edu.inca.abonosverdes.data.local.entities.Cultivos
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad Cultivos.
 * Proporciona métodos para interactuar con los datos de los cultivos en la base de datos local.
 */
@Dao
interface CultivosDao {
    /**
     * Obtiene todos los cultivos almacenados.
     * @return Un Flow que emite la lista de todos los objetos [Cultivos].
     */
    @Query("SELECT * FROM cultivos")
    fun getAll(): Flow<List<Cultivos>>

    /**
     * Cuenta el número total de cultivos registrados.
     * @return La cantidad total de filas en la tabla cultivos.
     */
    @Query("SELECT COUNT(*) FROM cultivos")
    suspend fun count(): Int

    /**
     * Inserta una lista de cultivos en la base de datos.
     * Si un cultivo ya existe, será reemplazado.
     * @param cultivos Lista de objetos [Cultivos] a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cultivos: List<Cultivos>)

    /**
     * Elimina todos los registros de la tabla cultivos.
     */
    @Query("DELETE FROM cultivos")
    suspend fun deleteAll()
}
