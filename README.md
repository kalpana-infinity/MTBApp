# Multithreaded Banking Application (MTBApp)

A secure, Java-based desktop banking application that uses multithreading, Java Swing GUI, and Oracle 11g for backend database management. Built using the MVC architecture and incorporates AES encryption for password protection and thread-safe transaction processing.

---

## 🔧 Features

- Java Swing GUI interface
- Oracle 11g Database with JDBC
- Multithreaded transaction handling (ExecutorService + synchronized methods)
- MVC Architecture
- Account creation and login
- Deposit and withdrawal operations
- Admin dashboard to view and manage accounts

---

## 🛠️ Technologies Used

- **Language:** Java 
- **GUI:** Java Swing
- **Database:** Oracle 11g
- **Database Connection:** JDBC
- **Threading:** ExecutorService, synchronized methods

---

## 🖥️ How to Run

1. **Install Oracle 11g** and create the required tables (`CUSTOMERS`, `TRANSACTIONS`).
2. **Update database credentials** in the JDBC code:
   ```java
   Connection con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "your_username", "your_password");

