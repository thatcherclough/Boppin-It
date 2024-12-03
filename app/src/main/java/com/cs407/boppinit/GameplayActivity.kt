package com.cs407.boppinit

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cs407.boppinit.activities.standard.BopItActivity
import com.cs407.boppinit.activities.standard.BopItActivityRepository
import com.cs407.boppinit.activities.standard.BopItActivityView
import com.cs407.boppinit.activities.standard.EliminatedActivity
import com.cs407.boppinit.activities.standard.GameOverActivity
import com.cs407.boppinit.activities.standard.PassItActivity
import com.cs407.boppinit.databinding.ActivityGameplayBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameplayActivity : AppCompatActivity() {
    private val gameHistoryDao by lazy {
        AppDatabase.getDatabase(this).gameHistoryDao()
    }

    private lateinit var binding: ActivityGameplayBinding

    private var coop: Boolean = true
    private var numPlayers: Int = 1
    private lateinit var difficulty: Difficulty
    private var currentTimer: CountDownTimer? = null

    private lateinit var currentActivity: BopItActivity
    private lateinit var currentView: BopItActivityView
    private var playersLeft: Int = 1

    private var currentScore = 0
    private var activitiesCompleted = 0
    private var timeRemainingOnTimer: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        difficulty = Difficulty.valueOf(intent.getStringExtra(GameProps.DIFFICULTY.name) ?: Difficulty.EASY.name)
        numPlayers = intent.getIntExtra(GameProps.NUM_PLAYERS.name, 1)
        playersLeft = numPlayers
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

        currentTimer = object : CountDownTimer(timeLimit, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingOnTimer = millisUntilFinished
                val secondsLeft = ((millisUntilFinished / 1000) + 1).toString()
                updateTimer(secondsLeft)
            }

            override fun onFinish() {
                timeRemainingOnTimer = 0
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

                lifecycleScope.launch {
                    val isNewHighScore = isNewHighScore(currentScore, if (coop) GameMode.COOP else GameMode.SOLO)
                    saveGame()

                    // Create the game over activity with current game stats
                    val gameOverActivityInstance = GameOverActivity(
                        currentScore,
                        activitiesCompleted,
                        difficulty,
                        if (coop) GameMode.COOP else GameMode.SOLO,
                        isNewHighScore
                    )

                    updateActivity(gameOverActivityInstance)
                }


            } else {
                updateActivity(EliminatedActivity)
            }
        }
    }

    private suspend fun isNewHighScore(score: Int, gameMode: GameMode): Boolean {
        val currentHighScore = gameHistoryDao.getHighScore(gameMode)?.finalScore ?: 0
        return score > currentHighScore
    }

    private fun handleActivityComplete() {
        currentTimer?.cancel()
        if (currentActivity == EliminatedActivity) {
            playersLeft--
            updatePlayersLeft(playersLeft)
            updateActivity(BopItActivityRepository.getRandomActivity())
        } else if (coop) {
            updateScore()
            updateActivity(PassItActivity)
        } else {
            updateScore()
            updateActivity(BopItActivityRepository.getRandomActivity())
        }
    }

    private fun updateScore() {
        val timeLimit = currentActivity.getTimeLimit(difficulty) ?: return
        val points = ScoreCalculator.calculatePoints(timeRemainingOnTimer, timeLimit, difficulty)
        currentScore += points
        activitiesCompleted++
    }

    private fun saveGame() {
        val gameHistory = GameHistory(
            difficulty = difficulty,
            numPlayers = numPlayers,
            gameMode = if (coop) GameMode.COOP else GameMode.SOLO,
            finalScore = currentScore,
            activitiesCompleted = activitiesCompleted
        )

        lifecycleScope.launch(Dispatchers.IO) {
            gameHistoryDao.insert(gameHistory)
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