package net.emite.androidtv_project.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import net.emite.androidtv_project.presentation.viewmodel.SetupViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel = hiltViewModel()
) {
    var instancia by remember { mutableStateOf("") }
    val saved by viewModel.saved.collectAsState()

    // No necesitamos hacer nada aquí: cuando se guarda la instancia,
    // MainViewModel detecta el cambio en DB y navega al Slideshow automáticamente.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B2A3B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Card(
            modifier = Modifier
                .width(480.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Configuración Inicial",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )

                Text(
                    text = "Introduce el nombre de tu instancia de Tegestiona",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                androidx.compose.material3.OutlinedTextField(
                    value = instancia,
                    onValueChange = { instancia = it },
                    label = { androidx.compose.material3.Text("Instancia (ej: demo)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Text(
                    text = "https://$instancia.tegestiona.es",
                    fontSize = 12.sp,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                androidx.compose.material3.Button(
                    onClick = { viewModel.saveInstancia(instancia) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = instancia.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "Guardar y Continuar",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
