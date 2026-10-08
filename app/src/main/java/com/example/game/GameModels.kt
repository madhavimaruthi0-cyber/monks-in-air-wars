package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

data class Vector2D(
    var x: Float = 0f,
    var y: Float = 0f
)

enum class BulletType {
    PLASMA_BLUE,
    HEAVY_VULCAN,
    HOMING_MISSILE,
    SPREAD_RED,
    ENEMY_PLASMA,
    ENEMY_LASER_BALL,
    BOSS_ENERGY_RING,
    BOSS_MEGA_BEAM,
    FLAK_BURST
}

data class Bullet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val damage: Float,
    val isPlayer: Boolean,
    val type: BulletType,
    val radius: Float = 6f,
    var homingTargetId: Long? = null,
    var alive: Boolean = true
)

enum class EnemyType {
    DRONE_SCOUT,
    INTERCEPTOR,
    GUNSHIP,
    ELITE_ACE,
    KAMIKAZE,
    TORPEDO_BOMBER
}

data class Enemy(
    val id: Long,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var hp: Float,
    val maxHp: Float,
    val width: Float,
    val height: Float,
    var shootCooldown: Float,
    val scoreValue: Int,
    val coinValue: Int,
    var age: Float = 0f,
    var alive: Boolean = true
)

data class WeakPoint(
    val id: String,
    val name: String,
    val offsetX: Float,
    val offsetY: Float,
    val radius: Float,
    var hp: Float,
    val maxHp: Float,
    var isExposed: Boolean = true,
    var isDestroyed: Boolean = false
)

data class Boss(
    val name: String,
    var x: Float,
    var y: Float,
    var targetX: Float,
    var targetY: Float,
    val width: Float,
    val height: Float,
    var hp: Float,
    val maxHp: Float,
    var stage: Int = 1, // 1: Initial defenses, 2: Heavy barrage + weak points, 3: Core rage mode
    var attackTimer: Float = 0f,
    var stateTime: Float = 0f,
    var chargingBeam: Boolean = false,
    var beamChargeProgress: Float = 0f,
    var beamActiveTime: Float = 0f,
    var beamX: Float = 0f,
    var weakPoints: List<WeakPoint> = emptyList(),
    var isRageMode: Boolean = false,
    var cloakAlpha: Float = 1.0f,
    var cloakTimer: Float = 0f
)

enum class PowerUpType {
    WEAPON_UPGRADE,
    SHIELD_CHARGE,
    HEALTH_REPAIR,
    WINGMEN_DRONE,
    MEGA_BOMB,
    GOLD_COIN
}

data class PowerUpDrop(
    var x: Float,
    var y: Float,
    var vy: Float = 2.5f,
    val type: PowerUpType,
    var alive: Boolean = true
)

enum class ParticleType {
    PLASMA,
    FIRE,
    SMOKE,
    SPARK,
    SHOCKWAVE_RING,
    FLARE_SPARK,
    LIGHTNING,
    SAND_DUST
}

data class GameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val color: Color,
    val type: ParticleType
)

data class FlareCountermeasure(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float = 2.5f,
    val maxLife: Float = 2.5f
)

data class EscortTarget(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 28f,
    var hp: Float = 800f,
    val maxHp: Float = 800f,
    var alive: Boolean = true
)

data class ReconDataPod(
    var x: Float,
    var y: Float,
    var vy: Float = 100f,
    var collected: Boolean = false
)

data class FloatingCombatText(
    val text: String,
    var x: Float,
    var y: Float,
    var vy: Float = -1.5f,
    var alpha: Float = 1.0f,
    val color: Color = Color.Yellow,
    val isCritical: Boolean = false
)

enum class MissionType {
    ATTACK,
    ESCORT,
    RECON,
    DEFENSE
}

enum class WeatherType {
    CLEAR_SKIES,
    THUNDERSTORM,
    SANDSTORM,
    SOLAR_AURORA,
    NIGHT_RAID
}

enum class TimeOfDay {
    DAWN,
    MIDDAY,
    DUSK_SUNSET,
    MIDNIGHT
}

enum class BackgroundTheme {
    OCEAN_ARCHIPELAGO,
    STORM_CYCLONE,
    DESERT_CANYON,
    STRATOSPHERE_CYBER
}

data class LevelMissionData(
    val missionId: Int,
    val name: String,
    val theme: BackgroundTheme,
    val missionType: MissionType = MissionType.ATTACK,
    val weather: WeatherType = WeatherType.CLEAR_SKIES,
    val timeOfDay: TimeOfDay = TimeOfDay.MIDDAY,
    val targetKillsForBoss: Int = 15,
    val bossName: String = "Goliath AC-130",
    val bossMaxHp: Float = 1500f,
    val enemySpawnInterval: Float = 1.2f,
    val coinReward: Int = 600,
    val targetReconCount: Int = 8
)

enum class GameState {
    READY,
    PLAYING,
    BOSS_WARNING,
    BOSS_BATTLE,
    VICTORY,
    GAME_OVER,
    PAUSED
}
