🌊 OceanView Resort Management System

A web-based Resort Management System developed using Java (Java EE), Servlets, and JSP to streamline resort operations such as guest management, room reservations, billing, and staff administration.

This system provides a simple interface for administrators and receptionists to manage daily resort activities efficiently.

✨ Features
👤 User Roles
Administrator

Manage staff accounts (Add, Edit, Delete)

Monitor room inventory

View booking and revenue reports

Access system overview dashboard

Receptionist

Register and manage guest details

Create and update reservations

Perform check-in and check-out operations

Update room status

Generate guest bills

🏨 Core Modules
Guest Management

Register new guests

Search existing guest records

Update guest information

Room Management

Manage different room types (Deluxe, Suite, etc.)

Track room availability

Update room status (Available, Occupied, Maintenance)

Booking System

Create new reservations

Update booking status

Manage check-in and check-out processes

Automatic booking price calculation

Billing & Payment

Generate bills based on stay duration

Calculate total room charges

Track payment details

🛠️ Technology Stack
Layer	Technology
Backend	Java (JDK 8+), Java Servlets
Design Pattern	DAO Pattern
Database	MySQL / MariaDB
Frontend	HTML5, CSS3, JavaScript
Server	Apache Tomcat 9+
Build Tool	Apache Ant
🚀 Getting Started
Prerequisites

Before running the project, ensure the following software is installed:

Java Development Kit (JDK 8 or higher)

Apache Tomcat Server

MySQL Server

NetBeans IDE (recommended)

🗄️ Database Setup

Open your MySQL client (for example MySQL Workbench).

Import the provided database script:

SOURCE path/to/database.sql;

Update the database configuration inside:

src/java/util/DBConnection.java

Example configuration:

String url = "jdbc:mysql://localhost:3306/oceanviewresortdb";
String username = "root";
String password = "yourpassword";
⚙️ Deployment
Step 1 – Build the Project

Using Apache Ant:

ant build
Step 2 – Deploy to Tomcat

Copy the generated .war file into the Tomcat webapps folder.

tomcat/webapps/
Step 3 – Run the Application

Open your browser and visit:

http://localhost:8080/OceanViewResort

📂 Project Structure
OceanViewResort/
│
├── src/java/
│   ├── controller/      # Servlets for handling requests
│   ├── dao/             # Data Access Objects
│   ├── model/           # Entity classes (Room, Guest, Booking)
│   └── util/            # Database connection utilities
│
├── web/
│   ├── admin.html            # Admin dashboard
│   ├── receptionist.html     # Receptionist dashboard
│   ├── css/                  # Stylesheets
│   └── js/                   # Client-side scripts
│
└── database.sql              # Database schema and sample data


🔐 Default Login Credentials
Role	Username	Password
Administrator	admin	admin123
Receptionist	receptionist13	Receptionist
📚 Project Purpose

This system was developed as part of the Advanced Programming module assignment to demonstrate the implementation of:

Java Servlet-based web applications

Object-Oriented Programming principles

DAO Design Pattern

Database integration using MySQL

UML system design and software engineering practices

📄 License

This project was developed for academic purposes as part of a university coursework assignment.

All rights reserved © 2026.
