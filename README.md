Restaurant Management Application



Author: Ng Kean Chun BSSE2506036



This is a native Android application developed in Java for the Software Engineering 2 (COMP2000) coursework. The application serves two user roles: Guests and Staff.



\## Features



Guest Features



&nbsp;   •User authentication including sign-up, login, and forget/new password.

&nbsp;   

&nbsp;   •Browse the restaurant menu with category filtering and search functionality.

&nbsp;   

&nbsp;   •Create, view, edit, and cancel reservations.

&nbsp;   

&nbsp;   •A visual table map for selecting a table during booking.

&nbsp;   

&nbsp;   •Manage account details, including profile picture and notification preferences.

&nbsp;   

&nbsp;   •Securely delete their account with password verification.

&nbsp;   

&nbsp;   •Receive in-app and system notifications for reservation status updates.

&nbsp;   

Staff Features



&nbsp;   •A separate, secure login for staff members.

&nbsp;   

&nbsp;   •Full CRUD (Create, Read, Update, Delete) functionality for menu items, including image uploads.



&nbsp;   •Manage all guest reservations, with the ability to confirm, deny, or cancel bookings.

&nbsp;   

&nbsp;   •Manage staff account details and notification preferences.

&nbsp;   

&nbsp;   •Receive notifications for guest activities like new bookings or cancellations.



\## Technical Details



•Language: Java



•Architecture: MVVM (Model-View-ViewModel)



•Key Android Jetpack Components:



&nbsp;   •LiveData

&nbsp;   •ViewModel

&nbsp;   •Navigation Component

&nbsp;   

•Networking:



&nbsp;   •Retrofit for handling API requests.

&nbsp;   •Gson for JSON serialization and deserialization.

&nbsp;   

•Local Database:



&nbsp;   •SQLite managed via a custom DatabaseHelper for persistence of user data, menu items, reservations, and notifications.

&nbsp;   

•Asynchronous Operations:



&nbsp;   •ExecutorService is used to perform database operations on background threads.

&nbsp;   

•UI and Design:



&nbsp;   •Material Design 3 components.

&nbsp;   •Responsive layouts for portrait, landscape, and tablet (sw600dp) screen configurations.



\## Build and Run Instructions



1.Clone the repository: git clone https://github.com/Plymouth-COMP2000/design-exercises-NgKeanChun.git



2.Open the project in Android Studio.



3.Sync the project with Gradle files.



4.Connect to Plymouth university server through FortiClient



5.Run the 'app' configuration on an emulator or a connected Android device.



