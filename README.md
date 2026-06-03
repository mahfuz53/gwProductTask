# GW Tasks

Android client for Odoo task and user management. GW Tasks lets you sign in, view your tasks on a dashboard, create and update tasks, and edit your profile name.

## Features

- **Login** — Authenticate with Odoo credentials 
            - UserEmail: mahfuz53@gmail.com
            - Password : Mahfuz@123
- **Dashboard** — View profile summary and task list with status chips
- **Task management** — Open a task to view details and update its stage
- **Create Task** — Add a new task with name, description, and deadline
- **Update Task** — Change task stage (Pending, In Progress, Completed)
- **Profile update** — Tap the profile card to edit and save your display name

## Architecture

The app follows **MVVM** with a clean layered structure:

- **Presentation** — Jetpack Compose screens, ViewModels, UI state/events
- **Domain** — Use cases and repository interfaces
- **Data** — Odoo JSON-RPC API, repository implementations, DTO mappers

Dependency injection uses **Hilt**. Networking uses **Retrofit** and **Coroutines**.

## Tech stack

- Kotlin
- Jetpack Compose & Material 3
- Navigation Compose
- Hilt
- Retrofit / Gson
- DataStore (session)

## Getting started

1. Open the project in Android Studio
2. Sync Gradle
3. Run the `app` configuration on a device or emulator (min SDK 29)

Configure the Odoo base URL and database in the app’s network/session setup before connecting to your instance.
