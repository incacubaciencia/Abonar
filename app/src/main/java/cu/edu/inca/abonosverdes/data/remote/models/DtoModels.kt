package cu.edu.inca.abonosverdes.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import cu.edu.inca.abonosverdes.data.local.entities.*
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
data class AbonoOrganicoDto(
    val id: Int,
    @param:Json(name = "FechaMuestreo") val fechaMuestreo: String?,
    @param:Json(name = "Municipio") val municipio: String?,
    val finca: String?,
    @param:Json(name = "TipoAbono") val tipoAbono: String?,
    val humPercent: Double?,
    val relCN: Double?,
    @param:Json(name = "MO") val mo: Double?,
    @param:Json(name = "N") val n: Double?,
    @param:Json(name = "P") val p: Double?,
    @param:Json(name = "K") val k: Double?,
) {
    fun toEntity(): AbonoOrganico {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = try {
            fechaMuestreo?.let { sdf.parse(it) } ?: Date()
        } catch (_: Exception) {
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
    @param:Json(name = "N") val n: Double?,
    @param:Json(name = "P") val p: Double?,
    @param:Json(name = "K") val k: Double?,
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
    @param:Json(name = "Nomb") val nomb: String?,
    val humPercent: Double?,
    val relCN: String?,
    @param:Json(name = "MO") val mo: Double?,
    @param:Json(name = "N") val n: Double?,
    @param:Json(name = "P") val p: Double?,
    @param:Json(name = "K") val k: Double?,
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
    @param:Json(name = "tipo_suelo") val tipoSuelo: String?,
    val ph: Double?,
    @param:Json(name = "moPercent") val moPercent: Double?,
    @param:Json(name = "Ca") val ca: Double?,
    @param:Json(name = "Mg") val mg: Double?,
    @param:Json(name = "K") val k: Double?,
    @param:Json(name = "Na") val na: Double?,
    @param:Json(name = "P") val p: Double?,
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
