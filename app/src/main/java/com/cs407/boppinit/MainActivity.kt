package com.cs407.boppinit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import com.cs407.boppinit.databinding.ActivityMainBinding

enum class GameMode {
    SOLO, COOP
}

enum class Difficulty {
    EASY, MEDIUM, HARD
}

enum class GameProps {
    GAME_MODE, DIFFICULTY, NUM_PLAYERS
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedGameMode: GameMode = GameMode.SOLO
    private var selectedDifficulty: Difficulty = Difficulty.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupToggleGroups()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnPlay.setOnClickListener {
                startGame()
            }

            btnStar.setOnClickListener {
                // STATS
            }

            btnSettings.setOnClickListener {
                // SETTINGS
            }
        }
    }

    private fun setupToggleGroups() {
        binding.apply {
            gameModeToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) {  // Only process when a button is being checked
                    // Set all buttons to lower opacity
                    group.forEach { button ->
                        button.alpha = 0.5f
                    }
                    // Set selected button to full opacity
                    group.findViewById<Button>(checkedId)?.alpha = 1.0f

                    selectedGameMode = when (checkedId) {
                        R.id.btnSolo -> GameMode.SOLO
                        R.id.btnCoop -> GameMode.COOP
                        else -> GameMode.SOLO
                    }
                }
            }

            // Setup difficulty toggle group
            difficultyToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) {  // Only process when a button is being checked
                    // Set all buttons to lower opacity
                    group.forEach { button ->
                        button.alpha = 0.5f
                    }
                    // Set selected button to full opacity
                    group.findViewById<Button>(checkedId)?.alpha = 1.0f

                    selectedDifficulty = when (checkedId) {
                        R.id.btnEasy -> Difficulty.EASY
                        R.id.btnMedium -> Difficulty.MEDIUM
                        R.id.btnHard -> Difficulty.HARD
                        else -> Difficulty.EASY
                    }
                }
            }


            // Set default selections and initial opacity
            gameModeToggleGroup.apply {
                check(R.id.btnSolo)
                forEach { button ->
                    button.alpha = if (button.id == R.id.btnSolo) 1.0f else 0.5f
                }
            }

            difficultyToggleGroup.apply {
                check(R.id.btnEasy)
                forEach { button ->
                    button.alpha = if (button.id == R.id.btnEasy) 1.0f else 0.5f
                }
            }
        }
    }

    private fun startGame() {
        val intent = Intent(this@MainActivity, PlayerSelectionActivity::class.java)
        intent.putExtra(GameProps.GAME_MODE.name, selectedGameMode.name)
        intent.putExtra(GameProps.DIFFICULTY.name, selectedDifficulty.name)
        startActivity(intent)
    }
}