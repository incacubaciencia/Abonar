package cu.edu.inca.abonosverdes.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cu.edu.inca.abonosverdes.R
import cu.edu.inca.abonosverdes.ui.theme.AbonarTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbonarAppBar(
    title: String,
    onOpenDrawer: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) Color.Black else Color(0xFF386B01)
    val contentColor = Color.White

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = stringResource(R.string.menu_desc),
                    tint = contentColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor
        )
    )
}

@Preview(showBackground = true)
@Composable
fun AbonarAppBarPreview() {
    AbonarTheme {
        AbonarAppBar(
            title = "Abonar",
            onOpenDrawer = {}
        )
    }
}
