# Banking Application - Práctica de Testing y Refactoring

## Descripción

Aplicación bancaria desarrollada con Spring Boot para practicar testing unitario, refactoring, TDD y pruebas de sistema con Selenium.

## Stack Tecnológico

- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Data JPA**
- **H2 Database** (en memoria)
- **Mustache** (motor de plantillas)
- **Bootstrap 5** (frontend)
- **JUnit 6** (testing)
- **Mockito** (mocks)
- **Selenium WebDriver** (testing web)
- **Maven** (gestión de dependencias)

## Requisitos Previos

- JDK 21 instalado
- Maven 3.8+ instalado
- IDE (IntelliJ IDEA, Eclipse, VS Code)
- SonarLint plugin instalado en el IDE
- Chrome o Chromium instalado (para tests de Selenium)

## Instalación y Ejecución

### 1. Clonar/Descargar el proyecto

```bash
cd banking-app
```

### 2. Compilar el proyecto

```bash
mvn clean compile
```

### 3. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La aplicación estará disponible en: http://localhost:8080

### 4. Acceder a la base de datos H2

Durante el desarrollo, puedes acceder a la consola H2:
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:bankingdb`
- Username: `sa`
- Password: (dejar vacío)

### 5. Ejecutar tests

```bash
# Ejecutar todos los tests
mvn test
```

## Usuarios de Prueba

La aplicación incluye datos de prueba cargados automáticamente:

### Clientes
- **Customer 1:** Juan García (DNI: 12345678A)
- **Customer 2:** María López (DNI: 87654321B)
- **Customer 3:** Pedro Martínez (DNI: 11223344C)

### Login Web
- **Usuario Cliente:**
  - Username: `customer`
  - Password: `Cu5t0m3r`
  - Redirige a: Dashboard del cliente 1
  
- **Usuario Gestor:**
  - Username: `manager`
  - Password: `M4n4g3r`
  - Redirige a: Gestión de préstamos
