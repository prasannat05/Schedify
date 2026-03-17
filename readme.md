# Schedify – Ultimate Study Planner

<p align="center">
  <b>A constraint-based study planner that optimizes schedules and improves productivity</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue?style=for-the-badge">
  <img src="https://img.shields.io/badge/Database-SQLite-orange?style=for-the-badge">
  <img src="https://img.shields.io/badge/UI-Dark%20Glow-black?style=for-the-badge">
</p>

## Screenshots

<p align="center">
  <img src="assets/home.png" width="250"/>
  <img src="assets/subject.png" width="250"/>
  <img src="assets/dashboard.png" width="250"/>
</p>

<p align="center">
  <img src="assets/efficiency.png" width="250"/>
  <img src="assets/settings.png" width="250"/>
</p>

## Features

### Three-Tier Navigation
- Home Screen – Quick and clean entry point  
- Subject Manager – Add subjects with:
  - Weekly study hours  
  - Difficulty level (1–5)  
  - Priority (Low, Medium, High)  
- Dynamic Dashboard – Auto-generated weekly schedule  

### Smart Scheduling Algorithm

Load = Weekly Hours × Difficulty × Priority Multiplier

**Priority Multipliers:**
- High → 1.6  
- Medium → 1.3  
- Low → 1.0  

### Constraint-Based Planning
- Daily Study Goal  
- Max Subjects Per Day  
- No repetition of the same subject per day  

Ensures balanced workload and prevents burnout.

### Efficiency Tracking

Efficiency (%) = (Completed Sessions / Total Sessions) × 100

- Real-time updates  
- Animated progress bar  
- Continuous productivity feedback  

### Persistent Storage (SQLite)
- Subjects  
- Schedule  
- Completion status  
- User configurations  

### Modern UI – Dark Glow Theme
- Minimal dark interface  
- Smooth transitions and animations  
- Clean and focused design  

## Installation

To install the application:

1. Click the link below  
2. Download the APK  
3. Install on your Android device  

**Download Schedify**  
https://tinyurl.com/Schedify  

> Enable "Install from Unknown Sources" in your device settings.

## Tech Stack

| Category       | Technology                          |
|---------------|------------------------------------|
| Language      | Kotlin                             |
| Database      | SQLite                             |
| UI            | XML and Dynamic Views              |
| Animations    | ObjectAnimator, TransitionManager  |
| Architecture  | Modular Android Design             |

## How It Works

1. Add subjects with preferences  
2. Define study constraints  
3. Generate optimized schedule  
4. Track daily completion  
5. Improve efficiency over time  

## Use Cases

- Exam preparation  
- Time management  
- Productivity tracking  
- Structured study planning  

## Future Enhancements

- Graphs and analytics  
- Cloud synchronization (Firebase)  
- Smart reminders and notifications  
- AI-based scheduling improvements  
- Pomodoro timer integration  

## License

This project is licensed under the MIT License.

## Author

Prasanna T
