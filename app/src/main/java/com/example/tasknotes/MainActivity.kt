package com.example.tasknotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasknotes.ui.theme.NoteTaskTheme
import com.example.tasknotes.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            
            NoteTaskTheme(darkTheme = themeViewModel.isDarkTheme) {
                NotesNavHost(themeViewModel = themeViewModel)
            }
        }
    }
}