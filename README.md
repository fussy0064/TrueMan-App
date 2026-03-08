# Mobile Learning and Safe Browsing Application with Parental Control

![TrueMan App Icon](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

## 1. Introduction

The rapid growth of mobile technology has made smartphones an important tool for education and communication. However, unrestricted internet access exposes users, especially children and students, to harmful online content such as adult websites, violent media, and other inappropriate materials.

This project proposes the development of a **Mobile Learning and Safe Browsing Application with Parental Control** (`TrueMan`). The application combines educational content with digital safety features, allowing users to learn daily lessons while browsing the internet safely.

The application integrates a **safe browsing system capable of globally blocking adult and harmful websites automatically** regardless of the browser being used, even operating independently from VPN constraints using Android's Accessibility Services.

---

## 2. Core Features of the Application

### 🛡️ VPN Lockdown & Anti-Bypass
The application implements a high-security lockdown system to prevent the user from bypassing digital safety:
* **TrueMan Safe VPN:** Strictly occupies the Android VPN slot (Bypass disabled) to prevent other VPN apps from being established.
* **Aggressive App Interceptor:** Using Global Accessibility Services, TrueMan detects and blocks the opening of unauthorized VPN, Proxy, or Tunneling applications.
* **Smart Isolation:** Restricted to self-routing, ensuring that your **hidden apps** and **work profile apps** maintain full internet connectivity while the device remains protected.

### 🚫 Global Ad & Tracker Blocking
TrueMan provides a cleaner and safer browsing experience by removing intrusive advertisements:
* **YouTube Ad Executioner:** Multilingual support (English, French, Spanish) to automatically detect and skip ads instantly via Resource IDs and text labels.
* **General Ad Radar:** Scans browsers and apps for generic ad labels like "Sponsored" or "Advertisement" to identify and notify the user.

### 🏛️ Digital Safety & Adult Content Radar
* **Hyper-Scan Keyword Radar:** Over 20+ strict adult keywords monitored in real-time across all browser activities.
* **Automated Block Overlay:** Immediately forces the device back to the Home Screen and shows a "Safe Browsing violation" overlay when prohibited content is detected.
* **System Whitelist:** Integrated safety for system launchers (e.g., Square Home, System UI) to ensure zero false positives during normal phone use.
* **Device Administrator:** Prevents the application from being uninstalled by the user for maximum security integrity.

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
