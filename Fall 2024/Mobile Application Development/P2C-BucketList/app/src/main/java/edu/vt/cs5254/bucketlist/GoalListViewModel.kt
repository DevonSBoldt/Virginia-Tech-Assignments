package edu.vt.cs5254.bucketlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoalListViewModel : ViewModel() {

    private val repository = GoalRepository.get()

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals get() = _goals.asStateFlow()

    suspend fun addGoal(goal: Goal){
        repository.addGoal(goal)
    }

    fun deleteGoal(goal: Goal){
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    init {
        viewModelScope.launch {
            repository.getGoals().collect(){
                _goals.value = it
            }
        }
    }
}
