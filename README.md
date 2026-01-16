# 💰 Personal Finance Manager

![Project Banner](file:///C:/Users/HP/.gemini/antigravity/brain/509436e5-e176-4fb1-8c64-40515fd7735c/finance_app_banner_1768597595508.png)

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![Language](https://img.shields.io/badge/Language-Java_17-orange.svg)](https://www.oracle.com/java/)
[![DB](https://img.shields.io/badge/Database-Raw_SQLite-blue.svg)](https://www.sqlite.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A robust, offline-first Android application designed to empower users with full control over their financial health. Built with a focus on clean architecture, high-performance local storage, and high-fidelity custom data visualization.

---

## 🌟 Key Features

### 📊 Financial Dashboard
- **Real-time Totals**: Instant visibility into Total Income, Expense, and Net Balance.
- **2026 Ready**: All date logic is projected for a futuristic user experience (defaults to **January 2026**).

### 💸 Transaction Management
- **Full CRUD**: Add, edit, and delete income/expense entries.
- **Smart Filtering**: Categorize transactions with dynamic filtering based on transaction type.

### 🎯 Strategic Budgeting
- **Monthly Limits**: Set spending boundaries for specific categories (Ex: Food, Fuel).
- **Automated Alerts**: Visual progress bars change color (Green -> Orange -> Red) based on customized alert thresholds.

### 🏆 Savings Goals
- **Milestone Tracking**: Monitor progress towards long-term savings goals.
- **Interactive Progress**: Detailed view of saved vs. remaining amounts with percentage completion.

### 📈 Advanced Reporting
- **Period Selection**: Generate reports for Daily, Weekly, Monthly, or Yearly ranges.
- **Category Breakdown**: Deep dive into where your money goes with category-specific totals.

---

## 🏗️ Technical Architecture (MVVM)

The project follows the **Model-View-ViewModel** structure combined with the **Repository Pattern** to ensure a separation of concerns and maximum testability.

### 1. Data Layer (Raw SQLite)
We've implemented a "Normal" SQLite pattern via `DatabaseHelper`, avoiding heavy ORM abstractions like Room for maximum performance and SQL transparency.

```mermaid
erDiagram
    USERS ||--o{ TRANSACTIONS : "owns"
    USERS ||--o{ BUDGETS : "sets"
    USERS ||--o{ GOALS : "tracks"
    CATEGORIES ||--o{ TRANSACTIONS : "classifies"
    CATEGORIES ||--o{ BUDGETS : "targets"

    USERS {
        string email PK
        string firstName
        string lastName
        string password
    }
    TRANSACTIONS {
        int id PK
        string user_email FK
        double amount
        long date
        int category_id FK
        string description
    }
    CATEGORIES {
        int id PK
        string user_email FK
        string type
        string name
    }
```

### 2. Repository Layer
Acts as the **Source of Truth**, mapping raw `Cursor` results from SQLite into clean Java POJOs. It handles all background threading logic to keep the UI smooth.

### 3. ViewModel Layer
Leverages `LiveData` to push data updates reactively to the Fragments.

---

## 🛠️ Development Stack

- **Tech**: Java 17, Android Studio Ladybug.
- **Storage**: Handwritten SQL queries in `DatabaseHelper`.
- **UI**: Material Design 3, ViewBinding, and ConstraintLayouts.
- **Charts**: Custom-rendered Pie and Bar charts using the Android Canvas API.

## 📂 Project Structure

```text
app/src/main/java/.../
├── data/           # SQLite Schema and POJO models
├── repository/     # Data mapping and logic abstraction
├── ui/             # Fragments and ViewModels
└── utils/          # Formatting and calculation helpers
```

## 🚀 How to Run

1. Clone the repository.
2. Open in **Android Studio**.
3. Sync Gradle and ensure SDK level 26+ is installed.
4. Click **Run** on any Emulator or Physical Device.

---
*Created as a course project for ENCS5150 - Advanced Software Development.*

