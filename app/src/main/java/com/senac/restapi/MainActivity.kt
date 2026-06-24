package com.senac.restapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senac.restapi.navigation.AppNavGraph
import com.senac.restapi.ui.theme.RestApiTheme
import com.senac.restapi.viewmodel.DestinationViewModel
import com.senac.restapi.viewmodel.TripPhotoViewModel
import com.senac.restapi.viewmodel.TripViewModel
import com.senac.restapi.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestApiTheme {
                val destinationViewModel: DestinationViewModel = viewModel()
                val userViewModel: UserViewModel = viewModel()
                val tripViewModel: TripViewModel = viewModel()
                val tripPhotoViewModel: TripPhotoViewModel = viewModel()
                AppNavGraph(
                    destinationViewModel = destinationViewModel,
                    userViewModel = userViewModel,
                    tripViewModel = tripViewModel,
                    tripPhotoViewModel = tripPhotoViewModel
                )
            }
        }
    }
}
