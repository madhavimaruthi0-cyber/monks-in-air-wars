# ✈️ War of Aircraft

[![Android CI](https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Room-orange.svg)](https://developer.android.com/topic/architecture)

**War of Aircraft** is a next-generation vertical arcade combat flight simulator developed with **Kotlin** and **Jetpack Compose**. Featuring high-framerate 60 FPS hardware-accelerated Canvas rendering, multi-layered parallax weather shaders, a dynamic procedural mission director, customizable fighter aircraft loadouts, and multi-stage epic boss battles.

---

## 🎮 Key Features

### 1. 🎯 Dynamic Mission Generation System
- **Varied Combat Operations**:
  - **Escort Duty**: Defend the heavy allied **Hercules C-130 VIP Transport** through hostile airspace.
  - **Recon Infiltration**: Infiltrate heavily fortified air sectors and recover floating tactical intel data pods.
  - **Naval Base Defense**: Protect aircraft carriers from incoming waves of dive-bombing torpedo aircraft.
  - **Air Superiority Strikes**: Multi-wave dogfights escalating toward dreadnought boss encounters.
- **Adaptive Threat Scaling**: Evaluates pilot rank (`Recruit`, `Veteran`, `Elite`, `Nightmare`) and past kill performance to adjust enemy flight AI, spawn rates, and rewards.
- **Environmental & Atmospheric Shaders**:
  - **Weather**: Clear Skies, Thunderstorm (heavy rain & lightning flashes), Sandstorm (atmospheric haze), Solar Aurora (ion currents), and Night Raid (sweeping searchlights).
  - **Time of Day**: Dawn, Midday, Dusk/Sunset, and Midnight.

### 2. 🛠️ Aircraft Customization & Armory Bay
- **Playable Combat Aircraft**:
  1. **F-22 Raptor ("Sky Phantom")**: Agile multi-role stealth fighter with twin vulcan cannons and guided sidewinder missiles.
  2. **A-10 Thunderbolt ("Titan Brute")**: Armored aerial tank with explosive 30mm rotary autocannons.
  3. **SU-57 Ghost ("Crimson Eclipse")**: Hyper-maneuverable next-gen jet with 3-way spread plasma beams.
  4. **Aurora X ("Nebula Valkyrie")**: Hypersonic prototype featuring focused tachyon lasers and autonomous escort drones.
- **Upgrades & Systems**:
  - Main Cannons (Damage & Fire rate)
  - Titanium Hull Armor & Energy Shields
  - Defensive Countermeasure Flares (decoys that divert enemy homing missiles)
  - Escort Drones & Tactical EMP Mega-Bombs
- **Custom Paint Jobs & Decals**:
  - *Liveries*: Stealth Carbon, Desert Viper Camo, Crimson Inferno, Arctic Ghost, and Golden Ace 24K.
  - *Decals*: Ace Star of Honor, Death Squad Skull, Apex Eagle Wings, and Dragon Crest.

### 3. 👹 Multi-Stage Boss Encounters with Targeted Weak Points
- **Bosses**:
  - **Goliath AC-130 Dreadnought**
  - **Voltus Stealth Leviathan** (Phase-cloaking & ion blade sweeps)
  - **Titan War Fortress "Colossus"** (Railgun artillery & drone bays)
  - **Apex Orbital Valkyrie** (Hexagonal force fields & bullet hell spirals)
- **Targetable Weak Points**: Interactive targeting reticles over flak batteries, ion radar domes, and exposed engine exhausts granting **2.5x Critical Damage** upon direct hits.

---

## 📱 Installation & Play

### Option 1: Direct APK Download
1. Download the latest compiled `.apk` file from the [Releases](../../releases) tab or from the GitHub Actions build artifacts.
2. Transfer or download the APK onto your Android phone.
3. Allow installation from unknown sources if prompted, and launch **War of Aircraft**!

### Option 2: Build from Source with Android Studio
1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/war-of-aircraft.git
   cd war-of-aircraft
   ```
2. Open the project in **Android Studio** (Koala / Ladybug or newer).
3. Let Gradle sync and build the project.
4. Run on an Android device or emulator with **minSdk 24+** (Android 7.0 and up).

Alternatively, build using Gradle CLI:
```bash
gradle assembleDebug
```
The output APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🏗️ Architecture & Technology Stack

- **UI & Graphics**: Jetpack Compose 100%, Hardware-accelerated `Canvas`, dynamic frame time `withFrameNanos` loop.
- **Database / Persistence**: Room 2.6 (KSP) storing player profile, high scores, unlocked liveries, and achievements.
- **Audio & Haptics**: Procedural synthetic audio engine via low-latency Android `AudioTrack` and crisp hardware haptic feedback.
- **Design System**: Material Design 3 with custom sci-fi dark aerospace aesthetic.

---

## 📄 License
This project is licensed under the Apache 2.0 License.
