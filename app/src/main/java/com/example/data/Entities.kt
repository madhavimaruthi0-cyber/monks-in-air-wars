package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 2500,
    val gems: Int = 50,
    val selectedAircraftId: String = "raptor",
    val totalKills: Int = 0,
    val missionsCompleted: Int = 0,
    val survivalHighScore: Long = 0L,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val touchSensitivity: Float = 1.0f,
    val fingerOffsetEnabled: Boolean = true,
    val pilotRank: String = "Flight Lieutenant",
    val flaresCount: Int = 3
)

@Entity(tableName = "aircraft_units")
data class AircraftEntity(
    @PrimaryKey val id: String,
    val name: String,
    val codename: String,
    val description: String,
    val unlocked: Boolean,
    val price: Int,
    val gunLevel: Int = 1,
    val armorLevel: Int = 1,
    val shieldLevel: Int = 1,
    val missileLevel: Int = 1,
    val bombLevel: Int = 1,
    val countermeasureLevel: Int = 1,
    val paintJob: String = "default", // default, stealth_carbon, desert_viper, crimson_inferno, arctic_ghost, golden_ace
    val selectedDecal: String = "none" // none, ace_star, skull, eagle, dragon
)

@Entity(tableName = "mission_progress")
data class MissionProgressEntity(
    @PrimaryKey val missionId: Int,
    val title: String,
    val subtitle: String,
    val location: String,
    val bossName: String,
    val unlocked: Boolean,
    val stars: Int = 0,
    val highScore: Long = 0L,
    val completed: Boolean = false
)

@Entity(tableName = "dynamic_missions")
data class DynamicMissionEntity(
    @PrimaryKey val missionId: Long,
    val title: String,
    val briefing: String,
    val missionType: String, // ATTACK, ESCORT, RECON, DEFENSE
    val threatLevel: String, // RECRUIT, VETERAN, ELITE, NIGHTMARE
    val weather: String, // CLEAR, THUNDERSTORM, SANDSTORM, AURORA, NIGHT
    val timeOfDay: String, // DAWN, MIDDAY, SUNSET, MIDNIGHT
    val targetKills: Int,
    val bossName: String,
    val rewardCoins: Int,
    val rewardGems: Int,
    val completed: Boolean = false
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val progress: Int = 0,
    val unlocked: Boolean = false,
    val rewardCoins: Int = 500
)
