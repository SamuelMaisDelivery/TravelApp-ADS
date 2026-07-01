package com.senac.restapi.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.senac.restapi.database.TripEntity
import com.senac.restapi.ui.theme.*
import com.senac.restapi.viewmodel.DestinationViewModel
import com.senac.restapi.viewmodel.LocationState
import com.senac.restapi.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MenuItem(val title: String, val icon: ImageVector) {
    HOME("Menu Geral", Icons.Default.Home),
    NEW_TRIP("Nova Viagem", Icons.Default.AddCircle),
    MY_TRIPS("Minhas Viagens", Icons.Default.Luggage)
}

data class DestinationPurchaseData(
    val destinationId: Int,
    val title: String,
    val image: String,
    val price: Double,
    val cidade: String,
    val tipo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    destinationViewModel: DestinationViewModel,
    tripViewModel: TripViewModel,
    userId: Int,
    onNavigateToPhotos: (tripId: Int, tripTitle: String) -> Unit,
    onNavigateToItinerary: (tripId: Int) -> Unit
) {
    val destinations by destinationViewModel.destinations.collectAsStateWithLifecycle()
    val isLoading by destinationViewModel.loadState.collectAsStateWithLifecycle()
    val locationState by tripViewModel.locationState.collectAsStateWithLifecycle()
    val currentTrip by tripViewModel.currentTrip.collectAsStateWithLifecycle()

    var selectedMenuItem by remember { mutableStateOf(MenuItem.HOME) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var selectedDestination by remember { mutableStateOf<DestinationPurchaseData?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Launcher para permissões de localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            tripViewModel.startPeriodicLocationUpdates(userId)
        }
    }

    LaunchedEffect(userId) {
        tripViewModel.setCurrentUserId(userId)
        destinationViewModel.loadDestinations()
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Para as atualizações periódicas quando o composable sair de tela
    DisposableEffect(Unit) {
        onDispose {
            tripViewModel.stopPeriodicLocationUpdates()
        }
    }

    val hasCurrentTrip = currentTrip != null
    val isOnHome = selectedMenuItem == MenuItem.HOME

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = TravelCardBg) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(text = "✈️", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "TravelApp",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TravelGreenDark
                        )
                    }

                    HorizontalDivider(color = TravelGreenLight.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(16.dp))

                    MenuItem.entries.forEach { menuItem ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = menuItem.icon,
                                    contentDescription = menuItem.title,
                                    tint = if (selectedMenuItem == menuItem) TravelGreen else TravelGray
                                )
                            },
                            label = {
                                Text(
                                    text = menuItem.title,
                                    fontWeight = if (selectedMenuItem == menuItem) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = selectedMenuItem == menuItem,
                            onClick = {
                                selectedMenuItem = menuItem
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = TravelGreen.copy(alpha = 0.2f),
                                selectedIconColor = TravelGreen,
                                selectedTextColor = TravelGreenDark,
                                unselectedIconColor = TravelGray,
                                unselectedTextColor = TravelGray
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Flight,
                                contentDescription = null,
                                tint = TravelOnPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                selectedMenuItem.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = TravelOnPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TravelGreen,
                        titleContentColor = TravelOnPrimary
                    )
                )
            },
            // Bottom bar aparece somente quando há viagem ativa e estamos no HOME
            bottomBar = {
                if (isOnHome && hasCurrentTrip) {
                    TripBottomBar(
                        onRoteiro = { currentTrip?.let { onNavigateToItinerary(it.id) } },
                        onFotos = {
                            currentTrip?.let { trip ->
                                onNavigateToPhotos(trip.id, trip.productTitle)
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TravelBackground)
                    .padding(innerPadding)
            ) {
                when (selectedMenuItem) {
                    MenuItem.HOME -> {
                        HomeContent(
                            destinations = destinations,
                            isLoading = isLoading,
                            locationState = locationState,
                            currentTrip = currentTrip
                        )
                    }
                    MenuItem.NEW_TRIP -> {
                        TripsScreen(
                            destinationViewModel = destinationViewModel,
                            userId = userId,
                            onPurchaseTrip = { destinationId, title, image, price, cidade, tipo ->
                                selectedDestination = DestinationPurchaseData(destinationId, title, image, price, cidade, tipo)
                                showPurchaseDialog = true
                            }
                        )
                    }
                    MenuItem.MY_TRIPS -> {
                        MyTripsScreen(
                            tripViewModel = tripViewModel,
                            userId = userId
                        )
                    }
                }
            }
        }
    }

    if (showPurchaseDialog && selectedDestination != null) {
        val destination = selectedDestination!!
        PurchaseTripDialog(
            productTitle = destination.title,
            onDismiss = {
                showPurchaseDialog = false
                selectedDestination = null
            },
            onConfirm = { startDate, endDate ->
                tripViewModel.addTrip(
                    userId = userId,
                    productId = destination.destinationId,
                    productTitle = destination.title,
                    productImage = destination.image,
                    productPrice = destination.price,
                    tripType = destination.tipo,
                    startDate = startDate,
                    endDate = endDate,
                    cidade = destination.cidade
                )
                showPurchaseDialog = false
                selectedDestination = null
            }
        )
    }
}

@Composable
private fun TripBottomBar(
    onRoteiro: () -> Unit,
    onFotos: () -> Unit
) {
    NavigationBar(
        containerColor = TravelCardBg,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onRoteiro,
            icon = {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Roteiro"
                )
            },
            label = { Text("Roteiro") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TravelGreen,
                unselectedIconColor = TravelGray,
                selectedTextColor = TravelGreen,
                unselectedTextColor = TravelGray,
                indicatorColor = TravelGreen.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onFotos,
            icon = {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Fotos"
                )
            },
            label = { Text("Fotos") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TravelGreen,
                unselectedIconColor = TravelGray,
                selectedTextColor = TravelGreen,
                unselectedTextColor = TravelGray,
                indicatorColor = TravelGreen.copy(alpha = 0.15f)
            )
        )
    }
}

@Composable
fun HomeContent(
    destinations: List<com.senac.restapi.database.DestinationEntity>,
    isLoading: com.senac.restapi.viewmodel.DestinationLoadState,
    locationState: LocationState,
    currentTrip: TripEntity?
) {
    when (isLoading) {
        is com.senac.restapi.viewmodel.DestinationLoadState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = TravelGreen, strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Buscando destinos...", color = TravelGray, fontSize = 14.sp)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mapa em tempo real quando há viagem ativa
                if (currentTrip != null) {
                    item {
                        ActiveTripMapCard(
                            locationState = locationState,
                            currentTrip = currentTrip
                        )
                    }
                } else {
                    item {
                        CurrentTripCard(
                            locationState = locationState
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                if (destinations.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✈️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nenhum destino encontrado", color = TravelGray)
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "${destinations.size} destinos disponíveis",
                            fontSize = 13.sp,
                            color = TravelGray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(destinations) { destination ->
                        DestinationCard(destination = destination)
                    }
                }
            }
        }
    }
}

// Card com mapa em tempo real para viagem ativa
@Composable
fun ActiveTripMapCard(
    locationState: LocationState,
    currentTrip: TripEntity
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardBg),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            // Header do card
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TravelExplore,
                    contentDescription = null,
                    tint = TravelGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Viagem em Andamento",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelGreenDark
                )
            }

            // Mapa OSMDroid
            when (locationState) {
                is LocationState.Success -> {
                    TripMapView(
                        latitude = locationState.latitude,
                        longitude = locationState.longitude,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    // Chip de localização sobre o mapa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = TravelGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TravelGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = locationState.city,
                                    fontSize = 13.sp,
                                    color = TravelGreenDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                is LocationState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TravelGreen, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Obtendo localização...", fontSize = 13.sp, color = TravelGray)
                        }
                    }
                }
                is LocationState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(TravelError.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = locationState.message,
                            fontSize = 13.sp,
                            color = TravelError
                        )
                    }
                }
                is LocationState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aguardando localização...", fontSize = 13.sp, color = TravelGray)
                    }
                }
            }

            // Informações da viagem
            Column(modifier = Modifier.padding(16.dp)) {
                HorizontalDivider(color = TravelGreenLight.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(label = "Destino:", value = currentTrip.productTitle)
                InfoRow(label = "Cidade:", value = currentTrip.cidade)
                InfoRow(label = "Início:", value = dateFormatter.format(Date(currentTrip.startDate)))
                InfoRow(label = "Fim:", value = dateFormatter.format(Date(currentTrip.endDate)))
                InfoRow(label = "Tipo:", value = currentTrip.tripType)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = TravelGreenLight.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Orçamento:", value = "R$ ${String.format(Locale.getDefault(), "%.2f", currentTrip.orcamento)}")
                InfoRow(label = "Gastos:", value = "R$ ${String.format(Locale.getDefault(), "%.2f", currentTrip.totalGastos)}")
            }
        }
    }
}

// Componente de mapa OSMDroid integrado ao Compose
@Composable
fun TripMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
        }
    }

    // Lifecycle do MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        update = { map ->
            val geoPoint = GeoPoint(latitude, longitude)
            map.controller.setZoom(14.0)
            map.controller.setCenter(geoPoint)

            // Limpa marcadores anteriores e adiciona novo
            map.overlays.removeAll { it is Marker }
            val marker = Marker(map).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Você está aqui"
            }
            map.overlays.add(marker)
            map.invalidate()
        },
        modifier = modifier
    )
}

@Composable
fun CurrentTripCard(
    locationState: LocationState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardBg),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Localização",
                    tint = TravelGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Localização Atual",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelGreenDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (locationState) {
                is LocationState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = TravelGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Obtendo localização...", fontSize = 14.sp, color = TravelGray)
                    }
                }
                is LocationState.Success -> {
                    Text(
                        text = "Localização: ${locationState.city}",
                        fontSize = 14.sp,
                        color = TravelGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhuma viagem em andamento no momento.",
                        fontSize = 14.sp,
                        color = TravelGray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                is LocationState.Error -> {
                    Text(
                        text = locationState.message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is LocationState.Idle -> {
                    Text(
                        text = "Aguardando permissão de localização...",
                        fontSize = 14.sp,
                        color = TravelGray
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TravelGreenDark,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TravelGray
        )
    }
}

@Composable
fun DestinationCard(destination: com.senac.restapi.database.DestinationEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = destination.imageUrl,
                contentDescription = destination.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = destination.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelGreenDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = TravelGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = destination.rating.toString(),
                        fontSize = 14.sp,
                        color = TravelGray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "R$ ${String.format(Locale.getDefault(), "%.2f", destination.price)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelGreen
                )
            }
        }
    }
}
