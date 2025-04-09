package edu.vt.cs5254.bucketlist

import androidx.lifecycle.ViewModel

class GoalListViewModel : ViewModel() {

    val goals = mutableListOf<Goal>()

    init {
        (0..99).forEach {
            val goal = Goal(
                title = "Goal #$it",
            )
            repeat(it % 4) { noteNum ->
                goal.notes += GoalNote(
                    type = GoalNoteType.PROGRESS,
                    text = "Progress $noteNum",
                    goalId = goal.id
                )
            }
            if (it % 3 == 1) {
                goal.notes += GoalNote(
                    type = GoalNoteType.PAUSED,
                    goalId = goal.id
                )
            }
            if (it % 3 == 2) {
                goal.notes += GoalNote(
                    type = GoalNoteType.COMPLETED,
                    goalId = goal.id
                )
            }
            goals += goal
        }
    }
}