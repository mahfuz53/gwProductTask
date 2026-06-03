package com.gwproductsusa.gwtasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gwproductsusa.gwtasks.presentation.main.MainViewModel
import com.gwproductsusa.gwtasks.presentation.navigation.GwTasksNavGraph
import com.gwproductsusa.gwtasks.ui.theme.GwtasksTheme
import com.gwproductsusa.gwtasks.ui.theme.OdooPurple
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GwtasksTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val startDestination by mainViewModel.startDestination
                    .collectAsStateWithLifecycle()

                if (startDestination == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OdooPurple)
                    }
                } else {
                    GwTasksNavGraph(startDestination = startDestination!!)
                }
            }
        }
    }
}
