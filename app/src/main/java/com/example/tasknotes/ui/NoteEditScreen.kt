package com.example.tasknotes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tasknotes.model.Note
import com.example.tasknotes.ui.theme.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    note: Note? = null,
    readOnly: Boolean = false,
    onSave: (Note) -> Unit = {},
    onCancel: () -> Unit = {},
    onEdit: () -> Unit = {},
    themeViewModel: ThemeViewModel
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var showError by remember { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    val isEditing = !readOnly
    val isNew = note == null
    
    // Auto-focus title field when creating a new note
    LaunchedEffect(Unit) {
        if (isNew && isEditing) {
            delay(300) // Small delay to ensure the UI is ready
            titleFocusRequester.requestFocus()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            readOnly -> "Ver Nota"
                            isNew -> "Nueva Nota"
                            else -> "Editar Nota"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    // Theme toggle button
                    IconButton(
                        onClick = { themeViewModel.toggleTheme() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (themeViewModel.isDarkTheme) {
                                Icons.Default.LightMode
                            } else {
                                Icons.Default.DarkMode
                            },
                            contentDescription = if (themeViewModel.isDarkTheme) {
                                "Cambiar a modo claro"
                            } else {
                                "Cambiar a modo oscuro"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Edit button (only shown in read-only mode)
                    if (readOnly) {
                        TextButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                "Editar",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { if (isEditing) title = it },
                        label = { Text("Título") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(titleFocusRequester),
                        enabled = isEditing,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = content,
                        onValueChange = { if (isEditing) content = it },
                        label = { Text("Contenido") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        enabled = isEditing,
                        maxLines = Int.MAX_VALUE,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            
            AnimatedVisibility(
                visible = showError,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Text(
                    "Por favor completa ambos campos",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            
            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(
                            "Cancelar",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                val newOrUpdatedNote = if (isNew) {
                                    Note(title = title, content = content)
                                } else {
                                    Note(id = note!!.id, title = title, content = content)
                                }
                                onSave(newOrUpdatedNote)
                            } else {
                                showError = true
                                scope.launch {
                                    delay(3000)
                                    showError = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            "Guardar",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                // Add a small space at the bottom
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
