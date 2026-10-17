# Nova AutoClicker (Файлы без вложенных папок)

Все исходные файлы собраны в одном месте.

## Куда поместить файлы в структуре Android Studio:

- `settings.gradle.kts` -> в корень
- `build.gradle.kts` -> в корень
- `gradle.properties` -> в корень
- `app_build.gradle.kts` -> переименовать в `build.gradle.kts` и положить в папку `app/`
- `AndroidManifest.xml` -> в `app/src/main/`
- `accessibility_service_config.xml` -> в `app/src/main/res/xml/`
- `strings.xml` -> в `app/src/main/res/values/`
- `themes.xml` -> в `app/src/main/res/values/`
- `MainActivity.kt` -> в `app/src/main/java/com/nova/autoclicker/`
- `OverlayService.kt` -> в `app/src/main/java/com/nova/autoclicker/`
- `ClickAccessibilityService.kt` -> в `app/src/main/java/com/nova/autoclicker/`
- `SettingsManager.kt` -> в `app/src/main/java/com/nova/autoclicker/`
- `android.yml` -> в `.github/workflows/`
