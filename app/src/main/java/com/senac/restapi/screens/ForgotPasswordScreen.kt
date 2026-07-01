package com.senac.restapi.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senac.restapi.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onEmailSent: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(value = false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Senha", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TravelOnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TravelBlue,
                    titleContentColor = TravelOnPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TravelBackground)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Ícone e descrição
                Text(text = "🔑", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Esqueceu sua senha?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelBlueDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Digite seu e-mail cadastrado e enviaremos um link para redefinir sua senha.",
                    fontSize = 14.sp,
                    color = TravelGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TravelCardBg),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; emailError = "" },
                            label = { Text("E-mail cadastrado") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TravelBlue) },
                            isError = emailError.isNotEmpty(),
                            supportingText = if (emailError.isNotEmpty()) { { Text(emailError, color = TravelError) } } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TravelBlue,
                                unfocusedBorderColor = TravelBlueLight
                            )
                        )

                        if (showSuccess) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TravelGreenLight.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "✅ E-mail de recuperação enviado! Verifique sua caixa de entrada.",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 13.sp,
                                    color = TravelGreenDark
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (email.isBlank() || !email.contains("@")) {
                                    emailError = "E-mail inválido"
                                } else {
                                    showSuccess = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TravelBlue)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar link de recuperação", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        if (showSuccess) {
                            OutlinedButton(
                                onClick = onEmailSent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TravelGreenDark),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, TravelGreen)
                            ) {
                                Text("Voltar para Login", fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
