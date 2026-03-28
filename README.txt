# 🎮 Gamifying Tasks 
**A Full-Stack Task Management System with Gamification Elements**

## 📖 Introduction
[cite_start]This application is designed for users who want to manage daily tasks while staying motivated through levels, ranks, streaks, and achievements[cite: 4]. [cite_start]The goal is to transform task management into an engaging experience[cite: 5].

## 🏗️ Architecture & Tech Stack
[cite_start]The system follows a **layered architecture**, separating domain, service, and persistence logic[cite: 18].
* [cite_start]**Backend:** Java (OOP, JDBC for database access) [cite: 14]
* [cite_start]**Frontend:** React (Modern Web Interface) — *Evolved from the original CLI design [cite: 15]*
* [cite_start]**Database:** Oracle SQL (Users, Tasks, Achievements tables) [cite: 16]
* [cite_start]**Persistence:** Supports both File-based (TXT) and Oracle Database storage [cite: 17]

## 🏆 Core Features
* [cite_start]**User Roles:** Supports **ADMIN** (user management) and **PLAYER** (task focus) roles[cite: 7, 44].
* [cite_start]**Task Management:** Create, edit, delete, and filter tasks by difficulty and category[cite: 8, 51].
* [cite_start]**Gamification:** Earn points to progress through **Bronze, Silver, and Gold** ranks[cite: 9, 52].
* [cite_start]**Persistence:** Data is saved across sessions using Oracle SQL or text files[cite: 53].

## 🛠️ Setup
1. **Database:** Run the scripts in `/db` on your Oracle instance.
2. **Backend:** Run `GamifyingTasksApplication.java`.
3. **Frontend:** Navigate to `/Frontend`, run `npm install` and `npm run dev`.