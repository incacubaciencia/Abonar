package cu.edu.inca.abonosverdes.data.local.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "fert_ab_org")
data class FertAbOrg(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String,
    @ColumnInfo(name = "Nomb") val nomb: String,
    @ColumnInfo(name = "%hum") val humPercent: Double?,
    @ColumnInfo(name = "Rel_C/N") val relCN: String?,
    @ColumnInfo(name = "MO") val mo: Double?,
    val n: Double?,
    val p: Double?,
    val k: Double?
) : Parcelable
