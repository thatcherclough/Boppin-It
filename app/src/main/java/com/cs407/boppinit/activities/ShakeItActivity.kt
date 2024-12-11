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
import com.cs407.boppinit.databinding.FragmentShakeItBinding
import kotlin.math.sqrt

class ShakeItActivityView(
    private val onComplete: () -> Unit,
    private val difficulty: Difficulty
) : Fragment(), BopItActivityView, SensorEventListener {
    private var _binding: FragmentShakeItBinding? = null
    private val binding get() = _binding!!
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var shakeCount = 0
    private var shakeThreshold = 15f // Acceleration threshold for detecting shakes
    private var requiredShakes = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShakeItBinding.inflate(inflater, container, false)
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
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        requiredShakes = getRequiredShakes()
        updateShakeDisplay()
    }

    override fun startActivity() {
        shakeCount = 0 // Reset shake count
        binding.shakeItText.visibility = View.VISIBLE
    }

    override fun stopActivity() {
        binding.shakeItText.visibility = View.GONE
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val accelerationMagnitude = calculateAccelerationMagnitude(it.values)

                if (accelerationMagnitude > shakeThreshold) {
                    shakeCount++
                    updateShakeDisplay()

                    if (shakeCount >= requiredShakes) {
                        sensorManager?.unregisterListener(this)
                        onComplete()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun calculateAccelerationMagnitude(values: FloatArray): Float {
        // Compute the magnitude of the acceleration vector
        return sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
    }

    private fun getRequiredShakes(): Int {
        // Define required shakes based on difficulty
        return when (difficulty) {
            Difficulty.EASY -> 5
            Difficulty.MEDIUM -> 10
            Difficulty.HARD -> 15
        }
    }

    private fun updateShakeDisplay() {
        // Update the text showing the shake progress
        binding.shakeItText.text = "Shakes: $shakeCount / $requiredShakes"
    }
}