# ReelGuard — Instagram Reels & Brain Rot Interceptor

A distraction-breaking Android application that tracks Instagram usage and Reels scrolling in real-time, displays an on-screen floating banner with live Brain Rot metrics, and presents customizable reality-check messages with an immediate "Exit to Home" intervention button.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the interactive clarification phase:

- **Floating Overlay Banner**: The intervention banner renders as a persistent, floating overlay pill directly on top of the Instagram screen (using `SYSTEM_ALERT_WINDOW` and a dedicated `ReelBannerOverlayService`).
- **Brain Rot Counter**: Measures real-time Reels scrolled count with humorous yet urgent escalating stages (from *Mindful Reader* to *Total Cognitive Collapse*).
- **Exit Action**: Tapping the banner or hitting the configured reel threshold triggers an immediate exit to the Android Home screen (using `GLOBAL_ACTION_HOME` via the Accessibility Service and `Intent.ACTION_MAIN` / `CATEGORY_HOME`).
- **Test Lab / In-App Reel Simulator**: Because testing outside an active Instagram account or inside emulator environments can be constrained, the app includes a high-fidelity **Reels Doomscroll Simulator** inside the app. This allows instant verification of scroll detection, brain rot escalation, and banner overlays.

---

## 1. Overview & Core Concept

**ReelGuard** attacks short-form video doomscrolling at the point of action. Unlike passive screen-time monitors that only show reports hours later, ReelGuard detects when the user opens Instagram and begins flicking through Reels.

- **Real-Time Reel Counting**: Accessibility Service monitors scroll events (`TYPE_VIEW_SCROLLED` and layout change signatures in `com.instagram.android`) to increment reels scrolled.
- **Persistent Floating Pill Banner**: Stays anchored on top of the screen while Instagram is open. Displays the current Reel count, escalating Brain Rot status tag, and a rotating configurable wake-up message.
- **Customizable Reality Checks**: Users can configure their own punchy wake-up quotes ("Is this reel paying your rent?", "Your dreams won't build themselves", "Touch grass right now").
- **One-Tap Emergency Exit**: Tapping the banner instantly executes a home gesture, breaking the infinite dopamine loop.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Setup & Permission Onboarding**:
   - Clean, step-by-step guidance cards for enabling "Draw over other apps" (`SYSTEM_ALERT_WINDOW`) and the "Accessibility Service" (ReelGuard Reel Detector).
   - Clear indicators showing permission status (Granted / Action Required) with direct intent shortcuts to system settings.
2. **Dashboard & Status Hub**:
   - Today's summary card: Total Reels Scrolled, Current Brain Rot Stage, Kicks to Home, and Doomscroll Time.
   - Master Shield Toggle: Instantly enable or pause tracking.
   - Quick Launch / Test Simulator button.
3. **Reels Doomscroll Simulator (Test Lab)**:
   - Interactive vertical swipeable feed mimicking Reels content to test scroll increments, brain rot level upgrades, and the live overlay banner behavior without needing Instagram installed.
4. **Brain Rot Escalation Settings**:
   - Customize reel thresholds (e.g., Warning at 5 reels, Critical at 15 reels, Lockdown at 25 reels).
   - Escalation tiers with distinctive colors and labels:
     - Level 1 (0–4 Reels): 🌱 *Mindful Browsing* (Green)
     - Level 2 (5–9 Reels): ⚡ *Dopamine Spike* (Yellow)
     - Level 3 (10–19 Reels): ⚠️ *Brain Fog Emerging* (Orange)
     - Level 4 (20–34 Reels): 💀 *Severe Brain Rot* (Red)
     - Level 5 (35+ Reels): 🧠💥 *Cognitive Meltdown* (Neon Purple/Danger Red)
5. **Wake-Up Message Manager**:
   - Pre-loaded motivational and reality-check messages.
   - Add, edit, toggle, and preview custom quotes directly on a mock banner.
6. **Usage Logs & Insights**:
   - Daily scroll counts, session breakdown, and estimated attention span saved.

### Visual Identity & Theme
- **Theme Direction**: High-contrast, focused Cyber-Dark theme with electric neon accent colors (Neon Cyan `#00E5FF`, Toxic Yellow `#FFD600`, Alert Crimson `#FF3B30`, Toxic Violet `#B388FF`, Slate Void `#0F172A`).
- **Surface Elevation**: Translucent glassmorphism cards (`Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))`) with crisp borders.
- **Floating Banner UI**: Compact, ergonomic floating capsule pill with draggable handle, pulsing brain icon, reel count badge, rotating user message, and a bright red "EXIT" action button.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual-Engine Architecture (Accessibility Service + Foreground Overlay Service)**:
  - *Chosen Approach*: `ReelDetectorAccessibilityService` handles window and scroll event detection for `com.instagram.android` and simulated feeds, while `ReelBannerOverlayService` manages the Android `WindowManager` floating view overlay.
  - *Why*: Accessibility services are the standard Android mechanism for observing in-app UI scrolling without root, while a foreground overlay service ensures the banner persists above external third-party apps.
  - *Trade-off*: Requires user to grant Accessibility and Overlay permissions; mitigated with an intuitive permission onboarding wizard and fallback in-app simulation mode.
- **Decision 2: Local Room Database for Quotes & History**:
  - *Chosen Approach*: Use Room to store user custom messages, daily reel logs, and trigger history locally on device.
  - *Why*: Fast, zero-latency, private, and works completely offline without requiring user login.
- **Decision 3: Built-In Doomscroll Simulator**:
  - *Chosen Approach*: Include an interactive test feed in Compose that fires the exact same tracking callbacks.
  - *Why*: Allows immediate visual and functional verification on emulators and development environments where the Instagram app is not logged in.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          ReelGuard Application                         │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  ┌─────────────────────────┐          ┌─────────────────────────────┐  │
│  │   Jetpack Compose UI    │          │     Background Services     │  │
│  │ ┌─────────────────────┐ │          │ ┌─────────────────────────┐ │  │
│  │ │ Dashboard & Metrics │ │          │ │ ReelDetectorService     │ │  │
│  │ └─────────────────────┘ │          │ │ (AccessibilityService)  │ │  │
│  │ ┌─────────────────────┐ │          │ └───────────┬─────────────┘ │  │
│  │ │ Reel Test Lab       │ │◄─────────┼─────────────┘               │  │
│  │ └─────────────────────┘ │  Events  │                             │  │
│  │ ┌─────────────────────┐ │          │ ┌─────────────────────────┐ │  │
│  │ │ Custom Quotes Mgr   │ │          │ │ ReelBannerOverlayService│ │  │
│  │ └─────────────────────┘ │          │ │ (WindowManager Overlay) │ │  │
│  │ ┌─────────────────────┐ │          │ └───────────▲─────────────┘ │  │
│  │ │ Escalation & Alert  │ │          │             │ Interventions │  │
│  └───────────┬─────────────┘          └─────────────┴───────────────┘  │
│              │                                      ▲                  │
│              ▼                                      │                  │
│  ┌──────────────────────────────────────────────────┴───────────────┐  │
│  │                       ReelGuardRepository                        │  │
│  │          (StateFlow, Active Session, Scroll Counter)             │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
│                                     ▼                                  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                  Room Database & App Preferences                 │  │
│  │   • CustomMessageEntity    • DailyScrollLogEntity  • Settings    │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Key Components to Implement:
1. **`data/`**:
   - `AppDatabase.kt`, `CustomMessageDao.kt`, `DailyScrollDao.kt`
   - `ReelGuardRepository.kt`: Central coordinator holding active session state, current brain rot level, scroll counts, and active messages.
2. **`service/`**:
   - `ReelDetectorService.kt`: `AccessibilityService` that inspects package `com.instagram.android`, filters scroll events on reel containers, throttles duplicate swipes, updates the repository, and triggers the overlay.
   - `ReelBannerOverlayService.kt`: Foreground service managing a draggable floating Compose/Android view via `WindowManager.addView()`. Includes brain rot stage indicator, message rotator, and `GLOBAL_ACTION_HOME` / `Intent.CATEGORY_HOME` exit handler.
3. **`ui/`**:
   - `DashboardScreen.kt`: Metrics summary, status toggles, brain rot stage bar.
   - `ReelTestLabScreen.kt`: Interactive in-app scrolling simulator to test reel detection and banner behavior.
   - `QuotesManagerScreen.kt`: Custom message creator, presets, and live banner preview.
   - `SettingsScreen.kt`: Threshold limits, audio/vibration feedback toggles, and permission checklists.
