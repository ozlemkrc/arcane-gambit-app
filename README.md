# Arcane Gambit

Arcane Gambit is an Android application designed for character management, joining the Arcane Gambit game using NFC, and spectating the game through Augmented Reality (AR). This app serves as a companion tool to enhance the gaming experience.

## Features

- **Login System**: Securely log in to manage your characters.
- **Character Management**: Create, view, and manage characters for the Arcane Gambit game.
- **NFC Integration**: Join the game by tapping your NFC-enabled device to the game station.
- **AR Spectator Mode**: Watch ongoing games in an immersive Augmented Reality experience.
- **Dynamic UI**: Adaptive user interface based on NFC availability and status.

## Prerequisites

- Android Studio (latest version recommended)
- Android device with NFC support
- Minimum Android SDK version: 21 (Lollipop)

## Getting Started

1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```

2. Open the project in Android Studio.

3. Sync the project with Gradle files.

4. Build and run the application on an NFC-enabled Android device or emulator.

## Project Structure

- **`app/src/main/java/com/example/arcane_gambit/ui/screens`**: Contains the main UI components.
- **`app/src/main/res`**: Resources such as layouts, drawables, and strings.
- **`build.gradle.kts`**: Project-level and app-level Gradle configuration files.

## Key Components

### Main Screens

The application features multiple screens, including:

- **GamePlaceholderScreen**: Handles NFC interactions and displays character stats.
- **AR Spectator Screen**: Provides an immersive AR experience for spectating games.

### `CompactStatItem`

A reusable composable function for displaying character stats in a compact format.

## How to Use

1. Launch the app on an NFC-enabled device.
2. Log in to your account.
3. Create or manage your characters.
4. If NFC is disabled, follow the on-screen instructions to enable it.
5. Tap your device to the game station to join the game.
6. Use the AR spectator mode to watch ongoing games.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Contact

For questions or feedback, please contact the project maintainer at [kurucaozlem@gmail.com].
