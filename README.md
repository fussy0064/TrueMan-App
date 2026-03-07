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

### 🛡️ VPN Lockdown & Anti-Bypass
The application implements a high-security lockdown system to prevent the user from bypassing digital safety:
* **TrueMan Safe VPN:** Occupies the Android VPN slot to automatically disconnect and block other VPN apps from running.
* **Aggressive App Interceptor:** Using Global Accessibility Services, TrueMan detects and blocks the opening of unauthorized VPN, Proxy, or Tunneling applications.
* **Uninstall Trap:** If an unauthorized VPN app is detected, TrueMan forcefully triggers the system uninstaller prompt to remove the threat.
* **Auto-Start on Boot:** TrueMan automatically engages its security layers and VPN service as soon as the device is restarted.

### 🚫 Global Ad & Tracker Blocking
TrueMan provides a cleaner and safer browsing experience by removing intrusive advertisements:
* **YouTube Ad Skipper:** Automatically detects and skips non-skippable and skippable YouTube ads in the native YouTube application.
* **Network-Level Ad Filtering:** Routes network traffic (via Safe DNS) through AdGuard DNS servers to block ads, malware, and trackers globally across all applications.

### 🏛️ Device Administrator Integration
TrueMan can be activated as a **Device Administrator**, preventing the application from being uninstalled by the user for maximum security integrity.

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
