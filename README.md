# GUE - Student Companion App

GUE is an Android app designed to help students manage their university life more easily. 
It helps you keep track of your classes, stay focused, and get rewards for attending lectures.

## Main Features

### Class Calendar
The app connects to your phone's calendar to show your upcoming lectures and events. 
You can see exactly where and when your next class is.

### Easy Attendance
GUE helps you track your attendance. It uses your location to check if you are actually at the 
lecture building, making it simple to prove you were there.

### Campus Map
Built-in map support helps you find your way to different buildings and rooms on campus. 
These buildings can be annotated by administrators. 

### App Blocking
To help you stay focused during class, the app can block distracting apps on your phone,
while you are in a lecture.

### Friends & Rewards
- Connect with your friends, see how you rank on the leaderboard, 
and collect digital rewards for being consistent with your attendance.
- Administrators are given the possibility to upload and modify collectibles.

## How it Works
1. **Login:** Create an account or sign in to save your progress.
2. **Permissions:** The app needs various permissions in order to function properly; 
- Location for the map functionality, 
- Access to calendar for correct importing, 
- Notifications for reminders, 
- App overlay and system processes for blocking functionality. 
3. **Notifications:** You will get reminders before your classes start so you're never late.

## Technical Details
- **Language:** Written in Java and Kotlin.
- **Database:** Uses Supabase for storing user data and attendance records.
- **Background Tasks:** Uses Android WorkManager to handle attendance checks.
