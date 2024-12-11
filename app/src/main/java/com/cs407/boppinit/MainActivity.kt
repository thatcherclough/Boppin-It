package com.cs407.boppinit

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    companion object {
        private const val MICROPHONE_PERMISSION_CODE = 200
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestMicrophonePermission()

        setupClickListeners()
        setupToggleGroups()

        // Start audio manager
        AudioManager.startAudioManager(this)
    }

    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                MICROPHONE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MICROPHONE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Show toast
                Toast.makeText(this, "Microphone permission is required to play the game", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnPlay.setOnClickListener {
                startGame()
            }

            btnStar.setOnClickListener {
                val intent = Intent(this@MainActivity, StatsActivity::class.java)
                startActivity(intent)
            }

            btnSettings.setOnClickListener {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
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
        if (selectedGameMode == GameMode.SOLO) {
            val intent = Intent(this@MainActivity, GameplayActivity::class.java)
            intent.putExtra(GameProps.GAME_MODE.name, selectedGameMode.name)
            intent.putExtra(GameProps.DIFFICULTY.name, selectedDifficulty.name)
            intent.putExtra(GameProps.NUM_PLAYERS.name, 1) // Assuming solo mode has 1 player
            startActivity(intent)
        } else {
            val intent = Intent(this@MainActivity, PlayerSelectionActivity::class.java)
            intent.putExtra(GameProps.GAME_MODE.name, selectedGameMode.name)
            intent.putExtra(GameProps.DIFFICULTY.name, selectedDifficulty.name)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioManager.stopMusic()
    }
}