# Fix [ksp] java.lang.IllegalStateException: unexpected jvm signature V

The error `unexpected jvm signature V` is a known issue when using Room 2.6.x with Kotlin 2.0+ and KSP. It usually occurs because the Room KSP processor fails to correctly handle `Unit` (represented as `V` in JVM signatures) for `suspend` DAO methods.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts](file:///C:/Users/klaft/AndroidStudioProjects/RuwaySpace/app/build.gradle.kts)
- Upgrade `roomVersion` from `2.6.1` to `2.7.0` (or later) to ensure compatibility with Kotlin 2.0 and the latest KSP.
- Add `kotlinOptions { jvmTarget = "11" }` to ensure consistent bytecode generation.

#### [MODIFY] [gradle.properties](file:///C:/Users/klaft/AndroidStudioProjects/RuwaySpace/gradle.properties)
- Add `kotlin.native.disableCompilerDaemon=true` which is often required to resolve stability issues with KSP and Room in some environments.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:kspDebugKotlin` to verify the KSP processing completes without the "unexpected jvm signature V" error.
- Run a full build: `./gradlew assembleDebug`.
