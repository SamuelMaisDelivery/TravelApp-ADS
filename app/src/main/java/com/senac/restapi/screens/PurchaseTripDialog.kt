package com.senac.restapi.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senac.restapi.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseTripDialog(
    productTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (startDate: Long, endDate: Long, orcamento: Double) -> Unit
) {
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var orcamentoText by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Adquirir Viagem",
                fontSize = 20.sp,
                color = TravelGreenDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = productTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TravelGray
                )

                Text(
                    text = "Selecione as datas da sua viagem",
                    style = MaterialTheme.typography.bodySmall,
                    color = TravelGray
                )

                // Data de Início
                OutlinedTextField(
                    value = startDate?.let { dateFormatter.format(Date(it)) } ?: "",
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
                    value = endDate?.let { dateFormatter.format(Date(it)) } ?: "",
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

                // Orçamento disponível
                OutlinedTextField(
                    value = orcamentoText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*[.,]?\\d{0,2}$"))) {
                            orcamentoText = input
                        }
                    },
                    label = { Text("Orçamento disponível (R$)") },
                    placeholder = { Text("Ex: 3000,00") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = TravelGreen)
                    },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TravelGreen,
                        unfocusedBorderColor = TravelGreenLight
                    )
                )
                Text(
                    text = "Vamos montar um roteiro que aproveite ao máximo esse valor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TravelGray
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
                    val orcamento = orcamentoText.replace(",", ".").toDoubleOrNull()
                    when {
                        startDate == null -> errorMessage = "Selecione a data de início"
                        endDate == null -> errorMessage = "Selecione a data de fim"
                        endDate!! < startDate!! -> errorMessage = "Data de fim deve ser posterior à data de início"
                        orcamento == null || orcamento <= 0.0 -> errorMessage = "Informe um orçamento válido"
                        else -> {
                            onConfirm(startDate!!, endDate!!, orcamento)
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TravelGreen)
            ) {
                Text("Confirmar")
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedDateMillis
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate ?: startDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = datePickerState.selectedDateMillis
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
