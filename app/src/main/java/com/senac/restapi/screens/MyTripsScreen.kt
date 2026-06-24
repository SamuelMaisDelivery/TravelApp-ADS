package com.senac.restapi.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.senac.restapi.database.TripEntity
import com.senac.restapi.ui.theme.*
import com.senac.restapi.viewmodel.TripViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripsScreen(
    tripViewModel: TripViewModel,
    userId: Int
) {
    val trips by tripViewModel.userTrips.collectAsStateWithLifecycle()
    var tripToEdit by remember { mutableStateOf<TripEntity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        tripViewModel.setCurrentUserId(userId)
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
        if (trips.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "✈️",
                    fontSize = 48.sp
                )
                Text(
                    text = "Nenhuma viagem adquirida",
                    color = TravelGray,
                    fontSize = 16.sp
                )
                Text(
                    text = "Adquira sua primeira viagem!",
                    color = TravelGray.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(trips, key = { it.id }) { trip ->
                    MyTripItem(
                        trip = trip,
                        onLongClick = {
                            tripToEdit = trip
                            showEditDialog = true
                        },
                        onDelete = {
                            tripViewModel.deleteTrip(trip)
                        }
                    )

                    // Barra preta separadora
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.Black
                    )
                }
            }
        }
    }

    // Dialog de edição
    if (showEditDialog && tripToEdit != null) {
        EditTripDialog(
            trip = tripToEdit!!,
            onDismiss = {
                showEditDialog = false
                tripToEdit = null
            },
            onConfirm = { tripType, startDate, endDate ->
                val updatedTrip = tripToEdit!!.copy(
                    tripType = tripType,
                    startDate = startDate,
                    endDate = endDate
                )
                tripViewModel.updateTrip(updatedTrip)
                showEditDialog = false
                tripToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyTripItem(
    trip: TripEntity,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> TravelError
            else -> Color.Transparent
        },
        label = "backgroundColor"
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TravelCardBg),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = trip.productImage,
                    contentDescription = trip.productTitle,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.productTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TravelGreenDark,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (trip.tripType) {
                                "Lazer" -> Icons.Default.BeachAccess
                                "Negócios" -> Icons.Default.BusinessCenter
                                else -> Icons.Default.MoreHoriz
                            },
                            contentDescription = trip.tripType,
                            tint = TravelGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = trip.tripType,
                            fontSize = 13.sp,
                            color = TravelGray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TravelGreen.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Início: ${dateFormatter.format(Date(trip.startDate))}",
                                fontSize = 12.sp,
                                color = TravelGreenDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TravelBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Fim: ${dateFormatter.format(Date(trip.endDate))}",
                                fontSize = 12.sp,
                                color = TravelBlueDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTripDialog(
    trip: TripEntity,
    onDismiss: () -> Unit,
    onConfirm: (tripType: String, startDate: Long, endDate: Long) -> Unit
) {
    var selectedTripType by remember { mutableStateOf(trip.tripType) }
    var expanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(trip.startDate) }
    var endDate by remember { mutableStateOf(trip.endDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val tripTypes = listOf("Lazer", "Negócios", "Outros")
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Viagem",
                fontSize = 20.sp,
                color = TravelGreenDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tipo de Viagem
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedTripType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Viagem") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TravelGreen,
                            unfocusedBorderColor = TravelGreenLight
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        tripTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedTripType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Data de Início
                OutlinedTextField(
                    value = dateFormatter.format(Date(startDate)),
                    onValueChange = {},
                    label = { Text("Data de Início") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar data", tint = TravelGreen)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TravelGreen,
                        unfocusedBorderColor = TravelGreenLight
                    )
                )

                // Data de Fim
                OutlinedTextField(
                    value = dateFormatter.format(Date(endDate)),
                    onValueChange = {},
                    label = { Text("Data de Fim") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar data", tint = TravelGreen)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TravelGreen,
                        unfocusedBorderColor = TravelGreenLight
                    )
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = TravelError,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (endDate < startDate) {
                        errorMessage = "Data de fim deve ser posterior à data de início"
                    } else {
                        onConfirm(selectedTripType, startDate, endDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TravelGreen)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TravelGray)
            }
        },
        containerColor = TravelCardBg,
        shape = RoundedCornerShape(16.dp)
    )

    // DatePicker para Data de Início
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    showStartDatePicker = false
                    errorMessage = ""
                }) {
                    Text("OK", color = TravelGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancelar", color = TravelGray)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = TravelCardBg)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DatePicker para Data de Fim
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = it }
                    showEndDatePicker = false
                    errorMessage = ""
                }) {
                    Text("OK", color = TravelGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancelar", color = TravelGray)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = TravelCardBg)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
