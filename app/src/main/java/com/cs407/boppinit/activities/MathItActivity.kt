package com.cs407.boppinit.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cs407.boppinit.Difficulty
import com.cs407.boppinit.activities.standard.BopItActivityView
import com.cs407.boppinit.databinding.FragmentMathItBinding

class MathItActivityView(
    private val onComplete: () -> Unit,
    private val difficulty: Difficulty
) : Fragment(), BopItActivityView {
    private var _binding: FragmentMathItBinding? = null
    private val binding get() = _binding!!
    private var correctAnswer: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMathItBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        startActivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initializeView() {
        // Generate a math problem based on difficulty level
        val mathProblem = generateMathProblem(difficulty)
        correctAnswer = mathProblem.second

        // Display the problem
        binding.mathItText.text = mathProblem.first

        // Set up button click listener
        binding.submitButton.setOnClickListener {
            val userAnswer = binding.answerInput.text.toString()
            if (userAnswer.isNotEmpty() && userAnswer.toIntOrNull() == correctAnswer) {
                onComplete()
            } else {
                binding.wrongAnswerView.visibility = View.VISIBLE
            }
        }
    }

    override fun startActivity() {
        // Show UI components for the activity
        binding.mathItText.visibility = View.VISIBLE
        binding.answerInput.visibility = View.VISIBLE
        binding.submitButton.visibility = View.VISIBLE
        binding.wrongAnswerView.visibility = View.INVISIBLE
    }

    override fun stopActivity() {
        // Hide UI components for the activity
        binding.mathItText.visibility = View.GONE
        binding.answerInput.visibility = View.GONE
        binding.submitButton.visibility = View.GONE
        binding.wrongAnswerView.visibility = View.GONE
    }

    private fun generateMathProblem(difficulty: Difficulty): Pair<String, Int> {
        // Generate math problems of varying complexity based on difficulty level
        return when (difficulty) {
            Difficulty.EASY -> {
                val a = (1..10).random()
                val b = (1..10).random()
                "$a + $b" to (a + b)
            }
            Difficulty.MEDIUM -> {
                val a = (10..50).random()
                val b = (10..50).random()
                "$a - $b" to (a - b)
            }
            Difficulty.HARD -> {
                val a = (1..12).random()
                val b = (1..12).random()
                "$a × $b" to (a * b)
            }
        }
    }
}