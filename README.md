# Image Store Application

The **Image Store Application** is a Spring Boot REST service that enables users to register, authenticate, and manage images. Authenticated users can upload images to Imgur, view all their images, retrieve a specific image by its ID, and delete images. User data and image metadata are stored in an H2 in-memory database using JPA, and JWT tokens secure the API endpoints.

Additionally, the application optionally integrates with Kafka for asynchronous messaging. When enabled, Kafka publishes an event whenever an image is uploaded.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)

## Features

- **User Management**
  - **Registration:** Users register by providing a username, password, email, first name, and last name.
  - **Authentication:** Users log in to receive a JWT token.
- **Image Management**
  - **Upload:** Authenticated users upload images to Imgur; the original filename is stored along with image metadata.
  - **Retrieve All:** Users fetch all images associated with their account.
  - **Retrieve Single:** Users retrieve details of a specific image by its ID.
  - **Deletion:** Users delete images (only if associated with their account).
- **Security:**  
  - JWT-based authentication secures endpoints.
  - Input validations ensure proper registration data and file uploads.
- **Messaging (Kafka):**
  - Optionally, Kafka can be enabled to publish events on successful image uploads.
  
## Technologies Used

- **Backend:** Spring Boot, Spring Security, Spring Data JPA
- **Database:** H2 (in-memory)
- **Authentication:** JWT
- **External API:** Imgur API for image hosting
- **Messaging:** Apache Kafka (optional)
- **Build:** Maven
- **Testing:** JUnit 5, Spring Boot Test, Mockito

## Configuration

The application uses an `application.properties` file for configuration. Before running, update the file with your own values:

```properties

# Application settings
spring.application.name=img-store
server.port=8081

# Imgur API Credentials
# To obtain your Imgur Client ID:
# 1. Register a new client at https://api.imgur.com/oauth2/addclient
# 2. Set the environment variable IMGUR_CLIENT_ID or replace the placeholder below.
imgur.client-id=${IMGUR_CLIENT_ID}

# H2 Database Configuration
# Customize the DB name, url(console.path), username, and password as desired.
spring.datasource.url=jdbc:h2:mem:img_store_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=${DB_USERNAME:admin}
spring.datasource.password=${DB_PASSWORD:password@123}
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA and Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# File Upload Configuration
# Change the file size value as desired
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Kafka Configuration
# Set kafka.enabled to true if you want to enable Kafka messaging.
kafka.enabled=false

# JWT Configuration
# Generate a JWT secret (minimum 32 characters) using: openssl rand -base64 32
# Update the token expiration time as needed
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

# Logging Configuration
logging.level.org.springframework.security=TRACE
