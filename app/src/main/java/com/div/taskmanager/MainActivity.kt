package com.div.taskmanager

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.div.taskmanager.R
import com.div.taskmanager.data.Priority
import com.div.taskmanager.data.Task
import com.div.taskmanager.databinding.ActivityMainBinding
import com.div.taskmanager.databinding.DialogAddTaskBinding
import com.div.taskmanager.ui.TaskAdapter
import com.div.taskmanager.ui.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private var allTasks: List<Task> = emptyList()
    private var isGridLayout = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupRecyclerView()
        setupClickListeners()
        observeTasks()
        setupFilterTask()
        observeLoading()
        setupLayoutOption()
        setupSearchBar()
    }

    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener { query ->
            val searchText = query.toString().trim().lowercase(Locale.getDefault())
            val filteredList = if (searchText.isEmpty()) {
                allTasks
            } else {
                allTasks.filter { task ->
                    task.title.lowercase(Locale.getDefault()).contains(searchText) ||
                            task.description.lowercase(Locale.getDefault()).contains(searchText)
                }
            }
            taskAdapter.submitList(filteredList)
        }
    }


    private fun setupLayoutOption() {
        binding.btnToggleLayout.setOnClickListener {
            isGridLayout = !isGridLayout
            if (isGridLayout) {
                binding.recyclerViewTasks.layoutManager =
                    GridLayoutManager(this, 2)
                binding.btnToggleLayout.setImageResource(R.drawable.ic_grid)
            } else {
                binding.recyclerViewTasks.layoutManager =
                    LinearLayoutManager(this)
                binding.btnToggleLayout.setImageResource(R.drawable.ic_list)
            }
        }
    }

    private fun observeLoading() {
        taskViewModel.isLoading.asLiveData().observe(this) { isLoading ->
            println("loading------ $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                showAddEditTaskDialog(task)
            },
        )
        
        binding.recyclerViewTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            showAddEditTaskDialog(null);
        }
    }
    
    private fun observeTasks() {
        taskViewModel.tasksLiveData.observe(this) { tasks ->
            println("%%%%%%%% observing.....");
            allTasks = tasks;
            val currentQuery = binding.searchEditText.text.toString().trim()
            if (currentQuery.isEmpty()) {
                taskAdapter.submitList(allTasks)
            } else {
                println("%%%%%%%% query.....$currentQuery");
                val searchText = currentQuery.lowercase(Locale.getDefault())
                val filteredList = allTasks.filter { task ->
                    task.title.lowercase(Locale.getDefault()).contains(searchText) ||
                            task.description.lowercase(Locale.getDefault()).contains(searchText)
                }
                println("%%%%%%%% filter list.....$filteredList");
                taskAdapter.submitList(filteredList)
            }
//            taskAdapter.submitList(allTasks);
        }
    }

    private  fun setupFilterTask(){
        val filterItems = resources.getStringArray(R.array.filter_levels)
        val adapter = ArrayAdapter(this, R.layout.list_item, filterItems)
        binding.filterDropdown.setAdapter(adapter)

        // Handle selection
        binding.filterDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedFilter = filterItems[position]
            filterTasks((selectedFilter == "COMPLETED"));
        }

    }

    private fun filterTasks(filter: Boolean) {
        val filteredList = if (filter == null) {
            allTasks
        } else {
            allTasks.filter { it.isCompleted == filter }
        }
        taskAdapter.submitList(filteredList)
    }
    
    private fun showAddEditTaskDialog(task: Task?) {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        
        val priorities = listOf("Low", "Medium", "High")
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        // Prefill fields if editing existing task
        task?.let {
            dialogBinding.editTextTitle.setText(it.title)
            dialogBinding.editTextDescription.setText(it.description)
            dialogBinding.editTextDueDate.setText(SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(it.dueDate))
            dialogBinding.editTextPriority.setText(it.priority.name)
            dialogBinding.switchCompleted.isChecked = it.isCompleted
        }
        
        // Set up date picker
        dialogBinding.editTextDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            task?.let { 
                calendar.time = it.dueDate 
            }
            
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    dialogBinding.editTextDueDate.setText(
                        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(selectedDate.time)
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        dialogBinding.editTextPriority.setOnClickListener {
            val priorityDialog = android.app.AlertDialog.Builder(this)
                .setTitle("Select Priority")
                .setItems(priorities.toTypedArray()) { _, which ->
                    dialogBinding.editTextPriority.setText(priorities[which])
                }
                .create()
            priorityDialog.show()
        }

        /// SAVE BUTTON
        dialogBinding.buttonSave.setOnClickListener {
            val title = dialogBinding.editTextTitle.text.toString().trim()
            val description = dialogBinding.editTextDescription.text.toString().trim()
            val dueDateText = dialogBinding.editTextDueDate.text.toString().trim()
            val priorityText = dialogBinding.editTextPriority.text.toString().trim()
            val isCompleted = dialogBinding.switchCompleted.isChecked()
            
            if (title.isEmpty() || description.isEmpty() || dueDateText.isEmpty() || priorityText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            try {
                val dueDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(dueDateText) ?: Date()
                val priority = when (priorityText) {
                    "Low" -> Priority.LOW
                    "Medium" -> Priority.MEDIUM
                    "High" -> Priority.HIGH
                    else -> Priority.MEDIUM
                }
                
                val newTask = task?.copy(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    isCompleted = isCompleted
                ) ?: Task(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    isCompleted = isCompleted
                )
                
                if (task == null) {
                    taskViewModel.addTask(newTask)
                    Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    taskViewModel.updateTask(newTask)
                    Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
                }
                
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            }
        }

        /// DELETE BUTTON
        dialogBinding.buttonDelete.setOnClickListener {
            if (task != null) {
                taskViewModel.deleteTask(task)
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Cannot delete new task", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}