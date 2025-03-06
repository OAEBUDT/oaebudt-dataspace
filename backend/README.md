# oaebudt-dataspace-backend
# Spring Boot with Keycloak Authentication

This project integrates **Spring Boot** with **Keycloak** for authentication and authorization. It uses a **PostgreSQL** database as the Keycloak data source.

---

## 🛠 Prerequisites

Ensure you have the following installed:

- **Java 23** 
- **Docker & Docker Compose**
- **Gradle 8.12 or later** (or use `./gradlew`)

---

## 🚀 Starting the Project

### 1️⃣ Start Keycloak and PostgreSQL with Docker Compose

Run the following command to start Keycloak and PostgreSQL:

```sh
docker-compose up -d
```

This will start Keycloak on `http://localhost:8080` and PostgreSQL on port `5433` (exposed for external connections).

---

### 2️⃣ Configure Keycloak Realm

1. Open **Keycloak Admin Console**: `http://localhost:8080`
2. Log in with:
    - **Username**: `admin`
    - **Password**: `admin`
3. Create a new realm `myrealm`
4. Create a new **Client**:
    - Client ID: `test-client`
    - Root URL: `http://localhost:8081`
5. Create a test **User**:
    - Username: `testuser`
    - Password: `password`

---

### 3️⃣ Run the Spring Boot Application

Ensure your `application.yml` is configured correctly. Then, start the application:

#### **With Gradle**

```sh
./gradlew bootRun
```

#### **With Java (JAR File)**

```sh
./gradlew build
java -jar build/libs/springboot-keycloak-example.jar
```

The application should start at: `http://localhost:8081`

---

## 🛂 Obtain Token and Access a Secured Resource

1. **Get a Keycloak Token**

```sh
curl -X POST http://localhost:8080/realms/myrealm/protocol/openid-connect/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "client_id=spring-boot-client" \
     -d "client_secret=YOUR_CLIENT_SECRET" \
     -d "username=testuser" \
     -d "password=password" \
     -d "grant_type=password"
```
Exclude `-d "client_secret=YOUR_CLIENT_SECRET" \` if client secret was not set on the UI.

Copy the `access_token` from the response.

2. **Access a Secured Endpoint**

```sh
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" http://localhost:8081/api/secure
```

If authentication is successful, you will receive a response from the secured endpoint.

---

## 🔄 Stopping Keycloak

To stop and remove the Keycloak and PostgreSQL containers:

```sh
docker-compose down
```

---

## 🎯 Summary

✅ **Spring Boot is integrated with Keycloak**  
✅ **Users are authenticated using JWT**  
✅ **Role-based access control (RBAC) is enabled**  
✅ **PostgreSQL is used as the Keycloak database**

---

### Need Help?

Feel free to ask questions or open an issue! 🚀

