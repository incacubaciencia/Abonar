package cu.edu.inca.abonosverdes.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import cu.edu.inca.abonosverdes.BuildConfig
import cu.edu.inca.abonosverdes.data.remote.GithubApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val githubApiService: GithubApiService,
    @get:ApplicationContext private val context: Context,
) {

    data class UpdateStatus(
        val hasUpdate: Boolean,
        val latestVersion: String,
        val downloadUrl: String? = null,
        val releaseNotes: String? = null,
    )

    suspend fun checkForUpdate(): UpdateStatus {
        return try {
            val latestRelease = githubApiService.getLatestRelease()
            val remoteVersion = cleanVersion(latestRelease.tagName)
            val currentVersion = cleanVersion(BuildConfig.VERSION_NAME)

            if (isNewer(remoteVersion, currentVersion)) {
                val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk") }
                UpdateStatus(
                    hasUpdate = true,
                    latestVersion = remoteVersion,
                    downloadUrl = apkAsset?.downloadUrl,
                    releaseNotes = latestRelease.body,
                )
            } else {
                UpdateStatus(hasUpdate = false, latestVersion = remoteVersion)
            }
        } catch (_: Exception) {
            UpdateStatus(hasUpdate = false, latestVersion = "")
        }
    }

    private fun cleanVersion(version: String): String {
        return version.replace("Version-", "", ignoreCase = true)
            .replace("v", "", ignoreCase = true)
            .trim()
    }

    private fun isNewer(remote: String, current: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        
        val length = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    fun downloadUpdate(url: String, fileName: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setTitle("Actualización de Abonos Verdes")
            .setDescription("Descargando versión $fileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }
}
