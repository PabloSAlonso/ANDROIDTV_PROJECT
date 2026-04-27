package net.emite.androidtv_project.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import androidx.compose.material3.CircularProgressIndicator
import net.emite.androidtv_project.presentation.viewmodel.LoginUiState
import net.emite.androidtv_project.presentation.viewmodel.LoginViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    var instancia by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Inicio de Sesión - Tegestiona",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Nota: OutlinedTextField no está en TV Material3 directamente, 
        // usamos el de Material3 estándar o implementamos uno simple.
        // Para TV es mejor usar componentes que manejen bien el foco.

        androidx.compose.material3.OutlinedTextField(
            value = instancia,
            onValueChange = { instancia = it },
            label = { androidx.compose.material3.Text("Instancia") },
            modifier = Modifier.width(400.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { androidx.compose.material3.Text("Correo Electrónico") },
            modifier = Modifier.width(400.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { androidx.compose.material3.Text("Contraseña") },
            modifier = Modifier.width(400.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                androidx.compose.material3.IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    androidx.compose.material3.Icon(imageVector = image, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState is LoginUiState.Loading) {
            CircularProgressIndicator()
        } else {
            androidx.compose.material3.Button(
                onClick = { viewModel.login(instancia, correo, password) },
                modifier = Modifier.width(200.dp)
            ) {
                androidx.compose.material3.Text("Entrar")
            }
        }

        if (uiState is LoginUiState.Error) {
            val errorMessage = (uiState as LoginUiState.Error).message
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
