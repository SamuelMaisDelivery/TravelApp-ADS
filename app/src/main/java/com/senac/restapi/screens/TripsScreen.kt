package com.senac.restapi.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.senac.restapi.database.DestinationEntity
import com.senac.restapi.ui.theme.*
import com.senac.restapi.viewmodel.DestinationViewModel
import com.senac.restapi.viewmodel.DestinationLoadState
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TripsScreen(
    destinationViewModel: DestinationViewModel,
    userId: Int,
    onPurchaseTrip: (destinationId: Int, destinationTitle: String, destinationImage: String, destinationPrice: Double, cidade: String, tipo: String) -> Unit
) {
    val destinations by destinationViewModel.destinations.collectAsStateWithLifecycle()
    val loadState by destinationViewModel.loadState.collectAsStateWithLifecycle()

    var expandedDestinationId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        destinationViewModel.loadDestinations()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TravelGreen.copy(alpha = 0.1f), TravelBlue.copy(alpha = 0.1f))
                )
            )
    ) {
        when (loadState) {
            is DestinationLoadState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = TravelGreen
                )
            }
            is DestinationLoadState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Erro ao carregar viagens",
                        color = TravelError,
                        fontSize = 16.sp
                    )
                    Text(
                        text = (loadState as DestinationLoadState.Error).message,
                        color = TravelGray,
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = { destinationViewModel.loadDestinations() },
                        colors = ButtonDefaults.buttonColors(containerColor = TravelGreen)
                    ) {
                        Text("Tentar Novamente")
                    }
                }
            }
            else -> {
                if (destinations.isEmpty()) {
                    Text(
                        text = "Nenhuma viagem disponível",
                        modifier = Modifier.align(Alignment.Center),
                        color = TravelGray,
                        fontSize = 16.sp
                    )
                } else {
                    LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(destinations) { destination ->
                        TripCatalogItem(
                            destination = destination,
                            isExpanded = expandedDestinationId == destination.id,
                            onExpandClick = {
                                expandedDestinationId = if (expandedDestinationId == destination.id) null else destination.id
                            },
                            onPurchaseClick = {
                                onPurchaseTrip(
                                    destination.id,
                                    destination.title,
                                    destination.imageUrl,
                                    destination.price,
                                    destination.cidade,
                                    destination.tipo
                                )
                            }
                        )
                    }
                }
                }
            }
        }
    }
}

@Composable
fun TripCatalogItem(
    destination: DestinationEntity,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onPurchaseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = destination.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TravelGreenDark,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R$ ${String.format("%.2f", destination.price)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TravelGreen
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = TravelGreen
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = TravelGreenLight.copy(alpha = 0.4f))

                    Text(
                        text = destination.description,
                        fontSize = 14.sp,
                        color = TravelGray,
                        lineHeight = 20.sp
                    )

                    Button(
                        onClick = onPurchaseClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TravelGreen)
                    ) {
                        Text(
                            text = "Adquirir Viagem",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
