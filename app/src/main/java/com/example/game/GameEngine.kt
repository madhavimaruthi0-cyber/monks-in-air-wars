package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.audio.ArcadeAudioEngine
import com.example.audio.GameHaptics
import com.example.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

class GameEngine(
    val selectedAircraftId: String,
    val missionConfig: LevelMissionData,
    private val audioEngine: ArcadeAudioEngine,
    private val haptics: GameHaptics,
    val isSurvivalMode: Boolean = false,
    val initialUpgrades: AircraftUpgrades = AircraftUpgrades(),
    val paintJob: String = "default",
    val decal: String = "none"
) {
    data class AircraftUpgrades(
        val gunLevel: Int = 1,
        val armorLevel: Int = 1,
        val shieldLevel: Int = 1,
        val missileLevel: Int = 1,
        val bombLevel: Int = 1,
        val countermeasureLevel: Int = 1
    )

    var screenWidth: Float = 720f
    var screenHeight: Float = 1280f

    var gameState: GameState = GameState.READY
    var gameTime: Float = 0f
    var scrollY: Float = 0f

    var score: Long = 0L
    var comboStreak: Int = 0
    var comboTimer: Float = 0f
    var coinsCollected: Int = 0
    var totalEnemiesDefeated: Int = 0

    // Player State
    var playerX: Float = 360f
    var playerY: Float = 1050f
    var targetPlayerX: Float = 360f
    var targetPlayerY: Float = 1050f
    var playerTilt: Float = 0f

    val maxHealth: Float = 100f + (initialUpgrades.armorLevel - 1) * 35f
    var currentHealth: Float = maxHealth

    val maxShield: Float = 60f + (initialUpgrades.shieldLevel - 1) * 25f
    var currentShield: Float = maxShield
    var shieldActive: Boolean = true
    var shieldRechargeCooldown: Float = 0f

    var weaponLevel: Int = minOf(4, initialUpgrades.gunLevel)
    var megaBombs: Int = 2 + (initialUpgrades.bombLevel - 1)
    var flaresRemaining: Int = 2 + initialUpgrades.countermeasureLevel
    var wingmenCount: Int = if (initialUpgrades.missileLevel >= 2) 2 else 0

    var invulnerableTimer: Float = 1.5f
    var fireCooldown: Float = 0f

    var bombFlashTimer: Float = 0f
    var screenShakeAmount: Float = 0f

    // Mission Specific Objectives
    var escortTarget: EscortTarget? = null
    var reconPodsCollected: Int = 0
    val targetReconPods: Int = missionConfig.targetReconCount

    var defenseBaseHp: Float = 1000f
    val maxDefenseBaseHp: Float = 1000f

    // Entities
    val bullets = mutableListOf<Bullet>()
    val enemies = mutableListOf<Enemy>()
    val powerUps = mutableListOf<PowerUpDrop>()
    val particles = mutableListOf<GameParticle>()
    val combatTexts = mutableListOf<FloatingCombatText>()
    val flares = mutableListOf<FlareCountermeasure>()
    val reconPods = mutableListOf<ReconDataPod>()

    // Spawning & Boss Director
    var spawnTimer: Float = 0f
    var boss: Boss? = null
    var bossDefeated: Boolean = false
    var bossWarningTimer: Float = 0f
    private var enemyIdCounter: Long = 0L

    init {
        targetPlayerX = screenWidth / 2f
        targetPlayerY = screenHeight * 0.82f
        playerX = targetPlayerX
        playerY = targetPlayerY

        if (missionConfig.missionType == MissionType.ESCORT) {
            escortTarget = EscortTarget(x = screenWidth / 2f, y = 200f)
        }
    }

    fun onScreenResize(width: Float, height: Float) {
        if (width > 50f && height > 50f) {
            val ratioX = width / screenWidth
            val ratioY = height / screenHeight
            screenWidth = width
            screenHeight = height
            playerX *= ratioX
            playerY *= ratioY
            targetPlayerX *= ratioX
            targetPlayerY *= ratioY
            escortTarget?.let {
                it.x *= ratioX
            }
        }
    }

    fun updateTouchTarget(touchX: Float, touchY: Float, offsetForFinger: Boolean = true) {
        val yOffset = if (offsetForFinger) -70f else 0f
        targetPlayerX = touchX.coerceIn(50f, screenWidth - 50f)
        targetPlayerY = (touchY + yOffset).coerceIn(120f, screenHeight - 90f)
    }

    fun deployFlares() {
        if (flaresRemaining <= 0 || (gameState != GameState.PLAYING && gameState != GameState.BOSS_BATTLE)) return
        flaresRemaining--
        audioEngine.playPowerup()
        haptics.fireHaptic()

        // Spawn defensive decoy flares spread out behind aircraft
        for (i in -2..2) {
            flares.add(
                FlareCountermeasure(
                    x = playerX + i * 14f,
                    y = playerY + 20f,
                    vx = i * 45f + (Random.nextFloat() * 20f - 10f),
                    vy = 120f + (Random.nextFloat() * 40f)
                )
            )
        }
        addFloatingText("COUNTERMEASURES DEPLOYED!", playerX, playerY - 35f, LaserOrange, true)
    }

    fun triggerMegaBomb() {
        if (megaBombs <= 0 || gameState != GameState.PLAYING && gameState != GameState.BOSS_BATTLE) return
        megaBombs--
        bombFlashTimer = 0.6f
        screenShakeAmount = 25f
        audioEngine.playMegaBomb()
        haptics.explosionHaptic()

        bullets.removeAll { !it.isPlayer }

        enemies.forEach { enemy ->
            enemy.hp -= 250f
            spawnExplosion(enemy.x, enemy.y, 16, WarningRed)
            if (enemy.hp <= 0) {
                enemy.alive = false
                score += enemy.scoreValue
                coinsCollected += enemy.coinValue
                totalEnemiesDefeated++
            }
        }
        enemies.removeAll { !it.alive }

        boss?.let { b ->
            b.hp -= 400f
            spawnExplosion(b.x, b.y, 25, WarningRed)
            addFloatingText("EMP IMPACT -400", b.x, b.y - 40f, WarningRed, true)
            if (b.hp <= 0) {
                onBossDefeated()
            }
        }

        for (i in 0..30) {
            val angle = (i.toFloat() / 30f) * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 12f + 8f
            particles.add(
                GameParticle(
                    x = playerX,
                    y = playerY,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 0.8f,
                    maxLife = 0.8f,
                    size = Random.nextFloat() * 8f + 5f,
                    color = PlasmaCyan,
                    type = ParticleType.SHOCKWAVE_RING
                )
            )
        }
        addFloatingText("EMP DETONATED!", screenWidth / 2f, screenHeight * 0.45f, PlasmaCyan, true)
    }

    fun update(deltaSeconds: Float) {
        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)
        gameTime += dt
        scrollY += 140f * dt

        if (gameState == GameState.READY) {
            gameState = GameState.PLAYING
        }

        if (screenShakeAmount > 0f) {
            screenShakeAmount = (screenShakeAmount - 40f * dt).coerceAtLeast(0f)
        }
        if (bombFlashTimer > 0f) {
            bombFlashTimer -= dt
        }

        // Weather impact on plane controls
        val windDrift = when (missionConfig.weather) {
            WeatherType.THUNDERSTORM -> sin(gameTime * 4f) * 25f * dt
            WeatherType.SANDSTORM -> cos(gameTime * 3f) * 15f * dt
            else -> 0f
        }

        val dx = targetPlayerX - playerX + windDrift
        val dy = targetPlayerY - playerY
        playerX += dx * 16f * dt
        playerY += dy * 16f * dt

        val targetTilt = (dx * 0.35f).coerceIn(-25f, 25f)
        playerTilt += (targetTilt - playerTilt) * 12f * dt

        if (invulnerableTimer > 0f) {
            invulnerableTimer -= dt
        }

        if (shieldRechargeCooldown > 0f) {
            shieldRechargeCooldown -= dt
        } else if (currentShield < maxShield) {
            currentShield = (currentShield + 15f * dt).coerceAtMost(maxShield)
            if (currentShield > 10f) shieldActive = true
        }

        if (comboTimer > 0f) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                comboStreak = 0
            }
        }

        if (gameState == GameState.PLAYING || gameState == GameState.BOSS_BATTLE) {
            updateFiring(dt)
            updateDirector(dt)
            updateEscortAndObjectives(dt)
        }

        updateBoss(dt)
        updateEnemies(dt)
        updateBullets(dt)
        updateFlares(dt)
        updateReconPods(dt)
        updatePowerUps(dt)
        updateParticles(dt)
        updateFloatingTexts(dt)
        handleCollisions()
    }

    private fun updateEscortAndObjectives(dt: Float) {
        // Escort Mission check
        escortTarget?.let { escort ->
            if (escort.alive) {
                // Escort gently weaves
                escort.x = (screenWidth / 2f) + sin(gameTime * 0.5f) * (screenWidth * 0.25f)
                if (escort.hp <= 0) {
                    escort.alive = false
                    gameState = GameState.GAME_OVER
                    audioEngine.playBossExplosion()
                    haptics.explosionHaptic()
                    addFloatingText("ESCORT TRANSPORT LOST!", screenWidth / 2f, screenHeight * 0.45f, WarningRed, true)
                }
            }
        }

        // Defense Mission check
        if (missionConfig.missionType == MissionType.DEFENSE) {
            if (defenseBaseHp <= 0) {
                gameState = GameState.GAME_OVER
                audioEngine.playBossExplosion()
                haptics.explosionHaptic()
                addFloatingText("ALLIED BASE DESTROYED!", screenWidth / 2f, screenHeight * 0.45f, WarningRed, true)
            }
        }
    }

    private fun updateFlares(dt: Float) {
        val iter = flares.iterator()
        while (iter.hasNext()) {
            val f = iter.next()
            f.life -= dt
            f.x += f.vx * dt
            f.y += f.vy * dt
            if (f.life <= 0f) {
                iter.remove()
            }
        }
    }

    private fun updateReconPods(dt: Float) {
        val iter = reconPods.iterator()
        while (iter.hasNext()) {
            val pod = iter.next()
            pod.y += pod.vy * dt
            val dist = hypot(playerX - pod.x, playerY - pod.y)
            if (dist < 55f) {
                audioEngine.playPowerup()
                haptics.fireHaptic()
                reconPodsCollected++
                addFloatingText("INTEL POD $reconPodsCollected/$targetReconPods", playerX, playerY - 30f, PlasmaCyan, true)
                iter.remove()
                continue
            }
            if (pod.y > screenHeight + 50f) {
                iter.remove()
            }
        }
    }

    private fun updateFiring(dt: Float) {
        fireCooldown -= dt
        if (fireCooldown <= 0f) {
            val fireInterval = when (selectedAircraftId) {
                "warthog" -> 0.09f
                "crimson" -> 0.12f
                "nebula" -> 0.11f
                else -> 0.13f
            }
            fireCooldown = fireInterval
            spawnPlayerProjectiles()
        }
    }

    private fun spawnPlayerProjectiles() {
        val baseDamage = 18f + (weaponLevel - 1) * 7f
        when (selectedAircraftId) {
            "warthog" -> {
                audioEngine.playHeavyLaser()
                haptics.fireHaptic()
                bullets.add(Bullet(playerX + (Random.nextFloat() * 6f - 3f), playerY - 45f, (Random.nextFloat() * 20f - 10f), -950f, baseDamage * 1.25f, true, BulletType.HEAVY_VULCAN, 8f))
                if (weaponLevel >= 2) {
                    bullets.add(Bullet(playerX - 22f, playerY - 20f, -80f, -900f, baseDamage * 0.7f, true, BulletType.HEAVY_VULCAN, 7f))
                    bullets.add(Bullet(playerX + 22f, playerY - 20f, 80f, -900f, baseDamage * 0.7f, true, BulletType.HEAVY_VULCAN, 7f))
                }
            }
            "crimson" -> {
                audioEngine.playLaser()
                haptics.fireHaptic()
                bullets.add(Bullet(playerX, playerY - 40f, 0f, -900f, baseDamage, true, BulletType.SPREAD_RED, 6f))
                bullets.add(Bullet(playerX - 12f, playerY - 35f, -140f, -870f, baseDamage * 0.9f, true, BulletType.SPREAD_RED, 6f))
                bullets.add(Bullet(playerX + 12f, playerY - 35f, 140f, -870f, baseDamage * 0.9f, true, BulletType.SPREAD_RED, 6f))
                if (weaponLevel >= 3) {
                    bullets.add(Bullet(playerX - 20f, playerY - 30f, -280f, -820f, baseDamage * 0.8f, true, BulletType.SPREAD_RED, 5f))
                    bullets.add(Bullet(playerX + 20f, playerY - 30f, 280f, -820f, baseDamage * 0.8f, true, BulletType.SPREAD_RED, 5f))
                }
            }
            "nebula" -> {
                audioEngine.playLaser()
                haptics.fireHaptic()
                bullets.add(Bullet(playerX - 8f, playerY - 45f, 0f, -1050f, baseDamage * 1.1f, true, BulletType.PLASMA_BLUE, 7f))
                bullets.add(Bullet(playerX + 8f, playerY - 45f, 0f, -1050f, baseDamage * 1.1f, true, BulletType.PLASMA_BLUE, 7f))
                if (weaponLevel >= 2) {
                    val target = enemies.firstOrNull()?.id
                    bullets.add(Bullet(playerX - 25f, playerY - 20f, -100f, -700f, baseDamage * 0.9f, true, BulletType.HOMING_MISSILE, 7f, target))
                    bullets.add(Bullet(playerX + 25f, playerY - 20f, 100f, -700f, baseDamage * 0.9f, true, BulletType.HOMING_MISSILE, 7f, target))
                }
            }
            else -> {
                audioEngine.playLaser()
                haptics.fireHaptic()
                bullets.add(Bullet(playerX - 10f, playerY - 40f, 0f, -920f, baseDamage, true, BulletType.PLASMA_BLUE, 6f))
                bullets.add(Bullet(playerX + 10f, playerY - 40f, 0f, -920f, baseDamage, true, BulletType.PLASMA_BLUE, 6f))
                if (weaponLevel >= 2) {
                    val target = enemies.minByOrNull { (it.x - playerX) * (it.x - playerX) + (it.y - playerY) * (it.y - playerY) }?.id
                    bullets.add(Bullet(playerX - 32f, playerY - 10f, -60f, -800f, baseDamage * 0.85f, true, BulletType.HOMING_MISSILE, 6f, target))
                    bullets.add(Bullet(playerX + 32f, playerY - 10f, 60f, -800f, baseDamage * 0.85f, true, BulletType.HOMING_MISSILE, 6f, target))
                }
            }
        }

        if (wingmenCount > 0) {
            bullets.add(Bullet(playerX - 60f, playerY + 5f, 0f, -900f, baseDamage * 0.5f, true, BulletType.PLASMA_BLUE, 5f))
            if (wingmenCount >= 2) {
                bullets.add(Bullet(playerX + 60f, playerY + 5f, 0f, -900f, baseDamage * 0.5f, true, BulletType.PLASMA_BLUE, 5f))
            }
        }
    }

    private fun updateDirector(dt: Float) {
        if (gameState == GameState.BOSS_WARNING) {
            bossWarningTimer -= dt
            if (bossWarningTimer <= 0f) {
                gameState = GameState.BOSS_BATTLE
                spawnBoss()
            }
            return
        }

        if (gameState == GameState.BOSS_BATTLE) {
            return
        }

        val readyForBoss = when (missionConfig.missionType) {
            MissionType.RECON -> reconPodsCollected >= targetReconPods
            else -> totalEnemiesDefeated >= missionConfig.targetKillsForBoss
        }

        if (!isSurvivalMode && readyForBoss && boss == null && !bossDefeated) {
            gameState = GameState.BOSS_WARNING
            bossWarningTimer = 3.2f
            audioEngine.playBossSiren()
            return
        }

        spawnTimer -= dt
        val spawnInterval = if (isSurvivalMode) {
            (1.4f - minOf(0.9f, totalEnemiesDefeated * 0.015f)).coerceAtLeast(0.45f)
        } else {
            missionConfig.enemySpawnInterval
        }

        if (spawnTimer <= 0f) {
            spawnTimer = spawnInterval
            spawnRandomEnemyWave()
        }
    }

    private fun spawnRandomEnemyWave() {
        val waveRoll = Random.nextFloat()
        val spawnY = -60f

        if (missionConfig.missionType == MissionType.DEFENSE && Random.nextFloat() < 0.45f) {
            // Torpedo Bomber diving towards allied base
            enemies.add(
                Enemy(
                    id = ++enemyIdCounter,
                    type = EnemyType.TORPEDO_BOMBER,
                    x = Random.nextFloat() * (screenWidth - 180f) + 90f,
                    y = spawnY,
                    vx = 0f,
                    vy = 140f,
                    hp = 180f,
                    maxHp = 180f,
                    width = 64f,
                    height = 64f,
                    shootCooldown = 1.0f,
                    scoreValue = 350,
                    coinValue = 25
                )
            )
            return
        }

        when {
            waveRoll < 0.35f -> {
                val centerX = Random.nextFloat() * (screenWidth - 240f) + 120f
                for (i in -1..1) {
                    enemies.add(
                        Enemy(
                            id = ++enemyIdCounter,
                            type = EnemyType.DRONE_SCOUT,
                            x = centerX + i * 55f,
                            y = spawnY - abs(i) * 45f,
                            vx = if (i < 0) 40f else -40f,
                            vy = 220f,
                            hp = 25f,
                            maxHp = 25f,
                            width = 40f,
                            height = 40f,
                            shootCooldown = Random.nextFloat() * 1.5f + 1.0f,
                            scoreValue = 100,
                            coinValue = 5
                        )
                    )
                }
            }
            waveRoll < 0.65f -> {
                val posX = Random.nextFloat() * (screenWidth - 160f) + 80f
                enemies.add(
                    Enemy(
                        id = ++enemyIdCounter,
                        type = EnemyType.INTERCEPTOR,
                        x = posX,
                        y = spawnY,
                        vx = (Random.nextFloat() * 80f - 40f),
                        vy = 180f,
                        hp = 65f,
                        maxHp = 65f,
                        width = 54f,
                        height = 54f,
                        shootCooldown = 0.8f,
                        scoreValue = 250,
                        coinValue = 12
                    )
                )
            }
            waveRoll < 0.85f -> {
                val posX = Random.nextFloat() * (screenWidth - 200f) + 100f
                enemies.add(
                    Enemy(
                        id = ++enemyIdCounter,
                        type = EnemyType.GUNSHIP,
                        x = posX,
                        y = spawnY - 30f,
                        vx = 30f * (if (Random.nextBoolean()) 1 else -1),
                        vy = 90f,
                        hp = 220f,
                        maxHp = 220f,
                        width = 96f,
                        height = 70f,
                        shootCooldown = 1.0f,
                        scoreValue = 600,
                        coinValue = 35
                    )
                )
            }
            else -> {
                val posX = Random.nextFloat() * (screenWidth - 140f) + 70f
                enemies.add(
                    Enemy(
                        id = ++enemyIdCounter,
                        type = EnemyType.KAMIKAZE,
                        x = posX,
                        y = spawnY,
                        vx = 0f,
                        vy = 360f,
                        hp = 45f,
                        maxHp = 45f,
                        width = 38f,
                        height = 44f,
                        shootCooldown = 999f,
                        scoreValue = 200,
                        coinValue = 10
                    )
                )
            }
        }
    }

    private fun spawnBoss() {
        val bossHp = missionConfig.bossMaxHp
        val wpList = when (missionConfig.missionId) {
            2 -> listOf(
                WeakPoint("port_cloak", "Port Cloak Gen", -85f, -10f, 16f, bossHp * 0.2f, bossHp * 0.2f),
                WeakPoint("star_cloak", "Starboard Cloak Gen", 85f, -10f, 16f, bossHp * 0.2f, bossHp * 0.2f),
                WeakPoint("ion_cannon", "Central Ion Core", 0f, 20f, 22f, bossHp * 0.4f, bossHp * 0.4f)
            )
            3 -> listOf(
                WeakPoint("railgun", "Titan Railgun", 0f, 35f, 20f, bossHp * 0.25f, bossHp * 0.25f),
                WeakPoint("left_bay", "Drone Bay Left", -95f, -15f, 18f, bossHp * 0.2f, bossHp * 0.2f),
                WeakPoint("right_bay", "Drone Bay Right", 95f, -15f, 18f, bossHp * 0.2f, bossHp * 0.2f)
            )
            4 -> listOf(
                WeakPoint("node_alpha", "Shield Node Alpha", -100f, 0f, 16f, bossHp * 0.15f, bossHp * 0.15f),
                WeakPoint("node_beta", "Shield Node Beta", 100f, 0f, 16f, bossHp * 0.15f, bossHp * 0.15f),
                WeakPoint("tachyon_lance", "Tachyon Beam Core", 0f, 25f, 24f, bossHp * 0.4f, bossHp * 0.4f)
            )
            else -> listOf(
                WeakPoint("left_flak", "Left Flak Battery", -90f, 15f, 18f, bossHp * 0.2f, bossHp * 0.2f),
                WeakPoint("right_flak", "Right Flak Battery", 90f, 15f, 18f, bossHp * 0.2f, bossHp * 0.2f),
                WeakPoint("core_exhaust", "Reactor Core", 0f, 0f, 22f, bossHp * 0.5f, bossHp * 0.5f)
            )
        }

        boss = Boss(
            name = missionConfig.bossName,
            x = screenWidth / 2f,
            y = -180f,
            targetX = screenWidth / 2f,
            targetY = 220f,
            width = 300f,
            height = 140f,
            hp = bossHp,
            maxHp = bossHp,
            stage = 1,
            weakPoints = wpList
        )
        addFloatingText("TARGET: ${missionConfig.bossName.uppercase()}", screenWidth / 2f, 380f, WarningRed, true)
    }

    private fun updateBoss(dt: Float) {
        val b = boss ?: return

        if (b.y < b.targetY) {
            b.y += 100f * dt
        } else {
            b.stateTime += dt
            b.x = (screenWidth / 2f) + sin(b.stateTime * 0.8f) * (screenWidth * 0.3f)
        }

        // Voltus Stealth Cloaking mechanics
        if (missionConfig.missionId == 2) {
            b.cloakTimer += dt
            if (b.cloakTimer > 4.5f) {
                b.cloakTimer = 0f
            }
            b.cloakAlpha = if (b.cloakTimer in 2.5f..4.0f) 0.35f else 1.0f
        }

        val hpPct = b.hp / b.maxHp
        if (hpPct < 0.35f && !b.isRageMode) {
            b.isRageMode = true
            b.stage = 3
            addFloatingText("CRITICAL STAGE 3: CORE MELTDOWN!", b.x, b.y + 70f, WarningRed, true)
            audioEngine.playBossSiren()
            screenShakeAmount = 18f
        } else if (hpPct < 0.7f && b.stage == 1) {
            b.stage = 2
            addFloatingText("STAGE 2: HEAVY BARRAGE ACTIVE!", b.x, b.y + 70f, LaserOrange, true)
        }

        b.attackTimer -= dt
        if (b.attackTimer <= 0f) {
            b.attackTimer = if (b.isRageMode) 1.1f else 1.7f
            fireBossAttacks(b)
        }

        if (b.stage >= 2) {
            if (!b.chargingBeam && Random.nextFloat() < (if (b.isRageMode) 0.02f else 0.01f)) {
                b.chargingBeam = true
                b.beamChargeProgress = 0f
                b.beamActiveTime = 0f
                b.beamX = b.x
            }

            if (b.chargingBeam) {
                if (b.beamChargeProgress < 1.0f) {
                    b.beamChargeProgress += dt * 0.8f
                    b.beamX = b.x
                } else {
                    b.beamActiveTime += dt
                    if (abs(playerX - b.beamX) < 45f && playerY > b.y) {
                        damagePlayer(50f * dt)
                    }
                    if (b.beamActiveTime >= 1.6f) {
                        b.chargingBeam = false
                        b.beamChargeProgress = 0f
                        b.beamActiveTime = 0f
                    }
                }
            }
        }
    }

    private fun fireBossAttacks(b: Boss) {
        val startY = b.y + b.height * 0.4f

        when (b.stage) {
            1 -> {
                // Aimed flak bursts
                for (a in -1..1) {
                    bullets.add(Bullet(b.x + a * 70f, startY, a * 90f, 260f, 18f, false, BulletType.FLAK_BURST, 9f))
                }
            }
            2 -> {
                val count = if (b.isRageMode) 10 else 7
                val baseAngle = b.stateTime * 2.5f
                for (i in 0 until count) {
                    val angle = baseAngle + (i.toFloat() / count) * 2f * PI.toFloat()
                    bullets.add(Bullet(b.x, startY, cos(angle) * 220f, (sin(angle) * 150f + 160f).coerceAtLeast(80f), 16f, false, BulletType.BOSS_ENERGY_RING, 10f))
                }
            }
            3 -> {
                for (a in -3..3) {
                    bullets.add(Bullet(b.x + a * 20f, startY, a * 65f, 330f, 22f, false, BulletType.ENEMY_LASER_BALL, 8f))
                }
            }
        }
    }

    private fun onBossDefeated() {
        val b = boss ?: return
        bossDefeated = true
        gameState = GameState.VICTORY
        audioEngine.playBossExplosion()
        haptics.explosionHaptic()
        screenShakeAmount = 35f

        for (i in 0..6) {
            spawnExplosion(b.x + Random.nextFloat() * 180f - 90f, b.y + Random.nextFloat() * 100f - 50f, 25, WarningRed)
        }

        score += 15000L
        coinsCollected += missionConfig.coinReward
        addFloatingText("MISSION ACCOMPLISHED!", screenWidth / 2f, screenHeight * 0.45f, RadarGreen, true)
        boss = null
    }

    private fun updateEnemies(dt: Float) {
        val iter = enemies.iterator()
        while (iter.hasNext()) {
            val e = iter.next()
            e.age += dt
            e.x += e.vx * dt
            e.y += e.vy * dt

            if (e.x < 40f) {
                e.x = 40f
                e.vx = abs(e.vx)
            } else if (e.x > screenWidth - 40f) {
                e.x = screenWidth - 40f
                e.vx = -abs(e.vx)
            }

            e.shootCooldown -= dt
            if (e.shootCooldown <= 0f && e.y in 50f..(screenHeight - 200f)) {
                when (e.type) {
                    EnemyType.DRONE_SCOUT -> {
                        e.shootCooldown = 2.0f
                        bullets.add(Bullet(e.x, e.y + 20f, 0f, 280f, 12f, false, BulletType.ENEMY_PLASMA))
                    }
                    EnemyType.INTERCEPTOR -> {
                        e.shootCooldown = 1.6f
                        bullets.add(Bullet(e.x - 12f, e.y + 20f, -40f, 320f, 15f, false, BulletType.ENEMY_PLASMA))
                        bullets.add(Bullet(e.x + 12f, e.y + 20f, 40f, 320f, 15f, false, BulletType.ENEMY_PLASMA))
                    }
                    EnemyType.GUNSHIP -> {
                        e.shootCooldown = 1.4f
                        for (i in -1..1) {
                            bullets.add(Bullet(e.x + i * 22f, e.y + 25f, i * 60f, 300f, 18f, false, BulletType.ENEMY_LASER_BALL, 8f))
                        }
                    }
                    EnemyType.ELITE_ACE -> {
                        e.shootCooldown = 1.1f
                        val angleToPlayer = atan2(playerY - e.y, playerX - e.x)
                        bullets.add(Bullet(e.x, e.y + 20f, cos(angleToPlayer) * 380f, sin(angleToPlayer) * 380f, 20f, false, BulletType.ENEMY_LASER_BALL, 7f))
                    }
                    EnemyType.KAMIKAZE -> {
                        // In Escort mission, kamikaze targets the VIP transport!
                        val targetX = escortTarget?.x ?: playerX
                        val dx = targetX - e.x
                        e.vx += (dx * 1.6f).coerceIn(-180f, 180f) * dt
                    }
                    EnemyType.TORPEDO_BOMBER -> {
                        e.shootCooldown = 1.8f
                        bullets.add(Bullet(e.x, e.y + 25f, 0f, 220f, 30f, false, BulletType.FLAK_BURST, 10f))
                    }
                }
            }

            // Torpedo bomber damage base if reaches bottom in defense mode
            if (e.y > screenHeight - 60f && e.type == EnemyType.TORPEDO_BOMBER) {
                defenseBaseHp -= 90f
                screenShakeAmount = 14f
                audioEngine.playExplosion()
                addFloatingText("BASE HIT! -90 HP", screenWidth / 2f, screenHeight - 120f, WarningRed, true)
                iter.remove()
                continue
            }

            if (e.y > screenHeight + 80f || e.hp <= 0) {
                iter.remove()
            }
        }
    }

    private fun updateBullets(dt: Float) {
        val iter = bullets.iterator()
        while (iter.hasNext()) {
            val b = iter.next()

            // If active decoy flares exist, divert enemy bullets away from player!
            if (!b.isPlayer && flares.isNotEmpty()) {
                val nearestFlare = flares.minByOrNull { hypot(it.x - b.x, it.y - b.y) }
                if (nearestFlare != null) {
                    val angle = atan2(nearestFlare.y - b.y, nearestFlare.x - b.x)
                    b.vx += cos(angle) * 750f * dt
                    b.vy += sin(angle) * 750f * dt
                }
            }

            if (b.type == BulletType.HOMING_MISSILE && b.isPlayer) {
                val target = enemies.find { it.id == b.homingTargetId }
                    ?: enemies.minByOrNull { (it.x - b.x) * (it.x - b.x) + (it.y - b.y) * (it.y - b.y) }
                if (target != null) {
                    val angle = atan2(target.y - b.y, target.x - b.x)
                    b.vx += cos(angle) * 1200f * dt
                    b.vy += sin(angle) * 1200f * dt
                    val speed = sqrt(b.vx * b.vx + b.vy * b.vy)
                    if (speed > 850f) {
                        b.vx = (b.vx / speed) * 850f
                        b.vy = (b.vy / speed) * 850f
                    }
                }
            }

            b.x += b.vx * dt
            b.y += b.vy * dt

            if (b.y < -50f || b.y > screenHeight + 50f || b.x < -40f || b.x > screenWidth + 40f || !b.alive) {
                iter.remove()
            }
        }
    }

    private fun updatePowerUps(dt: Float) {
        val iter = powerUps.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.y += p.vy * 60f * dt
            val dist = hypot(playerX - p.x, playerY - p.y)
            if (dist < 55f) {
                collectPowerUp(p.type)
                iter.remove()
                continue
            }
            if (p.y > screenHeight + 50f) {
                iter.remove()
            }
        }
    }

    private fun collectPowerUp(type: PowerUpType) {
        audioEngine.playPowerup()
        haptics.fireHaptic()
        when (type) {
            PowerUpType.WEAPON_UPGRADE -> {
                weaponLevel = minOf(4, weaponLevel + 1)
                addFloatingText("WEAPON UPGRADE LVL $weaponLevel!", playerX, playerY - 30f, LaserOrange, true)
            }
            PowerUpType.SHIELD_CHARGE -> {
                currentShield = maxShield
                shieldActive = true
                addFloatingText("SHIELD RESTORED!", playerX, playerY - 30f, ShieldBlue, true)
            }
            PowerUpType.HEALTH_REPAIR -> {
                currentHealth = (currentHealth + 45f).coerceAtMost(maxHealth)
                addFloatingText("+45 HULL REPAIRED", playerX, playerY - 30f, RadarGreen, true)
            }
            PowerUpType.WINGMEN_DRONE -> {
                wingmenCount = minOf(2, wingmenCount + 1)
                addFloatingText("ESCORT DRONE ONLINE", playerX, playerY - 30f, PlasmaCyan, true)
            }
            PowerUpType.MEGA_BOMB -> {
                megaBombs++
                addFloatingText("+1 EMP MEGA-BOMB", playerX, playerY - 30f, WarningRed, true)
            }
            PowerUpType.GOLD_COIN -> {
                coinsCollected += 50
                score += 500
                addFloatingText("+50 CREDITS", playerX, playerY - 30f, ArmorYellow, false)
            }
        }
    }

    private fun handleCollisions() {
        for (bullet in bullets) {
            if (!bullet.isPlayer || !bullet.alive) continue

            // Bullet vs Regular Enemies
            for (enemy in enemies) {
                if (!enemy.alive) continue
                if (abs(bullet.x - enemy.x) < enemy.width / 2f && abs(bullet.y - enemy.y) < enemy.height / 2f) {
                    bullet.alive = false
                    enemy.hp -= bullet.damage
                    spawnSparks(bullet.x, bullet.y, ArmorYellow)

                    if (enemy.hp <= 0) {
                        enemy.alive = false
                        onEnemyKilled(enemy)
                    }
                    break
                }
            }

            // Bullet vs Boss and Boss WEAK POINTS
            boss?.let { b ->
                if (bullet.alive && abs(bullet.x - b.x) < b.width / 2f && abs(bullet.y - b.y) < b.height / 2f) {
                    bullet.alive = false

                    // Check direct Weak Point hits for 2.5x Critical Damage
                    var hitWeakPoint: WeakPoint? = null
                    for (wp in b.weakPoints) {
                        if (wp.isExposed && !wp.isDestroyed) {
                            val wpWorldX = b.x + wp.offsetX
                            val wpWorldY = b.y + wp.offsetY
                            if (hypot(bullet.x - wpWorldX, bullet.y - wpWorldY) < wp.radius + 6f) {
                                hitWeakPoint = wp
                                break
                            }
                        }
                    }

                    if (hitWeakPoint != null) {
                        val critDamage = bullet.damage * 2.5f
                        hitWeakPoint.hp -= critDamage
                        b.hp -= critDamage
                        spawnSparks(bullet.x, bullet.y, WarningRed)
                        addFloatingText("CRIT! ${hitWeakPoint.name.uppercase()}", bullet.x, bullet.y - 20f, LaserOrange, true)

                        if (hitWeakPoint.hp <= 0) {
                            hitWeakPoint.isDestroyed = true
                            hitWeakPoint.hp = 0f
                            spawnExplosion(b.x + hitWeakPoint.offsetX, b.y + hitWeakPoint.offsetY, 20, WarningRed)
                            audioEngine.playExplosion()
                            haptics.explosionHaptic()
                            addFloatingText("${hitWeakPoint.name.uppercase()} DESTROYED!", b.x, b.y - 45f, WarningRed, true)
                        }
                    } else {
                        // Standard hull hit
                        b.hp -= bullet.damage
                        spawnSparks(bullet.x, bullet.y, LaserOrange)
                    }

                    if (b.hp <= 0) {
                        onBossDefeated()
                    }
                }
            }
        }

        // Enemy Bullets vs Player & Escort Target
        if (invulnerableTimer <= 0f && (gameState == GameState.PLAYING || gameState == GameState.BOSS_BATTLE)) {
            for (bullet in bullets) {
                if (bullet.isPlayer || !bullet.alive) continue

                // Check bullet vs Escort craft
                escortTarget?.let { escort ->
                    if (escort.alive && hypot(escort.x - bullet.x, escort.y - bullet.y) < 70f) {
                        bullet.alive = false
                        escort.hp -= bullet.damage
                        spawnSparks(bullet.x, bullet.y, WarningRed)
                    }
                }

                // Check bullet vs Player
                val dist = hypot(playerX - bullet.x, playerY - bullet.y)
                if (dist < 32f) {
                    bullet.alive = false
                    damagePlayer(bullet.damage)
                }
            }

            for (enemy in enemies) {
                if (!enemy.alive) continue

                // Enemy ramming Escort craft
                escortTarget?.let { escort ->
                    if (escort.alive && hypot(escort.x - enemy.x, escort.y - enemy.y) < 75f) {
                        escort.hp -= 80f
                        enemy.hp = 0f
                        enemy.alive = false
                        spawnExplosion(enemy.x, enemy.y, 16, WarningRed)
                    }
                }

                // Enemy ramming Player
                val dist = hypot(playerX - enemy.x, playerY - enemy.y)
                if (dist < 45f) {
                    damagePlayer(35f)
                    enemy.hp -= 80f
                    if (enemy.hp <= 0) {
                        enemy.alive = false
                        onEnemyKilled(enemy)
                    }
                }
            }
        }
    }

    private fun damagePlayer(damage: Float) {
        invulnerableTimer = 0.6f
        screenShakeAmount = 14f
        haptics.hitHaptic()

        if (shieldActive && currentShield > 0) {
            audioEngine.playShieldHit()
            currentShield -= damage
            if (currentShield <= 0) {
                currentShield = 0f
                shieldActive = false
                shieldRechargeCooldown = 4.0f
                addFloatingText("SHIELD DEPLETED!", playerX, playerY - 35f, WarningRed, true)
            }
        } else {
            audioEngine.playExplosion()
            currentHealth -= damage
            if (currentHealth <= 0) {
                currentHealth = 0f
                onPlayerDead()
            }
        }
    }

    private fun onPlayerDead() {
        gameState = GameState.GAME_OVER
        audioEngine.playBossExplosion()
        haptics.explosionHaptic()
        screenShakeAmount = 30f
        spawnExplosion(playerX, playerY, 35, WarningRed)
        addFloatingText("CRITICAL ENGINE FAILURE!", screenWidth / 2f, screenHeight * 0.45f, WarningRed, true)
    }

    private fun onEnemyKilled(e: Enemy) {
        audioEngine.playExplosion()
        spawnExplosion(e.x, e.y, 14, WarningRed)
        totalEnemiesDefeated++

        comboStreak++
        comboTimer = 2.5f
        val multiplier = 1.0f + (comboStreak * 0.1f)
        val addedScore = (e.scoreValue * multiplier).toLong()
        score += addedScore
        coinsCollected += e.coinValue

        if (comboStreak > 1) {
            addFloatingText("+$addedScore (x${String.format("%.1f", multiplier)})", e.x, e.y, ArmorYellow, false)
        } else {
            addFloatingText("+$addedScore", e.x, e.y, ArmorYellow, false)
        }

        // In Recon mission, drop Recon Data Pods
        if (missionConfig.missionType == MissionType.RECON && Random.nextFloat() < 0.4f && reconPodsCollected < targetReconPods) {
            reconPods.add(ReconDataPod(e.x, e.y))
        }

        val dropRoll = Random.nextFloat()
        if (dropRoll < 0.22f) {
            val type = when {
                dropRoll < 0.05f -> PowerUpType.WEAPON_UPGRADE
                dropRoll < 0.09f -> PowerUpType.SHIELD_CHARGE
                dropRoll < 0.13f -> PowerUpType.HEALTH_REPAIR
                dropRoll < 0.16f -> PowerUpType.WINGMEN_DRONE
                dropRoll < 0.18f -> PowerUpType.MEGA_BOMB
                else -> PowerUpType.GOLD_COIN
            }
            powerUps.add(PowerUpDrop(x = e.x, y = e.y, type = type))
        }
    }

    private fun spawnExplosion(x: Float, y: Float, count: Int, primaryColor: Color) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 280f + 60f
            val life = Random.nextFloat() * 0.45f + 0.25f
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = life,
                    maxLife = life,
                    size = Random.nextFloat() * 8f + 3f,
                    color = if (Random.nextBoolean()) primaryColor else LaserOrange,
                    type = ParticleType.FIRE
                )
            )
        }
    }

    private fun spawnSparks(x: Float, y: Float, color: Color) {
        for (i in 0..4) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 120f + 40f
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 0.2f,
                    maxLife = 0.2f,
                    size = 3.5f,
                    color = color,
                    type = ParticleType.SPARK
                )
            )
        }
    }

    private fun updateParticles(dt: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.life -= dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vx *= (1f - 2f * dt)
            p.vy *= (1f - 2f * dt)
            if (p.life <= 0f) {
                iter.remove()
            }
        }
    }

    private fun updateFloatingTexts(dt: Float) {
        val iter = combatTexts.iterator()
        while (iter.hasNext()) {
            val txt = iter.next()
            txt.y += txt.vy * 50f * dt
            txt.alpha -= dt * 1.1f
            if (txt.alpha <= 0f) {
                iter.remove()
            }
        }
    }

    private fun addFloatingText(text: String, x: Float, y: Float, color: Color, isCritical: Boolean) {
        combatTexts.add(FloatingCombatText(text, x, y, -1.8f, 1f, color, isCritical))
    }
}
