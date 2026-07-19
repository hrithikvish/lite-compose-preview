# Lite Compose Preview

A lightweight alternative to Android Studio's built-in Jetpack Compose Preview.

Studio's live preview can be heavy on RAM/CPU and sometimes causes autocomplete to lag or stop
responding entirely, even on high-end machines. This plugin renders `@Preview` composables
out-of-process using Android Studio's [`android` CLI][android-cli], instead of loading Studio's
own preview engine — so you get a static image of your UI without the IDE slowing down.

> [!NOTE]
> You lose interactive previews and instant live updates with this approach — it's meant for
> quickly checking what a composable looks like, not for iterating on layout in real time.

## How it works

1. A gutter run icon appears next to any Kotlin function annotated with `@Preview`.
2. Clicking it runs `android studio render-compose-preview '<file>' <ComposableName>` in the
   background.
3. The rendered PNG is displayed in the **Compose Preview** tool window.

## Requirements

- Android Studio (or IntelliJ IDEA with the Android/Compose plugins).
- The [`android` CLI][android-cli] installed and available on your `PATH`.
- Gemini in Android Studio signed in, since the `android` CLI talks to a running Studio instance
  through it.

## Known limitations

- Composables that open a separate window — e.g. those wrapped in `Dialog(...)` or `Popup(...)` —
  generally fail to render. This is a limitation of Compose/layoutlib preview rendering in
  general (Studio's own preview panel has the same issue), not something specific to this plugin.

## Building

```bash
./gradlew buildPlugin
```

The installable plugin ZIP is written to `build/distributions/`.

## Development

```bash
./gradlew runIde
```

Launches a sandboxed IDE instance with the plugin installed, for manual testing.

[android-cli]: https://developer.android.com/tools/agents
