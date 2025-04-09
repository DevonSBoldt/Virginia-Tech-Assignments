package edu.vt.cs5254.bucketlist

import java.util.Date
import java.util.UUID

data class Goal(
    val id: UUID = UUID.randomUUID(),
    val title: String = "",
    val lastUpdated: Date = Date(),
) {
    var notes = emptyList<GoalNote>()
    val isCompleted get() = notes.any { it.type == GoalNoteType.COMPLETED }
    val isPaused get() = notes.any { it.type == GoalNoteType.PAUSED }
}

data class GoalNote(
    val id: UUID = UUID.randomUUID(),
    val text: String = "",
    val type: GoalNoteType,
    val goalId: UUID
)

enum class GoalNoteType {
    PROGRESS,
    PAUSED,
    COMPLETED,
}