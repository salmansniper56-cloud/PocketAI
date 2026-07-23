package com.pocketpalai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import com.pocketpalai.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: SettingsViewModel,
    onOpenDrawer: () -> Unit
) {
    val feedbackSubmitted by viewModel.feedbackSubmitted.collectAsState()
    var feedbackText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(feedbackSubmitted) {
        if (feedbackSubmitted) {
            Toast.makeText(context, "Thank you! Feedback submitted.", Toast.LENGTH_SHORT).show()
            feedbackText = ""
            viewModel.resetFeedbackStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Info", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.pocketpalai.R.drawable.logo),
                        contentDescription = "Pocket AI Logo",
                        modifier = Modifier.size(72.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Pocket AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A private AI assistant running language models, text-to-speech, and benchmarks on your phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Developer Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Made with ❤️ by a Pakistani 🇵🇰",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Privacy Guarantee
            Text("Privacy Guarantee", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Private",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("100% Private & Offline", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Every prompt, message, and document stays on your device. Nothing is uploaded or stored on external servers.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
