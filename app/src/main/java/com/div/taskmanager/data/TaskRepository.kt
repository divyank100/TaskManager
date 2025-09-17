package com.div.taskmanager.data

import androidx.lifecycle.LiveData

class TaskRepository(private val taskDao: TaskDao) {
    
    fun getAllTasks(): LiveData<List<Task>> {
        return taskDao.getAllTasks()
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
    
    suspend fun deleteTask(task: Task) {taskDao.deleteTask(task)}

}
