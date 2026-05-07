package net.emite.androidtv_project.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import net.emite.androidtv_project.core.boot.BootLogger
import net.emite.androidtv_project.presentation.theme.DarkBackground

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BootDebugScreen(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf(BootLogger.getRecentLogs(context)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground.copy(alpha = 0.95f))
            .padding(32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BOOT DEBUG LOGS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                
                Row {
                    Button(onClick = { 
                        BootLogger.clearLogs(context)
                        logs = emptyList()
                    }) {
                        Text("Borrar Logs")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay logs registrados.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            color = when {
                                log.contains("ERROR") || log.contains("Error") || log.contains("failure") -> Color.Red
                                log.contains("SUCCESS") || log.contains("éxito") -> Color.Green
                                log.contains("RECEIVER") -> Color.Cyan
                                else -> Color.White
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
