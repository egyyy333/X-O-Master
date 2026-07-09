package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FantasyAnimatedBackground(
    theme: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundTick")
    val animState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )

    val backgroundBrush = when (theme) {
        "BG_NEON_PORTAL" -> {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF03001e), Color(0xFF7303c0), Color(0xFFec38bc), Color(0xFF03001e))
            )
        }
        "BG_GALACTIC_NEBULA" -> {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0B192C), Color(0xFF1E3E62), Color(0xFF000000))
            )
        }
        "BG_ABYSSAL_WAVE" -> {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF001219), Color(0xFF005F73), Color(0xFF0A9396), Color(0xFF001219))
            )
        }
        else -> { // "DEFAULT" or "COSMIC_FANTASY"
            Brush.verticalGradient(
                colors = listOf(Color(0xFF090A15), Color(0xFF160E36), Color(0xFF020205))
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (theme) {
                "BG_NEON_PORTAL" -> {
                    // Rotating cyber magic portal at screen center
                    val centerX = w * 0.5f
                    val centerY = h * 0.5f
                    val baseRadius = w * 0.35f + sin(animState.toDouble() * 2).toFloat() * 10f

                    // Draw outer glowing halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00F5D4).copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(centerX, centerY),
                            radius = baseRadius * 1.8f
                        ),
                        center = Offset(centerX, centerY),
                        radius = baseRadius * 1.8f
                    )

                    // Draw outer rotating portal ring
                    drawCircle(
                        color = Color(0xFF7B2CBF),
                        radius = baseRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 4f)
                    )

                    // Draw spinning particle stars on the portal rim
                    val count = 12
                    for (i in 0 until count) {
                        val angle = animState.toDouble() + (i * (2 * Math.PI / count))
                        val pX = centerX + cos(angle).toFloat() * baseRadius
                        val pY = centerY + sin(angle).toFloat() * baseRadius
                        
                        drawCircle(
                            color = Color(0xFF00F5D4),
                            radius = 6f + (i % 3) * 3f,
                            center = Offset(pX, pY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2f + (i % 3) * 1f,
                            center = Offset(pX, pY)
                        )
                    }
                }

                "BG_GALACTIC_NEBULA" -> {
                    // Nebulae clouds
                    val nX = w * 0.5f + cos(animState.toDouble() * 0.5).toFloat() * 50f
                    val nY = h * 0.4f + sin(animState.toDouble() * 0.5).toFloat() * 50f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFB703).copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(nX, nY),
                            radius = w * 0.7f
                        ),
                        center = Offset(nX, nY),
                        radius = w * 0.7f
                    )

                    // Drifting stars
                    val starCount = 25
                    for (i in 0 until starCount) {
                        val seed = i * 219.7
                        val baseX = (seed % 1.0) * w
                        val baseY = ((seed * 1.9) % 1.0) * h

                        // Wave animation for drift
                        val dx = sin(animState.toDouble() * 0.3 + i).toFloat() * 25f
                        val dy = cos(animState.toDouble() * 0.3 + i * 1.5).toFloat() * 30f
                        val sx = (baseX + dx).toFloat()
                        val sy = (baseY + dy).toFloat()

                        val brightness = 0.3f + sin(animState.toDouble() * 1.5 + i).toFloat() * 0.25f
                        drawCircle(
                            color = Color(0xFFFFD166).copy(alpha = brightness),
                            radius = 4f + (i % 2) * 2f,
                            center = Offset(sx, sy)
                        )
                        if (i % 5 == 0) {
                            // Cross flare
                            val len = 12f * brightness
                            drawLine(
                                color = Color.White.copy(alpha = brightness),
                                start = Offset(sx - len, sy),
                                end = Offset(sx + len, sy),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = brightness),
                                start = Offset(sx, sy - len),
                                end = Offset(sx, sy + len),
                                strokeWidth = 2f
                            )
                        }
                    }
                }

                "BG_ABYSSAL_WAVE" -> {
                    // Deep wave path drawings
                    val wavePath1 = Path()
                    val wavePath2 = Path()

                    wavePath1.moveTo(0f, h * 0.7f)
                    wavePath2.moveTo(0f, h * 0.75f)

                    for (x in 0..w.toInt() step 20) {
                        val angle = animState.toDouble() + (x * 0.01)
                        val y1 = h * 0.7f + sin(angle).toFloat() * 30f
                        val y2 = h * 0.75f + cos(angle + 1.0).toFloat() * 25f
                        wavePath1.lineTo(x.toFloat(), y1)
                        wavePath2.lineTo(x.toFloat(), y2)
                    }

                    drawPath(
                        path = wavePath1,
                        color = Color(0xFF00B4D8).copy(alpha = 0.12f),
                        style = Stroke(width = 6f)
                    )
                    drawPath(
                        path = wavePath2,
                        color = Color(0xFF90E0EF).copy(alpha = 0.08f),
                        style = Stroke(width = 4f)
                    )

                    // Bubbles floating up
                    val bubbleCount = 15
                    for (i in 0 until bubbleCount) {
                        val seed = i * 411.3
                        val bx = ((seed % 1.0) * w).toFloat()
                        // Floating vertical offset based on animState
                        val progress = ((animState.toDouble() / (2 * Math.PI)) + (seed * 0.7)) % 1.0
                        val by = (h * (1.0 - progress)).toFloat()

                        val r = 6f + (i % 4) * 4f
                        drawCircle(
                            color = Color(0xFF48CAE4).copy(alpha = 0.2f),
                            radius = r,
                            center = Offset(bx, by),
                            style = Stroke(width = 2f)
                        )
                    }
                }

                else -> { // DEFAULT
                    // Glowing magical pink and purple nebulae
                    val n1X = w * 0.3f + cos(animState.toDouble() * 0.7).toFloat() * 60f
                    val n1Y = h * 0.4f + sin(animState.toDouble() * 0.7).toFloat() * 70f
                    val n1Radius = w * 0.65f + sin(animState.toDouble()).toFloat() * 30f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(n1X, n1Y),
                            radius = n1Radius
                        ),
                        center = Offset(n1X, n1Y),
                        radius = n1Radius
                    )

                    val n2X = w * 0.7f - cos(animState.toDouble() * 0.8).toFloat() * 50f
                    val n2Y = h * 0.6f + sin(animState.toDouble() * 0.8).toFloat() * 50f
                    val n2Radius = w * 0.55f + cos(animState.toDouble() * 0.9).toFloat() * 20f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFD946EF).copy(alpha = 0.14f), Color.Transparent),
                            center = Offset(n2X, n2Y),
                            radius = n2Radius
                        ),
                        center = Offset(n2X, n2Y),
                        radius = n2Radius
                    )

                    // Beautiful particle stardust stars
                    val particlesCount = 24
                    for (i in 0 until particlesCount) {
                        val seed = i * 144.3
                        val baseX = (seed % 1.0) * w
                        val baseY = ((seed * 1.8) % 1.0) * h

                        val oscX = sin(animState.toDouble() * 0.6 + i).toFloat() * 35f
                        val oscY = cos(animState.toDouble() * 0.8 + i * 2.1).toFloat() * 45f
                        val x = (baseX + oscX).toFloat()
                        val y = (baseY + oscY).toFloat()

                        val pSize = 4f + (i % 3) * 2f
                        val alpha = 0.3f + sin(animState.toDouble() * 1.6 + i).toFloat() * 0.25f

                        // Outer glowing bloom
                        drawCircle(
                            color = Color(0xFFA55EEA).copy(alpha = alpha * 0.3f),
                            radius = pSize * 2.5f,
                            center = Offset(x, y)
                        )
                        // Shiny core
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            radius = pSize,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }
}
