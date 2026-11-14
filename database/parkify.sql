CREATE DATABASE IF NOT EXISTS parkify_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parkify_db;

CREATE TABLE IF NOT EXISTS parking_slots (
  slot_id INT PRIMARY KEY,
  status ENUM('AVAILABLE','OCCUPIED') NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS vehicles (
  vehicle_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  vehicle_number VARCHAR(20) NOT NULL,
  owner_name VARCHAR(100) NOT NULL,
  vehicle_type ENUM('Car','Bike','Scooter','Auto','Truck') NOT NULL,
  slot_id INT NOT NULL,
  entry_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  exit_time DATETIME NULL,
  duration_minutes BIGINT NULL,
  fee DECIMAL(10,2) NULL,
  status ENUM('PARKED','EXITED') NOT NULL DEFAULT 'PARKED',
  CONSTRAINT fk_vehicle_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id),
  INDEX idx_vehicle_number(vehicle_number),
  INDEX idx_vehicle_status(status)
);

CREATE TABLE IF NOT EXISTS vehicle_rates (
  vehicle_type VARCHAR(20) PRIMARY KEY,
  hourly_rate DECIMAL(10,2) NOT NULL
);

INSERT IGNORE INTO parking_slots(slot_id) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
(11),(12),(13),(14),(15),(16),(17),(18),(19),(20);

INSERT IGNORE INTO vehicle_rates(vehicle_type,hourly_rate) VALUES
('Car',40.00),('Bike',20.00),('Scooter',20.00),('Auto',30.00),('Truck',60.00);
