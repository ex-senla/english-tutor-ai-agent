# Релизная политика

> Назначение: зафиксировать, как код попадает в `master`, как идентифицируются прод- и
> тест-релизы, как выполняется переход test→prod и откат прода. Прод-пайплайн в рамках
> этой политики **не автоматизируется** (см. «Переход test→prod»).

## 1. Ветвление (trunk-based)

- Единственная долгоживущая ветка — `master`.
- Вся работа ведётся в короткоживущих фича-ветках вида `feat/<issue>-<slug>`
  (например, `feat/47-example`).
- Прямые push в `master` **запрещены** (branch protection). Любое изменение попадает в
  `master` только через pull request.
- После мержа фича-ветка удаляется.

## 2. Мерж (PR + squash)

- PR в `master` обязан ссылаться на issue.
- Мерж выполняется **только squash-мержем** — один коммит на PR, история `master` линейная.
- Перед мержем обязателен проходящий CI-гейт: job `build` из
  `.github/workflows/test-deploy.yml` (`./mvnw -B verify`). Упавший `build` блокирует мерж.
- Force push и прямые коммиты в `master` запрещены branch protection.

## 3. Версионирование

- **Прод-релиз** = аннотированный git-тег `v<major>.<minor>.<patch>` (semver) на коммите
  в `master`, созданный **вручную**.
- **Тестовое окружение** = rolling: на каждый push в `master` собирается Docker-образ с
  тегом, равным SHA коммита, и деплоится в тест.
- Версия в `pom.xml` **всегда** остаётся `0.0.1-SNAPSHOT` и не связана с релизами.
- Образы immutable: тег никогда не перезаписывается.

## 4. Переход test→prod (ручной)

- Тестовое окружение проверяется вручную (тестовый бот отвечает, данные корректны).
- После ручной проверки владелец создаёт аннотированный тег `vX.Y.Z` на текущем коммите
  `master`.
- Прод-деплой выполняется **вручную** (по тегу), вне пайплайна #46. Автоматического
  продвижения в прод не существует.

## 5. Откат прода

- Откат = повторный деплой предыдущего immutable-тега `vX.Y.Z` (например, вернуть
  `v1.0.2` вместо дефектного `v1.1.0`).
- Отдельных rollback-артефактов не создаётся: теги не перезаписываются, поэтому
  повторный деплой возвращает прода в известное состояние.

## 6. Где живут секреты

Секреты хранятся **вне репозитория** и разделены по назначению:

| Место | Что хранит | Кто использует |
|---|---|---|
| GitHub Secrets (Environment `test`) | `TEST_HOST`, `TEST_USER`, `TEST_SSH_KEY`; GHCR-login — встроенный `GITHUB_TOKEN` | только CI: login в GHCR и SSH-деплой |
| `/opt/eta-test/.env` на VPS (вне git, права 600) | `TELEGRAM_BOT_TOKEN`, `TELEGRAM_BOT_USERNAME`, `AI_API_KEY`, `AI_BASE_URL`, `AI_CHAT_MODEL`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | compose передаёт их в контейнеры при старте |

- Секреты приложения workflow **не читает и не синхронизирует**: scope SSH-деплоя строго
  `docker compose pull` + `docker compose up -d`.
- В репозитории — только примеры с плейсхолдерами (`.env.test.example`, `.env.example`).
- Секреты никогда не появляются в логах workflow и не запекаются в Docker-образ
  (`.dockerignore` исключает `.env*`, `target/`, `.git/` и т.д.).

## 7. Чеклист ручной настройки (однократно, вне git)

### GitHub

- [ ] Включить branch protection на `master`: require PR, require status check `build`,
      squash-merge only, запрет force push и прямых push.
- [ ] Создать GitHub Environment `test` и добавить CI-секреты:
      `TEST_HOST`, `TEST_USER`, `TEST_SSH_KEY`. (GHCR-login использует встроенный
      `GITHUB_TOKEN` с `packages: write` — отдельный секрет не нужен.)
- [ ] Настроить GHCR-пакет `ghcr.io/ex-senla/english-tutor-ai-agent` (приватный или
      публичный — на выбор владельца; в образе секретов нет).

### Тестовый VPS

- [ ] Linux VPS с публичным SSH, установить Docker и Compose v2.
- [ ] Создать каталог `/opt/eta-test` и разместить в нём `docker-compose.test.yml`
      (при изменении compose-файла в репозитории — обновить копию на VPS вручную).
- [ ] Создать `/opt/eta-test/.env` (права 600, вне git) с app-секретами по шаблону
      `.env.test.example`.
- [ ] Настроить `docker login ghcr.io` на VPS с PAT `read:packages` (если GHCR-пакет
      приватный) — выполняется от имени `TEST_USER` (того же пользователя, под которым
      SSH-деплой делает `docker compose pull`).
- [ ] Firewall: открыть только SSH (порты 5432/8080 наружу не публиковать).

### Тестовый бот и AI

- [ ] Создать отдельный тестовый Telegram-бот через @BotFather; его токен и username
      записать в `.env` на VPS (`TELEGRAM_BOT_TOKEN`, `TELEGRAM_BOT_USERNAME`).
- [ ] Подготовить отдельный тестовый AI-ключ (`AI_API_KEY`, `AI_BASE_URL`, `AI_CHAT_MODEL`).

## 8. Как это работает

- `pull_request` → `build` (`./mvnw -B verify`), `deploy` пропущен.
- `push` в `master` → `build` → `deploy`: образ `ghcr.io/ex-senla/english-tutor-ai-agent:<sha>`
  → push в GHCR → SSH на VPS → `docker compose pull && up -d`.
- `workflow_dispatch` → то же, что push (ручной повторный деплой, идемпотентен).

Деплой выполняется как stop старого контейнера → start нового (long-polling допускает
ровно один живой инстанс на токен; без blue-green).
