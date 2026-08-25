package cu.edu.inca.abonosverdes.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import cu.edu.inca.abonosverdes.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val titleRes: Int,
    val descriptionRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        titleRes = R.string.onboarding_1_title,
        descriptionRes = R.string.onboarding_1_desc
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_2_title,
        descriptionRes = R.string.onboarding_2_desc
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_3_title,
        descriptionRes = R.string.onboarding_3_desc
    )
)

/**
 * Pantalla de bienvenida que introduce al usuario a las funcionalidades de la app.
 * Utiliza un Pager horizontal para navegar entre diferentes tarjetas informativas.
 *
 * @param onFinished Callback que se llama cuando el usuario termina o salta el flujo inicial.
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage < onboardingPages.size - 1) {
                    TextButton(onClick = onFinished) {
                        Text(stringResource(R.string.btn_skip))
                    }
                    Button(onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }) {
                        Text(stringResource(R.string.btn_next))
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onFinished) {
                        Text(stringResource(R.string.btn_get_started))
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { pageIndex ->
            val page = onboardingPages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(page.titleRes),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(page.descriptionRes),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(onFinished = {})
}
