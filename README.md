# SE2 Restaurant Management Application

Student Name: Ng Kean Chun

Student ID: BSSE2506036

This is a comprehensive mobile application for Android, designed to streamline restaurant operations for both guests and staff. The app provides a dual-interface system, managed by a single codebase that adapts based on the user's role (Guest or Staff).

## Key Features

The application is split into two main user experiences:

### Guest Features
*   **Book a Table:** Guests can easily make new table reservations.
*   **View Reservations:** A clear, tabbed view of "Upcoming" and "Past" reservations.
*   **Browse Menu:** An interactive and categorized menu to view available food and drinks.
*   **Account Management:** Guests can view and manage their profile details.
*   **Notifications:** Receive real-time pop-up notifications and view a history of updates regarding their bookings (e.g., "Confirmed", "Changes").
*   **Customizable Notifications:** Guests can fine-tune which types of notifications they wish to receive.

### Staff Features
*   **Manage Reservations:** View a complete list of all guest reservations, with the ability to "Confirm" or "Decline" pending requests.
*   **Full Menu Control (CRUD):**
    *   **Create:** Add new items to the menu with a name, description, price, category, and image.
    *   **Read:** View all menu items in a categorized list.
    *   **Update:** Edit details of existing menu items.
    *   **Delete:** Remove items from the menu with a confirmation dialog.
*   **Account Management:** Staff can view their profile details.
*   **Customizable Notifications:** Staff can choose which notifications to receive, such as alerts for new bookings or cancellations.
*   **Notification Center:** A central place to view the history of all important events.

## Technical Implementation & Architecture

This project is built using modern Android development practices and follows a robust architectural pattern.

*   **Language:** Java
*   **Architecture:** MVVM (Model-View-ViewModel)
    *   **View:** `Fragments` and `Activities` responsible for displaying the UI and capturing user input.
    *   **ViewModel:** Holds and manages UI-related data in a lifecycle-conscious way (`AccountViewModel`, `MenuViewModel`, `ReservationViewModel`, etc.). Survives configuration changes.
    *   **Model:** Represents the data layer, managed by `Repositories` which abstract the data sources.
*   **Navigation:** Android Jetpack's **Navigation Component** is used to manage all in-app navigation, including the use of a shared navigation graph (`guest_nav_graph`, `staff_nav_graph`) and bottom navigation bars.
*   **UI Components:**
    *   Built with **Material Design** components (`MaterialSwitch`, `ChipGroup`, `FloatingActionButton`, etc.).
    *   **`RecyclerView`** is used extensively for displaying dynamic lists of reservations, menu items, and notifications.
    *   **`ConstraintLayout`** for creating complex and responsive layouts.
*   **Data Persistence:**
    *   **SQLite:** A local `DatabaseHelper` class manages the SQLite database for persisting all application data (Users, Reservations, Menu Items, Notifications).
    *   **`SharedPreferences`:** Used by `SessionManager` and `SettingsManager` to store simple key-value data like login state and user notification preferences.
*   **Concurrency:**
    *   **`ExecutorService`:** Used in the repositories to perform all database operations on a background thread, preventing the UI thread from being blocked.
    *   **`Handler(Looper.getMainLooper())`:** Used to post results from the background thread back to the main thread.
*   **Image Handling:**
    *   Uses Android's `ActivityResultLauncher` to let users pick images from their device gallery.
    *   Image URIs are stored in the database, and permissions are persisted to ensure access after the app restarts.

## How to Run the Project

1.  **Clone or Download:** Clone this repository or download the source code as a ZIP file.
2.  **Open in Android Studio:**
    *   If you downloaded a ZIP, extract it to a permanent location.
    *   Open Android Studio.
    *   Select **File > Open** and navigate to the root project folder.
3.  **Gradle Sync:** Wait for Android Studio to complete the Gradle sync. This will download all the necessary project dependencies.
4.  **Choose a Device:** Select a virtual device (Emulator) or connect a physical Android device (with USB Debugging enabled).
5.  **Run:** Click the green 'Run' button in the top toolbar.

