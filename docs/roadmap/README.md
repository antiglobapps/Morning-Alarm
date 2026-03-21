# Morning Alarm Roadmap

Этот каталог содержит пошаговый технический roadmap для развития:
- публичного маркетингового сайта `web`
- локальной desktop-админки `desktop-admin`
- серверных admin/client API
- модели безопасности, загрузки файлов и recovery-процессов

Каждый шаг оформлен как отдельное ТЗ и может реализовываться как самостоятельный change set.

Порядок выполнения:
1. [Шаг 01. Project Structure](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/01-project-structure.md)
2. [Шаг 02. Shared Contracts](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/02-shared-contracts.md)
3. [Шаг 03. Ringtone Domain And Admin API](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/03-ringtone-domain-and-admin-api.md)
4. [Шаг 04. Media Storage And Uploads](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/04-media-storage-and-uploads.md)
5. [Шаг 05. Admin Bootstrap And Auth Hardening](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/05-admin-bootstrap-and-auth-hardening.md)
6. [Шаг 06. Desktop Admin App](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/06-desktop-admin-app.md)
7. [Шаг 07. Marketing Web Site](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/07-marketing-web-site.md)
8. [Шаг 08. Security Recovery And Audit](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/08-security-recovery-and-audit.md)
9. [Шаг 09. OpenAPI Tests And CI](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/09-openapi-tests-and-ci.md)
10. [Шаг 10. Release And Operations](/mnt/c/work/pet/Morning-Alarm/docs/roadmap/10-release-and-operations.md)

Общие правила для реализации всех шагов:
- любые изменения API должны сопровождаться обновлением OpenAPI schema
- любые новые сущности, экраны, пакеты и endpoint'ы должны быть отражены в соответствующих модульных `md`
- DTO и route contracts, используемые сервером и Kotlin-клиентами, должны жить в `shared`
- миграции БД не писать до отдельной команды подготовки к релизу
