package com.qarena.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.qarena.android.presentation.navigation.AppNavigation
import com.qarena.android.ui.theme.QArenaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QArenaTheme {
                AppNavigation()
            }
        }
    }
}
