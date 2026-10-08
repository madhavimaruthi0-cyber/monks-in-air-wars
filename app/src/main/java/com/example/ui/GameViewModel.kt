package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ArcadeAudioEngine
import com.example.audio.GameHaptics
import com.example.data.*
import com.example.game.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN_MENU,
    MISSION_SELECT,
    DYNAMIC_OPERATIONS,
    HANGAR,
    ACHIEVEMENTS,
    GAMEPLAY
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = GameRepository(db.gameDao())
    val audioEngine = ArcadeAudioEngine(application)
    val haptics = GameHaptics(application)

    val profile: StateFlow<PlayerProfile?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val aircraftList: StateFlow<List<AircraftEntity>> = repository.allAircraft
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missions: StateFlow<List<MissionProgressEntity>> = repository.allMissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dynamicMissions: StateFlow<List<DynamicMissionEntity>> = repository.dynamicMissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.MAIN_MENU)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _activeMission = MutableStateFlow<LevelMissionData?>(null)
    val activeMission: StateFlow<LevelMissionData?> = _activeMission.asStateFlow()

    private val _isSurvivalMode = MutableStateFlow(false)
    val isSurvivalMode: StateFlow<Boolean> = _isSurvivalMode.asStateFlow()

    var activeGameEngine: GameEngine? = null

    init {
        viewModelScope.launch {
            profile.collect { p ->
                if (p != null) {
                    audioEngine.soundEnabled = p.soundEnabled
                    haptics.hapticsEnabled = p.hapticsEnabled
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun refreshDynamicOperations() {
        viewModelScope.launch {
            repository.refreshDynamicMissions()
        }
    }

    fun startMission(missionId: Int) {
        val mission = when (missionId) {
            1 -> LevelMissionData(
                missionId = 1,
                name = "Operation Skybreaker",
                theme = BackgroundTheme.OCEAN_ARCHIPELAGO,
                missionType = MissionType.ATTACK,
                weather = WeatherType.CLEAR_SKIES,
                timeOfDay = TimeOfDay.MIDDAY,
                targetKillsForBoss = 14,
                bossName = "Goliath AC-130",
                bossMaxHp = 1200f,
                enemySpawnInterval = 1.3f,
                coinReward = 600
            )
            2 -> LevelMissionData(
                missionId = 2,
                name = "Storm Tempest",
                theme = BackgroundTheme.STORM_CYCLONE,
                missionType = MissionType.ATTACK,
                weather = WeatherType.THUNDERSTORM,
                timeOfDay = TimeOfDay.DUSK_SUNSET,
                targetKillsForBoss = 18,
                bossName = "Voltus Leviathan",
                bossMaxHp = 1800f,
                enemySpawnInterval = 1.1f,
                coinReward = 950
            )
            3 -> LevelMissionData(
                missionId = 3,
                name = "Iron Canyon",
                theme = BackgroundTheme.DESERT_CANYON,
                missionType = MissionType.ATTACK,
                weather = WeatherType.SANDSTORM,
                timeOfDay = TimeOfDay.DAWN,
                targetKillsForBoss = 22,
                bossName = "Titan Colossus",
                bossMaxHp = 2500f,
                enemySpawnInterval = 0.95f,
                coinReward = 1400
            )
            else -> LevelMissionData(
                missionId = 4,
                name = "Orbital Valkyrie",
                theme = BackgroundTheme.STRATOSPHERE_CYBER,
                missionType = MissionType.ATTACK,
                weather = WeatherType.SOLAR_AURORA,
                timeOfDay = TimeOfDay.MIDNIGHT,
                targetKillsForBoss = 28,
                bossName = "Apex Dreadnought",
                bossMaxHp = 3400f,
                enemySpawnInterval = 0.8f,
                coinReward = 2000
            )
        }
        _isSurvivalMode.value = false
        _activeMission.value = mission
        setupEngine(mission, false)
        _currentScreen.value = AppScreen.GAMEPLAY
    }

    fun startDynamicMission(mission: DynamicMissionEntity) {
        val mType = when (mission.missionType) {
            "ESCORT" -> MissionType.ESCORT
            "RECON" -> MissionType.RECON
            "DEFENSE" -> MissionType.DEFENSE
            else -> MissionType.ATTACK
        }
        val wType = when (mission.weather) {
            "THUNDERSTORM" -> WeatherType.THUNDERSTORM
            "SANDSTORM" -> WeatherType.SANDSTORM
            "AURORA" -> WeatherType.SOLAR_AURORA
            "NIGHT" -> WeatherType.NIGHT_RAID
            else -> WeatherType.CLEAR_SKIES
        }
        val tDay = when (mission.timeOfDay) {
            "DAWN" -> TimeOfDay.DAWN
            "SUNSET" -> TimeOfDay.DUSK_SUNSET
            "MIDNIGHT" -> TimeOfDay.MIDNIGHT
            else -> TimeOfDay.MIDDAY
        }
        val theme = when (mission.weather) {
            "THUNDERSTORM" -> BackgroundTheme.STORM_CYCLONE
            "SANDSTORM" -> BackgroundTheme.DESERT_CANYON
            "AURORA" -> BackgroundTheme.STRATOSPHERE_CYBER
            else -> BackgroundTheme.OCEAN_ARCHIPELAGO
        }

        val levelData = LevelMissionData(
            missionId = (mission.missionId % 1000).toInt(),
            name = mission.title,
            theme = theme,
            missionType = mType,
            weather = wType,
            timeOfDay = tDay,
            targetKillsForBoss = mission.targetKills,
            bossName = mission.bossName,
            bossMaxHp = 1600f,
            enemySpawnInterval = 1.1f,
            coinReward = mission.rewardCoins
        )

        _isSurvivalMode.value = false
        _activeMission.value = levelData
        setupEngine(levelData, false)
        _currentScreen.value = AppScreen.GAMEPLAY
    }

    fun startSurvival() {
        val survivalMission = LevelMissionData(
            missionId = 99,
            name = "Endless Dogfight Survival",
            theme = BackgroundTheme.STRATOSPHERE_CYBER,
            missionType = MissionType.ATTACK,
            weather = WeatherType.SOLAR_AURORA,
            timeOfDay = TimeOfDay.MIDNIGHT,
            targetKillsForBoss = 9999,
            bossName = "Nemesis Overlord",
            bossMaxHp = 5000f,
            enemySpawnInterval = 1.0f,
            coinReward = 200
        )
        _isSurvivalMode.value = true
        _activeMission.value = survivalMission
        setupEngine(survivalMission, true)
        _currentScreen.value = AppScreen.GAMEPLAY
    }

    private fun setupEngine(mission: LevelMissionData, survival: Boolean) {
        val currentP = profile.value
        val selectedId = currentP?.selectedAircraftId ?: "raptor"
        val aircraft = aircraftList.value.find { it.id == selectedId }

        val upgrades = GameEngine.AircraftUpgrades(
            gunLevel = aircraft?.gunLevel ?: 1,
            armorLevel = aircraft?.armorLevel ?: 1,
            shieldLevel = aircraft?.shieldLevel ?: 1,
            missileLevel = aircraft?.missileLevel ?: 1,
            bombLevel = aircraft?.bombLevel ?: 1,
            countermeasureLevel = aircraft?.countermeasureLevel ?: 1
        )

        activeGameEngine = GameEngine(
            selectedAircraftId = selectedId,
            missionConfig = mission,
            audioEngine = audioEngine,
            haptics = haptics,
            isSurvivalMode = survival,
            initialUpgrades = upgrades,
            paintJob = aircraft?.paintJob ?: "default",
            decal = aircraft?.selectedDecal ?: "none"
        )
    }

    fun restartCurrentGame() {
        _activeMission.value?.let { mission ->
            setupEngine(mission, _isSurvivalMode.value)
        }
    }

    fun selectAircraft(aircraftId: String) {
        val p = profile.value ?: return
        viewModelScope.launch {
            repository.updateProfile(p.copy(selectedAircraftId = aircraftId))
        }
    }

    fun unlockAircraft(aircraft: AircraftEntity) {
        val p = profile.value ?: return
        if (p.coins >= aircraft.price && !aircraft.unlocked) {
            viewModelScope.launch {
                repository.updateProfile(p.copy(coins = p.coins - aircraft.price, selectedAircraftId = aircraft.id))
                repository.updateAircraft(aircraft.copy(unlocked = true))
                audioEngine.playPowerup()
            }
        }
    }

    fun setPaintJob(aircraft: AircraftEntity, paintJob: String) {
        viewModelScope.launch {
            repository.updateAircraft(aircraft.copy(paintJob = paintJob))
            audioEngine.playPowerup()
        }
    }

    fun setDecal(aircraft: AircraftEntity, decal: String) {
        viewModelScope.launch {
            repository.updateAircraft(aircraft.copy(selectedDecal = decal))
            audioEngine.playPowerup()
        }
    }

    fun upgradeSystem(aircraft: AircraftEntity, system: String) {
        val p = profile.value ?: return
        val currentLvl = when (system) {
            "gun" -> aircraft.gunLevel
            "armor" -> aircraft.armorLevel
            "shield" -> aircraft.shieldLevel
            "missile" -> aircraft.missileLevel
            "bomb" -> aircraft.bombLevel
            "countermeasures" -> aircraft.countermeasureLevel
            else -> 1
        }
        if (currentLvl >= 5) return
        val upgradeCost = currentLvl * 450
        if (p.coins >= upgradeCost) {
            viewModelScope.launch {
                repository.updateProfile(p.copy(coins = p.coins - upgradeCost))
                val updated = when (system) {
                    "gun" -> aircraft.copy(gunLevel = currentLvl + 1)
                    "armor" -> aircraft.copy(armorLevel = currentLvl + 1)
                    "shield" -> aircraft.copy(shieldLevel = currentLvl + 1)
                    "missile" -> aircraft.copy(missileLevel = currentLvl + 1)
                    "bomb" -> aircraft.copy(bombLevel = currentLvl + 1)
                    "countermeasures" -> aircraft.copy(countermeasureLevel = currentLvl + 1)
                    else -> aircraft
                }
                repository.updateAircraft(updated)
                audioEngine.playPowerup()
            }
        }
    }

    fun onGameFinished(engine: GameEngine) {
        viewModelScope.launch {
            if (engine.isSurvivalMode) {
                repository.addRewards(
                    coinsEarned = engine.coinsCollected,
                    kills = engine.totalEnemiesDefeated,
                    survivalScore = engine.score
                )
            } else {
                if (engine.bossDefeated) {
                    val stars = if (engine.currentHealth > engine.maxHealth * 0.7f) 3 else if (engine.currentHealth > 0) 2 else 1
                    repository.recordMissionVictory(
                        missionId = engine.missionConfig.missionId,
                        score = engine.score,
                        stars = stars,
                        coinsReward = engine.coinsCollected,
                        kills = engine.totalEnemiesDefeated
                    )
                } else {
                    repository.addRewards(
                        coinsEarned = engine.coinsCollected,
                        kills = engine.totalEnemiesDefeated
                    )
                }
            }
        }
    }

    fun toggleAudio() {
        val p = profile.value ?: return
        val newVal = !p.soundEnabled
        viewModelScope.launch {
            repository.updateProfile(p.copy(soundEnabled = newVal))
            audioEngine.soundEnabled = newVal
        }
    }

    fun toggleHaptics() {
        val p = profile.value ?: return
        val newVal = !p.hapticsEnabled
        viewModelScope.launch {
            repository.updateProfile(p.copy(hapticsEnabled = newVal))
            haptics.hapticsEnabled = newVal
        }
    }

    fun toggleFingerOffset() {
        val p = profile.value ?: return
        viewModelScope.launch {
            repository.updateProfile(p.copy(fingerOffsetEnabled = !p.fingerOffsetEnabled))
        }
    }
}
