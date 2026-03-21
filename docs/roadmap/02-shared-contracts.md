# Шаг 02. Shared Contracts

## Цель

Подготовить общие Kotlin contracts для взаимодействия между `server` и `desktop-admin`, а также зафиксировать границу между client API и admin API.

## Объем работ

### 1. Определить пакеты contracts

В `shared` нужно ввести или расширить пакеты:
- `com.morningalarm.api.admin.*`
- `com.morningalarm.dto.admin.*`
- `com.morningalarm.dto.ringtone.*`
- `com.morningalarm.dto.upload.*` при необходимости

### 2. Разделить client и admin contracts

Нужно отдельно описать:
- клиентские endpoints для приложения
- admin endpoints для desktop-админки

Admin contracts не должны смешиваться с обычными клиентскими сценариями.

### 3. Подготовить DTO для рингтонов

Нужно заложить контракты минимум для:
- списка рингтонов для клиента
- списка рингтонов для админа
- деталей рингтона
- создания рингтона
- обновления рингтона
- удаления рингтона
- переключения `isActive`
- переключения `isPremium`
- preview-данных карточки

### 4. Подготовить DTO для загрузки медиа

Нужно предусмотреть:
- upload image
- upload audio
- ответ с абсолютным URL и метаданными файла

## Требования к модели `Ringtone`

Нужно описать полный целевой контракт рингтона:
- `id`
- `title`
- `description`
- `imageUrl`
- `audioUrl`
- `durationSeconds`
- `isActive`
- `isPremium`
- `likesCount`
- `isLikedByUser`
- `createdAt`
- `updatedAt`

Если UI карточки требует отдельных данных для превью, допускается отдельная preview DTO.

## Результат шага

В `shared` появляется полный набор контрактов для:
- client ringtone API
- admin ringtone API
- upload API
- auth/access данных, нужных desktop-admin

## Критерии приемки

- все серверные и desktop-admin DTO, используемые в интеграции, живут в `shared`
- routes/constants также живут в `shared`
- `shared/AGENTS.md` обновлен и документирует новые пакеты и модели
