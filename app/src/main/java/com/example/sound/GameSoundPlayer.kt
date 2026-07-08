package com.example.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.exp

object GameSoundPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Synthesizes and plays a rich electronic synthesizer tone with harmonics and a pluck envelope.
     *
     * @param frequency base frequency in Hz
     * @param durationMs duration of the sound in milliseconds
     * @param type "PLUCK" (bell/synth), "WARM" (softer bubble), "SWEEP" (retro pitch bend down), "CHIME" (extra harmonics), "BOOP" (bend up)
     */
    fun playTone(frequency: Double, durationMs: Int, type: String = "PLUCK") {
        scope.launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples

                    // Calculate current frequency (useful for pitch sweeps)
                    val currentFreq = when (type) {
                        "SWEEP" -> frequency * (1.0 - progress * 0.4) // bend down 40%
                        "BOOP" -> frequency * (1.0 + progress * 0.25) // bend up 25%
                        else -> frequency
                    }

                    // Harmonics synthesis
                    val fundamental = sin(2.0 * Math.PI * t * currentFreq)
                    val secondHarmonic = sin(2.0 * Math.PI * t * currentFreq * 2.0)
                    val thirdHarmonic = sin(2.0 * Math.PI * t * currentFreq * 3.0)
                    val subHarmonic = sin(2.0 * Math.PI * t * currentFreq * 0.5)

                    val rawWave = when (type) {
                        "PLUCK" -> (fundamental + 0.5 * secondHarmonic + 0.2 * thirdHarmonic) / 1.7
                        "WARM" -> (fundamental + 0.3 * subHarmonic) / 1.3
                        "SWEEP" -> (fundamental + 0.4 * secondHarmonic + 0.3 * thirdHarmonic) / 1.7
                        "CHIME" -> (fundamental + 0.6 * secondHarmonic + 0.4 * thirdHarmonic + 0.25 * sin(2.0 * Math.PI * t * currentFreq * 4.0)) / 2.25
                        else -> fundamental
                    }

                    // Envelopes (Attack-Decay-Sustain-Release approximation)
                    val envelope = when (type) {
                        "PLUCK" -> {
                            // Fast attack, exponential decay
                            val attack = if (progress < 0.05) progress / 0.05 else 1.0
                            val decay = exp(-progress * 6.0)
                            attack * decay
                        }
                        "WARM" -> {
                            // Medium attack, linear decay
                            val attack = if (progress < 0.15) progress / 0.15 else 1.0
                            val decay = 1.0 - progress
                            attack * decay
                        }
                        "SWEEP" -> {
                            // Rapid attack, slow decay
                            val attack = if (progress < 0.02) progress / 0.02 else 1.0
                            val decay = exp(-progress * 4.0)
                            attack * decay
                        }
                        "CHIME" -> {
                            // Sweet crystal chime: longer decay, slight swell
                            val attack = if (progress < 0.1) progress / 0.1 else 1.0
                            val decay = exp(-progress * 3.0)
                            attack * decay
                        }
                        "BOOP" -> {
                            val attack = if (progress < 0.1) progress / 0.1 else 1.0
                            val decay = exp(-progress * 5.0)
                            attack * decay
                        }
                        else -> {
                            1.0 - progress // simple linear decay
                        }
                    }

                    buffer[i] = (rawWave * envelope * 32767 * 0.8).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                delay(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Standard UI click chime (retro micro arpeggio).
     */
    fun playClick() {
        scope.launch {
            playTone(880.0, 60, "PLUCK")
            delay(40)
            playTone(1320.0, 80, "CHIME")
        }
    }

    /**
     * Play when X (Player 1 / Blue) places a piece. Crystal high-energy bell.
     */
    fun playXSound() {
        scope.launch {
            playTone(987.77, 120, "CHIME") // B5
            delay(50)
            playTone(1318.51, 180, "PLUCK") // E6
        }
    }

    /**
     * Play when O (Player 2 / Red) places a piece. Round bubble/marimba pop.
     */
    fun playOSound() {
        scope.launch {
            playTone(440.00, 100, "WARM") // A4
            delay(50)
            playTone(523.25, 150, "WARM") // C5
        }
    }

    /**
     * Plays a gorgeous retro futuristic major arpeggio cascade for victory.
     */
    fun playWin() {
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98) // C5, E5, G5, C6, E6, G6
            for (note in notes) {
                playTone(note, 300, "CHIME")
                delay(90)
            }
        }
    }

    /**
     * Plays an arcade synth bend down for defeat.
     */
    fun playLose() {
        scope.launch {
            playTone(392.00, 150, "SWEEP") // G4
            delay(140)
            playTone(329.63, 150, "SWEEP") // E4
            delay(140)
            playTone(261.63, 350, "SWEEP") // C4
        }
    }

    /**
     * Plays a dreamy synth chord for a tie game.
     */
    fun playTie() {
        scope.launch {
            playTone(587.33, 200, "WARM") // D5
            playTone(739.99, 200, "WARM") // F#5
            delay(150)
            playTone(880.00, 250, "CHIME") // A5
        }
    }

    /**
     * Cute micro synth bleep bloop for bot computation.
     */
    fun playBotThinking() {
        scope.launch {
            playTone(660.0, 40, "BOOP")
            delay(50)
            playTone(880.0, 40, "BOOP")
        }
    }
}
