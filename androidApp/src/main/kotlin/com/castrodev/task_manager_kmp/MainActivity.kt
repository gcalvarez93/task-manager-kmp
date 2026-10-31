// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/MainActivity.kt
package com.castrodev.task_manager_kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.castrodev.task_manager_kmp.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as TaskManagerApp).container

        setContent {
            MaterialTheme {
                AppNavHost(container = container)
            }
        }
    }
}