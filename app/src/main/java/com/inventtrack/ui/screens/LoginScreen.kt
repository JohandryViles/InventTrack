package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Validaciones simples con remember y mutableStateOf
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    fun validateAndLogin() {
        errorMessage = when {
            email.trim().isEmpty() -> "Por favor ingresa tu correo electrónico."
            !email.contains("@") || !email.contains(".") -> "Ingresa un correo electrónico válido."
            password.isEmpty() -> "Por favor ingresa tu contraseña."
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            else -> null
        }

        if (errorMessage == null) {
            onLoginClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono y Logo
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = "Logo InventTrack",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "InventTrack",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Control inteligente de tu inventario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de Correo
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (errorMessage != null) errorMessage = null
                },
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email")
                },
                isError = errorMessage != null && (email.trim().isEmpty() || !email.contains("@")),
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

            // Campo de Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (errorMessage != null) errorMessage = null
                },
                label = { Text("Contraseña") },
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
                isError = errorMessage != null && (password.isEmpty() || password.length < 6),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        validateAndLogin()
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Mensaje de error
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ¿Olvidaste tu contraseña?
            TextButton(
                onClick = { showForgotPasswordDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Iniciar Sesión
            Button(
                onClick = {
                    focusManager.clearFocus()
                    validateAndLogin()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de acceso rápido modo demo
            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text("Acceder en modo demo", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enlace Crear una cuenta
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onRegisterClick) {
                    Text(
                        text = "Crear una cuenta",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Diálogo de recuperación de contraseña
    if (showForgotPasswordDialog) {
        var recoveryEmail by remember { mutableStateOf(email) }
        var recoverySent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text(if (recoverySent) "Correo enviado" else "¿Olvidaste tu contraseña?") },
            text = {
                if (recoverySent) {
                    Text("Se han enviado las instrucciones de recuperación a $recoveryEmail (Simulación de demo).")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ingresa tu correo para recibir un enlace de restablecimiento:")
                        OutlinedTextField(
                            value = recoveryEmail,
                            onValueChange = { recoveryEmail = it },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (recoverySent) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Aceptar")
                    }
                } else {
                    Button(
                        onClick = {
                            if (recoveryEmail.isNotBlank()) {
                                recoverySent = true
                            }
                        }
                    ) {
                        Text("Enviar enlace")
                    }
                }
            },
            dismissButton = {
                if (!recoverySent) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}