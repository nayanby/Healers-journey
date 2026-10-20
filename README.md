# Life RPG

An Android life-progress app with tasks, skills, study and sleep tracking, personal care, finances, milestones, and achievements.

The Kotlin / Jetpack Compose project is in `HealersJourney/`. Navigation is organized into Home, Today, Growth, and More. The colorful PNG artwork and generation prompts are in `HealersJourney/design/icons/`.

## Build

Install Android SDK 35 and JDK 17 or a compatible Android Studio runtime. Configure the SDK using `ANDROID_HOME` or an untracked `HealersJourney/local.properties` file.

```powershell
cd HealersJourney
.\gradlew.bat assembleDebug
```

Google Drive sign-in requires configuring an OAuth client in `app/build.gradle.kts`; the checked-in value is a placeholder.
