package io.wojciechosak.calendar.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import io.wojciechosak.calendar.calendar.screens.NamedScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun App() {
    Box {
        Navigator(MenuScreen()) {
            Scaffold(
                topBar = {
                    val navigator = LocalNavigator.current
					MediumTopAppBar(
                        colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                        ),
                        title = {
                            if (navigator?.canPop == true) {
                                Text((navigator.lastItem as? NamedScreen)?.name ?: "", fontSize = 14.sp)
                            } else {
                                Text("KMP Calendar Demo", fontSize = 14.sp)
                            }
                        },
                        navigationIcon = {
                            if (navigator?.canPop == true) {
                                IconButton(
                                    onClick = { navigator.pop() },
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                    )
                                }
                            }
                        },
                    )
                },
                content = { padding ->
                    Box(Modifier.padding(padding)) {
                        CurrentScreen()
                    }
                },
            )
        }
    }
}
