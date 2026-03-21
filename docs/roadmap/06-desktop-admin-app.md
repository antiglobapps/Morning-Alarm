# Шаг 06. Desktop Admin App

## Цель

Собрать первую рабочую desktop-админку на Compose Multiplatform Desktop для управления рингтонами.

## Объем работ

### 1. Базовый shell приложения

Нужно реализовать:
- entry point desktop-приложения
- базовую навигацию
- theme/styling
- конфигурацию server base URL

### 2. Экран авторизации

Нужно реализовать:
- login form
- secure session handling
- logout
- реакцию на session expiration

### 3. Список рингтонов

Нужно реализовать:
- таблицу или список всех рингтонов
- отображение `isActive`
- отображение `isPremium`
- отображение `likesCount`
- поиск и базовую фильтрацию

### 4. Создание и редактирование рингтона

Форма должна поддерживать:
- title
- description
- image upload / image URL
- audio upload / audio URL
- duration
- `isActive`
- `isPremium`

### 5. Preview карточки

Админка должна показывать preview, как карточка рингтона будет выглядеть у пользователя.

### 6. Клиентский view preview

Нужно иметь режим, где админ видит список рингтонов в виде, близком к пользовательскому клиенту.

## Архитектурные требования

- networking использует contracts из `shared`
- auth/session handling централизованы
- admin API client не дублирует route strings локально
- ошибки сервера обрабатываются через общую error model

## Результат шага

`desktop-admin` позволяет авторизоваться, загрузить медиа и полностью управлять каталогом рингтонов.

## Критерии приемки

- админ может создать, изменить и удалить рингтон из desktop-admin
- админ видит preview и список рингтонов
- админ видит количество лайков и статусы `isActive` / `isPremium`
- desktop-admin использует `shared` DTO и routes
