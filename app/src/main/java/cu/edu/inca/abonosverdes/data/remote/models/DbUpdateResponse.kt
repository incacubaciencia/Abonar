package cu.edu.inca.abonosverdes.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DbUpdateResponse(
    @param:Json(name = "latest_version") val latestVersion: Int,
    @param:Json(name = "minimum_required_version") val minimumRequiredVersion: Int,
    val updates: List<UpdateVersion>
)

@JsonClass(generateAdapter = true)
data class UpdateVersion(
    val version: Int,
    val changes: Map<String, List<Map<String, Any?>>>
)
