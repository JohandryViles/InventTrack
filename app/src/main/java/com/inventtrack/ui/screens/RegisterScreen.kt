package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventtrack.ui.components.OutlinedSecondaryButton
import com.inventtrack.ui.components.PrimaryButton
import com.inventtrack.ui.theme.CriticalRed
import com.inventtrack.ui.theme.PrimaryDarkBlue
import com.inventtrack.ui.theme.SecondaryTeal
import com.inventtrack.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Validaciones simples con remember y mutableStateOf
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    fun validateAndRegister() {
        errorMessage = when {
            name.trim().isEmpty() -> "Por favor ingresa tu nombre completo."
            name.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres."
            email.trim().isEmpty() -> "Por favor ingresa tu correo electrónico."
            !email.contains("@") || !email.contains(".") -> "Ingresa un correo electrónico válido."
            password.isEmpty() -> "Por favor ingresa tu contraseña."
            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            password != confirmPassword -> "Las contraseñas no coinciden."
            else -> null
        }

        if (errorMessage == null) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryDarkBlue)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Únete a InventTrack hoy mismo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                // Campo Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Nombre completo") },
                    placeholder = { Text("Ej: Andy García") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Nombre")
                    },
                    isError = errorMessage != null && (name.trim().isEmpty() || name.trim().length < 3),
                    shape = Shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal,
                        focusedLabelColor = SecondaryTeal
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Correo electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("ejemplo@correo.com") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "Correo")
                    },
                    isError = errorMessage != null && (email.trim().isEmpty() || !email.contains("@")),
                    shape = Shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal,
                        focusedLabelColor = SecondaryTeal
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Contraseña (Mínimo 8 caracteres)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Contraseña")
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = errorMessage != null && (password.isEmpty() || password.length < 8),
                    shape = Shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal,
                        focusedLabelColor = SecondaryTeal
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Confirmar Contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirmar contraseña")
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Ocultar confirmación" else "Mostrar confirmación"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = errorMessage != null && (confirmPassword != password),
                    shape = Shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal,
                        focusedLabelColor = SecondaryTeal
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            validateAndRegister()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Mensaje de error si falla la validación
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = CriticalRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Botón Registrarme usando componente PrimaryButton
                PrimaryButton(
                    text = "Registrarme",
                    onClick = {
                        focusManager.clearFocus()
                        validateAndRegister()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón volver a iniciar sesión usando OutlinedSecondaryButton
                OutlinedSecondaryButton(
                    text = "Volver a iniciar sesión",
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Modal de confirmación de registro exitoso
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onRegisterClick()
            },
            title = { Text("¡Registro exitoso!") },
            text = { Text("Tu cuenta ha sido creada correctamente con el nombre $name. Ya puedes comenzar a gestionar tu inventario.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onRegisterClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                ) {
                    Text("Continuar")
                }
            }
        )
    }
}