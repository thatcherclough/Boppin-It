package com.cs407.boppinit

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import androidx.preference.PreferenceManager

object AudioManager {
    private lateinit var soundPool: SoundPool
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences
    private var alarmClockSoundId: Int = 0
    private var incorrectSoundId: Int = 0
    private var successSoundId: Int = 0

    fun initialize(context: Context) {
        soundPool = SoundPool.Builder().setMaxStreams(10).build()
        mediaPlayer = MediaPlayer()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Load sound effects
        alarmClockSoundId = soundPool.load(context, R.raw.alarmclock_fiveseconds, 1)
        incorrectSoundId = soundPool.load(context, R.raw.incorrect, 1)
        successSoundId = soundPool.load(context, R.raw.success, 1)

        // Load music
        val musicUri = Uri.parse("android.resource://${context.packageName}/${R.raw.gamemusic2}")
        mediaPlayer.setDataSource(context, musicUri)
        mediaPlayer.prepare()
        mediaPlayer.isLooping = true

        // Apply saved volume settings
        applyVolumeSettings()
    }

    fun playAlarmClockSound() {
        playSoundEffect(alarmClockSoundId)
    }

    fun playIncorrectSound() {
        playSoundEffect(incorrectSoundId)
    }

    fun playSuccessSound() {
        playSoundEffect(successSoundId)
    }

    private fun playSoundEffect(soundId: Int) {
        if (sharedPreferences.getBoolean("sound_enabled", true)) {
            val volume = sharedPreferences.getInt("volume", 50) / 100f
            soundPool.play(soundId, volume, volume, 1, 0, 1f)
        }
    }

    fun playMusic() {
        if (sharedPreferences.getBoolean("sound_enabled", true)) {
            mediaPlayer.start()
        }
    }

    fun stopMusic() {
        mediaPlayer.pause()
    }

    fun applyVolumeSettings() {
        val volume = sharedPreferences.getInt("volume", 50) / 100f
        mediaPlayer.setVolume(volume, volume)
    }
}