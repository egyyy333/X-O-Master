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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FantasyAnimatedBackground(
    theme: String,
    modifier: Modifier = Modifier
) {
    // Simple transition of time variable to run animations under a single ticker safely
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundTick")
    val animState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )

    val backgroundBrush = when (theme) {
        "LIGHT" -> {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFFF1F5F9))
            )
        }
        "DARK" -> {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
            )
        }
        else -> { // "COSMIC_FANTASY"
            Brush.verticalGradient(
                colors = listOf(Color(0xFF090A15), Color(0xFF020205))
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        if (theme == "COSMIC_FANTASY") {
            // Cosmic Fantasy - Pulsating glowing Nebulae and drifting magic stars
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Pulsating purple nebula
                val n1X = w * 0.25f + cos(animState.toDouble()).toFloat() * 45f
                val n1Y = h * 0.35f + sin(animState.toDouble()).toFloat() * 60f
                val n1Radius = w * 0.6f + sin(animState.toDouble()).toFloat() * 20f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(n1X, n1Y),
                        radius = n1Radius
                    ),
                    center = Offset(n1X, n1Y),
                    radius = n1Radius
                )

                // Pulsating pink/red nebula
                val n2X = w * 0.75f - cos(animState.toDouble() * 1.1).toFloat() * 55f
                val n2Y = h * 0.65f + sin(animState.toDouble() * 0.8).toFloat() * 65f
                val n2Radius = w * 0.5f + cos(animState.toDouble() * 0.9).toFloat() * 15f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEC4899).copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(n2X, n2Y),
                        radius = n2Radius
                    ),
                    center = Offset(n2X, n2Y),
                    radius = n2Radius
                )

                // Pulsating cyan nebula
                val n3X = w * 0.45f + sin(animState.toDouble() * 0.6).toFloat() * 40f
                val n3Y = h * 0.5f - cos(animState.toDouble() * 1.3).toFloat() * 50f
                val n3Radius = w * 0.4f + sin(animState.toDouble() * 1.1).toFloat() * 15f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.1f), Color.Transparent),
                        center = Offset(n3X, n3Y),
                        radius = n3Radius
                    ),
                    center = Offset(n3X, n3Y),
                    radius = n3Radius
                )

                // Drifting starlight particles (efficient, small amount for low battery impact)
                val particlesCount = 20
                for (i in 0 until particlesCount) {
                    val seed = i * 137.5
                    val baseX = (seed % 1.0) * w
                    val baseY = ((seed * 1.7) % 1.0) * h
                    
                    // Modulate coordinates
                    val oscX = sin(animState.toDouble() * 0.5 + i).toFloat() * 30f
                    val oscY = cos(animState.toDouble() * 0.7 + i * 2.2).toFloat() * 40f
                    val x = (baseX + oscX).toFloat()
                    val y = (baseY + oscY).toFloat()

                    val particleSize = 3f + (i % 3) * 2f
                    val alpha = 0.35f + sin(animState.toDouble() * 1.4 + i).toFloat() * 0.2f

                    // Outer soft bloom
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = alpha * 0.25f),
                        radius = particleSize * 2.4f,
                        center = Offset(x, y)
                    )
                    // Bright star point
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = particleSize,
                        center = Offset(x, y)
                    )
                }
            }
        } else if (theme == "DARK") {
            // Sleek digital grid of neon cyber points
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val columns = 12
                val rows = 24
                val cellW = w / columns
                val cellH = h / rows
                for (i in 0..columns) {
                    for (j in 0..rows) {
                        drawCircle(
                            color = Color(0xFF1E293B).copy(alpha = 0.25f),
                            radius = 2f,
                            center = Offset(i * cellW, j * cellH)
                        )
                    }
                }
            }
        } else {
            // Modern Light - Clean dot grid pattern
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val columns = 10
                val rows = 20
                val cellW = w / columns
                val cellH = h / rows
                for (i in 0..columns) {
                    for (j in 0..rows) {
                        drawCircle(
                            color = Color(0xFF94A3B8).copy(alpha = 0.25f),
                            radius = 2f,
                            center = Offset(i * cellW, j * cellH)
                        )
                    }
                }
            }
        }
    }
}
