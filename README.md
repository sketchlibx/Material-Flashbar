# SK SketchLibX Material Flashbar

A Java-first Android in-app notification library built with Material 3 Expressive components. It provides compact, animated feedback surfaces with Material You theme colours and an optional translucent Glass appearance.

**Package:** `com.material.flashbar`  
**Current version:** `1.0.3`  
**Minimum Android version:** API 23 (Android 6.0)

## Features

- Material 3 Expressive notification design.
- Material You theme colours, including support for DayNight themes and dynamic colours when enabled by the host app.
- Optional Glass appearance with configurable translucency.
- Top or bottom placement inside the current Activity.
- Enter and exit animations, swipe-to-dismiss and an optional close button.
- Success, error, warning, information and loading presets.
- Primary and secondary action buttons.
- Determinate and indeterminate progress indicators.
- Configurable duration, icon visibility, haptic feedback, corner radius and text/background colours.
- Java API; no Kotlin code is required by the library consumer.

> Glass appearance uses a translucent Material surface, highlight stroke and elevation. It does not capture or blur a screenshot of the content behind the notification.

## Requirements

- Android `minSdk 23` or higher.
- JDK 17 for building the library from source.
- Android Gradle Plugin 9.5.0 and Gradle 9.6.0 when building this source project.
- Material Components for Android `1.14.0` (resolved transitively by the library).
- A Material 3 theme in the host Activity.

## Installation with JitPack

### 1. Add the JitPack repository

In your project's `settings.gradle` file, add JitPack to `dependencyResolutionManagement.repositories`:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Keep any repositories that your project already needs.

### 2. Add the library dependency

In your app module's `build.gradle`:

```groovy
dependencies {
    implementation 'com.github.sketchlibx.SK-SketchLibX-MaterialFlashbar:library:1.0.3'
}
```

Use a Git tag that has been published by JitPack. The version in the dependency must match that tag.

## Basic usage

Create and show a Flashbar from an Activity:

```java
import com.material.flashbar.Flashbar;

new Flashbar.Builder(this)
        .title("Changes saved")
        .message("Your preferences have been updated.")
        .duration(Flashbar.DURATION_SHORT)
        .show();
```

You can also use the fluent helper:

```java
Flashbar.with(this)
        .title("Connection restored")
        .message("You're back online.")
        .show();
```

## Notification types and presets

Use a type directly:

```java
new Flashbar.Builder(this)
        .type(Flashbar.Type.SUCCESS)
        .title("Upload complete")
        .message("Your file is ready.")
        .show();
```

Or use the preset builders:

```java
import com.material.flashbar.FlashbarPresets;

FlashbarPresets.success(this, "Saved", "Your project was saved.").show();
FlashbarPresets.error(this, "Upload failed", "Check your connection.").show();
FlashbarPresets.warning(this, "Storage almost full", "Review your available space.").show();
FlashbarPresets.info(this, "New update", "A new version is available.").show();
FlashbarPresets.loading(this, "Preparing your export…").show();
```

Available types: `DEFAULT`, `SUCCESS`, `ERROR`, `WARNING`, `INFO` and `LOADING`.

## Glass appearance

Glass is opt-in. The standard Material appearance remains active unless Glass is enabled:

```java
new Flashbar.Builder(this)
        .title("Glass notification")
        .message("A translucent Material surface.")
        .glassEffect(true)
        .glassOpacity(0.82f)
        .show();
```

To return to the standard Material appearance, use `.glassEffect(false)` or `.style(Flashbar.Style.MATERIAL)`.

`glassOpacity(float)` is clamped to the range `0.35f`–`0.96f`.

## Actions

Action listeners receive the active `Flashbar` instance:

```java
new Flashbar.Builder(this)
        .title("Item deleted")
        .message("The item was moved to the recycle bin.")
        .primaryActionText("UNDO", bar -> {
            restoreItem();
            bar.dismiss();
        })
        .secondaryActionText("CLOSE", bar -> bar.dismiss())
        .show();
```

Use `.dismissOnAction(false)` when the notification should remain visible after an action callback.

## Progress indicators

Determinate progress accepts a value from `0` to `100`:

```java
new Flashbar.Builder(this)
        .title("Exporting")
        .message("Processing your project…")
        .progress(68)
        .duration(Flashbar.DURATION_INDEFINITE)
        .show();
```

For indeterminate progress:

```java
new Flashbar.Builder(this)
        .message("Please wait…")
        .indeterminateProgress()
        .duration(Flashbar.DURATION_INDEFINITE)
        .show();
```

## Position and duration

```java
new Flashbar.Builder(this)
        .type(Flashbar.Type.INFO)
        .title("Notice")
        .message("This notification appears at the top.")
        .gravity(Flashbar.Gravity.TOP)
        .duration(Flashbar.DURATION_LONG)
        .show();
```

- `Flashbar.Gravity.TOP` — top of the Activity content.
- `Flashbar.Gravity.BOTTOM` — bottom of the Activity content (default).
- `Flashbar.DURATION_SHORT` — 2.5 seconds.
- `Flashbar.DURATION_LONG` — 4.5 seconds.
- `Flashbar.DURATION_INDEFINITE` — stays visible until dismissed.

## Builder API

| Method | Description |
| --- | --- |
| `title(CharSequence)` | Sets the title. |
| `message(CharSequence)` | Sets the message. |
| `type(Flashbar.Type)` | Sets the notification type. |
| `gravity(Flashbar.Gravity)` | Sets top or bottom placement. |
| `style(Flashbar.Style)` | Selects `MATERIAL` or `GLASS`. |
| `glassEffect(boolean)` | Enables or disables Glass. |
| `glassOpacity(float)` | Sets Glass surface opacity. |
| `duration(long)` | Sets the auto-dismiss delay. |
| `primaryActionText(text, listener)` | Adds a primary action. |
| `secondaryActionText(text, listener)` | Adds a secondary action. |
| `progress(int)` | Adds determinate progress (0–100). |
| `indeterminateProgress()` | Adds indeterminate progress. |
| `noProgress()` | Removes progress. |
| `swipeToDismiss(boolean)` | Enables or disables swipe dismissal. |
| `showCloseButton(boolean)` | Shows or hides the close control. |
| `showIcon(boolean)` / `icon(Drawable)` | Controls the leading icon. |
| `dismissOnAction(boolean)` | Controls automatic dismissal after an action. |
| `hapticFeedback(boolean)` | Enables haptic feedback when shown. |
| `cornerRadius(float)` | Sets the corner radius in dp (8–40). |
| `backgroundColor(int)` | Overrides the notification background. |
| `titleColor(int)` / `messageColor(int)` | Overrides text colours. |

The built `Flashbar` provides `show()`, `dismiss()`, `isShowing()` and `isShown()`.

## Theme and lifecycle

Use a Material 3 theme in the Activity that displays the Flashbar. For example:

```xml
<style name="Theme.MyApp" parent="Theme.Material3Expressive.DynamicColors.DayNight.NoActionBar" />
```

For dynamic system colours, apply Material `DynamicColors` in your Application or Activity as appropriate for your app. Flashbar reads theme colours from the host Activity.

Flashbar is attached to the Activity's `android.R.id.content` view. Create it from the UI thread (the library posts `show()` and `dismiss()` calls to the main thread when needed). Keep a reference when using an indefinite duration and dismiss it when the message is no longer relevant or before the Activity finishes.

## Local module integration

When using the source project directly instead of JitPack, include the `library` module in your project's `settings.gradle` and add:

```groovy
dependencies {
    implementation project(':library')
}
```

