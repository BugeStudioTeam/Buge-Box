# Buge-Box

[![Kotlin](https://img.shields.io/badge/Kotlin-59.0%25-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Java](https://img.shields.io/badge/Java-41.0%25-f89820)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Android-34A853?logo=android)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-GPL%203.0-blue.svg)](./LICENSE)

**BugeBox** is a modern Android application cloning tool built with **Material Design 3**. It empowers users to create multiple, isolated instances of installed applications through an intuitive interface, enabling seamless multi-account management.

<div align="center">
  
</div>

## ✨ Key Features

- **🔍 Searchable App Drawer**: Instantly filter and locate target apps from the installed list with real-time search.
- **🧩 Isolated Cloning Engine**: Each cloned instance operates with a unique User ID, ensuring strict separation of app data, cache, and login sessions.
- **🎨 Google Blue Theme**: Implements a clean and modern interface adhering to Material Design 3 guidelines, with support for dynamic color and dark mode.
- **🏗️ Robust Architecture**: Developed in Kotlin and structured around the MVVM pattern for smooth performance, maintainability, and testability.

## 📥 Download & Installation

> ⚠️ **Note**: This repository currently does not provide pre-compiled release packages.

You can obtain the application via the following methods:

### Option 1: Build from Source (Recommended)
1.  Clone the repository:
    ```bash
    git clone https://github.com/BugeStudioTeam/Buge-Box.git
    ```
2.  Open the project in **Android Studio Hedgehog | 2023.1.1** or later.
3.  Wait for Gradle synchronization to complete.
4.  Connect an Android device or start an emulator and click **Run**.

### Option 2: Wait for Official Releases
Click the **Watch** button at the top right of the repository and select "Releases only" to be notified when the first APK is published.

## 🛠️ Technology Stack

| Category | Technology |
| :--- | :--- |
| **Languages** | Kotlin (59.0%) + Java (41.0%) |
| **UI Toolkit** | Jetpack Compose / ViewBinding (Material Design 3) |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Isolation Mechanism** | Multi-User ID Management & Dynamic Proxy |
| **Build System** | Gradle (Kotlin DSL) |

## 🚀 Getting Started

1.  Launch **BugeBox** and grant the necessary storage and installation permissions.
2.  Browse or search for the application you wish to clone (e.g., WhatsApp, Telegram).
3.  Tap the **Clone** button next to the app entry.
4.  Wait for the process to finish. The cloned app, identified by a distinct badge, will appear on your home screen or app drawer.

## 🤝 Contributing

Contributions are what make the open-source community an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

Please check the [Issues](https://github.com/BugeStudioTeam/Buge-Box/issues) page before creating a new one.

## 📄 License

Distributed under the **GNU General Public License v3.0**. See `LICENSE` file for more information.
