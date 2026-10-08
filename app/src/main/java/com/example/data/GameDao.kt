package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PlayerProfile)

    @Update
    suspend fun updateProfile(profile: PlayerProfile)

    @Query("SELECT * FROM aircraft_units")
    fun getAllAircraft(): Flow<List<AircraftEntity>>

    @Query("SELECT * FROM aircraft_units WHERE id = :id LIMIT 1")
    suspend fun getAircraftById(id: String): AircraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAircraftList(list: List<AircraftEntity>)

    @Update
    suspend fun updateAircraft(aircraft: AircraftEntity)

    @Query("SELECT * FROM mission_progress ORDER BY missionId ASC")
    fun getAllMissions(): Flow<List<MissionProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<MissionProgressEntity>)

    @Update
    suspend fun updateMission(mission: MissionProgressEntity)

    @Query("SELECT * FROM dynamic_missions ORDER BY missionId DESC")
    fun getDynamicMissions(): Flow<List<DynamicMissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDynamicMissions(missions: List<DynamicMissionEntity>)

    @Query("DELETE FROM dynamic_missions")
    suspend fun clearDynamicMissions()

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
}
