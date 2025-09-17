package com.div.taskmanager.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.div.taskmanager.data.Task
import com.div.taskmanager.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    var tasksLiveData: LiveData<List<Task>> = MutableLiveData()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        tasksLiveData = repository.getAllTasks()
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.insertTask(task)
            _isLoading.value = false
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateTask(task)
            _isLoading.value = false
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteTask(task)
            _isLoading.value = false
        }
    }


}
