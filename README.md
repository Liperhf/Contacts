# ContactsApp



## Описание

ContactsApp — это Android-приложение, которое позволяет:
- Просматривать список контактов
- Искать контакты по имени
- Удалять дублирующиеся контакты
- Совершать звонки

Вся работа с контактами реализована через отдельный сервис с использованием AIDL для межпроцессного взаимодействия.

---

## Архитектура

- **Jetpack Compose** — для построения UI
- **ViewModel** — для управления состоянием и логикой
- **AIDL + Service** — для работы с контактами через отдельный сервис
- **ContentResolver** — для доступа к системным контактам

---

## Как запустить

1. Клонируйте репозиторий:
   ```
   git clone https://github.com/Liperhf/Contacts.git
   ```

2. Откройте проект в Android Studio.

3. Соберите и запустите приложение на устройстве или эмуляторе.

4. При первом запуске разрешите доступ к контактам и звонкам.

---

## Требуемые разрешения

- `READ_CONTACTS` — для чтения контактов
- `WRITE_CONTACTS` — для удаления контактов
- `CALL_PHONE` — для совершения звонков из приложения

---

## Стек технологий

- Kotlin
- Jetpack Compose
- AIDL
