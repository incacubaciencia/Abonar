package cu.edu.inca.abonosverdes.ui.viewmodel

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.edu.inca.abonosverdes.data.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repository: UpdateRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    sealed class UpdateUiState {
        data object Idle : UpdateUiState()
        data object Checking : UpdateUiState()
        data class NewVersionAvailable(val status: UpdateRepository.UpdateStatus) : UpdateUiState()
        data object Downloading : UpdateUiState()
    }

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private var downloadId: Long = -1

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                installApk()
            }
        }
    }

    init {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onDownloadComplete, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(onDownloadComplete, filter)
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.value = UpdateUiState.Checking
            val status = repository.checkForUpdate()
            if (status.hasUpdate && (status.downloadUrl != null)) {
                _uiState.value = UpdateUiState.NewVersionAvailable(status)
            } else {
                _uiState.value = UpdateUiState.Idle
            }
        }
    }

    fun startDownload(status: UpdateRepository.UpdateStatus) {
        val url = status.downloadUrl ?: return
        val fileName = "AbonosVerdes_${status.latestVersion}.apk"
        downloadId = repository.downloadUpdate(url, fileName)
        _uiState.value = UpdateUiState.Downloading
    }

    fun installApk() {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val uriString = cursor.getString(columnIndex)
            val apkUri = uriString.toUri()
            
            // Convert file:// to content:// if needed
            val file = File(apkUri.path ?: "")
            if (file.exists()) {
                triggerInstall(file)
            }
        }
        cursor.close()
    }

    private fun triggerInstall(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setDataAndType(contentUri, "application/vnd.android.package-archive")
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            // Report explicitly to Sentry and flush as requested
            Sentry.captureException(e)
            Sentry.flush(2000)
            // Also notify via SentryManager helper if available
            // SentryManager.captureFatal(e) 
        }
    }

    override fun onCleared() {
        context.unregisterReceiver(onDownloadComplete)
    }

    fun dismissDialog() {
        _uiState.value = UpdateUiState.Idle
    }
}
