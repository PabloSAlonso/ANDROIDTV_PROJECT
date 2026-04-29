package net.emite.androidtv_project.presentation.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import net.emite.androidtv_project.presentation.viewmodel.SetupViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel = hiltViewModel()
) {
    var instancia by remember { mutableStateOf("") }
    val saved by viewModel.saved.collectAsState()

    val focusRequester = remember { FocusRequester() }
    var textFieldFocused by remember { mutableStateOf(false) }
    var buttonFocused by remember { mutableStateOf(false) }

    var menuExpanded by remember { mutableStateOf(false) }
    var menuFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Ignore if not attached yet
        }
    }

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
        // Dropdown Menu Opciones
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .onFocusChanged { menuFocused = it.isFocused }
                    .border(
                        width = if (menuFocused) 2.dp else 0.dp,
                        color = if (menuFocused) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(Color(0xFF1B2A3B))
            ) {
                DropdownMenuItem(
                    text = { androidx.compose.material3.Text("Minimizar (Dejar en segundo plano)", color = Color.White) },
                    onClick = {
                        menuExpanded = false
                        (context as? Activity)?.moveTaskToBack(true)
                    }
                )
                DropdownMenuItem(
                    text = { androidx.compose.material3.Text("Cerrar app completamente", color = Color.White) },
                    onClick = {
                        menuExpanded = false
                        (context as? Activity)?.finishAffinity()
                        exitProcess(0)
                    }
                )
            }
        }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { textFieldFocused = it.isFocused }
                        .border(
                            width = if (textFieldFocused) 3.dp else 0.dp,
                            color = if (textFieldFocused) Color(0xFF42A5F5) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (instancia.isNotBlank()) {
                                viewModel.saveInstancia(instancia)
                            }
                        }
                    )
                )

                Text(
                    text = "https://$instancia.tegestiona.es",
                    fontSize = 12.sp,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                androidx.compose.material3.Button(
                    onClick = { viewModel.saveInstancia(instancia) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { buttonFocused = it.isFocused }
                        .border(
                            width = if (buttonFocused) 3.dp else 0.dp,
                            color = if (buttonFocused) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    enabled = instancia.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (buttonFocused) Color(0xFF1976D2) else Color(0xFF1565C0)
                    )
                ) {
                    androidx.compose.material3.Text(
                        text = "Guardar y Continuar",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
