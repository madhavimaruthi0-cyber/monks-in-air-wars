package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.*
import com.example.ui.theme.*
import kotlinx.coroutines.isActive

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    onReturnToMenu: () -> Unit,
    onNavigateToHangar: () -> Unit
) {
    val engine = viewModel.activeGameEngine ?: return
    val profile by viewModel.profile.collectAsState()
    val fingerOffsetEnabled = profile?.fingerOffsetEnabled ?: true

    var isPaused by remember { mutableStateOf(false) }
    var frameTimeNanos by remember { mutableLongStateOf(0L) }

    BackHandler {
        if (engine.gameState == GameState.PLAYING || engine.gameState == GameState.BOSS_BATTLE) {
            isPaused = true
        } else {
            onReturnToMenu()
        }
    }

    LaunchedEffect(isPaused) {
        var lastTime = 0L
        while (isActive) {
            withFrameNanos { now ->
                if (lastTime == 0L) {
                    lastTime = now
                }
                val deltaSeconds = (now - lastTime) / 1_000_000_000f
                lastTime = now
                frameTimeNanos = now

                if (!isPaused && engine.gameState != GameState.VICTORY && engine.gameState != GameState.GAME_OVER) {
                    engine.update(deltaSeconds)
                }
            }
        }
    }

    LaunchedEffect(engine.gameState) {
        if (engine.gameState == GameState.VICTORY || engine.gameState == GameState.GAME_OVER) {
            viewModel.onGameFinished(engine)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Interactive 60FPS Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            engine.updateTouchTarget(offset.x, offset.y, fingerOffsetEnabled)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            engine.updateTouchTarget(change.position.x, change.position.y, fingerOffsetEnabled)
                        }
                    )
                }
        ) {
            engine.onScreenResize(size.width, size.height)

            val shakeX = if (engine.screenShakeAmount > 0f) {
                (Math.random().toFloat() * 2f - 1f) * engine.screenShakeAmount
            } else 0f
            val shakeY = if (engine.screenShakeAmount > 0f) {
                (Math.random().toFloat() * 2f - 1f) * engine.screenShakeAmount
            } else 0f

            // Dynamic Weather and Environmental Background
            BackgroundRenderer.drawParallaxBackground(
                scope = this,
                theme = engine.missionConfig.theme,
                weather = engine.missionConfig.weather,
                timeOfDay = engine.missionConfig.timeOfDay,
                scrollY = engine.scrollY,
                width = size.width,
                height = size.height,
                time = engine.gameTime
            )

            // Draw Escort Target (if Escort Mission)
            engine.escortTarget?.let { escort ->
                AircraftGraphicsRenderer.drawEscortTarget(this, escort, engine.gameTime)
            }

            // Draw Recon Intel Pods
            engine.reconPods.forEach { pod ->
                AircraftGraphicsRenderer.drawReconPod(this, pod, engine.gameTime)
            }

            // Draw Flare Countermeasures
            engine.flares.forEach { flare ->
                AircraftGraphicsRenderer.drawFlare(this, flare, engine.gameTime)
            }

            // Draw Powerups
            engine.powerUps.forEach { p ->
                AircraftGraphicsRenderer.drawPowerUp(this, p, engine.gameTime)
            }

            // Draw Bullets
            engine.bullets.forEach { b ->
                AircraftGraphicsRenderer.drawBullet(this, b)
            }

            // Draw Enemies
            engine.enemies.forEach { e ->
                AircraftGraphicsRenderer.drawEnemy(this, e, engine.gameTime)
            }

            // Draw Boss with Weak Points
            engine.boss?.let { b ->
                AircraftGraphicsRenderer.drawBoss(this, b, engine.gameTime)
            }

            // Draw Player Aircraft with Custom Paint Job & Decal
            AircraftGraphicsRenderer.drawPlayerAircraft(
                scope = this,
                aircraftId = engine.selectedAircraftId,
                x = engine.playerX + shakeX,
                y = engine.playerY + shakeY,
                tiltAngle = engine.playerTilt,
                shieldActive = engine.shieldActive,
                invulnerable = engine.invulnerableTimer > 0f,
                time = engine.gameTime,
                wingmenCount = engine.wingmenCount,
                paintJob = engine.paintJob,
                decal = engine.decal
            )

            // Draw Particles
            engine.particles.forEach { part ->
                val alpha = (part.life / part.maxLife).coerceIn(0f, 1f)
                when (part.type) {
                    ParticleType.SHOCKWAVE_RING -> {
                        drawCircle(
                            color = part.color.copy(alpha = alpha),
                            radius = part.size * (1f + (1f - alpha) * 4f),
                            center = Offset(part.x, part.y),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                        )
                    }
                    ParticleType.FIRE -> {
                        drawCircle(
                            color = part.color.copy(alpha = alpha),
                            radius = part.size * alpha,
                            center = Offset(part.x, part.y)
                        )
                    }
                    else -> {
                        drawCircle(
                            color = part.color.copy(alpha = alpha),
                            radius = part.size,
                            center = Offset(part.x, part.y)
                        )
                    }
                }
            }

            // Mega Bomb Screen Flash
            if (engine.bombFlashTimer > 0f) {
                val flashAlpha = (engine.bombFlashTimer / 0.6f).coerceIn(0f, 0.7f)
                drawRect(color = Color.White.copy(alpha = flashAlpha), size = size)
            }
        }

        // HUD Overlay Top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCORE: ${engine.score}",
                        color = TextLight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (engine.comboStreak > 1) {
                        Text(
                            text = "COMBO x${engine.comboStreak} 🔥",
                            color = LaserOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mission Type & Weather Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = AircraftSurface.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${engine.missionConfig.missionType.name} | ${engine.missionConfig.weather.name.replace("_", " ")}",
                            color = PlasmaCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Pause Button
                IconButton(
                    onClick = { isPaused = true },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("pause_button")
                        .clip(CircleShape)
                        .background(AircraftSurface.copy(alpha = 0.85f))
                ) {
                    Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = PlasmaCyan)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Player Status Bars (HP & Shield)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Hull HP
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "HULL", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${engine.currentHealth.toInt()}/${engine.maxHealth.toInt()}",
                            color = TextLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val hpRatio = (engine.currentHealth / engine.maxHealth).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { hpRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (hpRatio > 0.35f) RadarGreen else WarningRed,
                        trackColor = Color(0xFF263238),
                    )
                }

                // Shield
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "SHIELD", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${engine.currentShield.toInt()}/${engine.maxShield.toInt()}",
                            color = PlasmaCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val shieldRatio = (engine.currentShield / engine.maxShield).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { shieldRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PlasmaCyan,
                        trackColor = Color(0xFF263238),
                    )
                }
            }

            // MISSION OBJECTIVE TRACKER
            when (engine.missionConfig.missionType) {
                MissionType.ESCORT -> {
                    engine.escortTarget?.let { escort ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xCC111827))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "VIP ESCORT", color = RadarGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            val ratio = (escort.hp / escort.maxHp).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RadarGreen,
                                trackColor = Color(0xFF37474F)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${(ratio * 100).toInt()}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                MissionType.RECON -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC111827))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "RECON INTEL DISKS", color = PlasmaCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${engine.reconPodsCollected} / ${engine.targetReconPods} COLLECTED",
                            color = ArmorYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                MissionType.DEFENSE -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC111827))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ALLIED BASE", color = ShieldBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        val ratio = (engine.defenseBaseHp / engine.maxDefenseBaseHp).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ShieldBlue,
                            trackColor = Color(0xFF37474F)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${(ratio * 100).toInt()}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {}
            }

            // Boss Health Bar
            engine.boss?.let { b ->
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD111827))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "⚠ ${b.name.uppercase()} (STAGE ${b.stage})",
                            color = if (b.isRageMode) WarningRed else LaserOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${((b.hp / b.maxHp) * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    val bossHpRatio = (b.hp / b.maxHp).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { bossHpRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (b.isRageMode) WarningRed else LaserOrange,
                        trackColor = Color(0xFF37474F),
                    )
                }
            }
        }

        // Boss Warning Screen Banner
        if (engine.gameState == GameState.BOSS_WARNING) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .background(WarningRed.copy(alpha = 0.85f))
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠ WARNING: BOSS APPROACHING ⚠",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = engine.missionConfig.bossName.uppercase(),
                        color = ArmorYellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom Tactical Controls HUD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // FLARES DEFENSIVE COUNTERMEASURES BUTTON (LEFT)
            Button(
                onClick = { engine.deployFlares() },
                enabled = engine.flaresRemaining > 0,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .testTag("flares_button")
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaserOrange,
                    disabledContainerColor = Color(0xFF424242)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = "Flares", tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(text = "FLARES", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text(text = "(${engine.flaresRemaining}) DEPLOY", color = Color(0xFF212121), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // EMP MEGA BOMB BUTTON (RIGHT)
            Button(
                onClick = { engine.triggerMegaBomb() },
                enabled = engine.megaBombs > 0,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .testTag("mega_bomb_button")
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningRed,
                    disabledContainerColor = Color(0xFF424242)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.CrisisAlert, contentDescription = "EMP Bomb", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "EMP BOMB", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text(text = "(${engine.megaBombs}) READY", color = ArmorYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Pause Menu Dialog
        if (isPaused) {
            AlertDialog(
                onDismissRequest = { isPaused = false },
                containerColor = AircraftCardBg,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "TACTICAL PAUSE",
                        color = TextLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = engine.missionConfig.name, color = PlasmaCyan, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Sound Effects", color = TextLight)
                            Switch(checked = profile?.soundEnabled ?: true, onCheckedChange = { viewModel.toggleAudio() })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Haptic Feedback", color = TextLight)
                            Switch(checked = profile?.hapticsEnabled ?: true, onCheckedChange = { viewModel.toggleHaptics() })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Finger View Offset", color = TextLight)
                            Switch(checked = fingerOffsetEnabled, onCheckedChange = { viewModel.toggleFingerOffset() })
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PlasmaCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RESUME ENGAGEMENT", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            isPaused = false
                            onReturnToMenu()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ABORT MISSION", color = WarningRed)
                    }
                }
            )
        }

        // VICTORY SCREEN
        if (engine.gameState == GameState.VICTORY) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AircraftCardBg),
                    shape = RoundedCornerShape(24.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(RadarGreen, PlasmaCyan)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "MISSION VICTORY!", color = RadarGreen, fontSize = 26.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "AIRSPACE DOMINANCE SECURED", color = TextMuted, fontSize = 12.sp, letterSpacing = 2.sp)

                        Spacer(modifier = Modifier.height(18.dp))

                        val starsEarned = if (engine.currentHealth > engine.maxHealth * 0.7f) 3 else 2
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (s in 1..3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (s <= starsEarned) ArmorYellow else Color(0xFF424242),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AircraftSurface)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatRow("Final Score", "${engine.score} PTS", PlasmaCyan)
                            StatRow("Hostiles Neutralized", "${engine.totalEnemiesDefeated}", TextLight)
                            StatRow("War Credits Earned", "+${engine.coinsCollected}", ArmorYellow)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onReturnToMenu,
                            colors = ButtonDefaults.buttonColors(containerColor = PlasmaCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("continue_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("DEBRIEFING COMPLETE", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onNavigateToHangar,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("UPGRADE AIRCRAFT", color = TextLight)
                        }
                    }
                }
            }
        }

        // DEFEAT SCREEN
        if (engine.gameState == GameState.GAME_OVER) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AircraftCardBg),
                    shape = RoundedCornerShape(24.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(WarningRed, Color(0xFF37474F))))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "MISSION FAILED", color = WarningRed, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "TACTICAL LOSS SUSTAINED", color = TextMuted, fontSize = 12.sp, letterSpacing = 2.sp)

                        Spacer(modifier = Modifier.height(18.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AircraftSurface)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatRow("Score", "${engine.score}", TextLight)
                            StatRow("Hostiles Shot Down", "${engine.totalEnemiesDefeated}", TextLight)
                            StatRow("Salvaged Credits", "+${engine.coinsCollected}", ArmorYellow)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.restartCurrentGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = LaserOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("retry_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("SCRAMBLE AGAIN (RETRY)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onReturnToMenu,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("RETURN TO BASE", color = TextLight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
