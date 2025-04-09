package edu.vt.cs5254.bucketlist

import android.content.Context
import androidx.room.Room
import edu.vt.cs5254.bucketlist.database.GoalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

private const val DATABASE_NAME = "goal-database"

class GoalRepository private constructor(context: Context) {

    private val database = Room.databaseBuilder(
        context,
        GoalDatabase::class.java,
        DATABASE_NAME
    ).createFromAsset(DATABASE_NAME)
        .build()

    // Flow that maps goals to their respective notes
    fun getGoals(): Flow<List<Goal>> {
        val flowMultiMap = database.goalDao().getGoals() // Flow<Map<Goal, List<GoalNote>>>
        return flowMultiMap.map { multiMap ->
            multiMap.keys.map { goal ->
                goal.apply {
                    notes = multiMap.getValue(goal)
                }
            }
        }
    }

    suspend fun getGoal(id: UUID): Goal = database.goalDao().getGoalAndNotes(id)

    suspend fun updateGoal(goal: Goal) {
        database.goalDao().updateGoalAndNotes(goal)
    }

    suspend fun deleteGoal(goal: Goal){
        database.goalDao().deleteGoalAndNotes(goal)
    }

    fun updateGoalAsync(goal: Goal) {
        CoroutineScope(Dispatchers.IO).launch {
            database.goalDao().updateGoalAndNotes(goal)
        }
    }

    suspend fun addGoal(goal: Goal) = database.goalDao().addGoal(goal)

    companion object {
        private var INSTANCE: GoalRepository? = null

        fun initialize(context: Context) {
            check(INSTANCE == null) { "GoalRepository is ALREADY initialized!!!!" }
            INSTANCE = GoalRepository(context)
        }

        fun get(): GoalRepository {
            return checkNotNull(INSTANCE) { "GoalRepository MUST BE initialized!!! " }
        }
    }
}
