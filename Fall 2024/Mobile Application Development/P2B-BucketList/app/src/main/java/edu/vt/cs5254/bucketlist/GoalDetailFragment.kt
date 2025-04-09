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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class GoalDetailFragment : Fragment() {

    private val args: GoalDetailFragmentArgs by navArgs()
    private val vm: GoalDetailViewModel by viewModels{
        GoalDetailViewModelFactory(args.goalId)
    }
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.goal.collect{ goal ->
                    goal?.let {
                        updateView(it)
                    }
                }
            }
        }

        binding.pausedCheckbox.setOnClickListener {
            vm.updateGoal { oldGoal ->
                val isCurrentlyPaused = oldGoal.isPaused
                oldGoal.copy().apply {
                    notes = if (!isCurrentlyPaused) {
                        oldGoal.notes + GoalNote(
                            type = GoalNoteType.PAUSED,
                            goalId = oldGoal.id
                        )
                    } else {
                        oldGoal.notes.filter { it.type != GoalNoteType.PAUSED }
                    }
                }
            }
        }

        binding.completedCheckbox.setOnClickListener {
            vm.updateGoal { oldGoal ->
                oldGoal.copy().apply { notes =
                    if (oldGoal.isCompleted) {
                        oldGoal.notes.filter { it.type != GoalNoteType.COMPLETED }
                    } else {
                        oldGoal.notes + GoalNote(
                            type = GoalNoteType.COMPLETED,
                            goalId = oldGoal.id
                        )
                    }
                }
            }
        }

        binding.titleText.doOnTextChanged{text, _, _, _ ->
            vm.updateGoal { oldGoal ->
                oldGoal.copy(title = text.toString()).apply { notes = oldGoal.notes }
            }
        }

        // Fetching the String from dialog using bundle.getString()
        setFragmentResultListener(ProgressDialogFragment.REQUEST_KEY) { _, bundle ->
            val progressText = bundle.getString(ProgressDialogFragment.BUNDLE_KEY)
            progressText?.let {
                vm.updateGoal { oldGoal ->
                    oldGoal.copy().apply { notes =
                        oldGoal.notes + GoalNote(
                            type = GoalNoteType.PROGRESS,
                            text = it,
                            goalId = oldGoal.id
                        )
                    }
                }
            }
        }

        binding.addProgressButton.setOnClickListener{
            findNavController().navigate(GoalDetailFragmentDirections.addProgress())
        }

    } // onViewCreated ending brace

    // UpdateView
    private fun updateView(goal: Goal) {
        if(binding.titleText.text.toString() != goal.title){
            binding.titleText.setText(goal.title)
        }

        // Time formatting
        binding.lastUpdatedText.text =
            DateFormat.format(
                "'Last updated' yyy-MM-dd 'at' hh:mm:ss A",
                goal.lastUpdated
            )

        entryButtons = listOf(
            binding.note0Button,
            binding.note1Button,
            binding.note2Button,
            binding.note3Button,
            binding.note4Button
        )

        binding.pausedCheckbox.isChecked = goal.isPaused
        binding.completedCheckbox.isChecked = goal.isCompleted

        binding.completedCheckbox.isEnabled = !binding.pausedCheckbox.isChecked
        binding.pausedCheckbox.isEnabled = !binding.completedCheckbox.isChecked

        // Show or hide the FAB based on the completion status of the goal
        if (goal.isCompleted) {
            binding.addProgressButton.hide()
        } else {
            binding.addProgressButton.show()
        }

        entryButtons.forEach { it.visibility = View.GONE }

        goal.notes.zip(entryButtons).forEach { (note, button) ->
            button.configureForNote(note)
        }
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
