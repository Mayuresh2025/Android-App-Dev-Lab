# Voyageur 🌍✈️

**Voyageur** is a feature-rich, AI-powered Smart Travel Planner and Companion Android application designed to elevate every step of your journey—from itinerary planning and budget forecasting to live navigation, AI chatbot assistance, and real-time worldwide weather tracking.

---

## 🚀 Key Features

### 🗺️ Smart Trip Planning & Management
- **Interactive Trip Planner**: Create customized day-by-day itineraries with custom destinations, dates, and activity notes.
- **Trip Dashboard & Tracking**: View upcoming, active, and past trips with rich card previews and status tags.
- **Offline & Cloud Sync**: Local SQLite persistence with Room Database and real-time cloud synchronization via Firebase Firestore.

### ☀️ Real-Time Worldwide Weather Forecast
- **Instant Live Weather**: Real-time conditions including temperature, "feels like", humidity, wind speed, visibility, and UV index.
- **Worldwide City Search**: Instant search for any city, country, or landmark across the globe with automated geocoding.
- **5-Day Visual Forecast**: Daily weather breakdown with custom weather condition badges (Clear, Rain, Storms, Snow, Clouds).
- **Auto-Suggestions**: Quick-access tags for popular travel hubs (Tokyo, Paris, New York, London, Bali, Mumbai, Dubai, Sydney, Rome, Singapore).

### 🤖 AI Travel Assistant & Smart Alerts
- **On-Device AI Chatbot**: Conversational travel assistant to answer questions, suggest packing lists, and recommend local attractions.
- **AI Smart Alerts Engine**: Proactive travel insights, packing reminders, departure notices, and transit warnings tailored to trip timelines.
- **ML Budget Predictor**: Machine learning-based cost estimates and expense projections based on travel style, duration, and destination.

### 🧭 Navigation & Media Tools
- **Campus & City Navigation**: Interactive waypoint routing, step-by-step directions, distance calculations, and real-time travel duration estimates.
- **Built-in Camera & Gallery**: Capture trip memories on the go with built-in camera integration and dedicated photo galleries.
- **ML Image Classifier**: Recognize scenes and landmarks using on-device machine learning image processing.

### 👤 Profile & Personalization
- **User Authentication**: Secure Login & Registration powered by Firebase Authentication and local Room fallback.
- **Travel Stats & Badges**: Personal travel statistics tracking visited countries, completed trips, and saved bucket-list destinations.
- **Modern Material Design**: Polished UI with custom gradients, rounded cards, micro-animations, and unified bottom navigation.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Java / Android SDK (Min SDK 24, Target SDK 34)
- **Architecture**: MVVM / Clean Architecture Principles
- **Local Persistence**: Room Database (SQLite), Android SharedPreferences
- **Cloud Backend**: Firebase Authentication, Cloud Firestore
- **Networking & API**: OkHttp3, Retrofit2, Open-Meteo REST API
- **Machine Learning**: TensorFlow Lite / ML Kit
- **UI & Layouts**: Android Material Components (Material 3), ConstraintLayout, CardView, ViewPager2

---

## 📥 Getting Started & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android Device or Emulator running Android 7.0 (API 24) or higher

### Setup Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/Mayuresh2025/Android-App-Dev-Lab.git
   cd Android-App-Dev-Lab
   ```

2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select **Open** and choose the `Voyageur` folder.

3. **Sync Gradle:**
   - Wait for Gradle to download all required dependencies.

4. **Run the App:**
   - Select your target device or emulator.
   - Click **Run** (`Shift + F10`) or press the green Play button.

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
