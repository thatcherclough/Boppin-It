package com.cs407.boppinit.activities

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.cs407.boppinit.Difficulty
import com.cs407.boppinit.R
import com.cs407.boppinit.activities.standard.BopItActivityView
import com.cs407.boppinit.databinding.FragmentPickItBinding

class PickItActivityView(
    private val onComplete: () -> Unit,
    private val difficulty: Difficulty
) : Fragment(), BopItActivityView {
    private var _binding: FragmentPickItBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickItBinding.inflate(inflater, container, false)
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
        setupCircles()
    }

    private fun setupCircles() {
        // Clear the container layout
        binding.circleContainer.removeAllViews()

        // Determine the number of circles based on difficulty
        val numCircles = when (difficulty) {
            Difficulty.EASY -> 4
            Difficulty.MEDIUM -> 8
            Difficulty.HARD -> 12
        }

        // Configure the GridLayout
        val gridLayout = GridLayout(requireContext()).apply {
            rowCount = 7
            columnCount = 5
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
        }
        binding.circleContainer.addView(gridLayout)

        // Create a list of all possible positions in the grid
        val gridSize = gridLayout.rowCount * gridLayout.columnCount
        val positions = (0 until gridSize).shuffled().take(numCircles)

        // Determine the correct circle
        val correctIndex = positions.random()

        // Add circles dynamically
        for (i in 0 until numCircles) {
            val circleView = TextView(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 150
                    height = 150
                    marginStart = 8
                    topMargin = 8
                    columnSpec = GridLayout.spec(positions[i] % gridLayout.columnCount)
                    rowSpec = GridLayout.spec(positions[i] / gridLayout.columnCount)
                }
                setBackgroundResource(R.drawable.circle_background) // Drawable for the circle shape
                setOnClickListener {
                    if (positions[i] == correctIndex) {
                        onComplete()
                    } else {
                        setBackgroundResource(R.drawable.circle_background_wrong) // Feedback for incorrect selection
                    }
                }
            }

            gridLayout.addView(circleView)
        }
    }





    override fun startActivity() {
        // Start activity by making circles visible
        binding.circleContainer.visibility = View.VISIBLE
    }

    override fun stopActivity() {
        // Clean up or hide views if needed
        binding.circleContainer.visibility = View.GONE
    }
}
