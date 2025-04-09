package edu.vt.cs5254.multiquiz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import edu.vt.cs5254.multiquiz.databinding.ActivityScoreBinding

private const val EXTRA_SCORE = "edu.vt.cs5254.multiquiz.score"
private const val KEY_SCORE_TEXT = "score_text"
private const val KEY_RESET_PRESSED = "reset_pressed"

class ScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoreBinding
    private var resetPressed = false  // Track if reset was pressed

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra(EXTRA_SCORE, -1)

        if (savedInstanceState != null) {
            binding.scoreText.text = savedInstanceState.getString(KEY_SCORE_TEXT)
            resetPressed = savedInstanceState.getBoolean(KEY_RESET_PRESSED)
            binding.resetButton.isEnabled = !resetPressed
        } else {
            binding.scoreText.text = if (score == -1) "?" else score.toString()
        }

        // Handle reset button click
        binding.resetButton.setOnClickListener {
            binding.scoreText.text = "?"
            binding.resetButton.isEnabled = false
            resetPressed = true  // Set flag when reset button is pressed
        }

        // Handle back button press with the new onBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_RESET_PRESSED, resetPressed)  // Pass the reset state with the correct key
                }
                setResult(RESULT_OK, resultIntent)
                finish()  // Finish the activity when back is pressed
            }
        })
    }

    // Save instance state during rotation or configuration changes
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SCORE_TEXT, binding.scoreText.text.toString())
        outState.putBoolean(KEY_RESET_PRESSED, resetPressed)
    }

    // RESET LOGIC
    companion object {
        const val EXTRA_RESET_PRESSED = "edu.vt.cs5254.multiquiz.reset"  // Now public

        fun newIntent(context: Context, score: Int): Intent {
            return Intent(context, ScoreActivity::class.java).apply {
                putExtra(EXTRA_SCORE, score)
            }
        }
    }
}
