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

## Результат шага

Изменения в API, админке и сайте перестают быть невалидируемыми вручную и проходят через обязательные автоматические проверки.

## Критерии приемки

- CI запускает build/test/validation для всех релевантных модулей
- OpenAPI validation обязательна
- server tests покрывают все значимые сценарии для новых endpoints
