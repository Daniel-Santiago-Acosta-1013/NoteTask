package com.example.tasknotes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tasknotes.model.Note
import com.example.tasknotes.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    notes: List<Note>,
    onAddNote: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onDeleteNote: (String) -> Unit = {},
    themeViewModel: ThemeViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Estado para el diálogo de confirmación
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    
    // Modal de confirmación
    if (showDeleteConfirmation && noteToDelete != null) {
        DeleteConfirmationDialog(
            note = noteToDelete!!,
            onConfirm = {
                onDeleteNote(noteToDelete!!.id)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Nota eliminada",
                        actionLabel = "Deshacer",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        // Aquí se implementaría la lógica para restaurar la nota
                        // Esta funcionalidad requeriría modificar el ViewModel
                    }
                }
                noteToDelete = null
                showDeleteConfirmation = false
            },
            onDismiss = {
                showDeleteConfirmation = false
                noteToDelete = null
            }
        )
    }
    
    Scaffold(
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp),
                snackbar = { data ->
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        action = {
                            data.visuals.actionLabel?.let { actionLabel ->
                                TextButton(
                                    onClick = { data.performAction() }
                                ) {
                                    Text(
                                        text = actionLabel,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    ) {
                        Text(data.visuals.message)
                    }
                }
            )
        },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mis Notas",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNote,
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Agregar nota",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = notes.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300)) + 
                        slideInVertically(animationSpec = tween(300)) { it / 2 },
                exit = fadeOut()
            ) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onNoteClick = onNoteClick,
                            onDeleteNote = { 
                                // En lugar de eliminar directamente, mostramos el diálogo de confirmación
                                noteToDelete = note
                                showDeleteConfirmation = true
                            },
                            isDarkTheme = themeViewModel.isDarkTheme
                        )
                    }
                }
            }
            
            AnimatedVisibility(
                visible = notes.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyNotesMessage()
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    note: Note,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de advertencia
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp)
                )
                
                // Título
                Text(
                    text = "Confirmar eliminación",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mensaje
                Text(
                    text = "¿Estás seguro que deseas eliminar la nota \"${note.title}\"?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
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
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(
                            "Eliminar",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onNoteClick: (Note) -> Unit,
    onDeleteNote: (String) -> Unit,
    isDarkTheme: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val formattedDate = remember(note) { 
        dateFormat.format(Date(note.timestamp)) 
    }
    
    // Colores pastel para las notas
    val noteColors = remember {
        listOf(
            Color(0xFFF8BBD0), // Rosa claro
            Color(0xFFE1BEE7), // Púrpura claro
            Color(0xFFD1C4E9), // Índigo claro
            Color(0xFFC5CAE9), // Azul claro
            Color(0xFFBBDEFB), // Azul cielo
            Color(0xFFB3E5FC), // Azul claro
            Color(0xFFB2EBF2), // Cian claro
            Color(0xFFB2DFDB), // Verde azulado claro
            Color(0xFFC8E6C9), // Verde claro
            Color(0xFFDCEDC8), // Verde lima claro
            Color(0xFFF0F4C3), // Amarillo claro
            Color(0xFFFFE0B2), // Ámbar claro
            Color(0xFFFFCCBC)  // Naranja claro
        )
    }
    
    // Determinar el color de la nota (basado en el ID para consistencia)
    val colorIndex = abs(note.id.hashCode()) % noteColors.size
    val noteColor = remember(note.id) {
        note.color?.let { Color(it) } ?: noteColors[colorIndex]
    }
    
    // Colores de texto fijos para mejor legibilidad
    val textColor = Color.Black
    val secondaryTextColor = Color.DarkGray
    
    // Aplicar transparencia en modo oscuro
    val finalNoteColor = if (isDarkTheme) {
        noteColor.copy(alpha = 0.7f)
    } else {
        noteColor
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNoteClick(note) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = finalNoteColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { onDeleteNote(note.id) },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyNotesMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "No hay notas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "¡Agrega una nueva!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
