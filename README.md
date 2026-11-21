# SK SketchLibX · Material Flashbar

A Java-first Android in-app notification component with a Material 3 Expressive look, dynamic theme colours, swipe dismissal, progress feedback and an optional translucent **Glass** appearance.

Package / namespace: `com.material.flashbar`

This is an independently authored implementation for SK SketchLibX. It is not an official fork and does not include source files copied from `Netsnake-TN/material-flashbar`. The reference project was used only to understand the general notification-library use case and feature expectations.

## What is included

- Material 3 Expressive components and theme-aware surface, text, outline and primary colours.
- Day/night support through the host application's Material theme.
- Optional Glass presentation with translucent surface tint, a soft highlight stroke and elevation.
- Top or bottom placement inside the current Activity content view, with system-bar inset handling for edge-to-edge screens.
- Enter/exit motion, horizontal swipe-to-dismiss, optional haptics and close control.
- Success, error, warning, info and loading presets.
- Primary and secondary actions, indeterminate loading and determinate progress.
- A small sample app demonstrating the visual modes and common configurations.

### Glass appearance

Glass mode is enabled with `.glassEffect(true)`. It uses a translucent surface treatment, a light outline and layered colour tinting. It does **not** capture or blur the app behind the notification; avoiding screenshot-based background capture keeps the component lightweight and avoids collecting screen content. The exact appearance depends on the Activity theme and what's behind the card.

## Project requirements

| Setting | Version |
| --- | --- |
| Android Gradle Plugin | 9.5.0 |
| Gradle | 9.6.0 |
| JDK | 17 |
| Compile SDK | 37 |
| Minimum SDK | 23 |
| Material Components for Android | 1.14.0 |

Material Components for Android 1.14.0 supplies the Views-based Material 3 Expressive widgets used here. Google's Views-based Material Components project entered maintenance mode in 2026; future new Material Android UI development is Compose-first. This Java/View package therefore uses the latest stable Views dependency selected for this implementation, but it cannot promise future Views feature releases from Google.

## Add the library to an Android project

Copy the `library` folder into your project and include it in `settings.gradle`:

```groovy
include ':library'
```

Then add the module dependency:

```groovy
dependencies {
    implementation project(':library')
}
```

Make sure the project repositories include Google Maven and Maven Central:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

The host Activity should use a Material 3 theme. For the sample app, the theme parent is:

```xml
<style name="Theme.MyApp" parent="Theme.Material3Expressive.DynamicColors.DayNight.NoActionBar" />
```

If your app uses a custom theme, it must provide the Material theme attributes consumed by Material Components.

## Quick start

```java
import com.material.flashbar.Flashbar;

new Flashbar.Builder(this)
        .title("Saved successfully")
        .message("Your changes are now up to date.")
        .primaryActionText("UNDO", bar -> bar.dismiss())
        .gravity(Flashbar.Gravity.BOTTOM)
        .duration(Flashbar.DURATION_SHORT)
        .build()
        .show();
```

The builder also provides a one-call display method:

```java
Flashbar.with(this)
        .title("Connection restored")
        .message("You're back online.")
        .show();
```

## Glass mode

```java
new Flashbar.Builder(this)
        .type(Flashbar.Type.INFO)
        .title("Glass appearance")
        .message("A translucent Material surface with a soft highlight.")
        .glassEffect(true)
        .glassOpacity(0.82f) // clamped to 0.35–0.96
        .build()
        .show();
```

Disable it again with `.glassEffect(false)` or `.style(Flashbar.Style.MATERIAL)`.

## Presets

```java
import com.material.flashbar.Flashbar;
import com.material.flashbar.FlashbarPresets;

FlashbarPresets.success(this, "Done", "Your project was saved.")
        .primaryActionText("OPEN", bar -> bar.dismiss())
        .show();

FlashbarPresets.error(this, "Couldn't upload", "Check your connection and retry.")
        .primaryActionText("RETRY", bar -> retryUpload())
        .secondaryActionText("CLOSE", bar -> bar.dismiss())
        .show();

FlashbarPresets.loading(this, "Preparing your export…").show();
```

Available presets: `success`, `error`, `warning`, `info`, `loading`.

## API overview

| Method | Purpose |
| --- | --- |
| `title(CharSequence)` / `message(CharSequence)` | Notification text |
| `type(Flashbar.Type)` | Default, success, error, warning, info or loading |
| `gravity(Flashbar.Gravity)` | Top or bottom position |
| `style(Flashbar.Style)` / `glassEffect(boolean)` | Material or Glass appearance |
| `glassOpacity(float)` | Glass surface opacity |
| `duration(long)` | Auto-dismiss delay; `0` or `DURATION_INDEFINITE` disables the timer |
| `primaryActionText(text, listener)` | Primary Material action |
| `secondaryActionText(text, listener)` | Secondary outlined action |
| `progress(int)` | Determinate progress from 0 to 100 |
| `indeterminateProgress()` | Indeterminate progress animation |
| `swipeToDismiss(boolean)` | Horizontal gesture dismissal |
| `showCloseButton(boolean)` | Show or hide the close control |
| `hapticFeedback(boolean)` | Haptic feedback when shown |
| `cornerRadius(float)` | Corner radius in dp (clamped to 8–40 dp) |
| `backgroundColor(int)` | Explicit background colour override |
| `titleColor(int)` / `messageColor(int)` | Explicit text colour overrides |

`Flashbar` exposes `show()`, `dismiss()`, `isShowing()` and `isShown()`.

## Build the sample

Open this folder in Android Studio or AndroidIDE with JDK 17, Android SDK Platform 37, and Gradle 9.6.0. The wrapper distribution is declared in `gradle/wrapper/gradle-wrapper.properties`. If your IDE uses an IDE-managed Gradle instead of the wrapper, set its Gradle distribution to **9.6.0**. Then run the `sample` configuration or execute:

```bash
gradle :sample:assembleDebug
```

The IDE can resolve the plugins and libraries from Google Maven and Maven Central. See [GRADLE_FIX.md](GRADLE_FIX.md) for AndroidIDE configuration and troubleshooting.

## Lifecycle note

The Flashbar is attached to the Activity's `android.R.id.content` view. Keep a reference when using an indefinite duration and call `dismiss()` when the feedback is no longer relevant or before the host Activity finishes. Use it from an Activity with a Material 3 theme.

## Licensing and third-party components

- Original files in this project are released by **SK SketchLibX** under the MIT License in `LICENSE`.
- Material Components for Android is a separate third-party dependency, distributed under its own Apache License 2.0 terms. Its source and license are not relicensed by this project.
- Android SDK and Gradle tooling remain subject to their respective owners' terms. See `THIRD_PARTY_NOTICES.md`.

The MIT license applies only to the original source and documentation in this project; it does not claim ownership of Google Material Components or any other third-party work.
