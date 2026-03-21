# Шаг 09. OpenAPI Tests And CI

## Цель

Зафиксировать дисциплину контрактов, тестирования и CI-проверок для новых `web`, `desktop-admin` и server/admin API.

## Объем работ

### 1. OpenAPI discipline

Нужно описать и реализовать правило:
- любое изменение API меняет OpenAPI schema в том же change set

Нужно расширить schema:
- admin auth / access policy
- admin ringtone CRUD
- media upload endpoints
- visibility rules и ошибки

### 2. Server test coverage

Нужно покрыть:
- admin endpoints
- upload flows
- visibility rules (`isActive`, `isPremium`)
- client/admin separation
- recovery and bootstrap flows

### 3. Desktop-admin checks

Нужно добавить минимум:
- build check
- smoke test или UI-state test
- API client checks

### 4. Web checks

Нужно добавить минимум:
- build check
- route availability check
- базовые SEO smoke checks

### 5. CI pipeline

Нужно собрать pipeline, который проверяет:
- сборку server
- тесты server
- OpenAPI validation
- сборку desktop-admin
- сборку web
- тесты и сборку standalone `design-app`

## Текущая реализация

GitHub Actions workflow находится в `.github/workflows/ci.yml`.

Он запускается:
- на каждом `pull_request`
- на `push` в `main`
- на `push` в `release/*`

Workflow проверяет:
- path-based selective CI для измененных модулей
- полный suite для `pull_request` в `release/*`
- `./gradlew :shared:check :server:check` и отсутствие незакоммиченных изменений в OpenAPI artifacts после server checks
- `desktop-admin`: isolated local dev server + live smoke test against real admin API + `./gradlew :desktop-admin:check`
- `web`: `npm ci`, `npm run test --if-present`, `npm run build`
- `design-app`: `npm ci`, `npm run test --if-present`, `npm run build`

Для server-driven клиентских проверок каждый клиентский job поднимает собственный локальный dev server, ждет `/health/ready` и заполняет данные через API самого сервера, а не через прямые DB-хуки.

## Результат шага

Изменения в API, админке и сайте перестают быть невалидируемыми вручную и проходят через обязательные автоматические проверки.

## Критерии приемки

- CI запускает build/test/validation для всех релевантных модулей
- OpenAPI validation обязательна
- server tests покрывают все значимые сценарии для новых endpoints
