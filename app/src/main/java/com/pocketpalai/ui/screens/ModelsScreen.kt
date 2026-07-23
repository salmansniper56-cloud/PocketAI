package com.pocketpalai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import com.pocketpalai.data.model.HuggingFaceModel
import com.pocketpalai.data.model.LocalModel
import com.pocketpalai.ui.viewmodel.ModelsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(
    viewModel: ModelsViewModel,
    onOpenDrawer: () -> Unit
) {
    val localModels by viewModel.localModels.collectAsState()
    val showAddModelDialog by viewModel.showAddModelDialog.collectAsState()

    var isReadyToUseExpanded by remember { mutableStateOf(true) }
    var isAvailableExpanded by remember { mutableStateOf(true) }

    val readyModels = localModels.filter { it.isLoaded || it.isDownloaded }
    val availableModels = localModels.filter { !it.isLoaded && !it.isDownloaded }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Models", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddModelDialog() }) {
                        Icon(Icons.Default.Search, contentDescription = "Search HuggingFace", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddModelDialog() },
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Model from Hugging Face")
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Ready to Use Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isReadyToUseExpanded = !isReadyToUseExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ready to Use (${readyModels.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = if (isReadyToUseExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = Color.White
                        )
                    }
                }

                if (isReadyToUseExpanded) {
                    if (readyModels.isEmpty()) {
                        item {
                            Text(
                                text = "No models currently downloaded or loaded.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8E8E93),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(readyModels, key = { it.id }) { model ->
                            ModelCardItem(
                                model = model,
                                onLoad = { viewModel.loadModel(model.id) },
                                onOffload = { viewModel.offloadModel(model.id) },
                                onDownload = { viewModel.startDownloadForModel(model) },
                                onPause = { viewModel.pauseDownload(model) },
                                onResume = { viewModel.resumeDownload(model) },
                                onDelete = { viewModel.deleteModel(model.id) }
                            )
                        }
                    }
                }

                // Section 2: Available to Download Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAvailableExpanded = !isAvailableExpanded }
                            .padding(top = 12.dp, bottom = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available to Download (${availableModels.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = if (isAvailableExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Tap + button below to search & download Hugging Face models",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }

                if (isAvailableExpanded) {
                    items(availableModels, key = { it.id }) { model ->
                        ModelCardItem(
                            model = model,
                            onLoad = { viewModel.loadModel(model.id) },
                            onOffload = { viewModel.offloadModel(model.id) },
                            onDownload = { viewModel.startDownloadForModel(model) },
                            onPause = { viewModel.pauseDownload(model) },
                            onResume = { viewModel.resumeDownload(model) },
                            onDelete = { viewModel.deleteModel(model.id) }
                        )
                    }
                }
            }

            if (showAddModelDialog) {
                AddModelDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeAddModelDialog() }
                )
            }
        }
    }
}

@Composable
fun ModelCardItem(
    model: LocalModel,
    onLoad: () -> Unit,
    onOffload: () -> Unit,
    onDownload: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Color(0xFF141416),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${model.parameters} • ${model.quantization} • ${model.author}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }

                if (model.isLoaded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Loaded ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF34C759),
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34C759))
                        )
                    }
                }
            }

            if (model.downloadStatus == "DOWNLOADING" || model.downloadStatus == "PAUSED") {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { model.downloadProgress / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF1E88E5),
                    trackColor = Color(0xFF2C2C2E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${model.downloadProgress}% (${if (model.downloadStatus == "PAUSED") "Paused" else "Downloading..."})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8E8E93)
                    )
                    Text(
                        text = "${model.downloadedBytes / (1024 * 1024)} / ${model.sizeBytes / (1024 * 1024)} MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8E8E93)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    model.isLoaded -> {
                        Button(
                            onClick = onOffload,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B1E1E),
                                contentColor = Color(0xFFFF453A)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Offload",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    model.isDownloaded -> {
                        Button(
                            onClick = onLoad,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B382B),
                                contentColor = Color(0xFF34C759)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Load",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    model.downloadStatus == "DOWNLOADING" -> {
                        Button(
                            onClick = onPause,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF382E1B),
                                contentColor = Color(0xFFFF9F0A)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Pause", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    else -> {
                        Button(
                            onClick = {
                                when (model.downloadStatus) {
                                    "PAUSED" -> onResume()
                                    else -> onDownload()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E2A38),
                                contentColor = Color(0xFF0A84FF)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (model.downloadStatus) {
                                        "PAUSED" -> "Resume"
                                        "ERROR" -> "Retry"
                                        else -> "Download"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDelete() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddModelDialog(
    viewModel: ModelsViewModel,
    onDismiss: () -> Unit
) {
    val hfSearchResults by viewModel.hfSearchResults.collectAsState()
    val searchQuery by viewModel.hfSearchQuery.collectAsState()
    val hfToken by viewModel.hfToken.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Search HuggingFace, 1: Custom Repo / URL
    var customRepoInput by remember { mutableStateOf("") }
    var customTokenInput by remember { mutableStateOf(hfToken) }
    var selectedQuant by remember { mutableStateOf("Q4_K_M") }
    var showQuantDropdown by remember { mutableStateOf(false) }

    val quantsList = listOf("Q4_K_M", "Q5_K_M", "Q8_0", "F16")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Model from Hugging Face", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E8E93))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                // Tab Selection Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF2C2C2E),
                    contentColor = Color.White
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Search HF", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Custom Repo/URL", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchHuggingFace(it) },
                        placeholder = { Text("Search Hugging Face GGUF models...", color = Color(0xFF8E8E93)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E8E93)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color(0xFF3A3A3C),
                            focusedContainerColor = Color(0xFF2C2C2E),
                            unfocusedContainerColor = Color(0xFF2C2C2E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hfSearchResults, key = { it.id }) { hfModel ->
                            Surface(
                                color = Color(0xFF2C2C2E),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(hfModel.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                            Text("${hfModel.author} • ${hfModel.parameters} • ${hfModel.downloads} downloads", color = Color(0xFF8E8E93), fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.startHfDownload(hfModel, selectedQuant)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Custom Repo or Direct URL Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Enter Hugging Face Repository ID or Direct GGUF Model Download Link:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93)
                        )

                        OutlinedTextField(
                            value = customRepoInput,
                            onValueChange = { customRepoInput = it },
                            placeholder = { Text("e.g. bartowski/Gemma-2-2B-It-GGUF", color = Color(0xFF8E8E93)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E88E5),
                                unfocusedBorderColor = Color(0xFF3A3A3C),
                                focusedContainerColor = Color(0xFF2C2C2E),
                                unfocusedContainerColor = Color(0xFF2C2C2E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Text("Select Quantization Level:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8E93))

                        Box {
                            OutlinedButton(
                                onClick = { showQuantDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedQuant, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = showQuantDropdown,
                                onDismissRequest = { showQuantDropdown = false }
                            ) {
                                quantsList.forEach { q ->
                                    DropdownMenuItem(
                                        text = { Text(q, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedQuant = q
                                            showQuantDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = customTokenInput,
                            onValueChange = { customTokenInput = it },
                            placeholder = { Text("HF Token (Optional for gated models)", color = Color(0xFF8E8E93)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E88E5),
                                unfocusedBorderColor = Color(0xFF3A3A3C),
                                focusedContainerColor = Color(0xFF2C2C2E),
                                unfocusedContainerColor = Color(0xFF2C2C2E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (customRepoInput.isNotBlank()) {
                                    viewModel.startCustomHfDownload(customRepoInput, selectedQuant, customTokenInput)
                                    onDismiss()
                                }
                            },
                            enabled = customRepoInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Hugging Face Download", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
