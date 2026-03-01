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
instagram-lite-in-memory/
├── pom.xml                                          # Maven configuration
├── README.md                                        # This file
├── src/
│   ├── main/
│   │   ├── java/com/mnc/instagram/
│   │   │   ├── InstagramLiteInMemoryApplication.java
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
    └── instagram-lite-in-memory-0.0.1-SNAPSHOT.jar
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
docker-compose down -v

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
docker-compose restart cassandra

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
docker-compose ps -a
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
1. Docker Compose logs: `docker-compose logs`
2. Application logs: `docker logs <container_id>`
3. Cassandra shell: `docker exec -it cassandra-instagram-lite-docker cqlsh`

---

## 📄 License

This project is licensed under the MIT License.

