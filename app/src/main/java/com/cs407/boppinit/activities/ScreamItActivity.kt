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

class ScreamItActivityView(
    private val onComplete: () -> Unit,
    private val difficulty: Difficulty
) : Fragment(), BopItActivityView {
    private var _binding: FragmentScreamItBinding? = null
    private val binding get() = _binding!!
    private var mediaRecorder: MediaRecorder? = null
    private val handler = Handler(Looper.getMainLooper())
    private val threshold = 5000 // Adjust this based on testing
    private var calibrationPeriod = 2000L // 2 seconds
    private var startTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        binding.btnComplete.setOnClickListener {
            onComplete()
        }
    }

    override fun startActivity() {
        startMicrophone()
        handler.postDelayed(checkVolumeRunnable, 100)
    }

    override fun stopActivity() {
        stopMicrophone()
        handler.removeCallbacks(checkVolumeRunnable)
    }

    private fun startMicrophone() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null")
                prepare()
                start()
            }
            startTime = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("ScreamItActivityView", "Error starting MediaRecorder", e)
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
                if (elapsedTime > calibrationPeriod) {
                    val maxAmplitude = it.maxAmplitude
                    binding.volumeProgressBar.progress = maxAmplitude
                    binding.thresholdText.text = "Current Amplitude: $maxAmplitude"
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