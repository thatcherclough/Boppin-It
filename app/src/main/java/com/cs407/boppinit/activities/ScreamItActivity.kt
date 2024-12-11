package com.cs407.boppinit.activities

import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cs407.boppinit.Difficulty
import com.cs407.boppinit.activities.standard.BopItActivityView
import com.cs407.boppinit.databinding.FragmentScreamItBinding
import java.io.File

class ScreamItActivityView(
    private val onComplete: () -> Unit,
    private val difficulty: Difficulty
) : Fragment(), BopItActivityView {
    private var _binding: FragmentScreamItBinding? = null
    private val binding get() = _binding!!
    private var mediaRecorder: MediaRecorder? = null
    private val handler = Handler(Looper.getMainLooper())
    private var threshold = 1000
    private var calibrationPeriod = 1000L // 2 seconds
    private var startTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        threshold = when (difficulty) {
            Difficulty.EASY -> 1000
            Difficulty.MEDIUM -> 5000
            Difficulty.HARD -> 10000
        }

        _binding = FragmentScreamItBinding.inflate(inflater, container, false)
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
        stopActivity()
    }

    override fun initializeView() {
        binding.thresholdText.text = "Threshold: $threshold"
    }

    override fun startActivity() {
        binding.volumeProgressBar.max = threshold
        startMicrophone()
        handler.postDelayed(checkVolumeRunnable, 100)
    }

    override fun stopActivity() {
        stopMicrophone()
        handler.removeCallbacks(checkVolumeRunnable)
    }

    private fun startMicrophone() {
        try {
            val outputFile = File(requireContext().cacheDir, "audio_test.mp3")

            mediaRecorder = MediaRecorder(requireContext()).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1) // Mono recording
                setAudioSamplingRate(44100) // Standard sampling rate
                setAudioEncodingBitRate(128000) // 128kbps
                setOutputFile(outputFile.absolutePath)

                try {
                    prepare()
                    start()
                    Log.d("ScreamItActivityView", "Microphone started successfully")
                } catch (e: Exception) {
                    Log.e("ScreamItActivityView", "Error in prepare/start", e)
                    release()
                    mediaRecorder = null
                }
            }
            startTime = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("ScreamItActivityView", "Error configuring MediaRecorder", e)
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    private fun stopMicrophone() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("ScreamItActivityView", "Error stopping MediaRecorder", e)
        } finally {
            mediaRecorder = null
        }
    }

    private val checkVolumeRunnable = object : Runnable {
        override fun run() {
            mediaRecorder?.let {
                val elapsedTime = System.currentTimeMillis() - startTime
                val maxAmplitude = it.maxAmplitude
                binding.volumeProgressBar.progress = maxAmplitude
                binding.thresholdText.text = "Current Amplitude: $maxAmplitude"
                if (elapsedTime > calibrationPeriod) {
                    if (maxAmplitude > threshold) {
                        onComplete()
                        return
                    }
                }
                handler.postDelayed(this, 100)
            }
        }
    }
}