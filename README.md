# PARKIFY – Vehicle Parking Management System

A Java 17 + Swing desktop application backed by MySQL. Built from the supplied PARKIFY Software Requirement Specification.

## Implemented modules
- Dashboard with 20 live parking slots and occupancy indicators
- Vehicle entry with owner, vehicle number, type and slot assignment
- Vehicle exit with automatic fee calculation and slot release
- Parking history and vehicle/owner search
- Revenue totals and revenue split by vehicle type
- Configurable hourly rates
- MySQL persistence through JDBC
- Modern FlatLaf Swing interface

## Fee calculation
The system bills by vehicle type and rounds any part-hour up to the next hour.
Default hourly rates (editable in the Rates screen):
- Car: ₹40/hour
- Bike: ₹20/hour
- Scooter: ₹20/hour
- Auto: ₹30/hour
- Truck: ₹60/hour

These defaults are starter values; adjust them to the parking facility's actual tariff.

## Requirements
- JDK 17+
- MySQL Server 8.0+
- IntelliJ IDEA (Community or Ultimate)
- Maven (bundled Maven in IntelliJ is sufficient)

## Database setup
1. Start MySQL Server.
2. Open MySQL Workbench or a MySQL terminal.
3. Run `database/parkify.sql`. It creates `parkify_db`, the tables, 20 parking slots and default hourly rates.
4. The default connection is:
   - URL: `jdbc:mysql://localhost:3306/parkify_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata`
   - Username: `root`
   - Password: `root`

**Important:** Change the password to match your local MySQL installation. Do not use the sample password in a production deployment.

### Configure database credentials
The app supports environment variables:
- `PARKIFY_DB_URL`
- `PARKIFY_DB_USER`
- `PARKIFY_DB_PASSWORD`

Or JVM system properties:
- `-Dparkify.db.url=...`
- `-Dparkify.db.user=...`
- `-Dparkify.db.password=...`

Example (Windows PowerShell):
```powershell
$env:PARKIFY_DB_USER="root"
$env:PARKIFY_DB_PASSWORD="your_mysql_password"
```

## Run in IntelliJ IDEA
1. Extract the ZIP file.
2. Open IntelliJ IDEA → **Open** → select the extracted `PARKIFY` folder (the folder containing `pom.xml`).
3. Allow IntelliJ to import/sync the Maven project.
4. Set Project SDK to JDK 17: **File → Project Structure → Project SDK**.
5. Ensure MySQL is running and database setup is complete.
6. Open `src/main/java/com/parkify/ParkifyApp.java`.
7. Click the green Run icon beside `main()`.

Alternatively, run from the project root:
```bash
mvn clean compile exec:java
```

## Project structure
```text
PARKIFY/
├── database/
│   └── parkify.sql
├── src/main/java/com/parkify/
│   ├── DB.java
│   ├── ParkingService.java
│   └── ParkifyApp.java
├── pom.xml
└── README.md
```

## Notes and scope
- The database is initialized on application startup; the SQL script is also provided for manual setup.
- Entry, exit, slot status, vehicle history, and revenue are persisted in MySQL.
- Live slot duration refreshes on the dashboard every second.
- The supplied SRS lists Administrator, Manager, Parking Attendant, Slot Coordinator and Vehicle Owner as user types. This starter desktop build is an operator-facing system and does not yet implement role-based authentication.
- Online slot pre-booking/payment, mobile app and ANPR are future-scope items in the SRS and are not implemented.
- PDF/Excel export, QR functionality and graphical analytics are listed as technology possibilities in the SRS but are not included in this initial build.
