package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object AircraftGraphicsRenderer {

    fun drawPlayerAircraft(
        scope: DrawScope,
        aircraftId: String,
        x: Float,
        y: Float,
        tiltAngle: Float,
        shieldActive: Boolean,
        invulnerable: Boolean,
        time: Float,
        wingmenCount: Int,
        paintJob: String = "default",
        decal: String = "none"
    ) {
        if (invulnerable && ((time * 15).toInt() % 2 == 0)) {
            return
        }

        scope.withTransform({
            translate(x, y)
            rotate(tiltAngle)
        }) {
            when (aircraftId) {
                "warthog" -> drawA10Warthog(this, time, paintJob, decal)
                "crimson" -> drawSU57Crimson(this, time, paintJob, decal)
                "nebula" -> drawAuroraX(this, time, paintJob, decal)
                else -> drawF22Raptor(this, time, paintJob, decal)
            }

            // Protective Energy Shield Bubble
            if (shieldActive) {
                val shieldPulse = 1f + 0.05f * sin(time * 8f)
                val shieldRadius = 55f * shieldPulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0x3300E5FF), Color(0xAA2979FF)),
                        center = Offset.Zero,
                        radius = shieldRadius
                    ),
                    radius = shieldRadius
                )
                drawCircle(
                    color = PlasmaCyan,
                    radius = shieldRadius,
                    style = Stroke(width = 2.5f)
                )
                val sparkAngle = (time * 4f)
                val sparkX = shieldRadius * cos(sparkAngle)
                val sparkY = shieldRadius * sin(sparkAngle)
                drawCircle(color = Color.White, radius = 4f, center = Offset(sparkX, sparkY))
            }
        }

        // Tactical escort drones
        if (wingmenCount > 0) {
            val droneOffset1X = x - 60f + 5f * sin(time * 5f)
            val droneOffset1Y = y + 15f + 4f * cos(time * 5f)
            drawEscortDrone(scope, droneOffset1X, droneOffset1Y, time)

            if (wingmenCount >= 2) {
                val droneOffset2X = x + 60f + 5f * sin(time * 5f + PI.toFloat())
                val droneOffset2Y = y + 15f + 4f * cos(time * 5f + PI.toFloat())
                drawEscortDrone(scope, droneOffset2X, droneOffset2Y, time + 1f)
            }
        }
    }

    private fun getPaintColors(paintJob: String, defaultColors: List<Color>): List<Color> {
        return when (paintJob) {
            "stealth_carbon" -> listOf(Color(0xFF15191E), Color(0xFF0B0E12), Color(0xFF1E252D))
            "desert_viper" -> listOf(Color(0xFF8D6E63), Color(0xFF5D4037), Color(0xFFA1887F))
            "crimson_inferno" -> listOf(Color(0xFFD50000), Color(0xFF5C0000), Color(0xFFFF5252))
            "arctic_ghost" -> listOf(Color(0xFFECEFF1), Color(0xFF90A4AE), Color(0xFFCFD8DC))
            "golden_ace" -> listOf(Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFFFFF8DC))
            else -> defaultColors
        }
    }

    private fun getTrimColor(paintJob: String, defaultTrim: Color): Color {
        return when (paintJob) {
            "stealth_carbon" -> PlasmaCyan
            "desert_viper" -> ArmorYellow
            "crimson_inferno" -> LaserOrange
            "arctic_ghost" -> Color(0xFF00E5FF)
            "golden_ace" -> Color(0xFFFFF9C4)
            else -> defaultTrim
        }
    }

    private fun drawDecal(scope: DrawScope, decal: String, x: Float, y: Float) {
        when (decal) {
            "ace_star" -> {
                scope.drawCircle(color = ArmorYellow, radius = 5f, center = Offset(x, y))
                scope.drawCircle(color = Color.White, radius = 2.5f, center = Offset(x, y))
            }
            "skull" -> {
                scope.drawCircle(color = Color.White, radius = 4f, center = Offset(x, y - 2f))
                scope.drawRect(color = Color.White, topLeft = Offset(x - 2f, y + 2f), size = Size(4f, 4f))
            }
            "eagle" -> {
                scope.drawLine(color = LaserOrange, start = Offset(x - 6f, y - 2f), end = Offset(x + 6f, y - 2f), strokeWidth = 2f)
                scope.drawLine(color = LaserOrange, start = Offset(x, y - 4f), end = Offset(x, y + 4f), strokeWidth = 2f)
            }
            "dragon" -> {
                scope.drawCircle(color = WarningRed, radius = 4.5f, center = Offset(x, y))
            }
        }
    }

    private fun drawF22Raptor(scope: DrawScope, time: Float, paintJob: String, decal: String) {
        val flameLength = 25f + 8f * sin(time * 30f)
        val flameColor = if (paintJob == "golden_ace") ArmorYellow else PlasmaCyan
        val flameBrush = Brush.verticalGradient(
            colors = listOf(flameColor, Color(0xFF00B0FF), Color.Transparent),
            startY = 35f,
            endY = 35f + flameLength
        )
        scope.drawOval(brush = flameBrush, topLeft = Offset(-14f, 35f), size = Size(10f, flameLength))
        scope.drawOval(brush = flameBrush, topLeft = Offset(4f, 35f), size = Size(10f, flameLength))

        val wingPath = Path().apply {
            moveTo(0f, -45f)
            lineTo(12f, -15f)
            lineTo(42f, 15f)
            lineTo(25f, 30f)
            lineTo(14f, 38f)
            lineTo(0f, 30f)
            lineTo(-14f, 38f)
            lineTo(-25f, 30f)
            lineTo(-42f, 15f)
            lineTo(-12f, -15f)
            close()
        }
        val defaultColors = listOf(Color(0xFF37474F), Color(0xFF1E272C), Color(0xFF263238))
        val bodyColors = getPaintColors(paintJob, defaultColors)
        val trimColor = getTrimColor(paintJob, Color(0xFF546E7A))

        scope.drawPath(path = wingPath, brush = Brush.linearGradient(bodyColors))
        scope.drawPath(path = wingPath, color = trimColor, style = Stroke(width = 1.8f))

        // Canopy
        val canopyPath = Path().apply {
            moveTo(0f, -30f)
            lineTo(6f, -10f)
            lineTo(0f, 5f)
            lineTo(-6f, -10f)
            close()
        }
        scope.drawPath(
            path = canopyPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF80D8FF), Color(0xFF0091EA), Color(0xFF01579B)),
                startY = -30f,
                endY = 5f
            )
        )
        scope.drawPath(path = canopyPath, color = Color.White, style = Stroke(width = 1f))

        // Missile pylons
        scope.drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(-38f, 10f), size = Size(4f, 18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
        scope.drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(34f, 10f), size = Size(4f, 18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))

        // Decals on wings
        if (decal != "none") {
            drawDecal(scope, decal, -26f, 15f)
            drawDecal(scope, decal, 26f, 15f)
        }
    }

    private fun drawA10Warthog(scope: DrawScope, time: Float, paintJob: String, decal: String) {
        val flameLength = 22f + 6f * sin(time * 28f)
        val flameBrush = Brush.verticalGradient(
            colors = listOf(LaserOrange, Color(0xFFFF9100), Color.Transparent),
            startY = 20f,
            endY = 20f + flameLength
        )
        scope.drawOval(brush = flameBrush, topLeft = Offset(-24f, 20f), size = Size(10f, flameLength))
        scope.drawOval(brush = flameBrush, topLeft = Offset(14f, 20f), size = Size(10f, flameLength))

        val wingPath = Path().apply {
            moveTo(0f, -48f)
            lineTo(10f, -25f)
            lineTo(46f, -8f)
            lineTo(46f, 16f)
            lineTo(16f, 20f)
            lineTo(18f, 38f)
            lineTo(0f, 32f)
            lineTo(-18f, 38f)
            lineTo(-16f, 20f)
            lineTo(-46f, 16f)
            lineTo(-46f, -8f)
            lineTo(-10f, -25f)
            close()
        }
        val defaultColors = listOf(Color(0xFF2E3D30), Color(0xFF1B241C), Color(0xFF38493B))
        val bodyColors = getPaintColors(paintJob, defaultColors)
        val trimColor = getTrimColor(paintJob, Color(0xFF66BB6A))

        scope.drawPath(path = wingPath, brush = Brush.verticalGradient(bodyColors))
        scope.drawPath(path = wingPath, color = trimColor, style = Stroke(width = 1.6f))

        // Nose Gatling Cannon
        val barrelY = -52f
        scope.drawRect(color = Color(0xFF1A1A1A), topLeft = Offset(-4f, barrelY), size = Size(8f, 12f))
        if (((time * 25).toInt() % 2 == 0)) {
            scope.drawCircle(color = LaserOrange, radius = 5f, center = Offset(0f, barrelY))
        }

        // Armored Cockpit
        scope.drawOval(
            brush = Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))),
            topLeft = Offset(-5f, -28f),
            size = Size(10f, 22f)
        )

        if (decal != "none") {
            drawDecal(scope, decal, -32f, 4f)
            drawDecal(scope, decal, 32f, 4f)
        }
    }

    private fun drawSU57Crimson(scope: DrawScope, time: Float, paintJob: String, decal: String) {
        val flameLength = 28f + 8f * sin(time * 35f)
        val flameBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFF1744), Color(0xFFFF5252), Color.Transparent),
            startY = 32f,
            endY = 32f + flameLength
        )
        scope.drawOval(brush = flameBrush, topLeft = Offset(-15f, 32f), size = Size(10f, flameLength))
        scope.drawOval(brush = flameBrush, topLeft = Offset(5f, 32f), size = Size(10f, flameLength))

        val hullPath = Path().apply {
            moveTo(0f, -46f)
            lineTo(12f, -20f)
            lineTo(44f, 18f)
            lineTo(22f, 34f)
            lineTo(12f, 30f)
            lineTo(0f, 38f)
            lineTo(-12f, 30f)
            lineTo(-22f, 34f)
            lineTo(-44f, 18f)
            lineTo(-12f, -20f)
            close()
        }
        val defaultColors = listOf(Color(0xFF212121), Color(0xFF880E4F), Color(0xFF1A1A1A))
        val bodyColors = getPaintColors(paintJob, defaultColors)
        val trimColor = getTrimColor(paintJob, Color(0xFFFF1744))

        scope.drawPath(path = hullPath, brush = Brush.linearGradient(bodyColors))
        scope.drawPath(path = hullPath, color = trimColor, style = Stroke(width = 2f))

        scope.drawOval(
            brush = Brush.verticalGradient(listOf(Color(0xFFFF8A80), Color(0xFFD50000))),
            topLeft = Offset(-6f, -24f),
            size = Size(12f, 24f)
        )

        if (decal != "none") {
            drawDecal(scope, decal, -25f, 10f)
            drawDecal(scope, decal, 25f, 10f)
        }
    }

    private fun drawAuroraX(scope: DrawScope, time: Float, paintJob: String, decal: String) {
        val flameLength = 32f + 10f * sin(time * 40f)
        val tachyonBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE040FB), Color(0xFF7C4DFF), Color.Transparent),
            startY = 30f,
            endY = 30f + flameLength
        )
        scope.drawOval(brush = tachyonBrush, topLeft = Offset(-18f, 30f), size = Size(36f, flameLength))

        val bodyPath = Path().apply {
            moveTo(0f, -50f)
            lineTo(14f, -15f)
            lineTo(45f, 24f)
            lineTo(28f, 22f)
            lineTo(18f, 35f)
            lineTo(0f, 28f)
            lineTo(-18f, 35f)
            lineTo(-28f, 22f)
            lineTo(-45f, 24f)
            lineTo(-14f, -15f)
            close()
        }
        val defaultColors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))
        val bodyColors = getPaintColors(paintJob, defaultColors)
        val trimColor = getTrimColor(paintJob, PlasmaCyan)

        scope.drawPath(path = bodyPath, brush = Brush.linearGradient(bodyColors))
        scope.drawPath(path = bodyPath, color = trimColor, style = Stroke(width = 2.2f))

        val coreRadius = 8f + 2f * sin(time * 10f)
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFF00E5FF), Color(0xFF651FFF)),
                center = Offset.Zero,
                radius = coreRadius * 2
            ),
            radius = coreRadius
        )

        if (decal != "none") {
            drawDecal(scope, decal, -28f, 12f)
            drawDecal(scope, decal, 28f, 12f)
        }
    }

    private fun drawEscortDrone(scope: DrawScope, x: Float, y: Float, time: Float) {
        scope.withTransform({
            translate(x, y)
        }) {
            val dronePath = Path().apply {
                moveTo(0f, -14f)
                lineTo(10f, 10f)
                lineTo(0f, 5f)
                lineTo(-10f, 10f)
                close()
            }
            drawPath(path = dronePath, color = Color(0xFF37474F))
            drawPath(path = dronePath, color = PlasmaCyan, style = Stroke(width = 1.5f))
            drawCircle(color = PlasmaCyan, radius = 3f, center = Offset(0f, 0f))
        }
    }

    // DRAW ESCORT ALLY CRAFT (VIP C-130 HERCULES)
    fun drawEscortTarget(scope: DrawScope, escort: EscortTarget, time: Float) {
        scope.withTransform({
            translate(escort.x, escort.y)
        }) {
            // Massive transport plane
            val hull = Path().apply {
                moveTo(0f, 45f)
                lineTo(16f, 25f)
                lineTo(80f, 5f)
                lineTo(80f, -15f)
                lineTo(18f, -10f)
                lineTo(22f, -42f)
                lineTo(0f, -35f)
                lineTo(-22f, -42f)
                lineTo(-18f, -10f)
                lineTo(-80f, -15f)
                lineTo(-80f, 5f)
                lineTo(-16f, 25f)
                close()
            }
            drawPath(path = hull, color = Color(0xFF2C3E50))
            drawPath(path = hull, color = RadarGreen, style = Stroke(width = 2f))

            // Propellers
            val propAngle = time * 30f
            val propLength = 14f
            for (px in listOf(-50f, -26f, 26f, 50f)) {
                drawLine(
                    color = Color.White,
                    start = Offset(px - propLength * cos(propAngle), 0f),
                    end = Offset(px + propLength * cos(propAngle), 0f),
                    strokeWidth = 2f
                )
            }

            // Green Ally Shield Bubble
            val shieldPulse = 1f + 0.05f * sin(time * 6f)
            drawCircle(color = Color(0x3300E676), radius = 90f * shieldPulse)
            drawCircle(color = RadarGreen, radius = 90f * shieldPulse, style = Stroke(width = 1.5f))

            // Health bar above
            val barW = 100f
            val barH = 7f
            val barY = -55f
            drawRect(color = Color.Black, topLeft = Offset(-barW / 2f, barY), size = Size(barW, barH))
            val hpPct = (escort.hp / escort.maxHp).coerceIn(0f, 1f)
            drawRect(color = RadarGreen, topLeft = Offset(-barW / 2f, barY), size = Size(barW * hpPct, barH))
        }
    }

    // DRAW RECON DATA POD
    fun drawReconPod(scope: DrawScope, pod: ReconDataPod, time: Float) {
        scope.withTransform({
            translate(pod.x, pod.y)
        }) {
            val pulse = 1f + 0.15f * sin(time * 10f)
            drawCircle(color = Color(0x4400E5FF), radius = 22f * pulse)
            drawCircle(color = PlasmaCyan, radius = 14f)
            drawCircle(color = Color.White, radius = 6f)
            drawCircle(color = ArmorYellow, radius = 22f * pulse, style = Stroke(width = 1.5f))
        }
    }

    // DRAW FLARE COUNTERMEASURES
    fun drawFlare(scope: DrawScope, flare: FlareCountermeasure, time: Float) {
        val alpha = (flare.life / flare.maxLife).coerceIn(0f, 1f)
        scope.drawCircle(
            color = LaserOrange.copy(alpha = alpha),
            radius = 12f * alpha,
            center = Offset(flare.x, flare.y)
        )
        scope.drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 5f * alpha,
            center = Offset(flare.x, flare.y)
        )
    }

    // DRAW ENEMIES
    fun drawEnemy(scope: DrawScope, enemy: Enemy, time: Float) {
        scope.withTransform({
            translate(enemy.x, enemy.y)
        }) {
            when (enemy.type) {
                EnemyType.DRONE_SCOUT -> drawScout(this)
                EnemyType.INTERCEPTOR -> drawInterceptor(this)
                EnemyType.GUNSHIP -> drawGunship(this, time)
                EnemyType.ELITE_ACE -> drawEliteAce(this)
                EnemyType.KAMIKAZE -> drawKamikaze(this, time)
                EnemyType.TORPEDO_BOMBER -> drawTorpedoBomber(this, time)
            }

            if (enemy.hp < enemy.maxHp) {
                val barW = enemy.width * 0.8f
                val barH = 5f
                val barY = -enemy.height / 2f - 10f
                drawRect(color = Color.Black, topLeft = Offset(-barW / 2f, barY), size = Size(barW, barH))
                val hpPct = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
                drawRect(
                    color = if (hpPct > 0.5f) RadarGreen else WarningRed,
                    topLeft = Offset(-barW / 2f, barY),
                    size = Size(barW * hpPct, barH)
                )
            }
        }
    }

    private fun drawScout(scope: DrawScope) {
        val path = Path().apply {
            moveTo(0f, 20f)
            lineTo(18f, -15f)
            lineTo(0f, -8f)
            lineTo(-18f, -15f)
            close()
        }
        scope.drawPath(path = path, color = Color(0xFF263238))
        scope.drawPath(path = path, color = WarningRed, style = Stroke(width = 1.5f))
        scope.drawCircle(color = WarningRed, radius = 3.5f, center = Offset(0f, 5f))
    }

    private fun drawInterceptor(scope: DrawScope) {
        val path = Path().apply {
            moveTo(0f, 28f)
            lineTo(22f, -5f)
            lineTo(28f, -22f)
            lineTo(8f, -16f)
            lineTo(0f, -20f)
            lineTo(-8f, -16f)
            lineTo(-28f, -22f)
            lineTo(-22f, -5f)
            close()
        }
        scope.drawPath(path = path, brush = Brush.verticalGradient(listOf(Color(0xFF3E2723), Color(0xFF1E100E))))
        scope.drawPath(path = path, color = LaserOrange, style = Stroke(width = 1.8f))
        scope.drawCircle(color = LaserOrange, radius = 4f, center = Offset(0f, 2f))
    }

    private fun drawGunship(scope: DrawScope, time: Float) {
        val path = Path().apply {
            moveTo(0f, 35f)
            lineTo(18f, 25f)
            lineTo(48f, 5f)
            lineTo(48f, -25f)
            lineTo(20f, -20f)
            lineTo(0f, -32f)
            lineTo(-20f, -20f)
            lineTo(-48f, -25f)
            lineTo(-48f, 5f)
            lineTo(-18f, 25f)
            close()
        }
        scope.drawPath(path = path, brush = Brush.verticalGradient(listOf(Color(0xFF263238), Color(0xFF37474F))))
        scope.drawPath(path = path, color = Color(0xFFCFD8DC), style = Stroke(width = 2f))

        scope.drawCircle(color = Color(0xFF455A64), radius = 8f, center = Offset(-22f, 0f))
        scope.drawCircle(color = WarningRed, radius = 3f, center = Offset(-22f, 0f))
        scope.drawCircle(color = Color(0xFF455A64), radius = 8f, center = Offset(22f, 0f))
        scope.drawCircle(color = WarningRed, radius = 3f, center = Offset(22f, 0f))
    }

    private fun drawEliteAce(scope: DrawScope) {
        val path = Path().apply {
            moveTo(0f, 32f)
            lineTo(26f, -10f)
            lineTo(15f, -26f)
            lineTo(0f, -18f)
            lineTo(-15f, -26f)
            lineTo(-26f, -10f)
            close()
        }
        scope.drawPath(path = path, brush = Brush.verticalGradient(listOf(Color(0xFFFFB300), Color(0xFF424242))))
        scope.drawPath(path = path, color = ArmorYellow, style = Stroke(width = 2f))
        scope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(0f, 6f))
    }

    private fun drawKamikaze(scope: DrawScope, time: Float) {
        val pulse = 1f + 0.15f * sin(time * 20f)
        val path = Path().apply {
            moveTo(0f, 24f * pulse)
            lineTo(15f, -18f)
            lineTo(0f, -10f)
            lineTo(-15f, -18f)
            close()
        }
        scope.drawPath(path = path, color = WarningRed)
        scope.drawPath(path = path, color = Color.White, style = Stroke(width = 2f))
    }

    private fun drawTorpedoBomber(scope: DrawScope, time: Float) {
        val path = Path().apply {
            moveTo(0f, 38f)
            lineTo(32f, 0f)
            lineTo(32f, -22f)
            lineTo(0f, -30f)
            lineTo(-32f, -22f)
            lineTo(-32f, 0f)
            close()
        }
        scope.drawPath(path = path, color = Color(0xFF1A237E))
        scope.drawPath(path = path, color = Color(0xFF00E5FF), style = Stroke(width = 2f))
        scope.drawOval(color = ArmorYellow, topLeft = Offset(-6f, 15f), size = Size(12f, 25f))
    }

    // DRAW MULTI-STAGE BOSS WITH WEAK POINTS
    fun drawBoss(scope: DrawScope, boss: Boss, time: Float) {
        scope.withTransform({
            translate(boss.x, boss.y)
        }) {
            val w = boss.width
            val h = boss.height
            val alpha = boss.cloakAlpha

            // Boss Engines
            val flameH = 30f + 10f * sin(time * 18f)
            val flameBrush = Brush.verticalGradient(
                listOf(WarningRed.copy(alpha = alpha), LaserOrange.copy(alpha = alpha), Color.Transparent),
                startY = -h / 2f,
                endY = -h / 2f - flameH
            )
            drawOval(brush = flameBrush, topLeft = Offset(-w * 0.35f, -h / 2f - flameH), size = Size(24f, flameH))
            drawOval(brush = flameBrush, topLeft = Offset(w * 0.35f - 24f, -h / 2f - flameH), size = Size(24f, flameH))

            // Main Boss Hull
            val bossHull = Path().apply {
                moveTo(0f, h * 0.45f)
                lineTo(w * 0.22f, h * 0.25f)
                lineTo(w * 0.48f, -h * 0.1f)
                lineTo(w * 0.45f, -h * 0.45f)
                lineTo(w * 0.15f, -h * 0.35f)
                lineTo(0f, -h * 0.48f)
                lineTo(-w * 0.15f, -h * 0.35f)
                lineTo(-w * 0.45f, -h * 0.45f)
                lineTo(-w * 0.48f, -h * 0.1f)
                lineTo(-w * 0.22f, h * 0.25f)
                close()
            }

            val hullColors = if (boss.isRageMode) {
                listOf(Color(0xFF5D101D).copy(alpha = alpha), Color(0xFF1E070B).copy(alpha = alpha), Color(0xFF881B2B).copy(alpha = alpha))
            } else {
                listOf(Color(0xFF263238).copy(alpha = alpha), Color(0xFF10171D).copy(alpha = alpha), Color(0xFF37474F).copy(alpha = alpha))
            }
            drawPath(path = bossHull, brush = Brush.verticalGradient(hullColors))
            drawPath(
                path = bossHull,
                color = (if (boss.isRageMode) WarningRed else ArmorYellow).copy(alpha = alpha),
                style = Stroke(width = 3.5f)
            )

            // INTERACTIVE WEAK POINTS
            boss.weakPoints.forEach { wp ->
                val wpCenter = Offset(wp.offsetX, wp.offsetY)
                if (wp.isDestroyed) {
                    // Destroyed smoking crater
                    drawCircle(color = Color.Black, radius = wp.radius, center = wpCenter)
                    drawCircle(color = Color(0xFF424242), radius = wp.radius, center = wpCenter, style = Stroke(width = 2f))
                    // Small smoking fire
                    drawCircle(color = LaserOrange, radius = wp.radius * 0.4f, center = wpCenter)
                } else if (wp.isExposed) {
                    // Active glowing weak point reticle
                    val pulse = 1f + 0.1f * sin(time * 12f)
                    val r = wp.radius * pulse

                    drawCircle(color = Color(0x44FF1744), radius = r, center = wpCenter)
                    drawCircle(color = WarningRed, radius = r, center = wpCenter, style = Stroke(width = 2f))
                    drawCircle(color = Color.White, radius = 4f, center = wpCenter)

                    // Crosshairs
                    drawLine(color = WarningRed, start = Offset(wpCenter.x - r - 4f, wpCenter.y), end = Offset(wpCenter.x + r + 4f, wpCenter.y), strokeWidth = 1.5f)
                    drawLine(color = WarningRed, start = Offset(wpCenter.x, wpCenter.y - r - 4f), end = Offset(wpCenter.x, wpCenter.y + r + 4f), strokeWidth = 1.5f)

                    // Weak point mini health arc
                    val hpRatio = (wp.hp / wp.maxHp).coerceIn(0f, 1f)
                    drawArc(
                        color = RadarGreen,
                        startAngle = -90f,
                        sweepAngle = 360f * hpRatio,
                        useCenter = false,
                        topLeft = Offset(wpCenter.x - r - 6f, wpCenter.y - r - 6f),
                        size = Size((r + 6f) * 2, (r + 6f) * 2),
                        style = Stroke(width = 2.5f)
                    )
                }
            }

            // Core Reactor (Glowing Central Eye)
            val corePulse = 16f + 4f * sin(time * 12f)
            val coreColor = if (boss.isRageMode) WarningRed else PlasmaCyan
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = alpha), coreColor.copy(alpha = alpha), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = corePulse * 2
                ),
                radius = corePulse
            )
        }

        // Charging Mega Beam Warning / Active Beam
        if (boss.chargingBeam) {
            val beamX = boss.x
            val startY = boss.y + boss.height * 0.45f
            if (boss.beamChargeProgress < 1.0f) {
                val pulseAlpha = (0.3f + 0.7f * sin(time * 40f)).coerceIn(0f, 1f)
                scope.drawLine(
                    color = WarningRed.copy(alpha = pulseAlpha),
                    start = Offset(beamX, startY),
                    end = Offset(beamX, 2200f),
                    strokeWidth = 3f
                )
            } else {
                val beamWidth = 70f + 15f * sin(time * 30f)
                val beamBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        WarningRed.copy(alpha = 0.6f),
                        Color.White,
                        WarningRed.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    startX = beamX - beamWidth / 2f,
                    endX = beamX + beamWidth / 2f
                )
                scope.drawRect(
                    brush = beamBrush,
                    topLeft = Offset(beamX - beamWidth / 2f, startY),
                    size = Size(beamWidth, 2200f)
                )
            }
        }
    }

    // DRAW BULLETS
    fun drawBullet(scope: DrawScope, bullet: Bullet) {
        when (bullet.type) {
            BulletType.PLASMA_BLUE -> {
                scope.drawLine(
                    color = PlasmaCyan,
                    start = Offset(bullet.x, bullet.y),
                    end = Offset(bullet.x, bullet.y + 18f),
                    strokeWidth = 4.5f,
                    cap = StrokeCap.Round
                )
                scope.drawCircle(color = Color.White, radius = 3f, center = Offset(bullet.x, bullet.y))
            }
            BulletType.HEAVY_VULCAN -> {
                scope.drawLine(
                    color = ArmorYellow,
                    start = Offset(bullet.x, bullet.y),
                    end = Offset(bullet.x, bullet.y + 24f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                scope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(bullet.x, bullet.y))
            }
            BulletType.HOMING_MISSILE -> {
                scope.drawOval(
                    color = Color.White,
                    topLeft = Offset(bullet.x - 4f, bullet.y - 10f),
                    size = Size(8f, 20f)
                )
                scope.drawCircle(color = LaserOrange, radius = 3f, center = Offset(bullet.x, bullet.y + 10f))
            }
            BulletType.SPREAD_RED -> {
                scope.drawCircle(color = Color(0xFFFF5252), radius = 5f, center = Offset(bullet.x, bullet.y))
                scope.drawCircle(color = Color.White, radius = 2.5f, center = Offset(bullet.x, bullet.y))
            }
            BulletType.ENEMY_PLASMA -> {
                scope.drawCircle(color = WarningRed, radius = 7f, center = Offset(bullet.x, bullet.y))
                scope.drawCircle(color = Color(0xFFFF8A80), radius = 4f, center = Offset(bullet.x, bullet.y))
            }
            BulletType.ENEMY_LASER_BALL -> {
                scope.drawCircle(color = LaserOrange, radius = 8f, center = Offset(bullet.x, bullet.y))
                scope.drawCircle(color = Color.White, radius = 4f, center = Offset(bullet.x, bullet.y))
            }
            BulletType.BOSS_ENERGY_RING -> {
                scope.drawCircle(
                    color = StealthPurple,
                    radius = 12f,
                    center = Offset(bullet.x, bullet.y),
                    style = Stroke(width = 3.5f)
                )
                scope.drawCircle(color = Color.White, radius = 5f, center = Offset(bullet.x, bullet.y))
            }
            BulletType.FLAK_BURST -> {
                scope.drawCircle(color = Color(0xFFFF9800), radius = 10f, center = Offset(bullet.x, bullet.y))
                scope.drawCircle(color = Color.White, radius = 4f, center = Offset(bullet.x, bullet.y))
            }
            BulletType.BOSS_MEGA_BEAM -> {}
        }
    }

    // DRAW POWERUPS
    fun drawPowerUp(scope: DrawScope, p: PowerUpDrop, time: Float) {
        val pulse = 1f + 0.1f * sin(time * 8f)
        val r = 18f * pulse
        scope.withTransform({
            translate(p.x, p.y)
        }) {
            val (bgColor, _) = when (p.type) {
                PowerUpType.WEAPON_UPGRADE -> Pair(LaserOrange, "P")
                PowerUpType.SHIELD_CHARGE -> Pair(ShieldBlue, "S")
                PowerUpType.HEALTH_REPAIR -> Pair(RadarGreen, "+")
                PowerUpType.WINGMEN_DRONE -> Pair(PlasmaCyan, "D")
                PowerUpType.MEGA_BOMB -> Pair(WarningRed, "B")
                PowerUpType.GOLD_COIN -> Pair(ArmorYellow, "$")
            }

            drawCircle(color = bgColor, radius = r)
            drawCircle(color = Color.White, radius = r, style = Stroke(width = 2.5f))
            drawCircle(color = Color.White, radius = r * 0.45f)
        }
    }
}
