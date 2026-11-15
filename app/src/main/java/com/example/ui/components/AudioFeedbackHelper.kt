package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import android.view.HapticFeedbackConstants
import android.view.View

object AudioFeedbackHelper {

    fun triggerSuccess(view: View?, soundEnabled: Boolean, reducedMotion: Boolean) {
        if (!reducedMotion) {
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_ACK, 90)
        }
    }

    fun triggerClick(view: View?, soundEnabled: Boolean, reducedMotion: Boolean) {
        if (!reducedMotion) {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_BEEP, 35)
        }
    }

    fun triggerCelebration(view: View?, soundEnabled: Boolean, reducedMotion: Boolean) {
        if (!reducedMotion) {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_BEEP2, 160)
        }
    }

    private fun playTone(tone: Int, durationMs: Int) {
        runCatching {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 40)
            tg.startTone(tone, durationMs)
            tg.release()
        }
    }
}
