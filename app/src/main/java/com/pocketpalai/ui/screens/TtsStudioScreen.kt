package com.pocketpalai.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketpalai.ui.viewmodel.ModelsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsStudioScreen(
    modelsViewModel: ModelsViewModel,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("Hello! Welcome to Pocket AI Text to Speech Studio. Type any text here to synthesize voice.") }
    var speechRate by remember { mutableStateOf(1.0f) }
    var pitch by remember { mutableStateOf(1.0f) }
    var isSpeaking by remember { mutableStateOf(false) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var instance: TextToSpeech? = null
        try {
            instance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    instance?.language = Locale.US
                }
            }
            tts = instance
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            try {
                instance?.stop()
                instance?.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text-to-Speech Studio", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Info Card
            item {
                Surface(
                    color = Color(0xFF141416),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF64B5F6))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Local Voice Synthesis Engine", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Synthesize text into speech using high-quality local voices or download Hugging Face voice models below.",
                            color = Color(0xFF8E8E93),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Input Text Card
            item {
                Surface(
                    color = Color(0xFF1C1C1E),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Enter Text to Synthesize:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Type text here...", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E88E5),
                                unfocusedBorderColor = Color(0xFF2C2C2E),
                                focusedContainerColor = Color(0xFF2C2C2E),
                                unfocusedContainerColor = Color(0xFF2C2C2E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Controls Sliders
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Speech Speed: ${String.format("%.1fx", speechRate)}", color = Color(0xFF8E8E93), fontSize = 12.sp)
                                Slider(
                                    value = speechRate,
                                    onValueChange = { speechRate = it },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF1E88E5), activeTrackColor = Color(0xFF1E88E5))
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text("Voice Pitch: ${String.format("%.1fx", pitch)}", color = Color(0xFF8E8E93), fontSize = 12.sp)
                                Slider(
                                    value = pitch,
                                    onValueChange = { pitch = it },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF64B5F6), activeTrackColor = Color(0xFF64B5F6))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        try {
                                            tts?.setSpeechRate(speechRate)
                                            tts?.setPitch(pitch)
                                            tts?.speak(inputText, TextToSpeech.QUEUE_FLUSH, null, "tts_id")
                                            isSpeaking = true
                                            Toast.makeText(context, "Speaking text...", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "TTS Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Speech", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    tts?.stop()
                                    isSpeaking = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1E1E), contentColor = Color(0xFFFF453A)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop")
                            }
                        }
                    }
                }
            }

            // Download Hugging Face Voice Models Card
            item {
                Surface(
                    color = Color(0xFF141416),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hugging Face Voice Models", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(color = Color(0xFF1E3A5F), shape = RoundedCornerShape(10.dp)) {
                                Text("TTS / Audio", color = Color(0xFF64B5F6), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Download community GGUF voice & speech recognition models directly from Hugging Face.",
                            color = Color(0xFF8E8E93),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val voiceModels = listOf(
                            Triple("Kokoro 82M TTS", "hexgrad/Kokoro-82M", "Lightweight high-quality TTS voice model"),
                            Triple("Whisper Small STT", "ggerganov/whisper.cpp", "GGUF speech recognition for Whisper"),
                            Triple("Piper TTS Voices", "rhasspy/piper-voices", "Fast local neural text-to-speech voices")
                        )

                        voiceModels.forEach { (name, repo, desc) ->
                            Surface(
                                color = Color(0xFF2C2C2E),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(repo, color = Color(0xFF1E88E5), fontSize = 11.sp)
                                        Text(desc, color = Color(0xFF8E8E93), fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = {
                                            modelsViewModel.searchHuggingFace(repo)
                                            modelsViewModel.openAddModelDialog()
                                            Toast.makeText(context, "Searching Hugging Face for $name...", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
