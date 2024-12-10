package com.cs407.boppinit

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.cs407.boppinit.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Load the current sound setting
        val isSoundEnabled = sharedPreferences.getBoolean("sound_enabled", true)
        binding.switchSound.isChecked = isSoundEnabled

        // Load the current volume setting
        val volume = sharedPreferences.getInt("volume", 50)
        binding.seekBarVolume.progress = volume

        // Set a listener to save the sound setting
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("sound_enabled", isChecked).apply()
        }

        // Set a listener to save the volume setting
        binding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sharedPreferences.edit().putInt("volume", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }
        })
    }
}