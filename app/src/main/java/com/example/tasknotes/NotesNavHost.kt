package com.example.tasknotes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tasknotes.ui.NoteEditScreen
import com.example.tasknotes.ui.NoteListScreen
import com.example.tasknotes.ui.theme.ThemeViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotesNavHost(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val notesViewModel: NotesViewModel = viewModel()

    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.NoteList.route
    ) {
        composable(Screen.NoteList.route) {
            NoteListScreen(
                notes = notesViewModel.notes,
                onAddNote = { navController.navigate(Screen.CreateNote.route) }, 
                onNoteClick = { note ->
                    if (note.id.isNotBlank()) { 
                        navController.navigate(Screen.ViewNote.createRoute(note.id))
                    } else {
                        // Opcional: Registrar un error o mostrar un mensaje al usuario
                        // Log.e("NotesNavHost", "Intento de navegar con ID de nota vacío")
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
    }
}

sealed class Screen(val route: String) {
    object NoteList : Screen("list")
    object ViewNote : Screen("view_note/{noteId}") {
        fun createRoute(noteId: String) = "view_note/$noteId"
    }
    object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: String) = "edit_note/$noteId"
    }
    object CreateNote : Screen("create_note")
}
