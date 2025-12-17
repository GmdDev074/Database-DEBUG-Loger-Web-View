# Android Debug Web View

A debug logging web view library for Android. This library provides a convenient way to view debug logs and database content directly within your application.

[![](https://jitpack.io/v/GmdDev074/Database-DEBUG-Loger-Web-View.svg)](https://jitpack.io/#GmdDev074/Database-DEBUG-Loger-Web-View)

## Installation

### Step 1. Add the JitPack repository to your build file

Add it in your `settings.gradle.kts` at the end of repositories:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2. Add the dependency

Add the dependency in your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.GmdDev074:Database-DEBUG-Loger-Web-View:1.0.0")
}
```

## Usage

Initialize the library in your `Application` class or main `Activity`:

```java
import android.app.Application;
import data.base.de.bug.web.view.DebugView;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Debug View
        DebugView.init(this);
    }
}
```

Once initialized, the library will start a web server.

### Accessing the Dashboard

1.  **Run your application** on an Android device or emulator.
2.  Open **Logcat** in Android Studio.
3.  Filter logs by the tag `DebugView`.
4.  You will see a log message indicating the URL, for example:
    ```
    D/DebugView: Open http://192.168.x.x:8080/index.html
    ```
5.  **Open this URL** in your computer's web browser.
    *   **Note:** Your Android device/emulator and your computer must be connected to the **same Wi-Fi network**.

## Features

*   **Web-based Dashboard**: View debug information comfortably in your desktop browser.
*   **Zero Configuration**: Works out of the box with a single line of code.
*   **Real-time Updates**: (Functionality to be implemented)

## API Reference

### `DebugView.init(Context context)`

Initializes the Debug View server. This method should be called once, typically in your `Application.onCreate()`.

*   **Parameters:**
    *   `context`: The application context (e.g., `this` from your `Application` class).

*   **Example:**
    ```java
    DebugView.init(this);
    ```
