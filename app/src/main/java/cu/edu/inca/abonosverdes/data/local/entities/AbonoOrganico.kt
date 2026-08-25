package cu.edu.inca.abonosverdes.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "abono_organico")
data class AbonoOrganico(
    @PrimaryKey val id: Int,
    val fechaMuestreo: Date,
    val municipio: String,
    val finca: String,
    val tipoAbono: String,
    val humPercent: Double,
    val relCN: Double,
    val mo: Double,
    val n: Double,
    val p: Double,
    val k: Double
) : Parcelable
