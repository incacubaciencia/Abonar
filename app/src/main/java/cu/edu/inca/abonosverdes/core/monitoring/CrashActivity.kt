package cu.edu.inca.abonosverdes.core.monitoring

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import cu.edu.inca.abonosverdes.ui.theme.AbonarTheme
import kotlin.system.exitProcess

class CrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val throwable = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_THROWABLE, Throwable::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_THROWABLE) as? Throwable
        }
        
        setContent {
            AbonarTheme {
                CrashDialog(
                    onConfirm = {
                        throwable?.let { t -> SentryManager.captureFatal(t) }
                        finishApp()
                    },
                    onDismiss = {
                        finishApp()
                    }
                )
            }
        }
    }

    private fun finishApp() {
        finishAffinity()
        exitProcess(1)
    }

    companion object {
        private const val EXTRA_THROWABLE = "extra_throwable"

        fun start(context: Context, throwable: Throwable) {
            val intent = Intent(context, CrashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(EXTRA_THROWABLE, throwable)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun CrashDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Error inesperado") },
        text = {
            Text(
                text = "Se ha producido un error inesperado. ¿Deseas enviar un informe anónimo a los desarrolladores para ayudarnos a solucionar el problema?",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Sí")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "No")
            }
        }
    )
}
