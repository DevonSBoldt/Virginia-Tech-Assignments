package edu.vt.cs5254.bucketlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.bucketlist.databinding.FragmentGoalListBinding
import kotlinx.coroutines.launch

class GoalListFragment : Fragment() {

    private var _binding: FragmentGoalListBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "FragmentGoalListBinding is null!!!!!!!!" }

    private val vm: GoalListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalListBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_goal_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_goal -> {
                        showGoal()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner
        )
        binding.noGoalButton.setOnClickListener {
            showGoal()
        }
        getItemTouchHelper().attachToRecyclerView(binding.goalRecyclerView)
        binding.goalRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    private fun showGoal() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newGoal = Goal()
            vm.addGoal(newGoal)
            findNavController().navigate(
                GoalListFragmentDirections.showDetail(newGoal.id)
            )
        }
    }

    private fun getItemTouchHelper(): ItemTouchHelper {

        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val goalHolder = viewHolder as GoalHolder
                    val swipedGoal = goalHolder.boundGoal
                    vm.deleteGoal(swipedGoal)
                }
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getItemTouchHelper().attachToRecyclerView(binding.goalRecyclerView)
        binding.goalRecyclerView.layoutManager = LinearLayoutManager(context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.goals.collect { goals ->
                    if (goals.isEmpty()) {
                        binding.noGoalText.visibility = View.VISIBLE
                        binding.noGoalButton.visibility = View.VISIBLE
                    } else {
                        binding.noGoalText.visibility = View.GONE
                        binding.noGoalButton.visibility = View.GONE
                    }
                    binding.goalRecyclerView.adapter = GoalListAdapter(goals) {
                        findNavController().navigate(GoalListFragmentDirections.showDetail(it))
                    }
                }
            }
            binding.noGoalButton.setOnClickListener{
                showGoal()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
