package cu.edu.inca.abonosverdes.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "suelos")
data class Suelos(
    @PrimaryKey val id: Int,
    val provincia: String? = null,
    val municipio: String? = null,
    val finca: String? = null,
    val tipoSuelo: String? = null,
    val ph: Double? = null,
    val moPercent: Double? = null,
    val ca: Double? = null,
    val mg: Double? = null,
    val k: Double? = null,
    val na: Double? = null,
    val p: Double? = null
) : Parcelable
