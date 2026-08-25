package cu.edu.inca.abonosverdes.data.local.daos

import androidx.room.*
import cu.edu.inca.abonosverdes.data.local.entities.DatabaseVersion

/**
 * DAO para gestionar la versión de la base de datos local.
 * Se utiliza para controlar la sincronización de datos con el servidor.
 */
@Dao
interface DatabaseVersionDao {
    /**
     * Obtiene la versión actual de la base de datos almacenada localmente.
     * Se asume que solo hay un registro con id = 1.
     * @return El objeto [DatabaseVersion] o null si no existe.
     */
    @Query("SELECT * FROM database_version WHERE id = 1")
    suspend fun getVersion(): DatabaseVersion?

    /**
     * Inserta o actualiza la versión de la base de datos.
     * @param version El nuevo objeto [DatabaseVersion] a guardar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateVersion(version: DatabaseVersion)
}
