package com.example.tasknotes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tasknotes.ui.NoteEditScreen
import com.example.tasknotes.ui.NoteListScreen
import com.example.tasknotes.ui.TaskEditScreen
import com.example.tasknotes.ui.TaskListScreen
import com.example.tasknotes.ui.theme.ThemeViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotesNavHost(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val notesViewModel: NotesViewModel = viewModel()
    val tasksViewModel: TasksViewModel = viewModel()
    
    var currentTab by remember { mutableStateOf(TabItem.Notes) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = "Notas") },
                    label = { Text("Notas") },
                    selected = currentTab == TabItem.Notes,
                    onClick = {
                        currentTab = TabItem.Notes
                        navController.navigate(Screen.NoteList.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.CheckBox, contentDescription = "Tareas") },
                    label = { Text("Tareas") },
                    selected = currentTab == TabItem.Tasks,
                    onClick = {
                        currentTab = TabItem.Tasks
                        navController.navigate(Screen.TaskList.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AnimatedNavHost(
                navController = navController,
                startDestination = Screen.NoteList.route
            ) {
                // Rutas para Notas
                composable(Screen.NoteList.route) {
                    NoteListScreen(
                        notes = notesViewModel.notes,
                        onAddNote = { navController.navigate(Screen.CreateNote.route) }, 
                        onNoteClick = { note ->
                            if (note.id.isNotBlank()) { 
                                navController.navigate(Screen.ViewNote.createRoute(note.id))
                            }
                        },
                        onDeleteNote = { noteId ->
                            notesViewModel.deleteNote(noteId)
                        },
                        themeViewModel = themeViewModel
                    )
                }

                composable(
                    route = Screen.ViewNote.route,
                    arguments = listOf(navArgument("noteId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")
                    val note = noteId?.let { id -> notesViewModel.notes.find { it.id == id } }
                    NoteEditScreen(
                        note = note,
                        readOnly = true, 
                        onEdit = { 
                            if (noteId != null) {
                                navController.navigate(Screen.EditNote.createRoute(noteId))
                            }
                        },
                        onCancel = { navController.popBackStack() },
                        themeViewModel = themeViewModel
                    )
                }

                composable(
                    route = Screen.EditNote.route,
                    arguments = listOf(navArgument("noteId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")
                    val note = noteId?.let { id -> notesViewModel.notes.find { it.id == id } }
                    NoteEditScreen(
                        note = note,
                        readOnly = false, 
                        onSave = {
                            notesViewModel.upsertNote(it)
                            navController.popBackStack(Screen.NoteList.route, false) 
                        },
                        onCancel = { navController.popBackStack() },
                        themeViewModel = themeViewModel
                    )
                }

                composable(Screen.CreateNote.route) {
                    NoteEditScreen(
                        note = null, 
                        readOnly = false, 
                        onSave = {
                            notesViewModel.upsertNote(it)
                            navController.popBackStack(Screen.NoteList.route, false) 
                        },
                        onCancel = { navController.popBackStack() },
                        themeViewModel = themeViewModel
                    )
                }
                
                // Rutas para Tareas
                composable(Screen.TaskList.route) {
                    TaskListScreen(
                        tasks = tasksViewModel.tasks,
                        onAddTask = { navController.navigate(Screen.CreateTask.route) },
                        onTaskClick = { task ->
                            if (task.id.isNotBlank()) {
                                navController.navigate(Screen.ViewTask.createRoute(task.id))
                            }
                        },
                        onDeleteTask = { taskId ->
                            tasksViewModel.deleteTask(taskId)
                        },
                        onToggleTaskCompletion = { taskId ->
                            tasksViewModel.toggleTaskCompletion(taskId)
                        },
                        themeViewModel = themeViewModel
                    )
                }
                
                composable(
                    route = Screen.ViewTask.route,
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId")
                    val task = taskId?.let { id -> tasksViewModel.tasks.find { it.id == id } }
                    TaskEditScreen(
                        task = task,
                        readOnly = true,
                        onEdit = {
                            if (taskId != null) {
                                navController.navigate(Screen.EditTask.createRoute(taskId))
                            }
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
                
                composable(
                    route = Screen.EditTask.route,
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId")
                    val task = taskId?.let { id -> tasksViewModel.tasks.find { it.id == id } }
                    TaskEditScreen(
                        task = task,
                        readOnly = false,
                        onSave = {
                            tasksViewModel.upsertTask(it)
                            navController.popBackStack(Screen.TaskList.route, false)
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.CreateTask.route) {
                    TaskEditScreen(
                        task = null,
                        readOnly = false,
                        onSave = {
                            tasksViewModel.upsertTask(it)
                            navController.popBackStack(Screen.TaskList.route, false)
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

enum class TabItem {
    Notes, Tasks
}

sealed class Screen(val route: String) {
    object NoteList : Screen("note_list")
    object ViewNote : Screen("view_note/{noteId}") {
        fun createRoute(noteId: String) = "view_note/$noteId"
    }
    object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: String) = "edit_note/$noteId"
    }
    object CreateNote : Screen("create_note")
    
    object TaskList : Screen("task_list")
    object ViewTask : Screen("view_task/{taskId}") {
        fun createRoute(taskId: String) = "view_task/$taskId"
    }
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }
    object CreateTask : Screen("create_task")
}
