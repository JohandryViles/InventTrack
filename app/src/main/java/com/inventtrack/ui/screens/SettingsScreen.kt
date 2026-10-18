package com.inventtrack.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventtrack.ui.components.ScreenContent
import com.inventtrack.ui.theme.AvailableGreen
import com.inventtrack.ui.theme.CriticalRed
import com.inventtrack.ui.theme.SecondaryTeal
import com.inventtrack.ui.theme.Shapes
import com.inventtrack.ui.theme.SurfaceLight

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onCategoriesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // Estado local de la sesion (simulacion de demo)
    var userName by rememberSaveable { mutableStateOf("Usuario Demo") }
    var userEmail by rememberSaveable { mutableStateOf("demo@inventtrack.com") }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var localSaveEnabled by rememberSaveable { mutableStateOf(true) }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    ScreenContent("Configuracion") {
        ProfileCard(
            userName = userName,
            userEmail = userEmail,
            onEditClick = { showEditProfileDialog = true }
        )

        Text("Preferencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = Shapes.medium) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Default.DarkMode,
                    title = "Modo oscuro",
                    subtitle = "Aplica el tema oscuro a toda la aplicacion",
                    checked = darkTheme,
                    onCheckedChange = onDarkThemeChange
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    subtitle = "Recibe alertas de stock bajo y movimientos",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSwitchRow(
                    icon = Icons.Default.Save,
                    title = "Guardado local",
                    subtitle = "Manten tus datos guardados en este dispositivo",
                    checked = localSaveEnabled,
                    onCheckedChange = { localSaveEnabled = it }
                )
            }
        }

        Text("Cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = Shapes.medium) {
            Column {
                SettingsActionRow(
                    icon = Icons.Default.Lock,
                    title = "Cambiar contrasena",
                    subtitle = "Actualiza tu contrasena de acceso",
                    onClick = { showChangePasswordDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.Edit,
                    title = "Editar perfil",
                    subtitle = "Modifica tu nombre y correo electronico",
                    onClick = { showEditProfileDialog = true }
                )
            }
        }

        Text("Mas opciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = Shapes.medium) {
            Column {
                SettingsActionRow(
                    icon = Icons.Default.Category,
                    title = "Categorias",
                    subtitle = "Organiza los productos por categorias",
                    onClick = onCategoriesClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.History,
                    title = "Historial",
                    subtitle = "Consulta los movimientos registrados",
                    onClick = onHistoryClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.Analytics,
                    title = "Analisis y estadisticas",
                    subtitle = "Revisa el rendimiento de tu inventario",
                    onClick = onAnalyticsClick
                )
            }
        }

        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = Shapes.medium,
            border = BorderStroke(1.dp, CriticalRed.copy(alpha = 0.6f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalRed)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesion", fontWeight = FontWeight.Bold)
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(onDismiss = { showChangePasswordDialog = false })
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = userName,
            initialEmail = userEmail,
            onSave = { name, email ->
                userName = name
                userEmail = email
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = CriticalRed
                )
            },
            title = { Text("Cerrar sesion") },
            text = { Text("Seguro que deseas cerrar sesion? Tendras que iniciar sesion nuevamente para acceder a tu inventario.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
                ) {
                    Text("Cerrar sesion")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    userName: String,
    userEmail: String,
    onEditClick: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = Shapes.medium) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initials = userName.trim()
                .split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2)
                .joinToString("")
                .ifEmpty { "U" }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SecondaryTeal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = SurfaceLight,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar perfil",
                    tint = SecondaryTeal
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SecondaryTeal)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = SecondaryTeal)
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SecondaryTeal)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = Shapes.medium,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Ocultar contrasena" else "Mostrar contrasena"
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SecondaryTeal,
            focusedLabelColor = SecondaryTeal
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ChangePasswordDialog(onDismiss: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordChanged by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (passwordChanged) "Contrasena actualizada" else "Cambiar contrasena") },
        text = {
            if (passwordChanged) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AvailableGreen
                    )
                    Text("Tu contrasena se actualizo correctamente (Simulacion de demo).")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PasswordField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            passwordError = null
                        },
                        label = "Contrasena actual",
                        visible = currentVisible,
                        onToggleVisibility = { currentVisible = !currentVisible }
                    )
                    PasswordField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            passwordError = null
                        },
                        label = "Nueva contrasena",
                        visible = newVisible,
                        onToggleVisibility = { newVisible = !newVisible }
                    )
                    PasswordField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            passwordError = null
                        },
                        label = "Confirmar contrasena",
                        visible = confirmVisible,
                        onToggleVisibility = { confirmVisible = !confirmVisible }
                    )
                    passwordError?.let {
                        Text(it, color = CriticalRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            if (passwordChanged) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                ) {
                    Text("Aceptar")
                }
            } else {
                Button(
                    onClick = {
                        passwordError = when {
                            currentPassword.isEmpty() -> "Ingresa tu contrasena actual."
                            newPassword.length < 6 -> "La nueva contrasena debe tener al menos 6 caracteres."
                            confirmPassword != newPassword -> "Las contrasenas no coinciden."
                            else -> null
                        }
                        if (passwordError == null) {
                            passwordChanged = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                ) {
                    Text("Actualizar")
                }
            }
        },
        dismissButton = {
            if (!passwordChanged) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
private fun EditProfileDialog(
    initialName: String,
    initialEmail: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameField by remember { mutableStateOf(initialName) }
    var emailField by remember { mutableStateOf(initialEmail) }
    var profileError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = {
                        nameField = it
                        profileError = null
                    },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    shape = Shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal,
                        focusedLabelColor = SecondaryTeal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emailField,
                    onValueChange = {
                        emailField = it
                        profileError = null
                    },
                    label = { Text("Correo electronico") },
                    singleLine = true,
                    shape = Shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal,
                        focusedLabelColor = SecondaryTeal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                profileError?.let {
                    Text(it, color = CriticalRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    profileError = when {
                        nameField.isBlank() -> "El nombre no puede estar vacio."
                        !emailField.contains("@") || !emailField.contains(".") -> "Ingresa un correo electronico valido."
                        else -> null
                    }
                    if (profileError == null) {
                        onSave(nameField.trim(), emailField.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
