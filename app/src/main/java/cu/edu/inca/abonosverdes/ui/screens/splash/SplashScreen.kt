package cu.edu.inca.abonosverdes.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import cu.edu.inca.abonosverdes.R
import kotlinx.coroutines.delay

/**
 * Pantalla de presentación (Splash) que se muestra al iniciar la aplicación.
 * Presenta los logos de las instituciones colaboradoras (CERAI, INCA, AECID)
 * y el nombre del proyecto.
 *
 * @param onTimeout Callback que se ejecuta después de que finaliza el tiempo de espera (delay).
 */
@Composable
@Preview(showBackground = true)
fun SplashScreen(
    onTimeout: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo con imagen blur
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(5.dp),
            contentScale = ContentScale.Crop
        )

        // Overlay oscuro para mejorar el contraste de los logos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.3f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.nombre),
                contentDescription = stringResource(R.string.logo_abonar_desc),
                modifier = Modifier.size(270.dp)
            )
            Spacer(modifier = Modifier.height(64.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_cerai),
                    contentDescription = stringResource(R.string.logo_cerai_desc),
                    modifier = Modifier.height(35.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo_inca),
                    contentDescription = stringResource(R.string.logo_inca_desc),
                    modifier = Modifier.height(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo_aecid),
                    contentDescription = stringResource(R.string.logo_aecid_desc),
                    modifier = Modifier.height(50.dp)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.logo_proyecto),
                contentDescription = stringResource(R.string.logo_abonar_desc),
                modifier = Modifier.size(100.dp)
            )
        }
    }
}
