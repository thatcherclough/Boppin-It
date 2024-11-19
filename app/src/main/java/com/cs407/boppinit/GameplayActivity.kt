package com.cs407.boppinit

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cs407.boppinit.activities.standard.BopItActivity
import com.cs407.boppinit.activities.standard.BopItActivityRepository
import com.cs407.boppinit.activities.standard.BopItActivityView
import com.cs407.boppinit.activities.standard.EliminatedActivity
import com.cs407.boppinit.activities.standard.GameOverActivity
import com.cs407.boppinit.activities.standard.PassItActivity
import com.cs407.boppinit.databinding.ActivityGameplayBinding

class GameplayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameplayBinding
    private lateinit var currentActivity: BopItActivity
    private lateinit var currentView: BopItActivityView
    private var currentTimer: CountDownTimer? = null
    private lateinit var difficulty: Difficulty
    private var playersLeft: Int = 1
    private var coop: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        difficulty = Difficulty.valueOf(intent.getStringExtra(GameProps.DIFFICULTY.name) ?: Difficulty.EASY.name)
        playersLeft = intent.getIntExtra(GameProps.NUM_PLAYERS.name, 1)
        coop = intent.getStringExtra(GameProps.GAME_MODE.name) == GameMode.COOP.name

        updateActivity(BopItActivityRepository.getRandomActivity())
        updatePlayersLeft(playersLeft)
    }

    override fun onDestroy() {
        super.onDestroy()
        currentTimer?.cancel()
    }

    private fun updateActivity(activity: BopItActivity) {
        // Cancel any existing timer
        currentTimer?.cancel()

        currentActivity = activity
        currentView = activity.viewProvider({
            handleActivityComplete()
        }, difficulty)

        // Update UI with activity details
        binding.titleText.text = activity.title
        binding.subtitleText.text = activity.subtitle

        // Add the activity view to the container
        supportFragmentManager.beginTransaction()
            .replace(R.id.activityContainer, currentView as Fragment)
            .commit()

        // Start timer if this activity has time limits
        val timeLimit = activity.getTimeLimit(difficulty)
        if (timeLimit == null) {
            updateTimer("")
        } else {
            startTimer(timeLimit)
        }
    }

    private fun startTimer(timeLimit: Long) {
        currentTimer?.cancel()

        currentTimer = object : CountDownTimer(timeLimit, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (((millisUntilFinished / 1000)) + 1).toString()
                updateTimer(secondsLeft)
            }

            override fun onFinish() {
                handleTimeUp()
            }
        }.start()
    }

    private fun handleTimeUp() {
        if (currentActivity == PassItActivity) {
            updateActivity(BopItActivityRepository.getRandomActivity())
        } else {
            if (playersLeft <= 1) {
                playersLeft = 0
                updatePlayersLeft(playersLeft)
                updateActivity(GameOverActivity)
            } else {
                updateActivity(EliminatedActivity)
            }
        }
    }

    private fun handleActivityComplete() {
        currentTimer?.cancel()
        if (currentActivity == EliminatedActivity) {
            playersLeft--
            updatePlayersLeft(playersLeft)
            updateActivity(BopItActivityRepository.getRandomActivity())
        } else if (coop) {
            updateActivity(PassItActivity)
        } else {
            updateActivity(BopItActivityRepository.getRandomActivity())
        }
    }

    private fun updateTimer(text: String) {
        binding.timerText.text = text
    }

    private fun updatePlayersLeft(players: Int) {
        var playersString = "$playersLeft"
        if (players == 1) {
            playersString += " player left"
        } else {
            playersString += " players left"
        }
        binding.playersRemainingText.text = playersString
    }
}