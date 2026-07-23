package com.pocketpalai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketpalai.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenDrawer: () -> Unit
) {
    val contextSize by viewModel.contextSize.collectAsState()
    val useMemoryLock by viewModel.useMemoryLock.collectAsState()
    val useMemoryMapping by viewModel.useMemoryMapping.collectAsState()
    val enableWeightRepacking by viewModel.enableWeightRepacking.collectAsState()
    val threads by viewModel.threads.collectAsState()
    val batchSize by viewModel.batchSize.collectAsState()

    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val hfToken by viewModel.hfToken.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val autoSpeak by viewModel.autoSpeak.collectAsState()

    var showAdvancedSettings by remember { mutableStateOf(false) }
    var contextSizeInput by remember(contextSize) { mutableStateOf(contextSize) }
    var threadsInput by remember(threads) { mutableStateOf(threads) }
    var batchSizeInput by remember(batchSize) { mutableStateOf(batchSize) }
    var hfInput by remember(hfToken) { mutableStateOf(hfToken) }
    var showLangDropdown by remember { mutableStateOf(false) }

    val darkBackgroundColor = Color(0xFF0C0C0E)
    val cardBackgroundColor = Color(0xFF161618)
    val dividerColor = Color(0xFF28282C)
    val titleTextColor = Color.White
    val subtitleTextColor = Color(0xFF9E9EA4)
    val inputBgColor = Color(0xFF0E0E10)

    val languages = listOf(
        "English (en)", "Español (es)", "Français (fr)", "Deutsch (de)",
        "Italiano (it)", "Português (pt)", "Русский (ru)", "中文 (zh)", "日本語 (ja)"
    )

    Scaffold(
        containerColor = darkBackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackgroundColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // CARD 1: Model Initialization Settings
            Surface(
                color = cardBackgroundColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Card Title
                    Text(
                        text = "Model Initialization Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = titleTextColor,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Device Selection
                    Column {
                        Text(
                            text = "Device Selection",
                            fontSize = 16.sp,
                            color = titleTextColor,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CPU only - No hardware accelerators detected",
                            fontSize = 13.sp,
                            color = subtitleTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Context Size
                    Column {
                        Text(
                            text = "Context Size",
                            fontSize = 16.sp,
                            color = titleTextColor,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Styled Input Field matching screenshot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(inputBgColor, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = contextSizeInput,
                                onValueChange = {
                                    contextSizeInput = it
                                    viewModel.setContextSize(it)
                                },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(Color.White),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Model reload needed for changes to take effect.",
                            fontSize = 13.sp,
                            color = subtitleTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Advanced Settings Expandable Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvancedSettings = !showAdvancedSettings }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Advanced Settings",
                            fontSize = 15.sp,
                            color = Color(0xFF8A93A0),
                            fontWeight = FontWeight.Normal
                        )
                        Icon(
                            imageVector = if (showAdvancedSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Advanced Settings",
                            tint = Color(0xFF8A93A0)
                        )
                    }

                    // Expanded Advanced Settings Details
                    AnimatedVisibility(
                        visible = showAdvancedSettings,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HorizontalDivider(color = dividerColor, thickness = 1.dp)

                            // CPU Threads
                            Column {
                                Text("CPU Threads", fontSize = 15.sp, color = titleTextColor)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .background(inputBgColor, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = threadsInput,
                                        onValueChange = {
                                            threadsInput = it
                                            viewModel.setThreads(it)
                                        },
                                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                                        cursorBrush = SolidColor(Color.White),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }

                            // Batch Size
                            Column {
                                Text("Evaluation Batch Size", fontSize = 15.sp, color = titleTextColor)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .background(inputBgColor, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = batchSizeInput,
                                        onValueChange = {
                                            batchSizeInput = it
                                            viewModel.setBatchSize(it)
                                        },
                                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                                        cursorBrush = SolidColor(Color.White),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }

                            // Hugging Face Token
                            Column {
                                Text("Hugging Face API Token", fontSize = 15.sp, color = titleTextColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Required for downloading gated repositories.", fontSize = 12.sp, color = subtitleTextColor)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .background(inputBgColor, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = hfInput,
                                        onValueChange = {
                                            hfInput = it
                                            viewModel.setHfToken(it)
                                        },
                                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                                        cursorBrush = SolidColor(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CARD 2: Memory Settings
            Surface(
                color = cardBackgroundColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Card Title
                    Text(
                        text = "Memory Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = titleTextColor,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // 1. Use Memory Lock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use Memory Lock",
                                fontSize = 16.sp,
                                color = titleTextColor,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Force system to keep model in RAM rather than swapping or compressing",
                                fontSize = 13.sp,
                                color = subtitleTextColor,
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = useMemoryLock,
                            onCheckedChange = { viewModel.toggleMemoryLock(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF34C759),
                                uncheckedThumbColor = Color(0xFFE5E5EA),
                                uncheckedTrackColor = Color(0xFF3A3A3C),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Memory Mapping
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Memory Mapping",
                                fontSize = 16.sp,
                                color = titleTextColor,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use memory-mapped files for faster model loading",
                                fontSize = 13.sp,
                                color = subtitleTextColor,
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = useMemoryMapping,
                            onCheckedChange = { viewModel.toggleMemoryMapping(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF34C759),
                                uncheckedThumbColor = Color(0xFFE5E5EA),
                                uncheckedTrackColor = Color(0xFF3A3A3C),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Enable Weight Repacking
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Weight Repacking",
                                fontSize = 16.sp,
                                color = titleTextColor,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Faster prompt processing with minimal memory overhead when memory mapping is disabled. Token generation is unaffected.",
                                fontSize = 13.sp,
                                color = subtitleTextColor,
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = enableWeightRepacking,
                            onCheckedChange = { viewModel.toggleWeightRepacking(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF34C759),
                                uncheckedThumbColor = Color(0xFFE5E5EA),
                                uncheckedTrackColor = Color(0xFF3A3A3C),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // CARD 3: Voice & Language Preferences
            Surface(
                color = cardBackgroundColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "App & Voice Preferences",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = titleTextColor,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Auto-Speak
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Speak Assistant Responses", fontSize = 16.sp, color = titleTextColor)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Automatically read generated AI messages aloud", fontSize = 13.sp, color = subtitleTextColor)
                        }
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { viewModel.toggleAutoSpeak(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF34C759),
                                uncheckedThumbColor = Color(0xFFE5E5EA),
                                uncheckedTrackColor = Color(0xFF3A3A3C),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Language Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("App Interface Language", fontSize = 16.sp, color = titleTextColor)

                        Box {
                            TextButton(onClick = { showLangDropdown = true }) {
                                Text(selectedLanguage, color = Color(0xFF34C759), fontSize = 14.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF34C759))
                            }

                            DropdownMenu(
                                expanded = showLangDropdown,
                                onDismissRequest = { showLangDropdown = false },
                                modifier = Modifier.background(cardBackgroundColor)
                            ) {
                                languages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang, color = Color.White) },
                                        onClick = {
                                            viewModel.setLanguage(lang)
                                            showLangDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // CARD 4: Developer Credit
            Surface(
                color = cardBackgroundColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Made with ❤️ by a Pakistani 🇵🇰",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = titleTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
