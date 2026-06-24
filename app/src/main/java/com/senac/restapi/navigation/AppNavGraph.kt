package com.senac.restapi.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.senac.restapi.screens.ForgotPasswordScreen
import com.senac.restapi.screens.LoginScreen
import com.senac.restapi.screens.MenuScreen
import com.senac.restapi.screens.RegisterScreen
import com.senac.restapi.screens.TripPhotosScreen
import com.senac.restapi.viewmodel.DestinationViewModel
import com.senac.restapi.viewmodel.TripPhotoViewModel
import com.senac.restapi.viewmodel.TripViewModel
import com.senac.restapi.viewmodel.UserViewModel
import java.net.URLDecoder

@Composable
fun AppNavGraph(
    destinationViewModel: DestinationViewModel,
    userViewModel: UserViewModel,
    tripViewModel: TripViewModel,
    tripPhotoViewModel: TripPhotoViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                userViewModel = userViewModel,
                onLoginSuccess = { userId ->
                    navController.navigate(Screen.Menu.createRoute(userId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                userViewModel = userViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onEmailSent = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable
            MenuScreen(
                destinationViewModel = destinationViewModel,
                tripViewModel = tripViewModel,
                userId = userId,
                onNavigateToPhotos = { tripId, tripTitle ->
                    navController.navigate(Screen.TripPhotos.createRoute(tripId, tripTitle))
                }
            )
        }

        composable(
            route = Screen.TripPhotos.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.IntType },
                navArgument("tripTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable
            val tripTitleEncoded = backStackEntry.arguments?.getString("tripTitle") ?: ""
            val tripTitle = URLDecoder.decode(tripTitleEncoded, "UTF-8")
            TripPhotosScreen(
                tripId = tripId,
                tripTitle = tripTitle,
                tripPhotoViewModel = tripPhotoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
