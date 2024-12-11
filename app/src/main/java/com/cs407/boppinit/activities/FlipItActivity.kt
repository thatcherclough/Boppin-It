package com.cs407.boppinit.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.cs407.boppinit.Difficulty
import com.cs407.boppinit.activities.standard.BopItActivityView
import com.cs407.boppinit.databinding.FragmentFlipItBinding

class FlipItActivityView(
    private val onComplete: () -> Unit,
    private val difficulty: Difficulty
) : Fragment(), BopItActivityView, SensorEventListener {
    private var _binding: FragmentFlipItBinding? = null
    private val binding get() = _binding!!
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var isFlipped = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlipItBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        startActivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager?.unregisterListener(this)
        _binding = null
    }

    override fun initializeView() {
        // Set up the SensorManager and accelerometer
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Set initial message
        binding.flipInstructionText.text = "Flip the phone over!"
    }

    override fun startActivity() {
        // Make views visible if needed
        binding.flipInstructionText.visibility = View.VISIBLE
    }

    override fun stopActivity() {
        // Hide views if needed
        binding.flipInstructionText.visibility = View.GONE
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val zAxis = it.values[2] // Z-axis accelerometer value
            if (!isFlipped && zAxis < -9.0) { // Detect if the phone is upside down
                isFlipped = true
                onComplete()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}