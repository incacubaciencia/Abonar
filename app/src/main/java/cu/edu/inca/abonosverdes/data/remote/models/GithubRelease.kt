package cu.edu.inca.abonosverdes.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubRelease(
    @param:Json(name = "tag_name") val tagName: String,
    @param:Json(name = "name") val name: String?,
    @param:Json(name = "body") val body: String?,
    @param:Json(name = "assets") val assets: List<GithubAsset>,
)

@JsonClass(generateAdapter = true)
data class GithubAsset(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "browser_download_url") val downloadUrl: String,
    @param:Json(name = "content_type") val contentType: String,
)
