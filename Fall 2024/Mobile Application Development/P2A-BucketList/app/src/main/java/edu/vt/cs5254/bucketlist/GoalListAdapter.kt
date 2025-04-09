package edu.vt.cs5254.bucketlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.bucketlist.databinding.ListItemGoalBinding

class GoalHolder(private val binding: ListItemGoalBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(goal: Goal){
        binding.listItemTitle.text = goal.title
        val notesCount = goal.notes.count() { it.type == GoalNoteType.PROGRESS }
        binding.listItemProgressCount.text = binding.root.context.getString(R.string.list_item_progress_count, notesCount)
        when{
            goal.isCompleted -> {
                binding.listItemImage.setImageResource(R.drawable.ic_goal_completed)
                binding.listItemImage.visibility = View.VISIBLE
            }
            goal.isPaused -> {
                binding.listItemImage.setImageResource(R.drawable.ic_goal_paused)
                binding.listItemImage.visibility = View.VISIBLE
            }
            else -> {
                binding.listItemImage.visibility = View.GONE
            }
        }
    }
}


class GoalListAdapter(private val goals: List<Goal>) : RecyclerView.Adapter<GoalHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalHolder{
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemGoalBinding.inflate(inflater, parent, false)
        return GoalHolder(binding)
    }

    override fun getItemCount(): Int {
        return goals.size
    }
    override fun onBindViewHolder(holder: GoalHolder, position: Int) {
        holder.bind(goals[position])
    }

}