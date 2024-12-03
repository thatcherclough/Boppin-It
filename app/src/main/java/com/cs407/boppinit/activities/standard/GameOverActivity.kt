package com.cs407.boppinit.activities.standard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cs407.boppinit.AppDatabase
import com.cs407.boppinit.Difficulty
import com.cs407.boppinit.GameMode
import com.cs407.boppinit.MainActivity
import com.cs407.boppinit.databinding.FragmentGameOverBinding
import kotlinx.coroutines.launch

class GameOverActivityView(
    private val onComplete: () -> Unit,
    private val score: Int,
    private val activitiesCompleted: Int,
    private val difficulty: Difficulty,
    private val gameMode: GameMode,
    private val isNewHighScore: Boolean
) : Fragment(), BopItActivityView {
    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding!!
    private val gameHistoryDao by lazy {
        AppDatabase.getDatabase(requireContext()).gameHistoryDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameOverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        startActivity()

        lifecycleScope.launch {
            updateStats()
        }
    }

    private suspend fun updateStats() {
        val highScore = gameHistoryDao.getHighScore(gameMode)?.finalScore ?: 0

        with(binding) {
            tvDifficulty.text = "Difficulty: ${difficulty.name}"
            tvScore.text = "Your Score: $score"
            tvHighScore.text = "High Score: $highScore"
            tvActivitiesCompleted.text = "Activities Completed: $activitiesCompleted"

            if (isNewHighScore) {
                tvNewHighScore.visibility = View.VISIBLE
                tvHighScore.visibility = View.GONE
            } else {
                tvHighScore.visibility = View.VISIBLE
                tvNewHighScore.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initializeView() {
        binding.btnMainMenu.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun startActivity() {
        // Nothing needed - just waiting for button click
    }

    override fun stopActivity() {
        // Nothing needed to clean up
    }
}