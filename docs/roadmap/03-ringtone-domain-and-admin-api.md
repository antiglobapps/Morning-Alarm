# Шаг 03. Ringtone Domain And Admin API

## Цель

Довести серверную модель `Ringtone` до состояния, пригодного для контент-менеджмента из админки, и ввести полноценный admin API.

## Объем работ

### 1. Расширить доменную модель рингтона

Серверная модель должна поддерживать:
- `title`
- `description`
- `imageUrl`
- `audioUrl`
- `durationSeconds`
- `isActive`
- `isPremium`
- `createdAt`
- `updatedAt`
- `createdByAdminId`
- `updatedByAdminId`

### 2. Разделить клиентские и админские сценарии

Клиентское приложение:
- видит только активные рингтоны
- получает `isLikedByUser`
- получает `likesCount`
- получает premium-доступ по отдельным правилам

Админка:
- видит все рингтоны
- видит статус `isActive`
- видит статус `isPremium`
- видит количество лайков
- умеет создавать, редактировать, удалять

### 3. Ввести admin endpoints

Нужно реализовать минимум:
- `GET /api/v1/admin/ringtones`
- `GET /api/v1/admin/ringtones/{id}`
- `POST /api/v1/admin/ringtones`
- `PUT /api/v1/admin/ringtones/{id}`
- `DELETE /api/v1/admin/ringtones/{id}`

Опционально можно выделить отдельные endpoints для частичных переключений:
- `POST /api/v1/admin/ringtones/{id}/activate-toggle`
- `POST /api/v1/admin/ringtones/{id}/premium-toggle`

### 4. Добавить endpoint пользовательского представления списка

Нужен admin-способ посмотреть текущий список рингтонов так, как он виден обычному пользователю.

Допустимые варианты:
- отдельный admin endpoint preview/list
- server-side режим response preview для админа

### 5. Добавить preview карточки рингтона

Админка должна уметь получать данные для локального preview карточки.
Если preview полностью строится на клиенте, сервер должен отдавать достаточный набор данных для этой карточки.

## Ограничения безопасности

- все admin endpoints доступны только пользователю с ролью `ADMIN`
- отсутствие админской роли должно давать `403`
- клиентские endpoints не должны позволять изменять рингтоны

## Результат шага

Сервер предоставляет полную API-базу для управления рингтонами из desktop-admin.

## Критерии приемки

- admin CRUD для рингтонов реализован
- клиентский ringtone API фильтрует неактивные записи
- premium/common флаг участвует в контракте
- OpenAPI обновлен
- тесты покрывают success, validation, auth, forbidden, not found и visibility rules
