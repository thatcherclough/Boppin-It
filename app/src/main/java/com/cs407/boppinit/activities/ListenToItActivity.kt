package com.cs407.boppinit.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cs407.boppinit.Difficulty
import com.cs407.boppinit.R
import com.cs407.boppinit.activities.standard.BopItActivityView
import com.cs407.boppinit.databinding.FragmentListenToItBinding
import android.widget.Toast

class ListenToItActivityView(
    private val onComplete: () -> Unit,
    private val difficulty: Difficulty
) : Fragment(), BopItActivityView {
    private var _binding: FragmentListenToItBinding? = null
    private val binding get() = _binding!!
    private var mediaPlayer: MediaPlayer? = null
    private var selectedSound: Int? = null

    private val soundToButtonMap = mapOf(
        R.raw.chimpanzee_sound_effect to "Monkey",
        R.raw.cow_sound_effect to "Cow",
        R.raw.dog_sound_effect to "Dog"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListenToItBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        startNewRound()
        startActivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
    }

    override fun initializeView() {
        binding.playSoundButton.setOnClickListener {
            playSound()
        }

        binding.option1Button.setOnClickListener {
            checkAnswer("Monkey")
        }

        binding.option2Button.setOnClickListener {
            checkAnswer("Dog")
        }

        binding.option3Button.setOnClickListener {
            checkAnswer("Cow")
        }
    }

    override fun startActivity() {
        // Nothing needed - just waiting for button click
    }

    override fun stopActivity() {
        // Nothing needed to clean up
    }

    private fun startNewRound() {
        val sounds = soundToButtonMap.keys.toList()
        selectedSound = sounds.random()
        playSound()
    }

    private fun playSound() {
        if (selectedSound == null) {
            println("Error: No sound selected!")
            return
        }
        selectedSound?.let { sound ->
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(requireContext(), sound)
            mediaPlayer?.start()
        }
    }

    private fun handleIncorrectAnswer() {
        Toast.makeText(requireContext(), "Incorrect! Try again.", Toast.LENGTH_SHORT).show()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.incorrect)
        mediaPlayer?.start()
    }

    private fun checkAnswer(selectedAnimal: String) {
        selectedSound?.let { sound ->
            val correctButton = soundToButtonMap[sound]
            if (selectedAnimal == correctButton) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.success_listen_to_it)
                mediaPlayer?.start()
                onComplete()
            } else {
                handleIncorrectAnswer()
            }
        }
    }
}