package edu.vt.cs5254.bucketlist

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import edu.vt.cs5254.bucketlist.databinding.FragmentGoalDetailBinding
import android.text.format.DateFormat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.io.File

class GoalDetailFragment : Fragment() {

    private val args: GoalDetailFragmentArgs by navArgs()
    private val vm: GoalDetailViewModel by viewModels {
        GoalDetailViewModelFactory(args.goalId)
    }
    private var _binding: FragmentGoalDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "FragmentGoalDetailBinding is NULL!!!!!!!!!!" }

    private val photoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {
        binding.goalPhoto.tag = null
        vm.goal.value?.let { goal ->
        updatePhoto(goal)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalDetailBinding.inflate(layoutInflater, container, false)
        binding.goalNoteRecyclerView.layoutManager = LinearLayoutManager(context)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_goal_detail, menu)
                val photoIntent = photoLauncher.contract.createIntent(
                    requireContext(),
                    Uri.EMPTY
                )
                menu.findItem(R.id.take_photo_menu).isVisible = canResolveIntent(photoIntent)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.share_goal_menu -> {
                        vm.goal.value?.let { goal ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareGoal(goal))
                            }
                            val chooserIntent = Intent.createChooser(
                                shareIntent,
                                getString(R.string.share_goal_via)
                            )
                            startActivity(chooserIntent)
                        }
                        true
                    }

                    R.id.take_photo_menu -> {
                        vm.goal.value?.let {
                            // Use the correct photo filename
                            val photoFile = File(
                                requireContext().applicationContext.filesDir,
                                it.photoFileName
                            )
                            val photoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "edu.vt.cs5254.bucketlist.fileprovider",
                                photoFile
                            )
                            photoLauncher.launch(photoUri)
                        }
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner)

        getItemTouchHelper().attachToRecyclerView(binding.goalNoteRecyclerView)
        binding.goalNoteRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.goal.collect { goal ->
                    goal?.let { updateView(goal) }
                }
            }
        }



        binding.apply {
            titleText.doOnTextChanged { text, _, _, _ ->
                vm.updateGoal { oldGoal ->
                    oldGoal.copy(title = text.toString()).apply { notes = oldGoal.notes }
                }
            }
            pausedCheckbox.setOnClickListener {
                vm.updateGoal { oldGoal ->
                    if (oldGoal.isPaused) {
                        oldGoal.copy().apply {
                            notes = oldGoal.notes.filter { it.type != GoalNoteType.PAUSED }
                        }
                    } else {
                        oldGoal.copy().apply {
                            notes = oldGoal.notes + GoalNote(
                                type = GoalNoteType.PAUSED,
                                goalId = oldGoal.id
                            )
                        }
                    }
                }
            }

            completedCheckbox.setOnClickListener {
                vm.updateGoal { oldGoal ->
                    if (oldGoal.isCompleted) {
                        oldGoal.copy().apply {
                            notes = oldGoal.notes.filter { it.type != GoalNoteType.COMPLETED }
                        }
                    } else {
                        oldGoal.copy().apply {
                            notes = oldGoal.notes + GoalNote(
                                type = GoalNoteType.COMPLETED,
                                goalId = oldGoal.id
                            )
                        }
                    }
                }
            }
            addProgressButton.setOnClickListener {
                findNavController().navigate(
                    GoalDetailFragmentDirections.addProgress()
                )
            }
            goalPhoto.setOnClickListener {
                vm.goal.value?.let {
                    findNavController().navigate(
                        GoalDetailFragmentDirections.showImageDetail(it.photoFileName)
                    )
                }
            }

            setFragmentResultListener(ProgressDialogFragment.REQUEST_KEY) { _, bundle ->
                val entryText = bundle.getString(ProgressDialogFragment.BUNDLE_KEY) ?: ""

                vm.updateGoal { oldGGoal ->
                    oldGGoal.copy().apply {
                        notes = oldGGoal.notes + GoalNote(
                            type = GoalNoteType.PROGRESS,
                            text = entryText,
                            goalId = oldGGoal.id,
                        )
                    }
                }
            }


        }

    }

    private fun shareGoal(goal: Goal): String {
        // Format the last updated date
        val dateString = DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss a", goal.lastUpdated)
        val lastUpdated = getString(R.string.last_updated, dateString)

        // Collect progress notes if any
        val progressNotes = buildString {
            if (goal.notes.any { it.type == GoalNoteType.PROGRESS }) {
                append(getString(R.string.goaL_report_progress))
                goal.notes.filter { it.type == GoalNoteType.PROGRESS }
                    .forEach { append("\n * ${it.text}") }
            }
        }

        // Determine the current status
        val status = when {
            goal.isPaused -> getString(R.string.goal_report_paused)
            goal.isCompleted -> getString(R.string.goal_report_completed)
            else -> ""
        }

        // Build the final message
        return buildString {
            append(goal.title)
            append("\n")
            append(lastUpdated)
            if (progressNotes.isNotEmpty()) {
                append("\n")
                append(progressNotes)
            }
            if (status.isNotEmpty()) {
                append(status) // Do not add extra newlines since they are included in the status string
            }
        }
    }


    private fun updateView(goal: Goal) {

        binding.goalNoteRecyclerView.adapter = GoalNoteListAdapter(goal.notes)

        val formattedDate = DateFormat.format(" yyyy-MM-dd 'at' hh:mm:ss a", goal.lastUpdated)
        binding.lastUpdatedText.text = getString(R.string.last_updated, formattedDate)

        if (binding.titleText.toString() != goal.title) {
            binding.titleText.setText(goal.title)
        }
        binding.pausedCheckbox.isChecked = goal.isPaused
        binding.completedCheckbox.isChecked = goal.isCompleted
        binding.completedCheckbox.isEnabled = !goal.isPaused
        binding.pausedCheckbox.isEnabled = !goal.isCompleted

        with(binding.addProgressButton) {
            if (goal.isCompleted) {
                hide()
            } else {
                show()
            }
        }
        updatePhoto(goal)

    }

    private fun updatePhoto(goal: Goal) {
        with(binding.goalPhoto) {
            val photoFile = File(requireContext().applicationContext.filesDir, goal.photoFileName)
            if (photoFile.exists()) {
                doOnLayout { photo ->
                    val bitmap = getScaledBitmap(

                        photoFile.path,
                    photo.width,
                    photo.height
                    )
                    setImageBitmap(bitmap)
                    tag = goal.photoFileName
                    isEnabled = true
                }
            } else {
                setImageBitmap(null)
                tag = null
                isEnabled = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        return requireActivity().packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ) != null
    }

    private fun getItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false  // No moving of items

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val noteHolder = viewHolder as GoalNoteHolder
                val note = noteHolder.boundNote
                if (note.type == GoalNoteType.PROGRESS) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        vm.updateGoal { oldGoal ->
                            oldGoal.copy().apply {
                                notes = oldGoal.notes.filter { it != note }
                            }
                        }
                    }
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val noteHolder = viewHolder as GoalNoteHolder
                val note = noteHolder.boundNote
                return if (note.type == GoalNoteType.PROGRESS) ItemTouchHelper.LEFT else 0
            }
        })
    }
}



