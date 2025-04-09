package edu.vt.cs5254.multiquiz

import androidx.lifecycle.ViewModel

// ViewModel class for the Quiz. It holds the data and logic for the quiz
class QuizViewModel : ViewModel() {

    // List of all questions
    // Each question has a string resource ID and a list of answers associated with it
    private val questionBank = listOf(
        // Question 1 with four possible answers, Ascend being the correct choice
        Question(
            R.string.question_0_button,
            listOf(
                Answer(R.string.question_0_answer_0_button, true),
                Answer(R.string.question_0_answer_1_button, false),
                Answer(R.string.question_0_answer_2_button, false),
                Answer(R.string.question_0_answer_3_button, false)
            )
        ),

        // Question 2 with four possible answers, Icebox being the correct choice
        Question(
            R.string.question_1_button,
            listOf(
                Answer(R.string.question_1_answer_0_button, false),
                Answer(R.string.question_1_answer_1_button, true),
                Answer(R.string.question_1_answer_2_button, false),
                Answer(R.string.question_1_answer_3_button, false)
            )
        ),

        // Question 3 with four possible answers, Sentinels being the correct choice
        Question(
            R.string.question_2_button,
            listOf(
                Answer(R.string.question_2_answer_0_button, false),
                Answer(R.string.question_2_answer_1_button, false),
                Answer(R.string.question_2_answer_2_button, true),
                Answer(R.string.question_2_answer_3_button, false)
            )
        ),

        // Question 4 with four possible answers, Yay being the correct choice
        Question(
            R.string.question_3_button,
            listOf(
                Answer(R.string.question_3_answer_0_button, false),
                Answer(R.string.question_3_answer_1_button, false),
                Answer(R.string.question_3_answer_2_button, false),
                Answer(R.string.question_3_answer_3_button, true)
            )
        )
    )

    private var currentIndex = 0

    val currentQuestionTextID: Int
        get() = questionBank[currentIndex].questionResId

    val currentQuestionAnswerList: List<Answer>
        get() = questionBank[currentIndex].answerList

    val isLastQuestion: Boolean
        get() = currentIndex == questionBank.size - 1

    // Move to the next question. Loops back to the first question after the last one
    fun next() {
        currentIndex = (currentIndex + 1) % questionBank.size
    }

    fun first() {
        currentIndex = 0
    }

    // Reset the quiz by resetting the index and re-enabling answers
    fun resetQuiz() {
        currentIndex = 0 // Reset index to the first question
        questionBank.forEach { question ->
            question.answerList.forEach { answer ->
                answer.isSelected = false  // Reset selection state
                answer.isEnabled = true    // Re-enable all buttons
            }
        }
    }

    // Calculate the total score based on correct answers and hints used
    fun calculateScore(): Int {
        var score = 0

        questionBank.forEach { question ->
            val correctAnswerSelected = question.answerList.any { it.isSelected && it.isCorrect }
            if (correctAnswerSelected) {
                score += 25 // Add 25 points for each correct answer
            }

            val hintsUsed = question.answerList.count { !it.isEnabled && !it.isCorrect }
            score -= hintsUsed * 8 // Subtract 8 points for each hint used
        }

        return score
    }
}
