# HostelHub — Core Java OOP Hostel Management System

A menu-driven console application for managing multiple hostels. Built with Core Java, OOP, collections, enums, `BigDecimal`, `LocalDate`, input validation, and Java object serialization for local persistence. No frameworks or external libraries are required.

## Requirements
- JDK 8 or later (developed and tested with JDK 21).
- Terminal/command prompt.

## Run the application
From the `HostelHub` project directory:

### Windows (PowerShell or CMD)
```powershell
javac -encoding UTF-8 -d out src\hostelhub\*.java
java -cp out hostelhub.HostelManagementSystem
```

### macOS / Linux
```bash
javac -encoding UTF-8 -d out src/hostelhub/*.java
java -cp out hostelhub.HostelManagementSystem
```

The app creates `hostelhub-data.ser` in the directory where you launch it. This file stores records between runs. Back up this file before moving or deleting it. Do not load serialized files from untrusted sources.

## Features
- Nested console menus (no frontend/pages)
- Hostel CRUD, search by name/location, hostel type, rent, and vacancy
- Manager registration, update, deactivation, search, and hostel assignment
- Room CRUD, room types, capacity, rent, AC flag, vacancy reporting
- Tenant registration, search, update, check-in/allocation, check-out, filtering by hostel/type
- Hostel-specific breakfast/lunch/dinner menus and mess charges
- Facility CRUD with availability and monthly charges
- Payment records, payment history, monthly collection reports, estimated current-month balances
- Booking/enquiry workflow and status management
- Complaints with status tracking
- Dashboard with hostels, managers, tenants, rooms, vacancies, occupancy, collections, and complaints
- Local persistence between program runs

## Data model / OOP
`Models.java` contains the domain classes (`Hostel`, `Manager`, `Tenant`, `Room`, `Facility`, `Payment`, `Booking`, and `Complaint`) and enums. `HostelManager` coordinates business operations and validation. `HostelManagementSystem` owns the menus. `DataStore` loads and saves application state.

## Important starter-version limitations
- This is a local, single-user console application, not a networked multi-user platform.
- Records are saved with Java serialization, not a database.
- Payment reports sum recorded PAID/PARTIAL payment entries by payment date. The outstanding amount is an estimate for the current month based on the tenant's room rent, mess fee, and available facility charges minus recorded PAID/PARTIAL payments for the current month. A production system should introduce formal bills, payment allocation to each bill, deposits/refunds, and audit history.
- Manager assignments are for administration and reporting; the starter app does not implement login/authentication or permission enforcement.
- The girls'/boys'/co-living category is a property classification used for listing and filtering, not a tenant identity attribute.

## Suggested manual test flow
1. Add a manager.
2. Add a hostel and assign the manager.
3. Add two or more rooms with different capacities and rents.
4. Add facilities and set the mess menu and monthly fee.
5. Register a tenant, with or without immediate room allocation.
6. View vacancies and confirm occupancy updates.
7. Record a payment with today's date and check the dashboard/monthly report.
8. Create a booking and a complaint; update their statuses.
9. Exit and run again to verify that records persist.
