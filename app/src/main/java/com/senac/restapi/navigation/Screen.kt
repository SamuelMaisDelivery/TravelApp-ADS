package com.senac.restapi.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Menu : Screen("menu/{userId}") {
        fun createRoute(userId: Int) = "menu/$userId"
    }
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
    object TripPhotos : Screen("trip_photos/{tripId}/{tripTitle}") {
        fun createRoute(tripId: Int, tripTitle: String) =
            "trip_photos/$tripId/${java.net.URLEncoder.encode(tripTitle, "UTF-8")}"
    }
    object Itinerary : Screen("itinerary/{tripId}") {
        fun createRoute(tripId: Int) = "itinerary/$tripId"
    }
}
