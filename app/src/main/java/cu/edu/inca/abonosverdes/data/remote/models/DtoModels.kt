package cu.edu.inca.abonosverdes.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import cu.edu.inca.abonosverdes.data.local.entities.*
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
data class AbonoOrganicoDto(
    val id: Int,
    @Json(name = "FechaMuestreo") val fechaMuestreo: String?,
    @Json(name = "Municipio") val municipio: String?,
    val finca: String?,
    @Json(name = "TipoAbono") val tipoAbono: String?,
    val humPercent: Double?,
    val relCN: Double?,
    @Json(name = "MO") val mo: Double?,
    @Json(name = "N") val n: Double?,
    @Json(name = "P") val p: Double?,
    @Json(name = "K") val k: Double?
) {
    fun toEntity(): AbonoOrganico {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = try {
            fechaMuestreo?.let { sdf.parse(it) } ?: Date()
        } catch (e: Exception) {
            Date()
        }
        return AbonoOrganico(
            id = id,
            fechaMuestreo = date,
            municipio = municipio ?: "",
            finca = finca ?: "",
            tipoAbono = tipoAbono ?: "",
            humPercent = humPercent ?: 0.0,
            relCN = relCN ?: 0.0,
            mo = mo ?: 0.0,
            n = n ?: 0.0,
            p = p ?: 0.0,
            k = k ?: 0.0
        )
    }
}

@JsonClass(generateAdapter = true)
data class CultivosDto(
    val id: Int?,
    val tipo: String?,
    val nombre: String?,
    val rendimientoTH: Double?,
    val phMin: Double?,
    val phMax: Double?,
    @Json(name = "N") val n: Double?,
    @Json(name = "P") val p: Double?,
    @Json(name = "K") val k: Double?
) {
    fun toEntity() = Cultivos(
        id = id ?: 0,
        tipo = tipo ?: "",
        nombre = nombre ?: "",
        rendimientoTH = rendimientoTH ?: 0.0,
        phMin = phMin,
        phMax = phMax,
        n = n ?: 0.0,
        p = p ?: 0.0,
        k = k ?: 0.0
    )
}

@JsonClass(generateAdapter = true)
data class FertAbOrgDto(
    val id: Int,
    val tipo: String?,
    @Json(name = "Nomb") val nomb: String?,
    val humPercent: Double?,
    val relCN: String?,
    @Json(name = "MO") val mo: Double?,
    @Json(name = "N") val n: Double?,
    @Json(name = "P") val p: Double?,
    @Json(name = "K") val k: Double?
) {
    fun toEntity() = FertAbOrg(
        id = id,
        tipo = tipo ?: "",
        nomb = nomb ?: "",
        humPercent = humPercent,
        relCN = relCN,
        mo = mo,
        n = n,
        p = p,
        k = k
    )
}

@JsonClass(generateAdapter = true)
data class SuelosDto(
    val id: Int,
    val provincia: String?,
    val municipio: String?,
    val finca: String?,
    @Json(name = "tipo_suelo") val tipoSuelo: String?,
    val ph: Double?,
    @Json(name = "moPercent") val moPercent: Double?,
    @Json(name = "Ca") val ca: Double?,
    @Json(name = "Mg") val mg: Double?,
    @Json(name = "K") val k: Double?,
    @Json(name = "Na") val na: Double?,
    @Json(name = "P") val p: Double?
) {
    fun toEntity() = Suelos(
        id = id,
        provincia = provincia ?: "",
        municipio = municipio ?: "",
        finca = finca ?: "",
        tipoSuelo = tipoSuelo ?: "",
        ph = ph ?: 0.0,
        moPercent = moPercent ?: 0.0,
        ca = ca ?: 0.0,
        mg = mg ?: 0.0,
        k = k ?: 0.0,
        na = na ?: 0.0,
        p = p ?: 0.0
    )
}
