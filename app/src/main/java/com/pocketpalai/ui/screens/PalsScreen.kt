package com.pocketpalai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketpalai.data.model.Pal
import com.pocketpalai.ui.viewmodel.PalsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PalsScreen(
    viewModel: PalsViewModel,
    onOpenDrawer: () -> Unit
) {
    val myPals by viewModel.myPals.collectAsState()
    val marketplacePals by viewModel.marketplacePals.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pals & Marketplace", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create Pal")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search Pals by name or role...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My Installed Pals (${myPals.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("PalsHub Marketplace") }
                )
            }

            val listToShow = if (selectedTab == 0) {
                myPals.filter { it.name.contains(searchQuery, true) || it.role.contains(searchQuery, true) }
            } else {
                marketplacePals.filter { it.name.contains(searchQuery, true) || it.role.contains(searchQuery, true) }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(listToShow, key = { it.id }) { pal ->
                    PalItemCard(
                        pal = pal,
                        isMarketplace = selectedTab == 1,
                        onInstall = { viewModel.installFromMarketplace(pal) },
                        onDelete = { viewModel.deletePal(pal.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePalDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, type, prompt, avatar, role, location, tools ->
                viewModel.savePal(
                    name = name,
                    type = type,
                    systemPrompt = prompt,
                    avatar = avatar,
                    colorHex = "#1976D2",
                    role = role,
                    location = location,
                    enableTools = tools
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun PalItemCard(
    pal: Pal,
    isMarketplace: Boolean,
    onInstall: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = pal.avatar, style = MaterialTheme.typography.titleLarge)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (pal.type == "Roleplay") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = pal.type,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${pal.role} • ${pal.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = pal.systemPrompt,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isMarketplace) {
                Button(onClick = onInstall) {
                    Text("Get")
                }
            } else if (!pal.isPreset) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Pal",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePalDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Assistant") }
    var prompt by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("🤖") }
    var role by remember { mutableStateOf("AI Companion") }
    var location by remember { mutableStateOf("Mobile") }
    var enableTools by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Pal Persona", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pal Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == "Assistant",
                        onClick = { type = "Assistant" }
                    )
                    Text("Assistant")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = type == "Roleplay",
                        onClick = { type = "Roleplay" }
                    )
                    Text("Roleplay")
                }

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role / Profession") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("System Prompt") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = enableTools,
                        onCheckedChange = { enableTools = it }
                    )
                    Text("Enable Built-in Talents/Tools")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, type, prompt, avatar, role, location, enableTools) },
                enabled = name.isNotBlank() && prompt.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
