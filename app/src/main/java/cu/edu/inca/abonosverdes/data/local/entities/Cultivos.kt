package cu.edu.inca.abonosverdes.data.local.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "cultivos")
data class Cultivos(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String,
    val nombre: String,
    @ColumnInfo(name = "rendimiento_t/h") val rendimientoTH: Double,
    @ColumnInfo(name = "ph_min") val phMin: Double?,
    @ColumnInfo(name = "ph_max") val phMax: Double?,
    val n: Double,
    val p: Double,
    val k: Double
) : Parcelable
