package cu.edu.inca.abonosverdes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cu.edu.inca.abonosverdes.data.local.converters.Converters
import cu.edu.inca.abonosverdes.data.local.daos.*
import cu.edu.inca.abonosverdes.data.local.entities.*

@Database(
    entities = [
        AbonoOrganico::class,
        Cultivos::class,
        FertAbOrg::class,
        Suelos::class,
        DatabaseVersion::class
    ],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun abonoOrganicoDao(): AbonoOrganicoDao
    abstract fun cultivosDao(): CultivosDao
    abstract fun fertAbOrgDao(): FertAbOrgDao
    abstract fun suelosDao(): SuelosDao
    abstract fun databaseVersionDao(): DatabaseVersionDao

    /**
     * Realiza una limpieza y repoblación atómica de los datos maestros.
     */
    @androidx.room.Transaction
    suspend fun clearAndPopulate(
        suelos: List<Suelos>,
        cultivos: List<Cultivos>,
        fertilizantes: List<FertAbOrg>,
        abonos: List<AbonoOrganico>
    ) {
        suelosDao().deleteAll()
        cultivosDao().deleteAll()
        fertAbOrgDao().deleteAll()
        abonoOrganicoDao().deleteAll()

        suelosDao().insertAll(suelos)
        cultivosDao().insertAll(cultivos)
        fertAbOrgDao().insertAll(fertilizantes)
        abonoOrganicoDao().insertAll(abonos)
    }

    companion object {
        const val DATABASE_NAME = "abonos_verdes_db"
    }
}
