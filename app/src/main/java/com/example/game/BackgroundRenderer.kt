package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin

object BackgroundRenderer {

    fun drawParallaxBackground(
        scope: DrawScope,
        theme: BackgroundTheme,
        weather: WeatherType,
        timeOfDay: TimeOfDay,
        scrollY: Float,
        width: Float,
        height: Float,
        time: Float
    ) {
        // Base Sky Color derived from Time of Day
        val baseColors = when (timeOfDay) {
            TimeOfDay.DAWN -> listOf(Color(0xFF2C1B2E), Color(0xFF5D2E46), Color(0xFF1F1124))
            TimeOfDay.MIDDAY -> when (theme) {
                BackgroundTheme.OCEAN_ARCHIPELAGO -> listOf(Color(0xFF031926), Color(0xFF0D324D), Color(0xFF001529))
                BackgroundTheme.STORM_CYCLONE -> listOf(Color(0xFF141923), Color(0xFF263238), Color(0xFF10141D))
                BackgroundTheme.DESERT_CANYON -> listOf(Color(0xFF2E1C14), Color(0xFF4E2C1D), Color(0xFF1F100B))
                BackgroundTheme.STRATOSPHERE_CYBER -> listOf(Color(0xFF05060F), Color(0xFF110E2E), Color(0xFF040308))
            }
            TimeOfDay.DUSK_SUNSET -> listOf(Color(0xFF3E1F17), Color(0xFF7C2D12), Color(0xFF1A0A06))
            TimeOfDay.MIDNIGHT -> listOf(Color(0xFF02040A), Color(0xFF080D1A), Color(0xFF010205))
        }

        scope.drawRect(brush = Brush.verticalGradient(baseColors), size = Size(width, height))

        // Parallax Terrain
        when (theme) {
            BackgroundTheme.OCEAN_ARCHIPELAGO -> drawOceanTerrain(scope, scrollY, width, height, time)
            BackgroundTheme.STORM_CYCLONE -> drawStormClouds(scope, scrollY, width, height, time)
            BackgroundTheme.DESERT_CANYON -> drawDesertBadlands(scope, scrollY, width, height, time)
            BackgroundTheme.STRATOSPHERE_CYBER -> drawCyberGrid(scope, scrollY, width, height, time)
        }

        // Environmental Weather Layers
        when (weather) {
            WeatherType.CLEAR_SKIES -> {
                // Subtle high-altitude speed streaks
                val vaporOffset = (scrollY * 2.2f) % height
                for (i in 0..6) {
                    val vx = ((i * 149) % width.toInt()).toFloat()
                    val vy = (vaporOffset + i * 280) % height
                    scope.drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(vx, vy),
                        end = Offset(vx, vy + 90f),
                        strokeWidth = 2.5f
                    )
                }
            }
            WeatherType.THUNDERSTORM -> {
                // Violent Rain + Lightning bolt flashes
                val rainScroll = (scrollY * 3.5f) % height
                for (r in 0..25) {
                    val rx = (r * 37 + 10) % width
                    val ry = (rainScroll + r * 65) % height
                    scope.drawLine(
                        color = Color(0x6680D8FF),
                        start = Offset(rx, ry),
                        end = Offset(rx - 8f, ry + 35f),
                        strokeWidth = 2f
                    )
                }

                // Periodic lightning strike
                val isLightning = (sin(time * 8f) > 0.94)
                if (isLightning) {
                    scope.drawRect(color = Color(0x4480D8FF), size = Size(width, height))
                    // Jagged lightning path
                    val lx = (width * 0.3f + 100f * sin(time * 3f))
                    scope.drawLine(color = Color.White, start = Offset(lx, 0f), end = Offset(lx + 40f, height * 0.4f), strokeWidth = 3f)
                    scope.drawLine(color = Color.White, start = Offset(lx + 40f, height * 0.4f), end = Offset(lx - 20f, height * 0.7f), strokeWidth = 2f)
                }
            }
            WeatherType.SANDSTORM -> {
                // Swirling orange-red desert haze
                scope.drawRect(color = Color(0x28FF6D00), size = Size(width, height))
                val dustScroll = (scrollY * 2f) % height
                for (d in 0..30) {
                    val dx = (d * 53 + 20) % width + 20f * sin(time * 4f + d)
                    val dy = (dustScroll + d * 45) % height
                    scope.drawCircle(color = Color(0x33FFA726), radius = 3f + (d % 4), center = Offset(dx, dy))
                }
            }
            WeatherType.SOLAR_AURORA -> {
                // Undulating Neon Green and Violet Curtains
                for (a in 0..2) {
                    val waveY = (height * 0.2f + a * 120f)
                    val color = if (a % 2 == 0) Color(0x3300E676) else Color(0x337C4DFF)
                    val waveOffset = time * 2f + a
                    for (x in 0..(width.toInt()) step 20) {
                        val y = waveY + 30f * sin((x * 0.015f) + waveOffset)
                        scope.drawCircle(color = color, radius = 24f, center = Offset(x.toFloat(), y))
                    }
                }
            }
            WeatherType.NIGHT_RAID -> {
                // Dark skies + dual sweeping searchlights
                val beam1Angle = sin(time * 1.2f) * 0.6f
                val beam1Origin = Offset(width * 0.2f, height)
                val beam1Target = Offset(width * 0.2f + sin(beam1Angle) * height, 0f)
                scope.drawLine(color = Color(0x25FFFFFF), start = beam1Origin, end = beam1Target, strokeWidth = 40f)

                val beam2Angle = -sin(time * 0.9f) * 0.5f
                val beam2Origin = Offset(width * 0.8f, height)
                val beam2Target = Offset(width * 0.8f + sin(beam2Angle) * height, 0f)
                scope.drawLine(color = Color(0x25FFFFFF), start = beam2Origin, end = beam2Target, strokeWidth = 40f)
            }
        }
    }

    private fun drawOceanTerrain(scope: DrawScope, scrollY: Float, width: Float, height: Float, time: Float) {
        val islandScroll = (scrollY * 0.4f) % height
        val islandColors = listOf(Color(0xFF19323C), Color(0xFF1B3B36), Color(0xFF244439))

        for (i in 0..4) {
            val ix = ((i * 260 + 70) % (width - 150))
            val iy = (islandScroll + i * 400) % height - 100f
            val islandRadius = 45f + (i * 12f)
            scope.drawCircle(color = islandColors[i % islandColors.size], radius = islandRadius, center = Offset(ix, iy))
            scope.drawCircle(color = Color(0x3300E5FF), radius = islandRadius + 8f, center = Offset(ix, iy))
        }

        val waveScroll = (scrollY * 0.9f) % height
        for (w in 0..12) {
            val wx = (w * 110 + 35) % width
            val wy = (waveScroll + w * 170) % height
            val waveLen = 35f + 15f * sin(time * 2f + w)
            scope.drawLine(color = Color(0x2280D8FF), start = Offset(wx, wy), end = Offset(wx + waveLen, wy), strokeWidth = 2f)
        }
    }

    private fun drawStormClouds(scope: DrawScope, scrollY: Float, width: Float, height: Float, time: Float) {
        val cloudScroll = (scrollY * 0.7f) % height
        for (i in 0..6) {
            val cx = ((i * 210 + 40) % width)
            val cy = (cloudScroll + i * 320) % height
            val r = 70f + (i * 15f)
            scope.drawCircle(color = Color(0x1F37474F), radius = r, center = Offset(cx, cy))
            scope.drawCircle(color = Color(0x14455A64), radius = r * 0.7f, center = Offset(cx + 20f, cy - 10f))
        }
    }

    private fun drawDesertBadlands(scope: DrawScope, scrollY: Float, width: Float, height: Float, time: Float) {
        val canyonScroll = (scrollY * 0.5f) % height
        for (i in 0..5) {
            val rx = (i * 230 + 50) % width
            val ry = (canyonScroll + i * 360) % height
            scope.drawOval(color = Color(0xFF3E2319), topLeft = Offset(rx - 60f, ry - 30f), size = Size(120f, 60f))
            scope.drawOval(color = Color(0xFF5D3422), topLeft = Offset(rx - 40f, ry - 20f), size = Size(80f, 40f))
        }
    }

    private fun drawCyberGrid(scope: DrawScope, scrollY: Float, width: Float, height: Float, time: Float) {
        val gridScroll = (scrollY * 1.2f) % 60f
        var gx = 0f
        while (gx < width) {
            scope.drawLine(color = Color(0x1A00E5FF), start = Offset(gx, 0f), end = Offset(gx, height), strokeWidth = 1f)
            gx += 60f
        }
        var gy = -60f + gridScroll
        while (gy < height + 60f) {
            scope.drawLine(color = Color(0x1A7C4DFF), start = Offset(0f, gy), end = Offset(width, gy), strokeWidth = 1f)
            gy += 60f
        }
    }
}
