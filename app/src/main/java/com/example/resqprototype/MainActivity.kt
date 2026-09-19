package com.example.resqprototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.resqprototype.ui.navigation.ResqMainContainer
import com.example.resqprototype.ui.theme.RESQPrototypeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RESQPrototypeTheme {
                ResqMainContainer()
            }
        }
    }
}
