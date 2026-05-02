# GamePlatform DB — Java Swing GUI

A role-aware desktop application for managing GamePlatformDB,
built with Java Swing + Microsoft JDBC.

---

## Requirements

- Java 17+
- Maven 3.6+  (or open as a Maven project in IntelliJ / Eclipse)
- SQL Server running on `DESKTOP-7OVINNS\SQLEXPRESS`
- `GamePlatformDB` database with all logins already created

---

## Build & Run

```bash
# 1. Build a fat JAR (includes JDBC driver)
mvn clean package -q

# 2. Run
java -jar target/GamePlatformGUI-1.0-jar-with-dependencies.jar
```

Or run `com.gameplatform.Main` directly from your IDE.

---

## Connection String

Edit `DBConnection.java` if your SQL Server instance name differs:

```java
private static final String BASE_URL =
    "jdbc:sqlserver://DESKTOP-7OVINNS\\SQLEXPRESS;databaseName=GamePlatformDB;encrypt=false";
```

---

## Role Permissions & Visible Tabs

| Tab          | admin1 | user1 | analyst1 |
|-------------|--------|-------|----------|
| Games        | ✓ | ✓ | ✓ |
| Library      | ✓ SELECT/INSERT/UPDATE/DELETE | ✓ SELECT/INSERT/UPDATE/DELETE | ✓ SELECT only |
| Wishlist     | ✓ SELECT/INSERT/DELETE | ✓ SELECT/INSERT/DELETE | ✓ SELECT only |
| Reviews      | ✓ SELECT/INSERT/UPDATE | ✓ SELECT/INSERT/UPDATE | ✓ SELECT only |
| Orders       | ✓ SELECT/INSERT | ✓ SELECT/INSERT | ✓ SELECT only |
| Users        | ✓ | ✗ | ✓ SELECT only |
| Payments     | ✓ | ✗ | ✓ SELECT only |
| Order Items  | ✓ | ✗ | ✓ SELECT only |
| Admin Tools  | ✓ SQL console | ✗ | ✗ |

---

## Project Structure

```
src/main/java/com/gameplatform/
├── Main.java
├── db/
│   └── DBConnection.java        ← JDBC connection + role detection
└── ui/
    ├── LoginFrame.java          ← Login screen
    ├── DashboardFrame.java      ← Main window + tab visibility
    └── panels/
        ├── BasePanel.java       ← Shared styles, table helpers
        ├── GamesPanel.java
        ├── LibraryPanel.java
        ├── WishlistPanel.java
        ├── ReviewsPanel.java
        ├── OrdersPanel.java
        ├── UsersPanel.java      ← Analyst + Admin only
        ├── PaymentsPanel.java   ← Analyst + Admin only
        ├── OrderItemsPanel.java ← Analyst + Admin only
        └── AdminPanel.java      ← Admin only (SQL console)
```
