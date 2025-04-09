package edu.vt.cs5254.bucketlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.vt.cs5254.bucketlist.databinding.FragmentGoalDetailBinding
import android.text.format.DateFormat
import android.widget.Button
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels

class GoalDetailFragment : Fragment() {

    private val vm: GoalDetailViewModel by viewModels()
    private var _binding: FragmentGoalDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "FragmentGoalDetailBinding is NULL!!!!!!!!!!" }
    private lateinit var entryButtons: List<Button>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGoalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Completed checkbox logic
        binding.completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val isFulfilled = vm.goal.isCompleted
            if (isChecked != isFulfilled) {
                if (isChecked) {
                    vm.goal.notes += GoalNote(
                        vm.goal.id,
                        "",
                        GoalNoteType.COMPLETED,
                        goalId = vm.goal.id
                    )
                } else {
                    vm.goal.notes = vm.goal.notes.filter { it.type != GoalNoteType.COMPLETED }
                }
            }
            updateView()
        }

        // Paused checkbox logic
        binding.pausedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val isDeferred = vm.goal.isPaused
            if (isChecked != isDeferred) {
                if (isChecked) {
                    vm.goal.notes += GoalNote(
                        vm.goal.id,
                        "",
                        GoalNoteType.PAUSED,
                        goalId = vm.goal.id
                    )
                } else {
                    vm.goal.notes = vm.goal.notes.filter { it.type != GoalNoteType.PAUSED }
                }
            }
            updateView()
        }

        binding.apply {
            titleText.doOnTextChanged { text, _, _, _ ->
                vm.goal = vm.goal.copy(title = text.toString()).apply {
                    notes = vm.goal.notes
                }
            }
            updateView()
        }

        updateView()
    }

    // UpdateView
    private fun updateView() {
        binding.titleText.setText(vm.goal.title)

        // Time stuff
        val time = DateFormat.format("hh:mm:ss a", vm.goal.lastUpdated)
        val day = DateFormat.format("yyyy-MM-dd", vm.goal.lastUpdated)
        binding.lastUpdatedText.text = "Last updated $day at $time"

        entryButtons = listOf(
            binding.note0Button,
            binding.note1Button,
            binding.note2Button,
            binding.note3Button,
            binding.note4Button
        )

        entryButtons.forEach { it.visibility = View.GONE }

        vm.goal.notes.zip(entryButtons).forEach { (note, button) ->
            button.configureForNote(note)
        }

        binding.pausedCheckbox.isChecked = vm.goal.isPaused
        binding.completedCheckbox.isChecked = vm.goal.isCompleted

        binding.completedCheckbox.isEnabled = !binding.pausedCheckbox.isChecked
        binding.pausedCheckbox.isEnabled = !binding.completedCheckbox.isChecked
    }

    private fun Button.configureForNote(note: GoalNote) {
        text = note.type.toString()
        visibility = View.VISIBLE

        when (note.type) {
            GoalNoteType.PAUSED -> {
                setBackgroundWithContrastingText("blue")
            }
            GoalNoteType.COMPLETED -> {
                setBackgroundWithContrastingText("green")
            }
            GoalNoteType.PROGRESS -> {
                text = note.text
                setBackgroundWithContrastingText("lightgray")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
