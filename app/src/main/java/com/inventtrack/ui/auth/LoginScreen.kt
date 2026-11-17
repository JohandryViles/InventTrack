package com.inventtrack.ui.auth

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.di.AppViewModelProvider
import com.inventtrack.ui.components.ObserveAsEvents
import com.inventtrack.ui.components.OutlinedSecondaryButton
import com.inventtrack.ui.components.PrimaryButton
import com.inventtrack.ui.theme.CriticalRed
import com.inventtrack.ui.theme.SecondaryTeal
import com.inventtrack.ui.theme.Shapes

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LoginEvent.LoginSuccess -> onLoginSuccess()
        }
    }

    LoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onDemoLoginClick = viewModel::loginDemo,
        onOpenRecoveryDialog = viewModel::openRecoveryDialog,
        onDismissRecoveryDialog = viewModel::dismissRecoveryDialog,
        onRecoveryEmailChange = viewModel::onRecoveryEmailChange,
        onSendRecovery = viewModel::sendPasswordRecovery,
        onRegisterClick = onRegisterClick
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onDemoLoginClick: () -> Unit,
    onOpenRecoveryDialog: () -> Unit,
    onDismissRecoveryDialog: () -> Unit,
    onRecoveryEmailChange: (String) -> Unit,
    onSendRecovery: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
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
                tint = SecondaryTeal
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
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email")
                },
                isError = uiState.error != null && (uiState.email.trim().isEmpty() || !uiState.email.contains("@")),
                shape = Shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryTeal,
                    focusedLabelColor = SecondaryTeal
                ),
                enabled = !uiState.isLoading,
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
                value = uiState.password,
                onValueChange = onPasswordChange,
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
                isError = uiState.error != null && (uiState.password.isEmpty() || uiState.password.length < 6),
                shape = Shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryTeal,
                    focusedLabelColor = SecondaryTeal
                ),
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onLoginClick()
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Mensaje de error
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = CriticalRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ¿Olvidaste tu contraseña?
            TextButton(
                onClick = onOpenRecoveryDialog,
                enabled = !uiState.isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryTeal
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Iniciar Sesión
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = SecondaryTeal
                )
            } else {
                PrimaryButton(
                    text = "Iniciar sesión",
                    onClick = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de acceso rápido modo demo
            OutlinedSecondaryButton(
                text = "Acceder en modo demo",
                onClick = onDemoLoginClick,
                modifier = Modifier.fillMaxWidth()
            )

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
                TextButton(
                    onClick = onRegisterClick,
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "Crear una cuenta",
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal
                    )
                }
            }
        }
    }

    // Diálogo de recuperación de contraseña controlado por ViewModel
    if (uiState.showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = onDismissRecoveryDialog,
            title = { Text(if (uiState.recoverySent) "Correo enviado" else "¿Olvidaste tu contraseña?") },
            text = {
                if (uiState.recoverySent) {
                    Text("Se han enviado las instrucciones de recuperación a ${uiState.recoveryEmail} (Simulación de demo).")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ingresa tu correo para recibir un enlace de restablecimiento:")
                        OutlinedTextField(
                            value = uiState.recoveryEmail,
                            onValueChange = onRecoveryEmailChange,
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            isError = uiState.recoveryError != null,
                            shape = Shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecondaryTeal,
                                focusedLabelColor = SecondaryTeal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        uiState.recoveryError?.let { err ->
                            Text(text = err, color = CriticalRed, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                if (uiState.recoverySent) {
                    TextButton(onClick = onDismissRecoveryDialog) {
                        Text("Aceptar")
                    }
                } else {
                    Button(
                        onClick = onSendRecovery,
                        enabled = !uiState.isRecoveryLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                    ) {
                        if (uiState.isRecoveryLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.surface)
                        } else {
                            Text("Enviar enlace")
                        }
                    }
                }
            },
            dismissButton = {
                if (!uiState.recoverySent) {
                    TextButton(onClick = onDismissRecoveryDialog) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}
