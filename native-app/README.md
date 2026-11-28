# SageStudio native Android app

This folder is the independent native Android project for SageStudio.

## Build

Open this folder in Android Studio, or run:

```bash
gradle --no-daemon assembleDebug
```

The debug APK is generated at:

`app/build/outputs/apk/debug/app-debug.apk`

## Current foundation

- Native Kotlin Android activity
- OpenGL ES 2 viewport
- Touch-drag orbit and pinch zoom
- Initial editor toolbar and a grid/cube scene

This is an early foundation, not a complete Roblox Studio replacement. Selection, transform gizmos, scene hierarchy, property editing, Luau execution, Roblox authentication, and Roblox file import/export are not implemented yet.
