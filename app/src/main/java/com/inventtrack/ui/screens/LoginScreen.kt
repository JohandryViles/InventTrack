package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo o Título
        Text(
            text = "InvenTrack",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        TextButton(
            onClick = { /* Acción para recuperar contraseña */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("¿Olvidaste tu contraseña?")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onRegisterClick) {
            Text("Crear una cuenta")
        }
    }
}