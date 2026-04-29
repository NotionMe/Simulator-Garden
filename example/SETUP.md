# Maven Project Setup

## Project Structure
```
audiobooks-library/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
├── pom.xml
└── .editorconfig
```

## Dependencies
- **SQLite JDBC Driver** (3.45.3.0) - Direct JDBC access without Hibernate
- **Lombok** (1.18.32) - Reduce boilerplate code
- **JUnit 5** (5.10.2) - Testing framework

## Build Plugins
- **Spotless** - Code formatting with Google Java Style (4 spaces indent)
- **SonarQube** - Code quality analysis
- **Maven Compiler** - Java 17 with Lombok annotation processing

## Usage

### Format code
```bash
mvn spotless:apply
```

### Check formatting
```bash
mvn spotless:check
```

### Run SonarQube analysis
```bash
mvn sonar:sonar
```

### Build project
```bash
mvn clean install
```
