# GW Tasks

**A modern Android app for managing Odoo tasks and your account — built with Kotlin and Jetpack Compose.**

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Screenshots](#screenshots)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Odoo API Integration](#odoo-api-integration)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)

---

## Overview

**GW Tasks** is a mobile client for [Odoo](https://www.odoo.com/) that helps you stay on top of your work from your phone. Sign in with your Odoo account, browse tasks on a clean dashboard, create new work items, update task status, and keep your profile name up to date.

The app is designed for clarity and speed: focused screens, clear feedback (loading, success, errors), and a consistent purple Material theme throughout.

---

## Key Features

| Area                 | What you can do |
|----------------------|-----------------|
| **Login**            | Sign in with email and password (with show/hide password toggle) |
| **Login Credential** |UserID : **`mahfuz53@gmail.com`** , password : **Mahfuz@123**
| **Dashboard**        | See your profile card, task count, and tasks with status chips (Pending, In Progress, Completed) |
| **Task details**     | Tap a task to view title, deadline, and update its stage |
| **Create task**      | Add a task with name, description, and deadline date |
| **Update task**      | Change only the task stage via a status dropdown |
| **Profile**          | Tap the profile card to open **Update Account** and edit your display name |

**Also included**

- Pull-to-refresh style reload from the dashboard menu  
- Session persistence so you stay logged in  
- Snackbar messages for success and errors  
- Floating action button to create tasks quickly  

---

## Screenshots

_Add screenshots here when available._

| Login | Dashboard | Update Task |
|:-----:|:---------:|:-----------:|
| _Coming soon_ | _Coming soon_ | _Coming soon_ |

| Create Task | Update Account |
|:-----------:|:--------------:|
| _Coming soon_ | _Coming soon_ |

---

## Architecture

GW Tasks uses **MVVM (Model–View–ViewModel)** and a simple **clean architecture** split:

```
┌─────────────────────────────────────┐
│  UI (Compose Screens)               │
│  Observes StateFlow, sends Actions  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  ViewModel                          │
│  UI state, validation, navigation   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Domain (Use Cases + Repository)    │
│  Business rules, no Android APIs    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Data (Repository impl, API, DTOs)  │
│  Odoo JSON-RPC over Retrofit        │
└─────────────────────────────────────┘
```

**In practice**

- **Presentation** — Compose UI, `UiState` / `UiAction` / `UiEvent`, Hilt ViewModels  
- **Domain** — Use cases (login, load dashboard, create/update task, update profile)  
- **Data** — `OdooRepository`, request builders, DTO mappers, encrypted session storage  

State is driven by **Kotlin Flow** (`StateFlow` for UI, `SharedFlow` for one-off events like navigation).

---

## Tech Stack

| Category | Tools |
|----------|--------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Use Cases, Repository pattern |
| DI | Hilt |
| Networking | Retrofit, Gson, OkHttp |
| Async | Coroutines, Flow |
| Navigation | Navigation Compose |
| Storage | DataStore, EncryptedSharedPreferences (session) |
| Min SDK | 29 |

---

## Odoo API Integration

The app talks to Odoo through **JSON-RPC 2.0** (`POST /jsonrpc`).

| Operation | Odoo model | Method |
|-----------|------------|--------|
| Login | `common` | `authenticate` |
| Load user | `res.users` | `search_read` |
| Load tasks | `project.task` | `search_read` |
| Load stages | `project.task.type` | `search_read` |
| Create task | `project.task` | `create` |
| Update task stage | `project.task` | `write` |
| Update user name | `res.users` | `write` |

Requests use the standard `execute_kw` shape: database, user id, password, model, method, and arguments. Session credentials are stored securely after login for later calls.

**Configuration** (in `OdooConstants.kt`):

- Base URL: `https://revere.odoo.com/`
- Database: `revere`

Update these values to point at your own Odoo instance before building for production.

---

## Getting Started

### Prerequisites

- Android Studio (recent stable version recommended)  
- JDK 17  
- Android device or emulator (API 29+)

### Run the app

1. Clone the repository  
   ```bash
   git clone <your-repo-url>
   cd gwtasks
   ```

2. Open the project in **Android Studio**

3. **Sync Gradle** and wait for dependencies to resolve

4. Select the **app** run configuration

5. Run on a device or emulator

6. Sign in with valid **Odoo credentials** for your configured database

> **Note:** If login fails, confirm the base URL and database name in `app/src/main/java/com/gwproductsusa/gwtasks/core/util/OdooConstants.kt` match your Odoo environment.

### Build from command line

```bash
./gradlew assembleDebug
```

Debug APK output: `app/build/outputs/apk/debug/`

---

## Project Structure

```
app/src/main/java/com/gwproductsusa/gwtasks/
├── core/           # DI, network, session, errors, logging
├── data/           # API, DTOs, mappers, repository implementations
├── domain/         # Models, repository interfaces, use cases
├── presentation/   # Screens, ViewModels, navigation
│   ├── login/
│   ├── dashboard/
│   ├── createtask/
│   ├── updatetask/
│   ├── updateaccount/
│   └── navigation/
└── ui/theme/       # Colors, typography, theme
```

**GW Tasks** — Odoo task management, simplified for mobile.
