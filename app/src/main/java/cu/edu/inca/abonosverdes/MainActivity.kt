package cu.edu.inca.abonosverdes

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.content.edit
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cu.edu.inca.abonosverdes.ui.navigation.AbonarNavHost
import cu.edu.inca.abonosverdes.ui.theme.AbonarTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de la aplicación AbonosVerdes.
 * Se encarga de la inicialización de la interfaz de usuario, el manejo del splash screen
 * y la navegación inicial (incluyendo el flujo de bienvenida/onboarding).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Punto de entrada de la actividad. Configura el splash screen, habilita el diseño edge-to-edge
     * y establece el contenido de Compose.
     *
     * @param savedInstanceState Estado guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getSharedPreferences("abonar_prefs", Context.MODE_PRIVATE)
        val showOnboardingInitial = sharedPref.getBoolean("show_onboarding", true)

        setContent {
            var showOnboarding by rememberSaveable { mutableStateOf(showOnboardingInitial) }

            AbonarTheme {
                AbonarNavHost(
                    showOnboarding = showOnboarding,
                    onOnboardingFinished = {
                        showOnboarding = false
                        sharedPref.edit { putBoolean("show_onboarding", false) }
                    }
                )
            }
        }
    }
}
