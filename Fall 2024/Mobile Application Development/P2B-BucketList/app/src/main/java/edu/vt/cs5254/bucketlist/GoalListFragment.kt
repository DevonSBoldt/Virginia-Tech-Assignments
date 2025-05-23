package edu.vt.cs5254.bucketlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
    ): View? {
        _binding = FragmentGoalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.goalRecyclerView.layoutManager = LinearLayoutManager(context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.goals.collect() { goals ->
                    binding.goalRecyclerView.adapter = GoalListAdapter(goals){
                        findNavController().navigate(GoalListFragmentDirections.showDetail(it))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
