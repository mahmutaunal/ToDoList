package com.mahmutalperenunal.todolist.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmutalperenunal.todolist.ADD_TASK_RESULT_OK
import com.mahmutalperenunal.todolist.EDIT_TASK_RESULT_OK
import com.mahmutalperenunal.todolist.data.Task
import com.mahmutalperenunal.todolist.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TaskAddEditViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state["taskName"] = value
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state["taskImportance"] = value
        }

    private val taskAddEditEventChannel = Channel<TaskAddEditEvent>()
    val taskAddEditEvent = taskAddEditEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }

        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createNewTask(newTask)
        }
    }

    private fun createNewTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        taskAddEditEventChannel.send(TaskAddEditEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        taskAddEditEventChannel.send(TaskAddEditEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        taskAddEditEventChannel.send(TaskAddEditEvent.ShowInvalidInputMessage(text))
    }

    sealed class TaskAddEditEvent {
        data class ShowInvalidInputMessage(val msg: String) : TaskAddEditEvent()
        data class NavigateBackWithResult(val result: Int) : TaskAddEditEvent()
    }
}