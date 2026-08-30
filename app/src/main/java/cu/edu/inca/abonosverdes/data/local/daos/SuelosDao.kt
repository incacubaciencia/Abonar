package cu.edu.inca.abonosverdes.data.local.daos

import androidx.room.*
import cu.edu.inca.abonosverdes.data.local.entities.Suelos
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad Suelos.
 * Contiene métodos para consultar, insertar y eliminar información sobre los suelos.
 */
@Dao
interface SuelosDao {
    /**
     * Obtiene todos los registros de suelos disponibles.
     * @return Un Flow que emite la lista de todos los objetos [Suelos].
     */
    @Query("SELECT * FROM suelos")
    fun getAll(): Flow<List<Suelos>>

    /**
     * Obtiene una lista de nombres de fincas únicos que tienen información de suelo.
     * @return Un Flow que emite una lista de nombres de fincas.
     */
    @Query("SELECT DISTINCT finca FROM suelos WHERE finca IS NOT NULL")
    fun getUniqueFincas(): Flow<List<String>>

    /**
     * Obtiene una lista de municipios únicos presentes en la base de datos de suelos.
     * @return Un Flow que emite una lista de nombres de municipios.
     */
    @Query("SELECT DISTINCT municipio FROM suelos WHERE municipio IS NOT NULL")
    fun getUniqueMunicipios(): Flow<List<String>>

    /**
     * Obtiene una lista de provincias únicas presentes en la tabla suelos.
     */
    @Query("SELECT DISTINCT provincia FROM suelos WHERE provincia IS NOT NULL")
    fun getUniqueProvincias(): Flow<List<String>>

    /**
     * Obtiene una lista de municipios únicos para una provincia específica desde la tabla suelos.
     */
    @Query("SELECT DISTINCT municipio FROM suelos WHERE provincia = :provincia AND municipio IS NOT NULL")
    fun getUniqueMunicipiosByProvincia(provincia: String): Flow<List<String>>

    /**
     * Obtiene una lista de tipos de suelo únicos disponibles.
     * @return Un Flow que emite una lista de tipos de suelo.
     */
    @Query("SELECT DISTINCT tipoSuelo FROM suelos WHERE tipoSuelo IS NOT NULL")
    fun getUniqueTiposSuelo(): Flow<List<String>>

    /**
     * Busca todos los registros de suelo por el tipo de suelo.
     * @param tipoSuelo Parte del nombre del tipo de suelo.
     * @return Lista de objetos [Suelos] correspondientes.
     */
    @Query("SELECT * FROM suelos WHERE tipoSuelo LIKE '%' || :tipoSuelo || '%'")
    suspend fun getAllByTipoSuelo(tipoSuelo: String): List<Suelos>

    /**
     * Busca registros de suelo por tipo de suelo, municipio y provincia.
     */
    @Query("SELECT * FROM suelos WHERE (tipoSuelo LIKE '%' || :tipoSuelo || '%') AND municipio = :municipio AND provincia = :provincia")
    suspend fun getAllByTipoSueloProvinciaMunicipio(tipoSuelo: String, provincia: String, municipio: String): List<Suelos>

    /**
     * Busca registros de suelo por tipo de suelo y provincia.
     */
    @Query("SELECT * FROM suelos WHERE (tipoSuelo LIKE '%' || :tipoSuelo || '%') AND provincia = :provincia")
    suspend fun getAllByTipoSueloAndProvincia(tipoSuelo: String, provincia: String): List<Suelos>

    /**
     * Busca todos los registros de suelo por el nombre de la finca.
     * @param fincaName Nombre de la finca a buscar.
     * @return Lista de objetos [Suelos] correspondientes.
     */
    @Query("SELECT * FROM suelos WHERE finca = :fincaName")
    suspend fun getAllByFinca(fincaName: String): List<Suelos>

    /**
     * Busca todos los registros de suelo por el municipio.
     * @param municipio Nombre del municipio.
     * @return Lista de objetos [Suelos] correspondientes.
     */
    @Query("SELECT * FROM suelos WHERE municipio = :municipio")
    suspend fun getAllByMunicipio(municipio: String): List<Suelos>

    /**
     * Busca un registro de suelo por el nombre de la finca.
     * @param fincaName Nombre de la finca a buscar.
     * @return El objeto [Suelos] correspondiente o null si no se encuentra.
     */
    @Query("SELECT * FROM suelos WHERE finca = :fincaName LIMIT 1")
    suspend fun getByFinca(fincaName: String): Suelos?

    /**
     * Cuenta el número total de registros de suelos.
     * @return El número total de filas en la tabla suelos.
     */
    @Query("SELECT COUNT(*) FROM suelos")
    suspend fun count(): Int

    /**
     * Inserta una lista de registros de suelos en la base de datos.
     * Reemplaza los registros existentes en caso de conflicto.
     * @param suelos Lista de objetos [Suelos] a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suelos: List<Suelos>)

    /**
     * Elimina todos los registros de la tabla suelos.
     */
    @Query("DELETE FROM suelos")
    suspend fun deleteAll()
}
