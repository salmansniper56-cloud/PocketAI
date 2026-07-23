package com.pocketpalai.ui.screens

import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketpalai.ui.components.MessageBubble
import com.pocketpalai.ui.viewmodel.ChatViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenDrawer: () -> Unit
) {
    val messages by viewModel.currentMessages.collectAsState()
    val models by viewModel.models.collectAsState()
    val isWebSearchEnabled by viewModel.isWebSearchEnabled.collectAsState()
    val selectedSessionId by viewModel.selectedSessionId.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingText by viewModel.streamingText.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }
    var attachedFileType by remember { mutableStateOf<String?>(null) } // "Photo" or "File"

    val currentSession = sessions.find { it.id == selectedSessionId }
    val currentTitle = currentSession?.title ?: "Chat"
    val loadedModel = models.find { it.isLoaded } ?: models.firstOrNull()

    val listState = rememberLazyListState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            attachedFileType = "Photo"
            attachedFileName = uri.lastPathSegment?.substringAfterLast("/") ?: "photo.jpg"
            Toast.makeText(context, "Photo attached: $attachedFileName", Toast.LENGTH_SHORT).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            attachedFileType = "File"
            attachedFileName = uri.lastPathSegment?.substringAfterLast("/") ?: "file.txt"
            Toast.makeText(context, "File attached: $attachedFileName", Toast.LENGTH_SHORT).show()
        }
    }

    // Setup Android System TTS safely
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        try {
            ttsInstance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        ttsInstance?.language = Locale.US
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            tts = ttsInstance
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            try {
                ttsInstance?.stop()
                ttsInstance?.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable { showModelMenu = true }
                    ) {
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = loadedModel?.name ?: "No Model Loaded (Select in Models)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false }
                        ) {
                            models.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${if (model.isLoaded) "🟢 " else if (model.isDownloaded) "📦 " else "⏳ "}${model.name}${if (!model.isDownloaded) " (Not Downloaded)" else ""}",
                                            fontWeight = if (model.isLoaded) FontWeight.Bold else FontWeight.Normal,
                                            color = if (model.isDownloaded) Color.Unspecified else Color.Gray
                                        )
                                    },
                                    onClick = {
                                        if (model.isDownloaded) {
                                            viewModel.selectModel(model.id)
                                        }
                                        showModelMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startNewChat() }) {
                        Icon(Icons.Default.EditNote, contentDescription = "New Chat", tint = Color.White)
                    }
                    IconButton(onClick = { /* Options menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            // Chat Messages Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start a conversation",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageBubble(
                                message = msg,
                                onSpeakMessage = { text ->
                                    try {
                                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "POCKET_AI_TTS")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            )
                        }

                        if (isGenerating) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, end = 48.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    Surface(
                                        color = Color(0xFF1C1C1E),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = if (streamingText.isNotBlank()) streamingText else "⏳ Generating...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Input Container Box (Screenshots 1, 5 & 6)
            Surface(
                color = Color(0xFF141416),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Attached Chip Preview
                    if (attachedFileName != null) {
                        Surface(
                            color = Color(0xFF2C2C2E),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (attachedFileType == "Photo") Icons.Default.Image else Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = Color(0xFF1E88E5),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$attachedFileType: $attachedFileName",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove attachment",
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            attachedFileName = null
                                            attachedFileType = null
                                        }
                                )
                            }
                        }
                    }

                    // Input Text Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Type your message here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6E6E73)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom Controls Bar Inside Input Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Control Icons (+ and ^)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                IconButton(
                                    onClick = { showAttachmentMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Attachment Options",
                                        tint = Color(0xFF8E8E93)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showAttachmentMenu,
                                    onDismissRequest = { showAttachmentMenu = false },
                                    modifier = Modifier.background(Color(0xFF2C2C2E))
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CameraAlt,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64B5F6),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text("Camera Vision Lens", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            showAttachmentMenu = false
                                            Toast.makeText(context, "Opening Camera Lens... Point camera at object", Toast.LENGTH_SHORT).show()
                                            viewModel.sendMessage("[Camera Lens Snapshot]: User scanned object with phone camera. Extract key visual facts and information.")
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.InsertDriveFile,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text("Add File", color = Color.White, fontWeight = FontWeight.Medium)
                                            }
                                        },
                                        onClick = {
                                            showAttachmentMenu = false
                                            filePickerLauncher.launch("*/*")
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text("Photo", color = Color.White, fontWeight = FontWeight.Medium)
                                            }
                                        },
                                        onClick = {
                                            showAttachmentMenu = false
                                            photoPickerLauncher.launch("image/*")
                                        }
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Tools panel: Web Search, Code Interpreter, Memory", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Expand Tools",
                                    tint = Color(0xFF8E8E93)
                                )
                            }
                        }

                        // Right Control Icons (Web Search Button, Voice Dropdown & Send/Stop Button)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 🌐 Web Search Toggle Button Pill
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isWebSearchEnabled) Color(0xFF1E3A5F) else Color(0xFF2C2C2E),
                                border = BorderStroke(1.dp, if (isWebSearchEnabled) Color(0xFF1E88E5) else Color(0xFF3A3A3C)),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.toggleWebSearch()
                                        val statusStr = if (!isWebSearchEnabled) "Web Search Activated 🌐" else "Web Search Turned OFF 🔒"
                                        Toast.makeText(context, statusStr, Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(end = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Web Search",
                                        tint = if (isWebSearchEnabled) Color(0xFF64B5F6) else Color(0xFF8E8E93),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isWebSearchEnabled) "Web: ON" else "Web: OFF",
                                        color = if (isWebSearchEnabled) Color(0xFF64B5F6) else Color(0xFF8E8E93),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Voice/Audio Selector Pill
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF2C2C2E),
                                modifier = Modifier
                                    .clickable {
                                        Toast.makeText(context, "Speech Engine: System TTS", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(end = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Audio",
                                        tint = Color(0xFF8E8E93),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Audio",
                                        tint = Color(0xFF8E8E93),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Send or Stop Button
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank() || attachedFileName != null) {
                                        val messageToSend = buildString {
                                            if (attachedFileName != null) {
                                                append("[$attachedFileType Attached: $attachedFileName]\n")
                                            }
                                            if (inputText.isNotBlank()) {
                                                append(inputText)
                                            }
                                        }
                                        viewModel.sendMessage(messageToSend)
                                        inputText = ""
                                        attachedFileName = null
                                        attachedFileType = null
                                    }
                                },
                                enabled = inputText.isNotBlank() || attachedFileName != null || isGenerating,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isGenerating) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                                    contentDescription = if (isGenerating) "Stop" else "Send",
                                    tint = if (inputText.isNotBlank() || attachedFileName != null || isGenerating) Color.White else Color(0xFF48484A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


