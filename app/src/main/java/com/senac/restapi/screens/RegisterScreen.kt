package com.senac.restapi.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senac.restapi.ui.theme.*
import com.senac.restapi.viewmodel.RegisterState
import com.senac.restapi.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    userViewModel: UserViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(value = false) }
    var confirmPasswordVisible by remember { mutableStateOf(value = false) }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val registerState by userViewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Reagir ao estado do Room
    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is RegisterState.Success -> {
                snackbarHostState.showSnackbar("Cadastro realizado com sucesso! 🎉")
                userViewModel.resetState()
                onRegisterSuccess()
            }
            is RegisterState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                userViewModel.resetState()
            }
            else -> Unit
        }
    }

    fun validate(): Boolean {
        var valid = true
        nameError = if (name.isBlank()) { valid = false; "Nome obrigatório" } else ""
        emailError = if (!email.contains("@") || email.isBlank()) { valid = false; "E-mail inválido" } else ""
        phoneError = if (phone.length < 8) { valid = false; "Telefone inválido" } else ""
        passwordError = if (password.length < 6) { valid = false; "Mínimo 6 caracteres" } else ""
        confirmPasswordError = if (confirmPassword != password) { valid = false; "Senhas não coincidem" } else ""
        return valid
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Criar Conta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TravelOnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TravelGreen,
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
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "👤 Dados do Viajante",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelGreenDark,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TravelCardBg),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Nome
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; nameError = "" },
                            label = { Text("Nome completo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TravelGreen) },
                            isError = nameError.isNotEmpty(),
                            supportingText = if (nameError.isNotEmpty()) { { Text(nameError, color = TravelError) } } else null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TravelGreen,
                                unfocusedBorderColor = TravelGreenLight
                            )
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; emailError = "" },
                            label = { Text("E-mail") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TravelGreen) },
                            isError = emailError.isNotEmpty(),
                            supportingText = if (emailError.isNotEmpty()) { { Text(emailError, color = TravelError) } } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TravelGreen,
                                unfocusedBorderColor = TravelGreenLight
                            )
                        )

                        // Telefone
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; phoneError = "" },
                            label = { Text("Telefone") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TravelGreen) },
                            isError = phoneError.isNotEmpty(),
                            supportingText = if (phoneError.isNotEmpty()) { { Text(phoneError, color = TravelError) } } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TravelGreen,
                                unfocusedBorderColor = TravelGreenLight
                            )
                        )

                        // Senha
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; passwordError = "" },
                            label = { Text("Senha") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TravelGreen) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = TravelGray
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = passwordError.isNotEmpty(),
                            supportingText = if (passwordError.isNotEmpty()) { { Text(passwordError, color = TravelError) } } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TravelGreen,
                                unfocusedBorderColor = TravelGreenLight
                            )
                        )

                        // Confirmar Senha
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; confirmPasswordError = "" },
                            label = { Text("Confirmar senha") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TravelGreen) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = TravelGray
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = confirmPasswordError.isNotEmpty(),
                            supportingText = if (confirmPasswordError.isNotEmpty()) { { Text(confirmPasswordError, color = TravelError) } } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TravelGreen,
                                unfocusedBorderColor = TravelGreenLight
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (validate()) {
                            userViewModel.registerUser(name, email, phone, password)
                        }
                    },
                    enabled = registerState !is RegisterState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TravelGreen)
                ) {
                    if (registerState is RegisterState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = TravelOnPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cadastrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
