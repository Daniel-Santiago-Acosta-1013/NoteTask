package com.example.tasknotes

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknotes.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> get() = _tasks
    
    private val tasksFile = File(application.filesDir, "tasks.dat")
    
    init {
        loadTasks()
    }
    
    fun upsertTask(task: Task) {
        val index = _tasks.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            _tasks[index] = task
        } else {
            _tasks.add(task)
        }
        saveTasks()
    }
    
    fun deleteTask(taskId: String) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            _tasks.removeAt(index)
            saveTasks()
        }
    }
    
    fun toggleTaskCompletion(taskId: String) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            val task = _tasks[index]
            _tasks[index] = task.copy(completed = !task.completed)
            saveTasks()
        }
    }
    
    private fun saveTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serializedTasks = _tasks.map { SerializableTask.fromTask(it) }
                FileOutputStream(tasksFile).use { fileOut ->
                    ObjectOutputStream(fileOut).use { objectOut ->
                        objectOut.writeObject(serializedTasks)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            if (tasksFile.exists()) {
                try {
                    val serializedTasks = FileInputStream(tasksFile).use { fileIn ->
                        ObjectInputStream(fileIn).use { objectIn ->
                            @Suppress("UNCHECKED_CAST")
                            objectIn.readObject() as List<SerializableTask>
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        _tasks.clear()
                        _tasks.addAll(serializedTasks.map { it.toTask() })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    // Helper class for serialization
    private data class SerializableTask(
        val id: String,
        val title: String,
        val description: String,
        val dueDate: Long?,
        val completed: Boolean,
        val priority: Int,
        val timestamp: Long
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
            
            fun fromTask(task: Task): SerializableTask {
                return SerializableTask(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    dueDate = task.dueDate,
                    completed = task.completed,
                    priority = task.priority.ordinal,
                    timestamp = task.timestamp
                )
            }
        }
        
        fun toTask(): Task {
            return Task(
                id = id,
                title = title,
                description = description,
                dueDate = dueDate,
                completed = completed,
                priority = com.example.tasknotes.model.Priority.values()[priority],
                timestamp = timestamp
            )
        }
    }
}
