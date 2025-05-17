package com.example.tasknotes.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val dueDate: Long? = null,
    val completed: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val timestamp: Long = System.currentTimeMillis(),
    val color: Int? = null
)

enum class Priority {
    LOW, MEDIUM, HIGH
}
