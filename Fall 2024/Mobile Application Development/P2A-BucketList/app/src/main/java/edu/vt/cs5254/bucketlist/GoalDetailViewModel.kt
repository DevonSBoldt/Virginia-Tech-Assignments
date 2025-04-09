package edu.vt.cs5254.bucketlist

import androidx.lifecycle.ViewModel

class GoalDetailViewModel : ViewModel() {

    var goal: Goal

    init {
        goal = Goal(title = "My New Goal").apply {
            notes += listOf(
                GoalNote(
                    type = GoalNoteType.PROGRESS,
                    text = "First Step",
                    goalId = id
                ),
                GoalNote(
                    type = GoalNoteType.PROGRESS,
                    text = "Second Step",
                    goalId = id
                ),
                GoalNote(
                    type = GoalNoteType.PAUSED,
                    goalId = id
                )
            )
        }
    }
}