package cu.edu.inca.abonosverdes.core.monitoring

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import cu.edu.inca.abonosverdes.BuildConfig
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.Device
import io.sentry.protocol.OperatingSystem

/**
 * Gestor central para la configuración y operación de Sentry.
 */
object SentryManager {

    private const val SENTRY_DSN = "https://cef69b8aa771d28ea0b39553ae14bd2c@sentry.inca.edu.cu/2"

    /**
     * Inicializa el SDK de Sentry con configuraciones personalizadas para recolección exhaustiva
     * y manejo manual de consentimiento en caso de fallos críticos.
     */
    fun init(context: Context) {
        SentryAndroid.init(context) { options ->
            options.dsn = SENTRY_DSN
            // Desactivamos el manejador automático para implementar el diálogo de consentimiento
            options.isEnableUncaughtExceptionHandler = false 
            options.isAttachStacktrace = true
            options.isEnableUserInteractionBreadcrumbs = true
            options.isEnableAutoSessionTracking = true
            
            // Sentry envía los reportes de forma asíncrona por defecto.
            
            options.setBeforeSend { event, _ ->
                enrichEvent(event, context)
                event
            }
        }
        
        setupCrashHandler(context)
    }

    /**
     * Intercepta excepciones no capturadas globalmente para mostrar el diálogo de consentimiento.
     */
    private fun setupCrashHandler(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            CrashActivity.start(context, throwable)
        }
    }

    /**
     * Enriquece el evento con metadatos detallados del dispositivo y la aplicación.
     */
    private fun enrichEvent(event: SentryEvent, context: Context) {
        val device = event.contexts.device ?: Device()
        event.contexts.put("device", device)
        
        device.model = Build.MODEL
        device.manufacturer = Build.MANUFACTURER
        device.archs = arrayOf(Build.SUPPORTED_ABIS[0])
        
        val os = event.contexts.operatingSystem ?: OperatingSystem()
        event.contexts.put("os", os)
        os.name = "Android"
        os.version = Build.VERSION.RELEASE
        
        // Información de Memoria
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        device.memorySize = memoryInfo.totalMem
        device.freeMemory = memoryInfo.availMem
        
        // Información de Almacenamiento
        val stat = StatFs(Environment.getDataDirectory().path)
        device.storageSize = stat.blockSizeLong * StatFs(Environment.getDataDirectory().path).blockCountLong
        device.freeStorage = stat.blockSizeLong * StatFs(Environment.getDataDirectory().path).availableBlocksLong
        
        // Metadatos de la App
        event.release = "${BuildConfig.APPLICATION_ID}@${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}"
        event.setTag("app.version_name", BuildConfig.VERSION_NAME)
        event.setTag("app.version_code", BuildConfig.VERSION_CODE.toString())
    }

    /**
     * Envía una excepción fatal a Sentry después del consentimiento del usuario.
     */
    fun captureFatal(throwable: Throwable) {
        Sentry.captureException(throwable)
        // Forzamos el envío antes de que el proceso termine definitivamente
        Sentry.flush(2000)
    }
}
