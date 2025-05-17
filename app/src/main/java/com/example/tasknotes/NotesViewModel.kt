package com.example.tasknotes

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknotes.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> get() = _notes
    
    private val notesFile = File(application.filesDir, "notes.dat")
    
    init {
        loadNotes()
    }
    
    fun upsertNote(note: Note) {
        val index = _notes.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            _notes[index] = note
        } else {
            _notes.add(note)
        }
        saveNotes()
    }
    
    fun deleteNote(noteId: String) {
        val index = _notes.indexOfFirst { it.id == noteId }
        if (index >= 0) {
            _notes.removeAt(index)
            saveNotes()
        }
    }
    
    private fun saveNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serializedNotes = _notes.map { SerializableNote.fromNote(it) }
                FileOutputStream(notesFile).use { fileOut ->
                    ObjectOutputStream(fileOut).use { objectOut ->
                        objectOut.writeObject(serializedNotes)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            if (notesFile.exists()) {
                try {
                    val serializedNotes = FileInputStream(notesFile).use { fileIn ->
                        ObjectInputStream(fileIn).use { objectIn ->
                            @Suppress("UNCHECKED_CAST")
                            objectIn.readObject() as List<SerializableNote>
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        _notes.clear()
                        _notes.addAll(serializedNotes.map { it.toNote() })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    // Helper class for serialization
    private data class SerializableNote(
        val id: String,
        val title: String,
        val content: String,
        val timestamp: Long
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
            
            fun fromNote(note: Note): SerializableNote {
                return SerializableNote(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    timestamp = note.timestamp
                )
            }
        }
        
        fun toNote(): Note {
            return Note(
                id = id,
                title = title,
                content = content,
                timestamp = timestamp
            )
        }
    }
}
