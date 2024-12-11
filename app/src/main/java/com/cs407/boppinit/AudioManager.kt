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
    private var vocalSuccessSoundId: Int = 0
    private var vocalGameOverSoundId: Int = 0
    private var vocalEliminatedSoundId: Int = 0
    private var vocalPassItSoundId: Int = 0
    private var vocalGoodJobSoundId: Int = 0

    private var isInitialized = false

    fun startAudioManager(context: Context) {
        if (isInitialized) {
            return
        }
        initialize(context)
        isInitialized = true
        playMusic()
    }

    fun initialize(context: Context) {
        soundPool = SoundPool.Builder().setMaxStreams(10).build()
        mediaPlayer = MediaPlayer()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Load sound effects
        alarmClockSoundId = soundPool.load(context, R.raw.alarmclock_fiveseconds, 1)
        incorrectSoundId = soundPool.load(context, R.raw.incorrect, 1)
        successSoundId = soundPool.load(context, R.raw.success, 1)
        vocalSuccessSoundId = soundPool.load(context, R.raw.correct_vocal, 1)
        vocalGameOverSoundId = soundPool.load(context, R.raw.game_over_vocal, 1)
        vocalEliminatedSoundId = soundPool.load(context, R.raw.eliminated_vocal, 1)
        vocalPassItSoundId = soundPool.load(context, R.raw.pass_vocal, 1)
        vocalGoodJobSoundId = soundPool.load(context, R.raw.good_job_vocal, 1)


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

    fun playVocalSuccessSound() {
        playSoundEffect(vocalSuccessSoundId)
    }

    fun playVocalGameOverSound() {
        playSoundEffect(vocalGameOverSoundId)
    }

    fun playVocalEliminatedSound() {
        playSoundEffect(vocalEliminatedSoundId)
    }

    fun playVocalPassItSound() {
        playSoundEffect(vocalPassItSoundId)
    }

    fun playVocalGoodJobSound() {
        playSoundEffect(vocalGoodJobSoundId)
    }

    private fun playSoundEffect(soundId: Int) {
        if (sharedPreferences.getBoolean("sound_enabled", true)) {
            val volume = sharedPreferences.getInt("volume", 50) / 100f
            soundPool.play(soundId, volume, volume, 1, 0, 1f)
        }
    }

    fun playMusic() {
        if (mediaPlayer.isPlaying) {
            return
        }
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