package cu.edu.inca.abonosverdes

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Clase principal de la aplicación para AbonosVerdes.
 * Configura Hilt para la inyección de dependencias y WorkManager para tareas en segundo plano.
 * Implementa [Configuration.Provider] para proporcionar una configuración personalizada de WorkManager.
 */
@HiltAndroidApp
class AbonosVerdesApp : Application(), Configuration.Provider {

    /**
     * Fábrica de trabajadores de Hilt inyectada para que WorkManager pueda crear instancias de Workers.
     */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Se llama al crear la aplicación. Inicializa WorkManager con la configuración personalizada
     * y programa la tarea de sincronización inicial.
     */
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Sentry con el DSN y configuraciones personalizadas
        cu.edu.inca.abonosverdes.core.monitoring.SentryManager.init(this)

        // Ensure WorkManager is initialized with the HiltWorkerFactory
        // Although Configuration.Provider should handle this, manual initialization 
        // can sometimes resolve timing issues in certain environments.
        try {
            androidx.work.WorkManager.initialize(this, workManagerConfiguration)
        } catch (e: Exception) {
            // Already initialized, which might happen if another component triggered it
        }
        cu.edu.inca.abonosverdes.worker.SyncWorker.enqueue(this)
    }

    /**
     * Proporciona la configuración de WorkManager, utilizando el [workerFactory] de Hilt.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
