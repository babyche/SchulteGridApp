package com.example.schulte.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.schulte.R

class SoundManager(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var correctId: Int = 0
    private var wrongId: Int = 0
    private var muted = false

    init {
        correctId = soundPool.load(context, R.raw.correct, 1)
        wrongId = soundPool.load(context, R.raw.wrong, 1)
    }

    fun playCorrect() = play(correctId)

    fun playWrong() = play(wrongId)

    fun setMuted(value: Boolean) {
        muted = value
    }

    fun isMuted(): Boolean = muted

    private fun play(soundId: Int) {
        if (muted || soundId == 0) return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}