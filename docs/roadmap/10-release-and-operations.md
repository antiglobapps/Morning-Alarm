# Шаг 10. Release And Operations

## Цель

Подготовить проект к управляемому deploy и эксплуатации после реализации сайта, desktop-admin и расширенного backend.

## Объем работ

### 1. Разделить контуры поставки

Нужно явно разделить:
- deploy `server`
- deploy `web`
- distribution `desktop-admin`

### 2. Подготовить конфиг окружений

Нужно описать:
- локальную разработку
- staging
- production

Отдельно должны настраиваться:
- DB
- JWT secrets
- admin bootstrap secret
- storage provider
- public URLs

### 3. Подготовить release-процесс

Нужно определить:
- как публикуется `web`
- как обновляется `server`
- как распространяется `desktop-admin`
- как проверяется совместимость contracts

### 4. Подготовить monitoring baseline

Минимум:
- health/readiness
- request tracing
- admin action audit access
- базовые alerts на auth/admin failures

### 5. Зафиксировать будущий переход к migrations

До отдельной release-команды миграции не пишутся.
Но на этом этапе нужно подготовить решение, как проект перейдет от bootstrap schema initialization к нормальному migration process.

## Результат шага

Проект получает понятную схему выпуска и эксплуатации трех отдельных deliverables:
- backend
- marketing site
- desktop-admin

## Критерии приемки

- документированы release-потоки для всех deliverables
- секреты и окружения разделены
- описан путь перехода к production operations и migration tooling
