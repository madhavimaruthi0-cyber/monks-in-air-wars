package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AircraftEntity
import com.example.data.DynamicMissionEntity
import com.example.data.MissionProgressEntity
import com.example.game.AircraftGraphicsRenderer
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onStartMissionSelect: () -> Unit,
    onOpenDynamicOperations: () -> Unit,
    onStartSurvival: () -> Unit,
    onOpenHangar: () -> Unit,
    onOpenAchievements: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AircraftDarkBg)
    ) {
        Image(
            painter = painterResource(id = R.drawable.hangar_hero),
            contentDescription = "Dogfight Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC090D16), Color(0x88090D16), Color(0xF0090D16))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val kills = profile?.totalKills ?: 0
                    val rank = profile?.pilotRank ?: "CADET PILOT"
                    Text(text = rank.uppercase(), color = LaserOrange, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text(text = "TOTAL KILLS: $kills", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AircraftCardBg.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = ArmorYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${profile?.coins ?: 0}", color = ArmorYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("settings_button")
                            .clip(CircleShape)
                            .background(AircraftCardBg.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextLight, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Title Banner
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "WAR OF", color = PlasmaCyan, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp)
                Text(text = "AIRCRAFT", color = TextLight, fontSize = 42.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                Text(text = "AIR COMBAT FLIGHT SIMULATOR", color = LaserOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            // Menu Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CAMPAIGN BUTTON
                Button(
                    onClick = onStartMissionSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = PlasmaCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("play_campaign_button")
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "CAMPAIGN MISSIONS", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }

                // DYNAMIC OPERATIONS (ESCORT / RECON / DEFENSE)
                Button(
                    onClick = onOpenDynamicOperations,
                    colors = ButtonDefaults.buttonColors(containerColor = LaserOrange),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("dynamic_ops_button")
                ) {
                    Icon(Icons.Default.Radar, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "DYNAMIC OPERATIONS", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }

                // ENDLESS SURVIVAL
                OutlinedButton(
                    onClick = onStartSurvival,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = AircraftCardBg.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("play_survival_button")
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = WarningRed)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "ENDLESS SURVIVAL", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // HANGAR & CUSTOMIZATION
                OutlinedButton(
                    onClick = onOpenHangar,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = AircraftCardBg.copy(alpha = 0.85f)),
                    border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.linearGradient(listOf(PlasmaCyan, ArmorYellow))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("hangar_button")
                ) {
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = PlasmaCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "HANGAR & SHIP CUSTOMIZATION", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // ACHIEVEMENTS
                OutlinedButton(
                    onClick = onOpenAchievements,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = AircraftCardBg.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("achievements_button")
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ArmorYellow)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "PILOT SERVICE RECORD", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettings = false })
    }
}

@Composable
fun DynamicOperationsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onStartDynamicMission: (DynamicMissionEntity) -> Unit
) {
    val dynamicMissions by viewModel.dynamicMissions.collectAsState()
    val profile by viewModel.profile.collectAsState()

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AircraftDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AircraftCardBg)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextLight)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "TACTICAL OPS TERMINAL", color = LaserOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "DYNAMIC MISSIONS", color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }

            IconButton(
                onClick = { viewModel.refreshDynamicOperations() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AircraftCardBg)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PlasmaCyan)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Threat Intel Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = AircraftCardBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "PILOT PERFORMANCE RATING", color = TextMuted, fontSize = 10.sp)
                    Text(text = profile?.pilotRank?.uppercase() ?: "CADET", color = PlasmaCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "ADAPTIVE ENEMY INTEL",
                    color = ArmorYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(dynamicMissions) { mission ->
                DynamicMissionCard(
                    mission = mission,
                    onLaunch = { onStartDynamicMission(mission) }
                )
            }
        }
    }
}

@Composable
fun DynamicMissionCard(
    mission: DynamicMissionEntity,
    onLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, AircraftCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = AircraftCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge
                val typeColor = when (mission.missionType) {
                    "ESCORT" -> RadarGreen
                    "RECON" -> PlasmaCyan
                    "DEFENSE" -> ShieldBlue
                    else -> LaserOrange
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = typeColor.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "OPERATION: ${mission.missionType}",
                        color = typeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Threat tier badge
                Text(
                    text = "THREAT: ${mission.threatLevel}",
                    color = if (mission.threatLevel == "NIGHTMARE") WarningRed else ArmorYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = mission.title, color = TextLight, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = mission.briefing, color = TextMuted, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(10.dp))

            // Environmental Parameters Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AircraftSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "WEATHER: ${mission.weather}", color = TextLight, fontSize = 10.sp)
                Text(text = "TIME: ${mission.timeOfDay}", color = TextLight, fontSize = 10.sp)
                Text(text = "+${mission.rewardCoins} CR", color = ArmorYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(containerColor = LaserOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "DEPLOY FOR MISSION", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MissionSelectScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onStartMission: (Int) -> Unit
) {
    val missions by viewModel.missions.collectAsState()

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AircraftDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AircraftCardBg)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextLight)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "TACTICAL THEATER", color = LaserOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "CAMPAIGN MISSIONS", color = TextLight, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(missions) { mission ->
                MissionCard(
                    mission = mission,
                    onLaunch = { onStartMission(mission.missionId) }
                )
            }
        }
    }
}

@Composable
fun MissionCard(
    mission: MissionProgressEntity,
    onLaunch: () -> Unit
) {
    val isUnlocked = mission.unlocked
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = if (isUnlocked) AircraftCardBorder else Color(0xFF263238),
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = if (isUnlocked) AircraftCardBg else Color(0xFF0F141C))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "OPERATION 0${mission.missionId}", color = if (isUnlocked) PlasmaCyan else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = mission.title, color = if (isUnlocked) TextLight else TextMuted, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(text = mission.subtitle, color = TextMuted, fontSize = 12.sp)
                }

                if (isUnlocked) {
                    Row {
                        for (s in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (s <= mission.stars) ArmorYellow else Color(0xFF37474F),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = WarningRed)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AircraftSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "SECTOR: ${mission.location}", color = TextMuted, fontSize = 11.sp)
                Text(text = "BOSS: ${mission.bossName}", color = if (isUnlocked) LaserOrange else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (isUnlocked) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onLaunch,
                    colors = ButtonDefaults.buttonColors(containerColor = PlasmaCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = if (mission.completed) "RE-ENGAGE MISSION" else "SCRAMBLE (START)",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HangarScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val aircraftList by viewModel.aircraftList.collectAsState()
    val selectedId = profile?.selectedAircraftId ?: "raptor"

    var currentIdx by remember(aircraftList, selectedId) {
        val idx = aircraftList.indexOfFirst { it.id == selectedId }
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Systems, 1: Paint Jobs, 2: Decals

    val selectedPlane = aircraftList.getOrNull(currentIdx)

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AircraftDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AircraftCardBg)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextLight)
            }

            Text(text = "HANGAR & ARMORY", color = TextLight, fontSize = 17.sp, fontWeight = FontWeight.Black)

            Card(
                colors = CardDefaults.cardColors(containerColor = AircraftCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = ArmorYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${profile?.coins ?: 0}", color = ArmorYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedPlane != null) {
            // Live 3D-style Aircraft Viewport
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = AircraftCardBg),
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(PlasmaCyanGlow, Color.Transparent)))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    var animTime by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            withFrameNanos { now ->
                                animTime = (now / 1_000_000_000f)
                            }
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        AircraftGraphicsRenderer.drawPlayerAircraft(
                            scope = this,
                            aircraftId = selectedPlane.id,
                            x = size.width / 2f,
                            y = size.height / 2f + 8f * sin(animTime * 2f),
                            tiltAngle = 0f,
                            shieldActive = selectedPlane.shieldLevel >= 2,
                            invulnerable = false,
                            time = animTime,
                            wingmenCount = if (selectedPlane.missileLevel >= 2) 2 else 0,
                            paintJob = selectedPlane.paintJob,
                            decal = selectedPlane.selectedDecal
                        )
                    }

                    if (currentIdx > 0) {
                        IconButton(
                            onClick = { currentIdx-- },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = PlasmaCyan, modifier = Modifier.size(34.dp))
                        }
                    }
                    if (currentIdx < aircraftList.size - 1) {
                        IconButton(
                            onClick = { currentIdx++ },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = PlasmaCyan, modifier = Modifier.size(34.dp))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = selectedPlane.name, color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(text = selectedPlane.codename, color = LaserOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs: SYSTEMS / LIVERY / DECALS
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AircraftCardBg,
                contentColor = PlasmaCyan,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("SYSTEMS", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("PAINT JOBS", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("DECALS", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                0 -> {
                    // Systems Upgrades
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            UpgradeRow("Plasma Main Cannons", selectedPlane.gunLevel, selectedPlane.gunLevel * 450, (profile?.coins ?: 0) >= (selectedPlane.gunLevel * 450), selectedPlane.unlocked) {
                                viewModel.upgradeSystem(selectedPlane, "gun")
                            }
                        }
                        item {
                            UpgradeRow("Titanium Armor Plating", selectedPlane.armorLevel, selectedPlane.armorLevel * 450, (profile?.coins ?: 0) >= (selectedPlane.armorLevel * 450), selectedPlane.unlocked) {
                                viewModel.upgradeSystem(selectedPlane, "armor")
                            }
                        }
                        item {
                            UpgradeRow("Defensive Countermeasure Flares", selectedPlane.countermeasureLevel, selectedPlane.countermeasureLevel * 450, (profile?.coins ?: 0) >= (selectedPlane.countermeasureLevel * 450), selectedPlane.unlocked) {
                                viewModel.upgradeSystem(selectedPlane, "countermeasures")
                            }
                        }
                        item {
                            UpgradeRow("Kinetic Shield Generator", selectedPlane.shieldLevel, selectedPlane.shieldLevel * 450, (profile?.coins ?: 0) >= (selectedPlane.shieldLevel * 450), selectedPlane.unlocked) {
                                viewModel.upgradeSystem(selectedPlane, "shield")
                            }
                        }
                        item {
                            UpgradeRow("Sidewinders & Drones", selectedPlane.missileLevel, selectedPlane.missileLevel * 450, (profile?.coins ?: 0) >= (selectedPlane.missileLevel * 450), selectedPlane.unlocked) {
                                viewModel.upgradeSystem(selectedPlane, "missile")
                            }
                        }
                    }
                }
                1 -> {
                    // Paint Jobs Selection
                    val skins = listOf(
                        Triple("default", "Standard Issue", "Military Factory Matte Finish"),
                        Triple("stealth_carbon", "Stealth Carbon", "Deep Radar-Absorbing Matte Carbon"),
                        Triple("desert_viper", "Desert Viper", "Badlands Camouflage with Gold Trim"),
                        Triple("crimson_inferno", "Crimson Inferno", "Volcanic Crimson Assault Camo"),
                        Triple("arctic_ghost", "Arctic Ghost", "Sub-Zero Titanium Frost & Cyan"),
                        Triple("golden_ace", "Golden Ace", "Elite Prestige 24K Polished Chrome")
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(skins) { (skinId, skinName, skinDesc) ->
                            val isSelected = selectedPlane.paintJob == skinId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setPaintJob(selectedPlane, skinId) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) AircraftSurface else AircraftCardBg
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PlasmaCyan, ArmorYellow))) else null,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = skinName, color = if (isSelected) PlasmaCyan else TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = skinDesc, color = TextMuted, fontSize = 11.sp)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PlasmaCyan)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Decals Selection
                    val decals = listOf(
                        Pair("none", "No Insignia"),
                        Pair("ace_star", "Ace Star of Honor"),
                        Pair("skull", "Death Squad Skull"),
                        Pair("eagle", "Apex Eagle Wings"),
                        Pair("dragon", "Crimson Dragon Crest")
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(decals) { (decalId, decalName) ->
                            val isSelected = selectedPlane.selectedDecal == decalId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setDecal(selectedPlane, decalId) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) AircraftSurface else AircraftCardBg
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PlasmaCyan, ArmorYellow))) else null,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = decalName, color = if (isSelected) PlasmaCyan else TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PlasmaCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Equip or Unlock
            if (!selectedPlane.unlocked) {
                val canBuy = (profile?.coins ?: 0) >= selectedPlane.price
                Button(
                    onClick = { viewModel.unlockAircraft(selectedPlane) },
                    enabled = canBuy,
                    colors = ButtonDefaults.buttonColors(containerColor = ArmorYellow),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "UNLOCK AIRCRAFT (${selectedPlane.price} CREDITS)", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else if (selectedId == selectedPlane.id) {
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = AircraftSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RadarGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "CURRENTLY DEPLOYED", color = RadarGreen, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { viewModel.selectAircraft(selectedPlane.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = PlasmaCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "SELECT FOR COMBAT", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun UpgradeRow(
    title: String,
    level: Int,
    cost: Int,
    canAfford: Boolean,
    isUnlocked: Boolean,
    onUpgrade: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AircraftCardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "LVL $level/5", color = PlasmaCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    for (i in 1..5) {
                        Box(
                            modifier = Modifier
                                .size(width = 10.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i <= level) PlasmaCyan else Color(0xFF37474F))
                        )
                    }
                }
            }

            if (level >= 5) {
                Text(text = "MAX", color = RadarGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
            } else if (!isUnlocked) {
                Text(text = "LOCKED", color = TextMuted, fontSize = 10.sp)
            } else {
                Button(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(containerColor = LaserOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "$cost CR", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun AchievementsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val achievements by viewModel.achievements.collectAsState()

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AircraftDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AircraftCardBg)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextLight)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "SERVICE RECORD", color = ArmorYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "PILOT MEDALS & INTEL", color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(achievements) { ach ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (ach.unlocked) AircraftCardBg else Color(0xFF10141D)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (ach.unlocked) ArmorYellow else TextMuted,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = ach.title, color = if (ach.unlocked) TextLight else TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = ach.description, color = TextMuted, fontSize = 11.sp)
                        }
                        Text(text = "+${ach.rewardCoins} CR", color = ArmorYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AircraftCardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(text = "COCKPIT SETTINGS", color = TextLight, fontWeight = FontWeight.Black, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Arcade Sound Effects", color = TextLight)
                    Switch(checked = profile?.soundEnabled ?: true, onCheckedChange = { viewModel.toggleAudio() })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Haptic Vibration", color = TextLight)
                    Switch(checked = profile?.hapticsEnabled ?: true, onCheckedChange = { viewModel.toggleHaptics() })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Vertical Finger Offset", color = TextLight)
                    Switch(checked = profile?.fingerOffsetEnabled ?: true, onCheckedChange = { viewModel.toggleFingerOffset() })
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PlasmaCyan)) {
                Text(text = "CONFIRM", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}
