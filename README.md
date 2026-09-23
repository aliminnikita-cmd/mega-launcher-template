# Mega Launcher Template

Многофункциональный Android-лаунчер на Kotlin + Jetpack Compose.

## Структура проекта

- `app/src/main/java/com/megalauncher/` — исходный код
  - `core/` — тема, общие утилиты
  - `data/` — Room, репозитории, системные штуки
  - `domain/` — модели и интерфейсы репозиториев
  - `ui/` — экраны на Compose
- `app/src/main/res/` — ресурсы
- `app/build.gradle.kts` — зависимости модуля

## Сборка

APK собирается через GitHub Actions. Артефакт — `MegaLauncher-debug`.
