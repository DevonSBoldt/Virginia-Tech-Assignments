package edu.vt.cs5254.multiquiz

import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.vt.cs5254.multiquiz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var buttonList: List<Button>
    private val vm: QuizViewModel by viewModels()

    // Launcher logic
    private val scoreActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val resetPressed = result.data?.getBooleanExtra(ScoreActivity.EXTRA_RESET_PRESSED, false) ?: false

            // Reset logic
            if (resetPressed) {
                // If reset button WAS pressed, reset fully
                vm.resetQuiz()
            } else {
                // If reset button was NOT pressed, just return to the first question
                // Everything else stays the same
                vm.first()
            }
            updateView(true)
        }
    }

    // onCreate logic
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonList = listOf(
            binding.answer0Button,
            binding.answer1Button,
            binding.answer2Button,
            binding.answer3Button
        )

        // Hint button functionality
        binding.hintButton.setOnClickListener {
            vm.currentQuestionAnswerList
                .filter { !it.isCorrect && it.isEnabled }
                .random()
                .let {
                    it.isSelected = false
                    it.isEnabled = false
                }
            updateView()
        }

        // Submit button functionality
        binding.submitButton.setOnClickListener {
            if (vm.isLastQuestion) {
                val score = vm.calculateScore()
                val intent = ScoreActivity.newIntent(this, score)
                scoreActivityLauncher.launch(intent)
            } else {
                vm.next()
                updateView(true)
            }
        }

        updateView(true)
    } // onCreate

    // updateView
    private fun updateView(update: Boolean = false) {
        binding.answerTextView.text = getString(vm.currentQuestionTextID)

        if (update) {
            vm.currentQuestionAnswerList.zip(buttonList).forEach { (answer, button) ->
                button.setText(answer.textResId)
                button.isEnabled = answer.isEnabled
                button.isSelected = answer.isSelected
                button.setOnClickListener {
                    vm.currentQuestionAnswerList.filter { it != answer }.forEach {
                        it.isSelected = false
                    }
                    answer.isSelected = !answer.isSelected
                    updateView()  // Refresh view when answer is selected
                }
            }
        }

        vm.currentQuestionAnswerList.zip(buttonList).forEach { (answer, button) ->
            button.isSelected = answer.isSelected
            button.isEnabled = answer.isEnabled
            button.updateColor()  // Make sure this updates the color properly
        }

        binding.hintButton.isEnabled = vm.currentQuestionAnswerList.any { !it.isCorrect && it.isEnabled }
        binding.submitButton.isEnabled = vm.currentQuestionAnswerList.any { it.isSelected }
    } // updateView

} // MainActivity
