package com.mahmutalperenunal.todolist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mahmutalperenunal.todolist.databinding.FragmentTaskAddEditBinding
import com.mahmutalperenunal.todolist.util.exhaustive
import com.mahmutalperenunal.todolist.viewmodel.TaskAddEditViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskAddEditFragment : Fragment() {

    private var _binding: FragmentTaskAddEditBinding? = null
    private val binding get() =_binding!!

    private val viewModel: TaskAddEditViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskAddEditBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            taskNameEditText.setText(viewModel.taskName)
            taskImportantCheckBox.isChecked = viewModel.taskImportance
            taskImportantCheckBox.jumpDrawablesToCurrentState()
            taskCreatedDateTextView.isVisible = viewModel.task != null
            taskCreatedDateTextView.text = "Created: ${viewModel.task?.createdDateFormat}"

            taskNameEditText.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            taskImportantCheckBox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            taskSaveFloatingButton.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskAddEditEvent.collect { event ->
                when(event) {
                    is TaskAddEditViewModel.TaskAddEditEvent.NavigateBackWithResult -> {
                        binding.taskNameEditText.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is TaskAddEditViewModel.TaskAddEditEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}