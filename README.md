# PokeDex 📱

A modern, native Android application built with **Kotlin** to showcase clean architecture, declarative UI, and modern Android development best practices. 

This app serves as a digital encyclopedia for Pokémon, powered by the open-source [PokéAPI](https://pokeapi.co/), while demonstrating integration with core mapping APIs as well as other essential services.


## Features

* **Pokémon Explorer:** Browse through a complete list of Pokémon with high-quality image caching, powered by [PokéAPI](https://pokeapi.co/).
* **Detailed Stats:** View in-depth details, including types, base stats, and abilities.
* **Google Maps Integration:** A dedicated mapping feature demonstrating flawless SDK integration. Users can view the map directly in-app, drop pinpoint markers, and utilize a ruler tool for measurement.
* **Offline Support:** Local caching mechanism ensures previously viewed Pokémon data remains accessible without an active internet connection.
* **Photo & Gallery Support:** Utilises the camera and gallery app for clicking pictures, using Intents
* **SavedPreferences:** Persistently stores user data in profile screen.
* **Supports essential Firebase features such as Push Notifications and Crashlytics.**
 

## Tech Stack

This project leverages the official Android modern tech stack guidelines for scalability and maintainability:

* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) – Modern, declarative UI framework.
* **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) – Standard Android DI containerization built on top of Dagger.
* **Networking:** [Retrofit](https://square.github.io/retrofit/) & **OkHttp** – For type-safe HTTP REST API consumption.
* **Local Database:** [Room DB](https://developer.android.com/training/data-storage/room) – SQLite object mapping library for robust offline data caching.
* **Image Loading:** [Coil](https://coil-kt.github.io/coil/) – An asynchronous, Kotlin-first image loading library.
* **Maps:** [Google Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk/overview) – Showing successful API implementation, marker manipulation, and UI tools.
* **Firebase Push Notifications**
* **Firebase Crashlytics**
* **Build System:** Kotlin DSL (`build.gradle.kts`).


## Architecture

The app strictly adheres to **MAD (Modern Android Development)** architectural patterns:

* **MVVM (Model-View-ViewModel):** Keeps UI logic cleanly decoupled from business logic.
* **Repository Pattern:** Serves as the single source of truth, gracefully switching between the remote network source (Retrofit) and local cache (Room DB).
* **Unidirectional Data Flow (UDF):** UI states are exposed via state flows from ViewModels and safely consumed by Jetpack Compose screens.


## Getting Started

### Prerequisites
* Android Studio (Ladybug or newer recommended)
* Android SDK 34+
* A Google Maps API Key

### Installation & Setup
1. Clone the repository:

   ```bash
   git clone [https://github.com/atshayk/Pokedex.git](https://github.com/atshayk/Pokedex.git)
   ```
3. Open the project in Android Studio.
4. Obtain a Google Maps API Key from the Google Cloud Console and add it to your local configuration (local.properties or manifest).
5. Sync the project with Gradle files.
6. Build and run the application on an emulator or physical device.


### Roadmap / Future Updates
* Update the UI
* Add robust Unit Testing using JUnit and Mockk for ViewModels and Repositories.
* Implement UI/Snapshot testing for Compose screens.
* Firebase Authentication
* Gemini SDK support
  
---
*Developed by @atshayk*
