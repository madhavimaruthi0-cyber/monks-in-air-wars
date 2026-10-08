package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameRepository(private val dao: GameDao) {

    val playerProfile: Flow<PlayerProfile?> = dao.getPlayerProfile()
    val allAircraft: Flow<List<AircraftEntity>> = dao.getAllAircraft()
    val allMissions: Flow<List<MissionProgressEntity>> = dao.getAllMissions()
    val dynamicMissions: Flow<List<DynamicMissionEntity>> = dao.getDynamicMissions()
    val allAchievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultsIfNecessary()
        }
    }

    private suspend fun seedDefaultsIfNecessary() {
        val currentProfile = dao.getPlayerProfile().firstOrNull()
        if (currentProfile == null) {
            dao.insertProfile(
                PlayerProfile(
                    id = 1,
                    coins = 2000,
                    gems = 35,
                    selectedAircraftId = "raptor",
                    totalKills = 0,
                    missionsCompleted = 0,
                    survivalHighScore = 0L,
                    soundEnabled = true,
                    hapticsEnabled = true,
                    touchSensitivity = 1.0f,
                    fingerOffsetEnabled = true,
                    pilotRank = "Flight Lieutenant",
                    flaresCount = 3
                )
            )
        }

        val aircraft = dao.getAllAircraft().firstOrNull()
        if (aircraft.isNullOrEmpty()) {
            dao.insertAircraftList(
                listOf(
                    AircraftEntity(
                        id = "raptor",
                        name = "F-22 Raptor",
                        codename = "Sky Phantom",
                        description = "Balanced multi-role stealth superiority fighter with dual plasma vulcan cannons and guided sidewinder missiles.",
                        unlocked = true,
                        price = 0,
                        gunLevel = 1,
                        armorLevel = 1,
                        shieldLevel = 1,
                        missileLevel = 1,
                        bombLevel = 1,
                        countermeasureLevel = 1,
                        paintJob = "default",
                        selectedDecal = "ace_star"
                    ),
                    AircraftEntity(
                        id = "warthog",
                        name = "A-10 Warthog",
                        codename = "Titan Brute",
                        description = "Heavily armored aerial tank with high-caliber explosive rotary autocannon and cluster payload.",
                        unlocked = false,
                        price = 2200,
                        gunLevel = 1,
                        armorLevel = 2,
                        shieldLevel = 1,
                        missileLevel = 1,
                        bombLevel = 2,
                        countermeasureLevel = 2,
                        paintJob = "desert_viper",
                        selectedDecal = "skull"
                    ),
                    AircraftEntity(
                        id = "crimson",
                        name = "SU-57 Ghost",
                        codename = "Crimson Eclipse",
                        description = "Hyper-maneuverable next-gen fighter equipped with wide 3-way plasma beam spread and EMP shock generator.",
                        unlocked = false,
                        price = 5000,
                        gunLevel = 2,
                        armorLevel = 1,
                        shieldLevel = 2,
                        missileLevel = 2,
                        bombLevel = 1,
                        countermeasureLevel = 2,
                        paintJob = "crimson_inferno",
                        selectedDecal = "dragon"
                    ),
                    AircraftEntity(
                        id = "nebula",
                        name = "Aurora X",
                        codename = "Nebula Valkyrie",
                        description = "Futuristic prototype with continuous focused tachyon lance and automated orbiting laser defense drones.",
                        unlocked = false,
                        price = 9500,
                        gunLevel = 3,
                        armorLevel = 2,
                        shieldLevel = 3,
                        missileLevel = 2,
                        bombLevel = 3,
                        countermeasureLevel = 3,
                        paintJob = "arctic_ghost",
                        selectedDecal = "eagle"
                    )
                )
            )
        }

        val missions = dao.getAllMissions().firstOrNull()
        if (missions.isNullOrEmpty()) {
            dao.insertMissions(
                listOf(
                    MissionProgressEntity(
                        missionId = 1,
                        title = "Operation Skybreaker",
                        subtitle = "Archipelago Coastal Infiltration",
                        location = "Pacific Sector 04",
                        bossName = "Goliath AC-130 Dreadnought",
                        unlocked = true,
                        stars = 0,
                        highScore = 0L,
                        completed = false
                    ),
                    MissionProgressEntity(
                        missionId = 2,
                        title = "Storm Tempest",
                        subtitle = "Category 5 Hurricane Incursion",
                        location = "Nimbus Vortex Ridge",
                        bossName = "Voltus Stealth Leviathan",
                        unlocked = false,
                        stars = 0,
                        highScore = 0L,
                        completed = false
                    ),
                    MissionProgressEntity(
                        missionId = 3,
                        title = "Iron Canyon",
                        subtitle = "Desert Fortress Surface Strike",
                        location = "Red Rock Badlands",
                        bossName = "Titan War Fortress Colossus",
                        unlocked = false,
                        stars = 0,
                        highScore = 0L,
                        completed = false
                    ),
                    MissionProgressEntity(
                        missionId = 4,
                        title = "Orbital Valkyrie",
                        subtitle = "Stratosphere High-Tech Assault",
                        location = "Sub-Orbital Platform 9",
                        bossName = "Apex Orbital Dreadnought",
                        unlocked = false,
                        stars = 0,
                        highScore = 0L,
                        completed = false
                    )
                )
            )
        }

        // Seed initial dynamic missions if empty
        val dyn = dao.getDynamicMissions().firstOrNull()
        if (dyn.isNullOrEmpty()) {
            refreshDynamicMissions()
        }

        val achievements = dao.getAllAchievements().firstOrNull()
        if (achievements.isNullOrEmpty()) {
            dao.insertAchievements(
                listOf(
                    AchievementEntity("first_blood", "First Blood", "Destroy 10 hostile aircraft in combat", 10, 0, false, 300),
                    AchievementEntity("ace_pilot", "Sky Dominator", "Shoot down 75 enemy aircraft", 75, 0, false, 800),
                    AchievementEntity("boss_hunter", "Titan Slayer", "Defeat your first Mission Boss", 1, 0, false, 1000),
                    AchievementEntity("bomb_master", "Tactical Overload", "Deploy 10 EMP Mega-Bombs", 10, 0, false, 500),
                    AchievementEntity("flares_ace", "Ghost Evader", "Deploy 5 countermeasure flare decoys", 5, 0, false, 400),
                    AchievementEntity("rich_pilot", "War Chest", "Collect 3,000 War Credits", 3000, 0, false, 600),
                    AchievementEntity("survivor_ace", "Iron Wings", "Reach 25,000 points in Survival Mode", 25000, 0, false, 1200)
                )
            )
        }
    }

    suspend fun refreshDynamicMissions() {
        dao.clearDynamicMissions()
        val profile = dao.getPlayerProfile().firstOrNull()
        val kills = profile?.totalKills ?: 0

        val threatTier = when {
            kills >= 100 -> "NIGHTMARE"
            kills >= 50 -> "ELITE"
            kills >= 20 -> "VETERAN"
            else -> "RECRUIT"
        }

        val types = listOf("ESCORT", "RECON", "DEFENSE", "ATTACK")
        val weathers = listOf("THUNDERSTORM", "SANDSTORM", "AURORA", "NIGHT", "CLEAR")
        val times = listOf("DAWN", "MIDDAY", "SUNSET", "MIDNIGHT")

        val generated = mutableListOf<DynamicMissionEntity>()

        // 1. Escort Operation
        generated.add(
            DynamicMissionEntity(
                missionId = System.currentTimeMillis() + 1,
                title = "Operation Guardian Angel",
                briefing = "Escort diplomatic Hercules C-130 VIP Transport through hostile airspace. Severe interceptor presence reported.",
                missionType = "ESCORT",
                threatLevel = threatTier,
                weather = weathers.random(),
                timeOfDay = times.random(),
                targetKills = 16,
                bossName = "Goliath AC-130 Interceptor",
                rewardCoins = 850,
                rewardGems = 15
            )
        )

        // 2. Recon Operation
        generated.add(
            DynamicMissionEntity(
                missionId = System.currentTimeMillis() + 2,
                title = "Cipher Ghost Infiltration",
                briefing = "Neutralize radar beacons and recover 8 encrypted tactical intel data pods while under heavy flak fire.",
                missionType = "RECON",
                threatLevel = threatTier,
                weather = "THUNDERSTORM",
                timeOfDay = "NIGHT",
                targetKills = 12,
                bossName = "Voltus Stealth Leviathan",
                rewardCoins = 1100,
                rewardGems = 20
            )
        )

        // 3. Defense Operation
        generated.add(
            DynamicMissionEntity(
                missionId = System.currentTimeMillis() + 3,
                title = "Naval Bastion Defense",
                briefing = "Protect the allied aircraft carrier battlegroup against synchronized waves of dive torpedo bombers.",
                missionType = "DEFENSE",
                threatLevel = threatTier,
                weather = "SANDSTORM",
                timeOfDay = "SUNSET",
                targetKills = 20,
                bossName = "Titan War Fortress Colossus",
                rewardCoins = 1450,
                rewardGems = 25
            )
        )

        dao.insertDynamicMissions(generated)
    }

    suspend fun updateProfile(profile: PlayerProfile) = dao.updateProfile(profile)
    suspend fun updateAircraft(aircraft: AircraftEntity) = dao.updateAircraft(aircraft)
    suspend fun updateMission(mission: MissionProgressEntity) = dao.updateMission(mission)
    suspend fun updateAchievement(achievement: AchievementEntity) = dao.updateAchievement(achievement)

    suspend fun addRewards(coinsEarned: Int, kills: Int, survivalScore: Long = 0L) {
        val profile = dao.getPlayerProfile().firstOrNull() ?: return
        val newKills = profile.totalKills + kills
        val newRank = when {
            newKills >= 100 -> "Air Marshal"
            newKills >= 50 -> "Wing Commander"
            newKills >= 20 -> "Flight Lieutenant"
            else -> "Cadet Pilot"
        }
        val updated = profile.copy(
            coins = profile.coins + coinsEarned,
            totalKills = newKills,
            pilotRank = newRank,
            survivalHighScore = maxOf(profile.survivalHighScore, survivalScore)
        )
        dao.updateProfile(updated)
    }

    suspend fun recordMissionVictory(missionId: Int, score: Long, stars: Int, coinsReward: Int, kills: Int) {
        val missions = dao.getAllMissions().firstOrNull() ?: return
        val currentMission = missions.find { it.missionId == missionId }
        if (currentMission != null) {
            val updatedStars = maxOf(currentMission.stars, stars)
            val updatedScore = maxOf(currentMission.highScore, score)
            dao.updateMission(
                currentMission.copy(
                    stars = updatedStars,
                    highScore = updatedScore,
                    completed = true
                )
            )
        }

        val nextMission = missions.find { it.missionId == missionId + 1 }
        if (nextMission != null && !nextMission.unlocked) {
            dao.updateMission(nextMission.copy(unlocked = true))
        }

        val profile = dao.getPlayerProfile().firstOrNull() ?: return
        val newKills = profile.totalKills + kills
        val newRank = when {
            newKills >= 100 -> "Air Marshal"
            newKills >= 50 -> "Wing Commander"
            newKills >= 20 -> "Flight Lieutenant"
            else -> "Cadet Pilot"
        }
        dao.updateProfile(
            profile.copy(
                coins = profile.coins + coinsReward,
                totalKills = newKills,
                pilotRank = newRank,
                missionsCompleted = profile.missionsCompleted + 1
            )
        )
    }
}
