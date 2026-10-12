package com.stadiolinks.kitabu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stadiolinks.kitabu.ui.KitabuApp
import com.stadiolinks.kitabu.ui.theme.KitabuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as KitabuApplication).container
        setContent {
            KitabuTheme {
                KitabuApp(viewModelFactory = container.viewModelFactory)
            }
        }
    }
}
