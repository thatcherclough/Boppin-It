package com.cs407.boppinit.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cs407.boppinit.AudioManager
import com.cs407.boppinit.Difficulty
import com.cs407.boppinit.activities.standard.BopItActivityView
import com.cs407.boppinit.databinding.FragmentMashItBinding

class MashItActivityView(
    private val onComplete: () -> Unit,
    private val difficulty: Difficulty
) : Fragment(), BopItActivityView {
    private var _binding: FragmentMashItBinding? = null
    private val binding get() = _binding!!
    private var remainingMashes: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMashItBinding.inflate(inflater, container, false)
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

    private fun getInitialMashes(): Int {
        return when (difficulty) {
            Difficulty.EASY -> (5..8).random()    // 5-8 mashes for easy
            Difficulty.MEDIUM -> (10..15).random() // 10-15 mashes for medium
            Difficulty.HARD -> (20..25).random()   // 20-25 mashes for hard
        }
    }

    override fun initializeView() {
        // Initialize the remaining mashes based on difficulty
        remainingMashes = getInitialMashes()

        // Update initial text
        updateMashesDisplay()

        // Set up button click listener
        binding.mashButton.setOnClickListener {
            onMashButtonClick()
        }
    }

    private fun updateMashesDisplay() {
        binding.remainingMashesText.text = remainingMashes.toString()
    }

    private fun onMashButtonClick() {
        remainingMashes--
        AudioManager.playPunch()
        updateMashesDisplay()

        if (remainingMashes <= 0) {
            onComplete()
        }
    }

    override fun startActivity() {
        // Make views visible if needed
        binding.remainingMashesText.visibility = View.VISIBLE
        binding.mashButton.visibility = View.VISIBLE
    }

    override fun stopActivity() {
        // Hide views if needed
        binding.remainingMashesText.visibility = View.GONE
        binding.mashButton.visibility = View.GONE
    }
}