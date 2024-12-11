package com.cs407.boppinit.activities

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cs407.boppinit.Difficulty
import com.cs407.boppinit.activities.standard.BopItActivityView
import com.cs407.boppinit.databinding.FragmentSpinItBinding

class SpinItActivityView(
    private val onComplete: () -> Unit,
    private val difficulty: Difficulty
) : Fragment(), BopItActivityView, SensorEventListener {
    private var _binding: FragmentSpinItBinding? = null
    private val binding get() = _binding!!
    private var sensorManager: SensorManager? = null
    private var gyroscope: Sensor? = null
    private var rotationSum = 0f
    private var requiredRotation: Float = 0f
    private var lastTimestamp: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpinItBinding.inflate(inflater, container, false)
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
        // Set up the SensorManager and gyroscope
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Register the gyroscope listener
        gyroscope?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // Set the required rotation based on difficulty
        requiredRotation = getRequiredRotation()
        updateRotationDisplay()
    }

    private fun getRequiredRotation(): Float {
        return when (difficulty) {
            Difficulty.EASY -> 360f  // One full spin
            Difficulty.MEDIUM -> 720f // Two full spins
            Difficulty.HARD -> 1080f // Three full spins
        }
    }

    private fun updateRotationDisplay() {
        binding.spinItText.text = "Spin progress: ${rotationSum.toInt()} / ${requiredRotation.toInt()} degrees"
    }

    override fun startActivity() {
        // Make views visible if needed
        rotationSum = 0f
        binding.spinItText.visibility = View.VISIBLE
        // Initial timestamp in nanoseconds
        lastTimestamp = System.nanoTime()
    }

    override fun stopActivity() {
        // Hide views if needed
        binding.spinItText.visibility = View.GONE
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                // Detect rotation around the Z-axis
                val zRotationRate = it.values[2] // Rotation rate around Z-axis in radians/second
                val deltaTime = (it.timestamp - lastTimestamp) / 1_000_000_000f // Convert nanoseconds to seconds

                // Update lastTimestamp for next calculation
                lastTimestamp = it.timestamp

                // Accumulate rotation in degrees
                val rotationDegrees = Math.abs(zRotationRate * deltaTime * (180f / Math.PI.toFloat()))
                rotationSum = rotationDegrees + rotationSum
                updateRotationDisplay()

                // Check if the required rotation is reached
                if (rotationSum >= requiredRotation) {
                    sensorManager?.unregisterListener(this)
                    onComplete()
                }
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}