# Модуль support

Ограниченный контекст обратной связи «учитель → поддержка».

## Модель

- `SupportThread` (агрегат): `SupportThreadId`, `TeacherId` (логическая ссылка на `teacher`), `SupportThreadStatus` (v1 — только `OPEN`), `createdAt`, `lastMessageAt`, список `SupportMessage`.
- `SupportMessage` (сущность агрегата): `SupportAuthor` (`TEACHER`/`ADMIN`), `text`, `createdAt`.
- Инвариант «один открытый тред на учителя»: домен делает get-or-create, БД страхует частичным уникальным индексом `uq_support_thread_teacher_open ON (teacher_id) WHERE status='OPEN'`.

## Публичные порты (`api/support`)

- `OpenSupportThread.open(TeacherId)` — get-or-create открытого треда.
- `AppendSupportMessage.append(AppendSupportMessageCommand)` — добавляет сообщение и обновляет `lastMessageAt`.
- `FindSupportThreads.findAll()` / `findById(SupportThreadId)` — `findById` — источник `teacherId` для push-доставки ответа админа.
- `FindSupportMessages.findByThreadId(SupportThreadId)` — хронологическая история.

## Схема

Миграция `V2__admin_support.sql`:
- `support_thread(id, teacher_id без FK, status, created_at, last_message_at)`.
- `support_message(id, thread_id FK ON DELETE CASCADE, author, text, created_at)`.
- Сортировка списка: `COALESCE(last_message_at, created_at) DESC` — тред без сообщений не теряется и сортируется по `created_at`.

## Доставка

Ответ админа сохраняется транзакционно; push в чат учителя — best-effort через порт `chatbot.application.notification.BotNotifier` (адаптер `TelegramBotNotifier`). Адресат вычисляется по владельцу треда (`FindSupportThreads.findById → teacherId → TeacherDirectory.findTelegramChatId`), состояние чата учителя не читается и не пишется.
