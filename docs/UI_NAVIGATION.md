# UI: навігація та overlay

## Архітектура

Один **`Stage`**, один **`Scene`**, керується **`SceneCoordinator`**:

- **contentHost** — поточний екран (login, menu, game, …) замінюється через `setContent()` / `loadContent()`
- **overlayHost** — панелі поверх екрану (settings, register, повідомлення)

Нові вікна (`Stage`) і модальні `Alert` для навігації **не використовуються** (де оновлено).

## Адаптивність

- Login / menu: `ScrollPane` + `maxWidth` на контенті
- Menu: фон `ImageView` прив’язаний до `menuRoot.width/height`
- Контент: `maxWidth` / `maxHeight` прив’язані до розміру вікна

## Settings

- Кнопка **Settings** у головному меню
- Overlay: `/fxml/settings_overlay.fxml`
- Налаштування в пам’яті: `AppSettings` (звук, гучність, підказки в грі, мова)

## Файли

| Файл | Роль |
|------|------|
| `SceneCoordinator.java` | Навігація + overlay |
| `AppSettings.java` | Налаштування |
| `SettingsController.java` | UI settings |
| `app.css` | Стилі overlay |
| `settings.css` | Панель settings |
