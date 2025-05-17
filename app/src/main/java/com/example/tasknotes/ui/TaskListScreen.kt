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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tasknotes.model.Priority
import com.example.tasknotes.model.Task
import com.example.tasknotes.ui.theme.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onDeleteTask: (String) -> Unit = {},
    onToggleTaskCompletion: (String) -> Unit = {},
    themeViewModel: ThemeViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Estado para el diálogo de confirmación
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    
    // Modal de confirmación
    if (showDeleteConfirmation && taskToDelete != null) {
        DeleteTaskConfirmationDialog(
            task = taskToDelete!!,
            onConfirm = {
                onDeleteTask(taskToDelete!!.id)
                taskToDelete = null
                showDeleteConfirmation = false
            },
            onDismiss = {
                showDeleteConfirmation = false
                taskToDelete = null
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
                        "Mis Tareas",
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
                onClick = onAddTask,
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Agregar tarea",
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
                visible = tasks.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300)) + 
                        slideInVertically(animationSpec = tween(300)) { it / 2 },
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onTaskClick = onTaskClick,
                            onDeleteTask = { 
                                taskToDelete = task
                                showDeleteConfirmation = true
                            },
                            onToggleCompletion = { onToggleTaskCompletion(task.id) },
                            isDarkTheme = themeViewModel.isDarkTheme
                        )
                    }
                }
            }
            
            AnimatedVisibility(
                visible = tasks.isEmpty(),
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut()
            ) {
                EmptyTasksMessage()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteTaskConfirmationDialog(
    task: Task,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Eliminar tarea")
        },
        text = {
            Text("¿Estás seguro de que quieres eliminar la tarea '${task.title}'?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: (Task) -> Unit,
    onDeleteTask: () -> Unit,
    onToggleCompletion: () -> Unit,
    isDarkTheme: Boolean
) {
    val backgroundColor = when (task.priority) {
        Priority.HIGH -> if (isDarkTheme) Color(0xFF783535) else Color(0xFFF5D0D0)
        Priority.MEDIUM -> if (isDarkTheme) Color(0xFF695C2E) else Color(0xFFF8F0D0)
        Priority.LOW -> if (isDarkTheme) Color(0xFF2E5044) else Color(0xFFD0F5E7)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox para marcar completada/incompleta
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggleCompletion() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
            
            // Contenido de la tarea
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTaskClick(task) }
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isDarkTheme) Color.White.copy(alpha = if (task.completed) 0.6f else 1f)
                           else Color.Black.copy(alpha = if (task.completed) 0.6f else 1f)
                )
                
                task.description.takeIf { it.isNotEmpty() }?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isDarkTheme) Color.White.copy(alpha = if (task.completed) 0.5f else 0.8f)
                               else Color.Black.copy(alpha = if (task.completed) 0.5f else 0.7f)
                    )
                }
                
                task.dueDate?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Fecha límite",
                            modifier = Modifier.size(16.dp),
                            tint = if (isDarkTheme) Color.White.copy(alpha = 0.7f)
                                  else Color.Black.copy(alpha = 0.6f)
                        )
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text(
                            text = dateFormat.format(Date(it)),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.7f)
                                   else Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Icono para eliminar
            IconButton(onClick = onDeleteTask) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar tarea",
                    tint = if (isDarkTheme) Color.White.copy(alpha = 0.7f)
                           else Color.Black.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun EmptyTasksMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "No hay tareas",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tienes tareas pendientes",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toca el botón + para crear una nueva tarea",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
