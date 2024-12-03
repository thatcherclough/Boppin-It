package com.cs407.boppinit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cs407.boppinit.databinding.ActivityPlayerSelectionBinding

class PlayerSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerSelectionBinding
    private var numPlayers = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        updatePlayerCount()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnMinus.setOnClickListener {
                if (numPlayers > 2) {
                    numPlayers--
                    updatePlayerCount()
                }
            }

            btnPlus.setOnClickListener {
                numPlayers++
                updatePlayerCount()
            }

            btnNext.setOnClickListener {

                val gameMode = intent.getStringExtra(GameProps.GAME_MODE.name) ?: GameMode.SOLO.name
                val difficulty = intent.getStringExtra(GameProps.DIFFICULTY.name) ?: Difficulty.EASY.name

                // Create intent for GameplayActivity
                val intent = Intent(this@PlayerSelectionActivity, GameplayActivity::class.java).apply {
                    putExtra(GameProps.GAME_MODE.name, gameMode)
                    putExtra(GameProps.DIFFICULTY.name, difficulty)
                    putExtra(GameProps.NUM_PLAYERS.name, numPlayers)
                }
                startActivity(intent)
            }
        }
    }

    private fun updatePlayerCount() {
        binding.playerCount.text = numPlayers.toString()
    }
}