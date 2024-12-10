# NEX Ticketing System

A comprehensive ticketing system with two implementations: CLI and Web API with Angular frontend. The system demonstrates concurrent ticket management with real-time updates using WebSockets and thread-safe operations.

## 🎯 Project Overview

This project showcases a ticketing system implemented in three different ways:

1. **CLI Version**: Command-line interface implementation
2. **Web Version**: Spring Boot backend + Angular frontend
3. **Java GUI Version**: Same CLI thing with Swing UI

Each version demonstrates the same core functionality with different interfaces.

## 🚀 Core Features

- Multi-threaded ticket pool management
- Real-time updates using WebSockets (Web version)
- Role-based access (Admin, Vendor, Customer)
- Concurrent ticket operations
- Transaction history tracking
- System monitoring and configuration

## 📋 Documentation & Resources

### API Documentation

- [Postman Workspace](https://app.getpostman.com/join-team?invite_code=b41725094bd4de203c09424ac4d93709&target_code=4b6069844a757617ae52d15ece864d6f)
- [API Documentation](https://drive.google.com/drive/folders/1-8NeqMdohC05IWJifXioNe7vMjHgWZIW?usp=drive_link)

### Project Repositories

- [CLI Project](https://github.com/AmandhaPanagoda/cli-ticketing-system)
- [Frontend Project](https://github.com/AmandhaPanagoda/ticketing-system-ui)
- [Backend Project](https://github.com/AmandhaPanagoda/ticketing-system)

### Technical Documentation

- [Sequence Diagram](https://drive.google.com/drive/folders/1-8NeqMdohC05IWJifXioNe7vMjHgWZIW?usp=drive_link)
- [Class Diagram](https://drive.google.com/drive/folders/1-8NeqMdohC05IWJifXioNe7vMjHgWZIW?usp=drive_link)

## 🛠️ Technology Stack

### Backend (Spring Boot)

- Java 19
- Spring Boot 3.3.4
- Spring Security
- Spring WebSocket
- MySQL
- JPA/Hibernate

### Frontend (Angular)

- Angular 17.3.0
- PrimeNG UI Components
- WebSocket (SockJS + STOMP)
- Chart.js

### CLI Version

- Pure Java
- Console-based UI

## 🚦 Getting Started

### Prerequisites

- Java JDK 19 or higher
- Node.js 18 or higher
- MySQL 8.0
- Maven
- Angular CLI

### Backend Setup

1. Clone the backend repository
2. Configure MySQL database (application.properties)
3. Run using Maven:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup

1. Clone the frontend repository
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start development server:
   ```bash
   ng serve
   ```

### CLI Version Setup

1. Clone the CLI repository
2. Compile and run using Java:
   ```bash
   javac TicketingSystemCLI.java
   java TicketingSystemCLI
   ```

## 🎮 Usage

### Web Version

- Access the application at `http://localhost:4200`
- Login as:
  - Admin: Configure system and monitor operations
  - Vendor: Add tickets to the pool
  - Customer: Purchase tickets from the pool

### CLI Version

- Follow on-screen instructions
- Use inputs to navigate menus
- View real-time ticket pool status

## 🔄 System Architecture

The system uses a thread-safe ticket pool implementation with:

- Semaphore-based concurrency control
- FIFO ticket allocation
- Real-time WebSocket updates (Web version)
- Transaction logging and monitoring

## 🤝 Contributing

Unfortunately, you won't be able to contribute to this. This is just a coursework🥲. But feel free to submit issues and enhancement requests, I will ignore them.
