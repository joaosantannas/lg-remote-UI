package com.github.heroslender.lgtvcontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.heroslender.lgtvcontroller.ui.theme.AppColors
import com.github.heroslender.lgtvcontroller.ui.theme.LGTVControllerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var deviceManager: DeviceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barras do sistema sempre com ícones claros, já que o app é sempre escuro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            LGTVControllerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Background) {
                    ControllerApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        deviceManager.resume()
    }

    override fun onPause() {
        super.onPause()

        deviceManager.pause()
    }
}

