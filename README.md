# Mobile Learning and Safe Browsing Application with Parental Control

![TrueMan App Icon](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

## 1. Introduction

The rapid growth of mobile technology has made smartphones an important tool for education and communication. However, unrestricted internet access exposes users, especially children and students, to harmful online content such as adult websites, violent media, and other inappropriate materials.

This project proposes the development of a **Mobile Learning and Safe Browsing Application with Parental Control** (`TrueMan`). The application combines educational content with digital safety features, allowing users to learn daily lessons while browsing the internet safely.

The application integrates a **safe browsing system capable of globally blocking adult and harmful websites automatically** regardless of the browser being used, even operating independently from VPN constraints using Android's Accessibility Services.

---

## 2. Core Features of the Application

### 📚 Daily Learning Lessons
The application provides short educational lessons to encourage continuous learning. It features topics like Time Management and Online Safety stored locally on the device using an SQLite database. You can track your reading progress, keeping a log of what you have finished to build healthy learning habits.

### 🛡️ VPN-Independent Safe Browsing
The application uses **Android Accessibility Services** to track the URL headers across all major browsers installed on the device (Chrome, Firefox, Edge, Opera, Samsung Internet).
If an adult keyword or blacklisted site (like *porn*, or *xxx*) is detected, TrueMan immediately blocks access and shows an Access Denied restriction screen.

### 👨‍👩‍👦 Parental Control System
The parental control module allows administrators to efficiently manage browsing restrictions by manually specifying domains to block and dynamically enabling the Accessibility Service.

### 🌐 Integrated API Learning Modules
TrueMan seamlessly fetches real-time data from completely free logic APIs:
* **IT Computer Studies:** Engages users with technical computing and software engineering multiple-choice trivia using the OpenTDB system.
* **Daily Exercises:** Retrieves randomized english exercise tutorials (with descriptions of the form/technique) leveraging the open WGER fitness database.
* **Nutrition Facts:** Breaks down complex macros like fat, sugars, and protein using the Fruityvice open endpoint.

---

## 3. Technology Stack

* **Development Environment:** Android Studio
* **Language:** Java, XML
* **Data Storage:** Local SQLite Database (via native SQLiteOpenHelper)
* **API Communication:** Native HttpURLConnection / JSON
* **Background Monitoring:** Android AccessibilityService
* **Concurrency:** Java ExecutorService / UI Thread Handlers

---

## 4. Run Locally

1. Clone the repository: `git clone https://github.com/YourUsername/TrueMan-App.git`
2. Open the project folder `TrueMan` in **Android Studio**.
3. Let Gradle sync and resolve project configurations.
4. Run the module dynamically on your connected debugger or emulator!

*(Note: To test the global web-filtering feature, you must explicitly enable the accessibility permission manually on your device via **Settings -> Accessibility -> Installed Apps -> TrueMan**)*
