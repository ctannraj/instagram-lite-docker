# Instagram Lite - Docker & Cassandra Setup

A lightweight Instagram-like social media service built with Spring Boot. This application provides REST APIs to create, update, delete, and view posts, with persistent storage using Cassandra and event-driven architecture with Kafka.

## 🚀 Project Overview

- **Technology Stack**: Java 17, Spring Boot 3.2.5, Maven, Cassandra 4.1, Kafka 7.5.0
- **Server Port**: 8080
- **Cassandra Port**: 9042
- **Kafka Port**: 9092
- **Database**: Cassandra (distributed, highly available)
- **Message Broker**: Apache Kafka (event streaming)
- **Architecture**: Event-driven with asynchronous post event processing

### Key Features
- ✅ Create, update, and delete posts with persistence
- ✅ Retrieve user timelines
- ✅ Event-driven architecture with Kafka integration
- ✅ Cassandra for distributed data storage
- ✅ Docker and Docker Compose support
- ✅ Application runs on spring-boot, and docker is used for running Kafka and Cassandra.

---

## 📋 Prerequisites

- **Java**: 17 or higher
- **Maven**: 3.6 or higher
- **Docker**: Latest version
- **Docker Compose**: Latest version

### Check Prerequisites
```bash
java -version
mvn -version
docker --version
docker-compose --version
```

---

## 🔧 Build & Run

### 1. Clean Build (Maven)
```bash
mvn clean install
```

### 2. View Dependencies
```bash
mvn dependency:tree
```

### 3. Build Docker Image
```bash
docker build -t instagram-lite:latest .
```

### 4. Start Services with Docker Compose
```bash
docker-compose up -d
```

This will start:
- **Kafka** on port 9092
- **Cassandra** on port 9042

### 5. Initialize Cassandra Database
See the [Cassandra Database Setup](#cassandra-database-setup) section below.

### 6. Run the Application
```bash
# Option 1: Run with Maven
mvn spring-boot:run

# Option 2: Run the JAR directly
java -jar target/instagram-lite-docker-0.0.1-SNAPSHOT.jar

# Option 3: Run as Docker container
docker run -p 8080:8080 --network instagram-lite-network instagram-lite:latest
```

---

## 🐳 Docker Commands

### Docker Compose Commands

#### Start Services
```bash
# Start with logs visible
docker-compose up

# Start all services in the background
docker-compose up -d

# Start specific service
docker-compose up -d cassandra
docker-compose up -d kafka
```

#### Stop Services
```bash
# Stop all services
docker-compose stop

# Stop specific service
docker-compose stop cassandra
docker-compose stop kafka
```

#### Restart Services
```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart cassandra
```

#### Remove Services
```bash
# Remove stopped containers, networks
docker-compose down

# Remove containers, networks, AND volumes
docker-compose down -v

# Remove all images as well
docker-compose down -v --rmi all
```

#### View Logs
```bash
# View logs from all services
docker-compose logs

# View logs from specific service
docker-compose logs cassandra
docker-compose logs kafka

# Follow logs in real-time
docker-compose logs -f cassandra
docker-compose logs -f kafka

# View last 50 lines
docker-compose logs --tail=50
```

#### Check Service Status
```bash
# List running containers
docker-compose ps

# Check container details
docker-compose ps -a
```

---

## 🗄️ Cassandra Database Setup

### Connect to Cassandra

```bash
# Enter Cassandra container
docker exec -it cassandra-instagram-lite-docker cqlsh
```

### Initialize Keyspace

Create the keyspace for Instagram Lite:

```cql
-- Create keyspace
CREATE KEYSPACE IF NOT EXISTS instagram 
WITH REPLICATION = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

-- Use the keyspace
USE instagram;
```

### Create Tables

#### Table 1: Posts by ID
```cql
CREATE TABLE IF NOT EXISTS instagram.posts_by_id (
    post_id TEXT PRIMARY KEY,
    user_id TEXT,
    text TEXT,
    created_at TIMESTAMP
);
```

#### Table 2: Posts by User
```cql
CREATE TABLE IF NOT EXISTS instagram.posts_by_user (
    user_id TEXT,
    created_at TIMESTAMP,
    post_id TEXT,
    text TEXT,
    PRIMARY KEY (user_id, created_at)
) WITH CLUSTERING ORDER BY (created_at DESC);
```

### Select Data from Tables

#### View all posts by ID
```cql
SELECT * FROM instagram.posts_by_id;
```

#### View posts by specific user
```cql
SELECT * FROM instagram.posts_by_user WHERE user_id = 'user123';
```

---

## 📄 Automated Setup Scripts

The project includes helper scripts in the `scripts/` directory for automated setup and management.

### Available Scripts

#### 1. `scripts/docker-setup.sh` - Complete System Initialization

This is the main setup script that orchestrates the entire initialization process.

```bash
chmod +x scripts/docker-setup.sh
./scripts/docker-setup.sh
```

**What it does:**
1. Stops and removes existing containers and volumes
2. Starts Docker Compose services (Kafka & Cassandra)
3. Waits for Cassandra to be ready (with health check)
4. Initializes Cassandra schema from `cassandra-init.cql`
5. Initializes Kafka topics via `kafka-init.sh`

#### 2. `scripts/cassandra-init.cql` - Cassandra Schema

CQL (Cassandra Query Language) script that defines the database schema.

**Creates:**
- Keyspace: `instagram` with SimpleStrategy replication
- Table: `posts_by_id` (indexed by post ID)
- Table: `posts_by_user` (indexed by user ID and timestamp for timeline queries)

This file is automatically executed by `docker-setup.sh`.

#### 3. `scripts/kafka-init.sh` - Kafka Topic Initialization

Bash script that creates the Kafka topic for post events.

```bash
chmod +x scripts/kafka-init.sh
./scripts/kafka-init.sh
```

**Creates:**
- Topic: `post-events` with 1 partition and replication factor 1

#### 4. `scripts/app-start` - Application Startup

Simple script to build and start the Spring Boot application.

```bash
chmod +x scripts/app-start
./scripts/app-start
```

**Performs:**
1. Cleans and builds the Maven project (`mvn clean install`)
2. Starts Spring Boot application (`mvn spring-boot:run`)

### Quick Start with Scripts

```bash
# 1. Make all scripts executable
chmod +x scripts/*.sh

# 2. Run the complete setup
./scripts/docker-setup.sh

# 3. In another terminal, start the application
./scripts/app-start

# Application will be available at http://localhost:8080
```

### Script Execution Details

**docker-setup.sh** performs these operations in sequence:
1. Cleans up existing containers: `docker compose down -v --rmi all`
2. Waits 5 seconds for cleanup
3. Starts services: `docker compose up -d`
4. Waits 5 seconds for services to initialize
5. Polls Cassandra with `cqlsh` until it's ready
6. Executes schema initialization: `cassandra-init.cql`
7. Initializes Kafka topics: `kafka-init.sh`

**kafka-init.sh** creates a Kafka topic:
- Topic: `post-events`
- Partitions: 1
- Replication Factor: 1
- Using: `kafka-topics` command in Kafka container

**app-start** runs:
- `mvn clean install` - Cleans and builds the project
- `mvn spring-boot:run` - Starts the application

---

## 🔄 Complete Workflow

### Quick Setup (Using Scripts)

```bash
# 1. Navigate to project directory
cd instagram-lite-docker

# 2. Make scripts executable
chmod +x scripts/*.sh

# 3. Run automated setup
./scripts/docker-setup.sh

# Wait for output indicating "Setup complete"
# Then in another terminal:

# 4. Build and start application
./scripts/app-start

# 5. Application is ready at http://localhost:8080
```

### Manual Setup (Without Scripts)

```bash
# 1. Build the Maven project
mvn clean install

# 2. Start Docker services
docker compose up -d

# 3. Wait for Cassandra to be ready (15-20 seconds)
sleep 20

# 4. Initialize Cassandra schema
docker exec -it cassandra-instagram-lite-docker cqlsh < scripts/cassandra-init.cql

# 5. Initialize Kafka topics
./scripts/kafka-init.sh

# 6. Run the application
mvn spring-boot:run

# Application will be available at http://localhost:8080
```

### Daily Operations

```bash
# Start services
docker compose up -d

# Check services are running
docker compose ps

# View logs (in separate terminals)
docker compose logs -f cassandra
docker compose logs -f kafka

# Start application (in another terminal)
./scripts/app-start

# Test API
curl -X POST http://localhost:8080/posts \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{"text": "Hello Instagram!"}'

# Check database
docker exec -it cassandra-instagram-lite-docker cqlsh <<EOF
USE instagram;
SELECT * FROM posts_by_id;
EOF

# Stop everything
docker compose down
```

---

## 📡 Architecture Overview

### Data Flow
```
┌─────────────────────────────────────────────────────────────────┐
│                     REST API Request                            │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
                 ┌───────────────────┐
                 │  PostsController  │
                 └────────┬──────────┘
                          │
                          ▼
                 ┌───────────────────┐
                 │  PostsService     │
                 └────────┬──────────┘
                          │
        ┌─────────────────┼──────────────────┐
        │                 │                  │
        ▼                 ▼                  ▼
┌──────────────┐  ┌──────────────────┐  ┌────────────────┐
│ PostsById    │  │ Kafka Broker     │  │ PostsKafka     │
│ Repository   │  │ (BlockingQueue)  │  │ Consumer       │
└──────────────┘  └────────┬─────────┘  └────────┬───────┘
                           │                     │
                           ▼                     ▼
                 ┌────────────────────┐  ┌──────────────────┐
                 │ PostCreatedEvent   │  │ PostsByUser      │
                 │ PostUpdatedEvent   │  │ Repository       │
                 │ PostDeletedEvent   │  │ (Timeline)       │
                 └────────────────────┘  └──────────────────┘
```

### Event-Driven Flow
1. **Create/Update/Delete** triggers a `PostEvent`
2. Event is published to `KafkaBroker.POSTS_EVENTS_TOPIC` (BlockingQueue)
3. `PostsKafkaConsumer` consumes events asynchronously
4. Updates are reflected in `PostsByUserRepository` for timeline queries

---

## 🌐 API Endpoints

### 1. Create a Post
**Endpoint**: `POST /posts`

**Request Headers**:
```
Content-Type: application/json
X-User-Id: user123
```

**Request Body**:
```json
{
  "text": "This is my first post!"
}
```

**cURL Command**:
```bash
curl -X POST http://localhost:8080/posts \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{
    "text": "This is my first post!"
  }'
```

**Expected Response** (201 Created):
```json
{
  "postId": "a1b2c3d4",
  "userId": "user123",
  "text": "This is my first post!",
  "createdAt": "2026-02-28T10:30:45.123Z"
}
```

**Response Headers**:
```
Location: /posts/a1b2c3d4
```

---

### 2. Update a Post
**Endpoint**: `PUT /posts/{postId}`

**Request Headers**:
```
Content-Type: application/json
X-User-Id: user123
```

**Request Body**:
```json
{
  "text": "Updated post content"
}
```

**Path Parameters**:
```
postId: a1b2c3d4
```

**cURL Command**:
```bash
curl -X PUT http://localhost:8080/posts/a1b2c3d4 \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{
    "text": "Updated post content"
  }'
```

**Expected Response** (200 OK):
```json
{
  "postId": "a1b2c3d4",
  "userId": "user123",
  "text": "Updated post content",
  "createdAt": "2026-02-28T10:30:45.123Z"
}
```

---

### 3. Delete a Post
**Endpoint**: `DELETE /posts/{postId}`

**Request Headers**:
```
X-User-Id: user123
```

**Path Parameters**:
```
postId: a1b2c3d4
```

**cURL Command**:
```bash
curl -X DELETE http://localhost:8080/posts/a1b2c3d4 \
  -H "X-User-Id: user123"
```

**Expected Response** (204 No Content):
```
(empty body)
```

---

### 4. Get User Timeline
**Endpoint**: `GET /users/{userId}/timeline`

**Path Parameters**:
```
userId: user123
```

**cURL Command**:
```bash
curl -X GET http://localhost:8080/users/user123/timeline
```

**Expected Response** (200 OK):
```json
[
  {
    "postId": "a1b2c3d4",
    "userId": "user123",
    "text": "Updated post content",
    "createdAt": "2026-02-28T10:30:45.123Z"
  },
  {
    "postId": "e5f6g7h8",
    "userId": "user123",
    "text": "My second post",
    "createdAt": "2026-02-28T11:00:00.000Z"
  }
]
```

---

## 🔍 API Response Examples

### Success Responses

#### 201 Created
```bash
curl -X POST http://localhost:8080/posts \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{"text": "Hello World"}'
```

Response:
```json
{
  "postId": "a1b2c3d4",
  "userId": "user123",
  "text": "Hello World",
  "createdAt": "2026-02-28T12:00:00.000Z"
}
```

#### 200 OK
```bash
curl -X GET http://localhost:8080/users/user123/timeline
```

Response:
```json
[
  {
    "postId": "a1b2c3d4",
    "userId": "user123",
    "text": "Hello World",
    "createdAt": "2026-02-28T12:00:00.000Z"
  }
]
```

#### 204 No Content
```bash
curl -X DELETE http://localhost:8080/posts/a1b2c3d4 \
  -H "X-User-Id: user123"
```

Response: (empty body, no content)

---

## ⚠️ Error Handling

The application includes global exception handling. Common error scenarios:

### 400 Bad Request
**Cause**: Invalid request body (missing required fields, validation failure) or missing headers

**Example**:
```bash
# Missing X-User-Id header
curl -X POST http://localhost:8080/posts \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Missing header"
  }'
```

**Response**:
```json
{
  "error": "Bad Request",
  "details": "Required request header 'X-User-Id' for method parameter of type String is not present"
}
```

### 400 Bad Request - Blank Text
**Cause**: Text field is empty or null

**Example**:
```bash
curl -X POST http://localhost:8080/posts \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{
    "text": ""
  }'
```

**Response**:
```json
{
  "error": "Validation failed",
  "details": "text must not be blank"
}
```

### 404 Not Found
**Cause**: Post ID doesn't exist

**Example**:
```bash
curl -X PUT http://localhost:8080/posts/nonexistent_id \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{
    "text": "Update text"
  }'
```

**Response**:
```json
{
  "error": "Not Found",
  "details": "Post not found"
}
```

---

## 📁 Project Structure

```
instagram-lite-docker/
├── pom.xml                                          # Maven configuration
├── README.md                                        # This file
├── src/
│   ├── main/
│   │   ├── java/com/mnc/instagram/
│   │   │   ├── InstagramLiteDockerApplication.java
│   │   │   └── posts/
│   │   │       ├── api/
│   │   │       │   ├── CreatePostRequest.java
│   │   │       │   └── UpdatePostRequest.java
│   │   │       ├── controller/
│   │   │       │   ├── PostsController.java
│   │   │       │   └── UsersController.java
│   │   │       ├── event/
│   │   │       │   ├── PostCreatedEvent.java
│   │   │       │   ├── PostDeletedEvent.java
│   │   │       │   ├── PostEvent.java
│   │   │       │   └── PostUpdatedEvent.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── PostNotFoundException.java
│   │   │       ├── infrastructure/
│   │   │       │   └── messaging/
│   │   │       │       ├── KafkaBroker.java
│   │   │       │       └── PostsKafkaConsumer.java
│   │   │       ├── model/
│   │   │       │   └── Post.java
│   │   │       └── service/
│   │   │           ├── PostsService.java
│   │   │           └── TimelineService.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── java/
└── target/
    └── instagram-lite-docker-0.0.1-SNAPSHOT.jar
```

### Directory Descriptions
- **api/**: Request/Response DTOs for API contracts
- **controller/**: REST endpoint handlers
- **service/**: Business logic and domain operations
- **repository/**: Data access layer (in-memory storage)
- **model/**: Domain model classes
- **event/**: Event classes for event-driven architecture
- **exception/**: Custom exceptions and global error handling
- **infrastructure/messaging/**: Kafka-like message broker implementation

---

## ⚙️ Configuration

The application uses `src/main/resources/application.yaml` for configuration:

```yaml
server:
  port: 8080

spring:
  application:
    name: instagram-lite-service
```

### Change Server Port
Edit `application.yaml`:
```yaml
server:
  port: 9090
```

Or pass as command-line argument:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

Or when running JAR:
```bash
java -jar target/instagram-lite-in-memory-0.0.1-SNAPSHOT.jar --server.port=9090
```

---

## 📚 Dependencies

### Core Dependencies
- **spring-boot-starter-web** (v3.2.5): REST API support
- **spring-boot-starter-validation**: Input validation with Jakarta Validation API
- **lombok**: Reduce boilerplate code with annotations

### Test Dependencies
- **spring-boot-starter-test**: Testing framework (JUnit 5, Mockito)

### Java Version
- **Java 17**: Required version for compilation and runtime

---

## 🔄 Event-Driven Architecture Details

### PostEvent Hierarchy
```
PostEvent (Parent)
├── PostCreatedEvent
├── PostUpdatedEvent
└── PostDeletedEvent
```

### Repositories
- **PostsByIdRepository**: Main storage for all posts (Primary database)
- **PostsByUserRepository**: User-indexed posts (Timeline database)

### Messaging Flow

**1. Create Post**
```
Request → PostsService.createPost() → PostsByIdRepository.save()
       → PostCreatedEvent → KafkaBroker → PostsKafkaConsumer
       → PostsByUserRepository.save() (Timeline update)
```

**2. Update Post**
```
Request → PostsService.updatePost() → PostsByIdRepository.save()
       → PostUpdatedEvent → KafkaBroker → PostsKafkaConsumer
       → PostsByUserRepository.update() (Timeline update)
```

**3. Delete Post**
```
Request → PostsService.deletePost() → PostsByIdRepository.delete()
       → PostDeletedEvent → KafkaBroker → PostsKafkaConsumer
       → PostsByUserRepository.delete() (Timeline removal)
```

---

## 🐛 Troubleshooting

### Port Already in Use
If port 8080 is already in use:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

Or find and kill the process using port 8080:
```bash
# macOS/Linux
lsof -i :8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Build Fails
Try cleaning the build directory:
```bash
mvn clean install -DskipTests
```

### Tests Fail
Ensure Java 17 is being used:
```bash
java -version
# Should show: openjdk version "17.x.x"
```

### Timeline Not Showing Posts
The timeline is updated asynchronously via the Kafka consumer. There may be a slight delay. Wait a moment and retry:
```bash
sleep 1
curl -X GET http://localhost:8080/users/user123/timeline
```

### Events Not Processing
Check that the application is running and logs show:
```
Consuming PostCreatedEvent, id=...
Consuming PostUpdatedEvent, id=...
Consuming PostDeletedEvent, id=...
```

Enable Spring Boot logging:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.root=DEBUG"
```

---

## 📝 Validation Rules

### Create Post Request
- `text`: Required, must not be blank (min 1 character)

### Update Post Request
- `text`: Required, must not be blank (min 1 character)

### Path Parameters
- `postId`: Must be a valid UUID fragment (format: alphanumeric)
- `userId`: Any non-empty string

### Headers
- `X-User-Id`: Required for all post operations, any non-empty string

---

## 🔐 Security Considerations

This is a demo/learning project. For production:
- Implement authentication and authorization
- Use Spring Security with JWT tokens
- Validate user ownership before updates/deletes
- Use proper database (PostgreSQL, MongoDB)
- Use real message broker (Kafka, RabbitMQ)
- Add rate limiting and API quotas
- Implement logging and monitoring
- Add CORS configuration

---

## 📄 License

This project is part of the Instagram Lite service suite.

---

## 👥 Support

For issues, questions, or improvements, please refer to the project documentation or contact the development team.

---

## 🧹 Complete Cleanup

```bash
# Stop all services and remove containers
docker compose down -v

# Remove Docker image
docker rmi instagram-lite:latest

# Remove Cassandra volume
docker volume prune -a

# Clean Maven build artifacts
mvn clean

# Complete system cleanup
docker system prune -a --volumes
```

---

## 🛠️ Troubleshooting

### Cassandra Connection Issues
```bash
# Check if Cassandra is running
docker ps | grep cassandra

# Check Cassandra logs
docker logs cassandra-instagram-lite-docker

# Restart Cassandra
docker compose restart cassandra

# Wait and retry connection
sleep 30
docker exec -it cassandra-instagram-lite-docker cqlsh
```

### Port Already in Use
```bash
# Find process using port 9042 (Cassandra)
lsof -i :9042

# Find process using port 9092 (Kafka)
lsof -i :9092

# Find process using port 8080 (Application)
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Cassandra Keyspace Not Found
```bash
# Drop and recreate keyspace
docker exec -it cassandra-instagram-lite-docker cqlsh <<EOF
DROP KEYSPACE IF EXISTS instagram;

CREATE KEYSPACE IF NOT EXISTS instagram 
WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE instagram;

-- Recreate tables...
EOF
```

### View All Running Containers
```bash
docker compose ps -a
docker ps -a
```

---

## 📚 Database Schema Reference

### posts_by_id Table
| Column | Type | Key | Purpose |
|--------|------|-----|---------|
| post_id | TEXT | PRIMARY | Unique post identifier |
| user_id | TEXT | | Post owner's user ID |
| text | TEXT | | Post content |
| created_at | TIMESTAMP | | Post creation timestamp |

### posts_by_user Table
| Column | Type | Key | Purpose |
|--------|------|-----|---------|
| user_id | TEXT | PARTITION | User identifier |
| created_at | TIMESTAMP | CLUSTER | Post creation time (DESC order) |
| post_id | TEXT | | Reference to post ID |
| text | TEXT | | Post content |

---

## 📞 Support

For issues or questions, check:
1. Docker Compose logs: `docker compose logs`
2. Application logs: `docker logs <container_id>`
3. Cassandra shell: `docker exec -it cassandra-instagram-lite-docker cqlsh`

---

## 📄 License

This project is licensed under the MIT License.

---

## 🚀 Quick Reference Guide

### One-Command Setup
```bash
chmod +x scripts/*.sh && ./scripts/docker-setup.sh && ./scripts/app-start
```

### Essential Commands

#### Docker Compose
| Command | Purpose |
|---------|---------|
| `docker compose up -d` | Start all services (Cassandra & Kafka) |
| `docker compose down -v` | Stop all services and remove volumes |
| `docker compose ps` | List running services |
| `docker compose logs -f` | View logs in real-time |
| `docker compose restart cassandra` | Restart Cassandra |

#### Cassandra
| Command | Purpose |
|---------|---------|
| `docker exec -it cassandra-instagram-lite-docker cqlsh` | Enter Cassandra shell |
| `docker exec -it cassandra-instagram-lite-docker cqlsh < scripts/cassandra-init.cql` | Initialize schema |
| `cqlsh localhost 9042` | Connect from local machine |

#### Cassandra CQL Queries
| Query | Purpose |
|-------|---------|
| `SELECT * FROM instagram.posts_by_id;` | View all posts |
| `SELECT * FROM instagram.posts_by_user WHERE user_id = 'user123';` | View user's timeline |
| `TRUNCATE instagram.posts_by_id;` | Clear all posts |
| `DESCRIBE TABLES;` | List all tables |

#### Application
| Command | Purpose |
|---------|---------|
| `./scripts/app-start` | Build and run application |
| `mvn spring-boot:run` | Run application directly |
| `java -jar target/instagram-lite-docker-0.0.1-SNAPSHOT.jar` | Run built JAR |
| `mvn clean install` | Build the project |

#### API Testing
| Command | Purpose |
|---------|---------|
| `curl -X POST http://localhost:8080/posts -H "Content-Type: application/json" -H "X-User-Id: user123" -d '{"text": "Hello"}'` | Create post |
| `curl -X GET http://localhost:8080/users/user123/timeline` | Get user timeline |
| `curl -X DELETE http://localhost:8080/posts/{postId} -H "X-User-Id: user123"` | Delete post |

### Port Reference
| Service | Port | Container Name |
|---------|------|-----------------|
| Spring Boot App | 8080 | N/A |
| Cassandra | 9042 | cassandra-instagram-lite-docker |
| Kafka Broker | 9092 | kafka-instagram-lite-docker |

### File Locations
| File | Purpose |
|------|---------|
| `docker-compose.yml` | Docker Compose configuration |
| `pom.xml` | Maven configuration |
| `scripts/docker-setup.sh` | Automated setup script |
| `scripts/cassandra-init.cql` | Cassandra schema |
| `scripts/kafka-init.sh` | Kafka topic initialization |
| `scripts/app-start` | Application startup script |
| `src/main/resources/application.yaml` | Spring Boot configuration |

### Directory Structure
```
instagram-lite-docker/
├── scripts/                    # Helper scripts
│   ├── docker-setup.sh        # Main setup script
│   ├── cassandra-init.cql     # Database schema
│   ├── kafka-init.sh          # Kafka configuration
│   └── app-start              # Application startup
├── src/
│   ├── main/
│   │   ├── java/              # Java source code
│   │   └── resources/         # Application configuration
│   └── test/
├── docker-compose.yml         # Docker services
├── dockerfile                 # Application Docker image
├── pom.xml                    # Maven dependencies
└── README.md                  # This file
```

### Health Checks
```bash
# Check if all services are running
docker compose ps

# Check Cassandra health
docker exec cassandra-instagram-lite-docker \
  cqlsh -e "SELECT release_version FROM system.local;"

# Check Kafka health
docker exec kafka-instagram-lite-docker \
  kafka-broker-api-versions --bootstrap-server localhost:9092

# Check application health
curl http://localhost:8080/actuator/health 2>/dev/null || echo "App may not be running"
```

### Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Cassandra not ready | Wait 15-20 seconds, then run schema init |
| Port already in use | Kill process using `lsof -i :PORT` and `kill -9 PID` |
| Docker daemon not running | Start Docker Desktop or Docker daemon |
| Schema already exists | Run `docker compose down -v` before setup |
| Kafka topic not found | Run `./scripts/kafka-init.sh` |
| Build fails | Run `mvn clean install -DskipTests` |

### Environment Variables
```bash
# Docker Compose creates these automatically:
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
CASSANDRA_CONTACT_POINTS=cassandra
CASSANDRA_PORT=9042
```

### Logs Location
```bash
# Docker Compose logs
docker compose logs

# Specific service logs
docker logs cassandra-instagram-lite-docker
docker logs kafka-instagram-lite-docker

# Application logs (when running via Maven)
# Printed to console

# Application logs (when running as JAR)
# Printed to console or redirected to file
java -jar target/instagram-lite-docker-0.0.1-SNAPSHOT.jar > app.log 2>&1
```

---

## 📖 Documentation Overview

- **Getting Started**: See [Build & Run](#build--run)
- **Docker Setup**: See [Docker Commands](#docker-commands)
- **Database**: See [Cassandra Database Setup](#cassandra-database-setup)
- **API Usage**: See [API Endpoints](#api-endpoints)
- **Troubleshooting**: See [Troubleshooting](#troubleshooting)
- **Scripts**: See [Automated Setup Scripts](#automated-setup-scripts)

---

**Last Updated**: March 1, 2026
**Project**: Instagram Lite - Docker & Cassandra Setup
**Version**: 0.0.1-SNAPSHOT

