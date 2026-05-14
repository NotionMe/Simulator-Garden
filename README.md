<div align="center">

# 🌱 Garden Simulator

### Симулятор садівництва з ізометричною графікою

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Educational-green.svg)](LICENSE)

[Особливості](#особливості) • [Встановлення](#встановлення) • [Запуск](#запуск) • [Розробка](#розробка) • [Документація](#документація)

</div>

---

## 🎮 Особливості

```
🌿 Управління садами        Створюйте та керуйте віртуальними садами
🌺 Каталог рослин          Різноманітні види з унікальними характеристиками
🎯 Система завдань         Завдання по догляду за рослинами
🏆 Досягнення              Нагороди за успішну діяльність
🌤️ Погодні події          Динамічний вплив погоди на ріст
👤 Аутентифікація          Безпечна система користувачів
💾 Локальна база даних     SQLite для збереження прогресу
🎨 Сучасний інтерфейс      AtlantaFX dark theme
```

---

## 🛠 Технологічний стек

| Категорія | Технологія | Версія |
|-----------|-----------|--------|
| **Runtime** | Java JDK | 21 |
| **UI Framework** | JavaFX | 21.0.2 |
| **UI Theme** | AtlantaFX | 2.0.1 |
| **Database** | SQLite JDBC | 3.45.3.0 |
| **Migrations** | Flyway | 10.10.0 |
| **DI Container** | Google Guice | 7.0.0 |
| **Code Generation** | Lombok | 1.18.32 |
| **Testing** | JUnit 5 | 5.10.2 |
| **Build Tool** | Maven | 3.6+ |

---

## 📋 Системні вимоги

### Мінімальні

```
OS:     Windows 10/11, macOS 10.14+, Linux
CPU:    Dual-core 2.0 GHz
RAM:    2 GB вільної пам'яті
Disk:   500 MB вільного місця
Screen: 800×700 мінімальна роздільна здатність
```

### Для розробки

```
✓ JDK 21 або новіша
✓ Apache Maven 3.6+
✓ IDE (IntelliJ IDEA / Eclipse / VS Code)
```

### Для створення інсталяторів

```
Windows:  WiX Toolset 3.11+
macOS:    Xcode Command Line Tools
Linux:    fakeroot, dpkg
```

---

## 🚀 Швидкий старт

### 1️⃣ Клонування

```bash
git clone https://github.com/your-username/garden-simulator.git
cd garden-simulator
```

### 2️⃣ Перевірка середовища

```bash
# Перевірка Java
java -version
# Очікується: openjdk version "21.0.10" або новіша

# Перевірка Maven
mvn -version
# Очікується: Apache Maven 3.6+ або новіша
```

### 3️⃣ Встановлення залежностей

```bash
mvn clean install
```

### 4️⃣ Запуск

```bash
mvn javafx:run
```

---

## ▶️ Запуск

### Через Maven (рекомендовано)

```bash
# Запуск через JavaFX plugin
mvn clean javafx:run

# Запуск через Exec plugin
mvn clean compile exec:java
```

### Через JAR файл

```bash
# Збірка
mvn clean package

# Запуск
java -jar target/garden-simulator-1.0.0-jar-with-dependencies.jar
```

### Через IDE

<details>
<summary><b>IntelliJ IDEA</b></summary>

1. `File` → `Open` → Виберіть папку проєкту
2. Дочекайтеся завантаження Maven залежностей
3. Знайдіть `ua.notion.Launcher`
4. `Right Click` → `Run 'Launcher.main()'`

</details>

<details>
<summary><b>Eclipse</b></summary>

1. `File` → `Import` → `Maven` → `Existing Maven Projects`
2. Виберіть папку проєкту
3. Знайдіть `Launcher.java`
4. `Right Click` → `Run As` → `Java Application`

</details>

<details>
<summary><b>VS Code</b></summary>

1. Відкрийте папку проєкту
2. Встановіть `Extension Pack for Java`
3. Відкрийте `src/main/java/ua/notion/Launcher.java`
4. Натисніть `F5` або клікніть `Run` над `main()`

</details>

---

## 🔨 Збірка

### JAR з залежностями

```bash
mvn clean package
```

**Результат:** `target/garden-simulator-1.0.0-jar-with-dependencies.jar`

### Windows Installer (MSI)

```bash
# Через скрипт
build-windows.bat

# Або вручну
mvn clean package
jpackage --input target ^
  --name GardenSimulator ^
  --main-jar garden-simulator-1.0.0-jar-with-dependencies.jar ^
  --main-class ua.notion.Launcher ^
  --type msi ^
  --dest target/dist ^
  --app-version 1.0.0 ^
  --vendor Notion ^
  --win-menu ^
  --win-dir-chooser ^
  --win-shortcut
```

**Результат:** `target/dist/GardenSimulator-1.0.0.msi`

### macOS Installer (DMG)

```bash
mvn clean package
jpackage --input target \
  --name GardenSimulator \
  --main-jar garden-simulator-1.0.0-jar-with-dependencies.jar \
  --main-class ua.notion.Launcher \
  --type dmg \
  --dest target/dist \
  --app-version 1.0.0 \
  --vendor Notion
```

**Результат:** `target/dist/GardenSimulator-1.0.0.dmg`

### Linux Package (DEB)

```bash
mvn clean package
jpackage --input target \
  --name garden-simulator \
  --main-jar garden-simulator-1.0.0-jar-with-dependencies.jar \
  --main-class ua.notion.Launcher \
  --type deb \
  --dest target/dist \
  --app-version 1.0.0 \
  --vendor Notion \
  --linux-shortcut
```

**Результат:** `target/dist/garden-simulator_1.0.0_amd64.deb`

---

## 🧪 Тестування

### Базові команди

```bash
# Запуск всіх тестів
mvn test

# Запуск з детальним виводом
mvn test -X

# Запуск конкретного класу
mvn test -Dtest=UserRepositoryTest

# Запуск конкретного методу
mvn test -Dtest=UserRepositoryTest#shouldCreateUser

# Збірка без тестів
mvn package -DskipTests
```

### Статистика

```
📊 Загальна кількість тестів: 175
✅ Успішно виконано: 175
❌ Помилок: 0
⏱️ Час виконання: ~6.8 секунд

Покриття:
├─ 70 тестів інфраструктури (репозиторії, Unit of Work)
├─ 82 тести доменної логіки (сервіси)
└─ 23 тести безпеки (хешування паролів)
```

---

## 🐛 Дебаг

### IntelliJ IDEA

```
1. Клікніть на номер рядка для встановлення breakpoint
2. Right Click на Launcher.java → Debug 'Launcher.main()'
3. Використовуйте панель Debug:
   F8        - Step Over
   F7        - Step Into
   Shift+F8  - Step Out
   F9        - Resume
```

### VS Code

Створіть `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Garden Simulator",
      "request": "launch",
      "mainClass": "ua.notion.Launcher",
      "projectName": "garden-simulator"
    }
  ]
}
```

### Remote Debugging через Maven

```bash
mvn clean compile exec:java \
  -Dexec.mainClass="ua.notion.Launcher" \
  -Dexec.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
```

Підключіться до `localhost:5005` з вашої IDE.

### Логування

Налаштуйте рівень логування в `src/main/resources/simplelogger.properties`:

```properties
org.slf4j.simpleLogger.defaultLogLevel=debug
org.slf4j.simpleLogger.log.ua.notion=debug
```

**Рівні:** `trace` → `debug` → `info` → `warn` → `error`

### Робота з базою даних

```bash
# Відкрити SQLite CLI
sqlite3 data/garden.db

# Переглянути таблиці
.tables

# Переглянути структуру
.schema users

# Виконати запит
SELECT * FROM users;

# Вийти
.quit
```

**GUI інструменти:**
- [DB Browser for SQLite](https://sqlitebrowser.org/)
- [DBeaver](https://dbeaver.io/)
- IntelliJ IDEA Database Tools

### Скидання бази даних

```bash
rm data/garden.db
mvn flyway:migrate
```

---

## 📁 Структура проєкту

```
Kursova/
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/ua/notion/
│   │   │   ├── 📂 domain/              # Доменна логіка
│   │   │   │   ├── entities/           # Сутності (User, Garden, Plant...)
│   │   │   │   ├── repositories/       # Інтерфейси репозиторіїв
│   │   │   │   └── services/           # Бізнес-логіка
│   │   │   ├── 📂 infrastructure/      # Інфраструктура
│   │   │   │   ├── persistence/        # Реалізація репозиторіїв
│   │   │   │   └── security/           # Хешування паролів
│   │   │   ├── 📂 presentation/        # UI шар
│   │   │   │   ├── controllers/        # JavaFX контролери
│   │   │   │   ├── views/              # View моделі
│   │   │   │   └── GardenSimulatorApp  # Головний клас JavaFX
│   │   │   └── 📄 Launcher.java        # Точка входу
│   │   └── 📂 resources/
│   │       ├── assets/                  # Зображення, іконки
│   │       ├── css/                     # Стилі (AtlantaFX)
│   │       ├── db/migration/            # Flyway міграції
│   │       ├── fxml/                    # FXML розмітка
│   │       └── flyway.conf              # Конфігурація БД
│   └── 📂 test/java/                    # 175 автоматизованих тестів
├── 📂 data/                             # SQLite база даних
├── 📂 docs/                             # Документація
├── 📂 packaging/                        # Скрипти пакування
├── 📂 target/                           # Збірка (генерується)
├── 📄 pom.xml                           # Maven конфігурація
├── 📄 build-windows.bat                 # Скрипт збірки Windows
└── 📄 README.md                         # Цей файл
```

---

## 🎮 Керування

```
Клавіатура:
  W, A, S, D    - Переміщення персонажа
  G             - Прискорення росту (debug)
  ESC           - Вихід до меню

Миша:
  Click         - Взаємодія з грядками
  Click         - Посадка рослин
```

---

## 🔐 Тестові дані

### Готовий акаунт

```
Username:  testuser
Password:  password123
Email:     test@example.com
```

### Створення нового акаунту

```
Вимоги:
├─ Username:  мінімум 3 символи
├─ Email:     валідний формат (example@domain.com)
├─ Password:  мінімум 8 символів
└─ Confirm:   має співпадати з паролем
```

---

## 🔧 Troubleshooting

<details>
<summary><b>❌ JavaFX runtime components are missing</b></summary>

**Причина:** JavaFX не знайдено в classpath

**Рішення:**
```bash
mvn clean javafx:run
```

</details>

<details>
<summary><b>❌ Database connection failed</b></summary>

**Причина:** Немає прав на запис або директорія не існує

**Рішення:**
```bash
mkdir -p data
chmod 755 data
```

</details>

<details>
<summary><b>❌ Port 5005 already in use</b></summary>

**Причина:** Порт дебагу зайнятий іншим процесом

**Рішення (Linux/macOS):**
```bash
lsof -ti:5005 | xargs kill -9
```

**Рішення (Windows):**
```cmd
netstat -ano | findstr :5005
taskkill /PID <PID> /F
```

</details>

<details>
<summary><b>❌ Tests fail with "Database locked"</b></summary>

**Причина:** Тестова БД не закрита коректно

**Рішення:**
```bash
mvn clean test
```

</details>

<details>
<summary><b>❌ Spotless formatting errors</b></summary>

**Причина:** Код не відповідає стандартам форматування

**Рішення:**
```bash
mvn spotless:apply
```

</details>

<details>
<summary><b>❌ OutOfMemoryError during build</b></summary>

**Причина:** Недостатньо пам'яті для Maven

**Рішення:**
```bash
export MAVEN_OPTS="-Xmx2048m"
mvn clean package
```

</details>

---

## 📚 Документація

### Внутрішня документація

```
docs/
├── 2.5_Testing.md              # Методологія тестування
├── 2.6-interface-user-guide.md # Посібник користувача
└── report-descriptions.md      # Опис звітів
```

### Корисні посилання

- [JavaFX Documentation](https://openjfx.io/)
- [AtlantaFX Theme](https://github.com/mkpaz/atlantafx)
- [Flyway Migrations](https://flywaydb.org/documentation/)
- [Google Guice](https://github.com/google/guice)

---

## 🗄️ База даних

### Схема

```
users              - Користувачі системи
gardens            - Сади користувачів
plants             - Довідник видів рослин
plant_instances    - Конкретні рослини в садах
tasks              - Завдання по догляду
weather_events     - Погодні події
achievements       - Досягнення користувачів
garden_plants      - Зв'язок садів та рослин (M:N)
```

### Міграції Flyway

```bash
# Застосувати міграції
mvn flyway:migrate

# Переглянути статус
mvn flyway:info

# Очистити БД (видалить всі дані!)
mvn flyway:clean
```

### Нормалізація

```
✓ 1NF - Атомарні атрибути
✓ 2NF - Немає часткових залежностей
✓ 3NF - Немає транзитивних залежностей
```

---

<div align="center">

## 📝 Ліцензія

Цей проєкт створено в освітніх цілях

---

**Garden Simulator** v1.0.0  
Розроблено з ❤️ by Notion

*Останнє оновлення: Травень 2026*

</div>
