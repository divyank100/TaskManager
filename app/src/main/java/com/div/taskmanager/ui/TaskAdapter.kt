package com.div.taskmanager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.div.taskmanager.R
import com.div.taskmanager.data.Priority
import com.div.taskmanager.data.Task
import com.div.taskmanager.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                taskTitle.text = task.title
                taskDescription.text = task.description
                taskDueDate.text = "Due: ${formatDate(task.dueDate)}"
                taskDate.text = formatDate(task.createdAt)
                taskStatus.text = if(task.isCompleted) "COMPLETED" else "PENDING"


                when (task.priority) {
                    Priority.HIGH -> {
                        taskPriority.text = "HIGH"
                        taskPriority.setBackgroundResource(R.drawable.priority_high_background)
                    }
                    Priority.MEDIUM -> {
                        taskPriority.text = "MEDIUM"
                        taskPriority.setBackgroundResource(R.drawable.priority_medium_background)
                    }
                    Priority.LOW -> {
                        taskPriority.text = "LOW"
                        taskPriority.setBackgroundResource(R.drawable.priority_low_background)
                    }
                }


                when (task.isCompleted) {
                    true -> {
                        taskStatus.text = "COMPLETED"
                        taskStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                    }
                    false -> {
                        taskStatus.text = "PENDING"
                        taskStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.yellow))
                    }
                }


                root.setOnClickListener { onTaskClick(task) }
            }
        }

        private fun formatDate(date: Date): String {
            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            return formatter.format(date)
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
