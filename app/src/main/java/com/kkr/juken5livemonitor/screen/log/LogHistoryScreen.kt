package com.kkr.juken5livemonitor.screen.log

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.kkr.juken5livemonitor.data.db.LogSession
import com.kkr.juken5livemonitor.ui.components.GlassBackground
import com.kkr.juken5livemonitor.ui.components.GlassCard
import com.kkr.juken5livemonitor.viewmodel.LogViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LogHistoryScreen(
    navController: NavHostController,
    vm: LogViewModel = viewModel()
) {
    val sessions by vm.sessions.collectAsState(initial = emptyList())
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()) }
    
    // Selection State
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedIds.isNotEmpty()
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var sessionToRename by remember { mutableStateOf<LogSession?>(null) }
    var newTitle by remember { mutableStateOf("") }

    GlassBackground {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Dynamic Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                if (isSelectionMode) {
                    // Selection Mode Header
                    IconButton(onClick = { selectedIds = emptySet() }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Selection", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${selectedIds.size} Selected",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = Color.Red)
                    }
                } else {
                    // Regular Header
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Data Log History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (sessions.isNotEmpty()) {
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            color = Color.Red.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${sessions.size} Files",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.Red,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (sessions.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No logs recorded yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(sessions) { session ->
                        val isSelected = selectedIds.contains(session.id)
                        
                        val totalSec = session.durationMillis / 1000
                        val durationStr = "%02d:%02d".format(totalSec / 60, totalSec % 60)
                        
                        LogSessionItem(
                            title = session.title,
                            subtitle = "${sdf.format(Date(session.timestamp))} • $durationStr",
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onPlay = { 
                                if (isSelectionMode) {
                                    selectedIds = if (isSelected) selectedIds - session.id else selectedIds + session.id
                                } else {
                                    navController.navigate("log_playback/${session.id}")
                                }
                            },
                            onRename = {
                                sessionToRename = session
                                newTitle = session.title
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedIds = setOf(session.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Rename Dialog
        sessionToRename?.let { session ->
            AlertDialog(
                onDismissRequest = { sessionToRename = null },
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White,
                title = { Text("Ubah Nama Log", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Red,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                vm.renameSession(session, newTitle)
                                sessionToRename = null
                            }
                        }
                    ) {
                        Text("SIMPAN", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { sessionToRename = null }) {
                        Text("BATAL", color = Color.White)
                    }
                }
            )
        }

        // Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                title = {
                    Text(text = "Hapus Log", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(text = "Yakin mau hapus bang?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.deleteSessions(selectedIds.toList())
                            selectedIds = emptySet()
                            showDeleteConfirmation = false
                        }
                    ) {
                        Text("YA, HAPUS", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("BATAL", color = Color.White)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogSessionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onPlay: () -> Unit,
    onRename: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale animation when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "press_scale"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onPlay,
                onLongClick = onLongClick
            )
            .background(
                if (isSelected) Color.Red.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onPlay() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.Red,
                            uncheckedColor = Color.Gray
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                }
                
                Column {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }
            }
            
            if (!isSelectionMode) {
                Row {
                    IconButton(onClick = onRename) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color.Gray)
                    }
                    IconButton(onClick = onPlay) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Green)
                    }
                }
            }
        }
    }
}
