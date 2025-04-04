# SparkFund: A Java-Based Crowdfunding Platform

**SparkFund** is a donation-based crowdfunding platform built as part of a self-driven learning project to deepen skills in full-stack web development, RESTful APIs, and microservice architecture. It allows users to create and support fundraising campaigns (called "Sparks"), contribute through wallet-based donations, and track progress in real time. 

While developed for educational purposes, SparkFund simulates a real-world application with features like donation management, top donor rankings, wallet management, and email notifications via the dedicated `SparkMail` microservice. The project emphasizes clean architecture, user experience, and modular, scalable design.

---

## 📖 About

**SparkFund** allows users to:
- Create and manage fundraising campaigns called *Sparks*.
- Donate using wallet-based transactions.
- Track funding progress and view donation history.
- View top donors and the total raised amount.
- Send signals that are reviewed and resolved by Admin users.
- Receive email notifications (via the SparkMail microservice) when donations are made, when *Sparks* are completed or cancelled, and when their signal requests are resolved.

---

## ✨ Features
- **User registration and authentication** – Secure sign-up and login system with password management.
- **Wallet-based donation system** – Users can donate using funds from their wallet, with seamless transaction tracking.
- **Fundraising goal tracking and progress bars** – Visual tracking of campaign goals and real-time progress updates.
- **Top donor rankings** – Displays a list of the highest contributors across all Sparks.
- **Email notifications via REST API (sparkmail-mvc)** – Users receive email updates when donations are made, Sparks are completed or cancelled, and when their signals are resolved.
- **Donation summaries and dashboards** – Provides detailed insights into donations made, including the total amount raised, donation history, and contributor information.
- **Filter Sparks by category, ownership and status** – Users can sort campaigns based on their status (e.g., active, cancelled, completed), ownership (e.g., campaigns they own or contributed to), or category (e.g., education, health, etc.).
- **Admin user functionality** – Admin users have access to information for all application users, can change user roles or statuses, and can review, close, or delete resolved signals.

> RESTful API architecture – Well-structured, scalable API for interaction with the platform, supporting integration with external systems.

---

## 🚀 Tech Stack

SparkFund is built using a modern Java-based tech stack designed for full-stack web development, RESTful API design, and microservice architecture.

### Backend

- **Java 17** – Core language for building the application.
- **Spring Boot 3.4.3** – Framework for rapid backend development with embedded server support.

#### Spring Boot Starters & Modules

- **Spring Web** – For building RESTful APIs and handling HTTP requests.
- **Spring Data JPA** – ORM for interacting with the database using Java entities.
- **Spring Security** – Secures endpoints and handles authentication/authorization.
- **Spring Boot Validation** – For input validation in forms and DTOs.
- **Spring Boot Actuator** – For monitoring and health checks.
- **Spring Boot DevTools** – Enables hot-reloading during development.

#### Other Backend Tools

- **Spring Cloud OpenFeign** – Declarative HTTP client for communication between microservices (e.g., SparkMail).
- **Lombok** – Reduces boilerplate code (e.g., getters, setters, constructors).

### Database

- **MySQL** – Main relational database used in production.
- **H2** – In-memory database for development and testing.
- **Hibernate (via JPA)** – ORM for database persistence.

### Frontend (Server-Side Rendered)

- **Thymeleaf** – Java templating engine for rendering dynamic HTML content.

### Testing

- **Spring Boot Starter Test** – Includes JUnit, Mockito, and other testing utilities.
- **Spring Security Test** – For testing secured routes and authentication logic.

### Build & Dependency Management

- **Maven** – Project build tool and dependency manager.
- **Spring Boot Maven Plugin** – For packaging and running the application.

> This stack enables modular, testable, and scalable development while mimicking production-level practices in a training project.
