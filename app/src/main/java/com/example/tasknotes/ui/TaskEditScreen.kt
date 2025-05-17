package com.example.tasknotes.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tasknotes.model.Priority
import com.example.tasknotes.model.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    task: Task? = null,
    readOnly: Boolean = false,
    onSave: ((Task) -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var dueDate by remember { mutableStateOf(task?.dueDate) }
    var priority by remember { mutableStateOf(task?.priority ?: Priority.MEDIUM) }
    var completed by remember { mutableStateOf(task?.completed ?: false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    var isFormValid by remember(title) { 
        mutableStateOf(title.isNotBlank())
    }
    
    if (showDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { timestamp ->
                dueDate = timestamp
                showDatePicker = false
            },
            initialDate = dueDate?.let { Date(it) }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            readOnly -> "Detalles de Tarea"
                            task != null -> "Editar Tarea"
                            else -> "Nueva Tarea"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (readOnly && onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar"
                            )
                        }
                    } else if (!readOnly && onSave != null) {
                        IconButton(
                            onClick = {
                                if (isFormValid) {
                                    val newTask = Task(
                                        id = task?.id ?: UUID.randomUUID().toString(),
                                        title = title,
                                        description = description,
                                        dueDate = dueDate,
                                        priority = priority,
                                        completed = completed,
                                        timestamp = System.currentTimeMillis(),
                                        color = task?.color
                                    )
                                    onSave(newTask)
                                }
                            },
                            enabled = isFormValid
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; isFormValid = it.isNotBlank() },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !readOnly
            )
            
            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                enabled = !readOnly
            )
            
            // Fecha límite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fecha límite:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 16.dp)
                )
                
                OutlinedButton(
                    onClick = { if (!readOnly) showDatePicker = true },
                    enabled = !readOnly,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Fecha límite"
                        )
                        Text(
                            text = dueDate?.let { dateFormat.format(Date(it)) } ?: "Sin fecha"
                        )
                    }
                }
                
                if (dueDate != null && !readOnly) {
                    IconButton(onClick = { dueDate = null }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Borrar fecha"
                        )
                    }
                }
            }
            
            // Prioridad
            Column {
                Text(
                    text = "Prioridad:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Usando Box con weight(1f) como contenedor para cada PriorityOption
                    Box(modifier = Modifier.weight(1f)) {
                        PriorityOption(
                            text = "Baja",
                            selected = priority == Priority.LOW,
                            color = Color(0xFF2E5044),
                            onClick = { if (!readOnly) priority = Priority.LOW },
                            enabled = !readOnly
                        )
                    }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        PriorityOption(
                            text = "Media",
                            selected = priority == Priority.MEDIUM,
                            color = Color(0xFF695C2E),
                            onClick = { if (!readOnly) priority = Priority.MEDIUM },
                            enabled = !readOnly
                        )
                    }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        PriorityOption(
                            text = "Alta",
                            selected = priority == Priority.HIGH,
                            color = Color(0xFF783535),
                            onClick = { if (!readOnly) priority = Priority.HIGH },
                            enabled = !readOnly
                        )
                    }
                }
            }
            
            // Estado de completado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = completed,
                    onCheckedChange = { if (!readOnly) completed = it },
                    enabled = !readOnly
                )
                
                Text(
                    text = "Marcar como completada",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PriorityOption(
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val backgroundColor = if (selected) color else Color.Transparent
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    val borderColor = if (enabled) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CustomDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
    initialDate: Date? = null
) {
    val calendar = Calendar.getInstance()
    initialDate?.let { calendar.time = it }
    
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Seleccionar fecha",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Implementación simple de selección de fecha
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Día
                    OutlinedTextField(
                        value = selectedDay.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { day -> 
                                if (day in 1..31) selectedDay = day
                            }
                        },
                        label = { Text("Día") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    // Mes
                    OutlinedTextField(
                        value = (selectedMonth + 1).toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { month -> 
                                if (month in 1..12) selectedMonth = month - 1
                            }
                        },
                        label = { Text("Mes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    // Año
                    OutlinedTextField(
                        value = selectedYear.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { year -> 
                                if (year > 0) selectedYear = year
                            }
                        },
                        label = { Text("Año") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val newCalendar = Calendar.getInstance()
                            newCalendar.set(selectedYear, selectedMonth, selectedDay)
                            onDateSelected(newCalendar.timeInMillis)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
