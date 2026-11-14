package com.parkify;

import java.sql.*;

public final class DB {
    private DB() {}
    private static final String URL = System.getProperty("parkify.db.url",
            System.getenv().getOrDefault("PARKIFY_DB_URL", "jdbc:mysql://localhost:3306/parkify_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"));
    private static final String USER = System.getProperty("parkify.db.user", System.getenv().getOrDefault("PARKIFY_DB_USER", "root"));
    private static final String PASS = System.getProperty("parkify.db.password", System.getenv().getOrDefault("PARKIFY_DB_PASSWORD", "user123"));
    static { try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException e) { throw new ExceptionInInitializerError(e); } }
    public static Connection get() throws SQLException { return DriverManager.getConnection(URL, USER, PASS); }
    public static void initialize() throws SQLException {
        String[] sql = {
          "CREATE TABLE IF NOT EXISTS parking_slots(slot_id INT PRIMARY KEY,status ENUM('AVAILABLE','OCCUPIED') NOT NULL DEFAULT 'AVAILABLE')",
          "CREATE TABLE IF NOT EXISTS vehicles(vehicle_id BIGINT AUTO_INCREMENT PRIMARY KEY,vehicle_number VARCHAR(20) NOT NULL,owner_name VARCHAR(100) NOT NULL,vehicle_type ENUM('Car','Bike','Scooter','Auto','Truck') NOT NULL,slot_id INT NOT NULL,entry_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,exit_time DATETIME NULL,duration_minutes BIGINT NULL,fee DECIMAL(10,2) NULL,status ENUM('PARKED','EXITED') NOT NULL DEFAULT 'PARKED',FOREIGN KEY(slot_id) REFERENCES parking_slots(slot_id),INDEX(vehicle_number),INDEX(status))",
          "CREATE TABLE IF NOT EXISTS vehicle_rates(vehicle_type VARCHAR(20) PRIMARY KEY,hourly_rate DECIMAL(10,2) NOT NULL)"
        };
        try(Connection c=get(); Statement s=c.createStatement()) {
            for(String q:sql) s.execute(q);
            try(PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO parking_slots(slot_id) VALUES (?)")) {
                for(int i=1;i<=20;i++){p.setInt(1,i);p.addBatch();} p.executeBatch();
            }
            String[][] rates={{"Car","40"},{"Bike","20"},{"Scooter","20"},{"Auto","30"},{"Truck","60"}};
            try(PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO vehicle_rates VALUES(?,?)")) {
                for(String[] r:rates){p.setString(1,r[0]);p.setBigDecimal(2,new java.math.BigDecimal(r[1]));p.addBatch();}p.executeBatch();
            }
        }
    }
}
