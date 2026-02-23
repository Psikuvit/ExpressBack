# Express Delivery API

A comprehensive delivery service API built with Spring Boot that handles authentication, checkout, price calculation, delivery guy management, and location tracking.

## Features

- **JWT Authentication** with refresh tokens
- **Order Checkout** with automatic delivery guy assignment
- **Price Calculation** based on product size and distance
- **Delivery Guy Management** with nearest location tracking
- **User Location** tracking from Expo or any mobile app
- **Distance Calculation** using Haversine formula

## Technology Stack

- Spring Boot 4.0.3
- Spring Security with JWT
- PostgreSQL Database
- JPA/Hibernate
- Lombok
- Maven

## Prerequisites

- Java 17 or higher
- PostgreSQL database
- Maven

## Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE express_db;
```

2. Update `application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/express_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Running the Application

```bash
./mvnw spring-boot:run
```

Or on Windows:
```bash
mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Authentication API (`/api/auth`)

#### Register a new user
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Logout
```http
POST /api/auth/logout
Authorization: Bearer <your_jwt_token>
```

### 2. Checkout API (`/api/checkout`)

Creates an order, validates products, assigns nearest delivery guy, and calculates total price.

```http
POST /api/checkout
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "products": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ],
  "deliveryLocation": {
    "latitude": 40.7128,
    "longitude": -74.0060,
    "address": "123 Main St, New York, NY"
  }
}
```

Response:
```json
{
  "orderId": 1,
  "message": "Order created successfully and assigned to delivery guy",
  "items": [
    {
      "productId": 1,
      "productName": "Pizza Margherita",
      "size": "MEDIUM",
      "quantity": 2,
      "basePrice": 12.99,
      "available": true
    }
  ],
  "totalPrice": 35.48,
  "distance": 5.2,
  "assignedDeliveryGuy": {
    "id": 1,
    "name": "John Smith",
    "age": 28,
    "car": "Honda Civic",
    "nearestLocation": {
      "latitude": 40.7128,
      "longitude": -74.0060,
      "address": "New York, NY"
    },
    "available": false,
    "distanceFromUser": 5.2
  }
}
```

### 3. Price Calculation API (`/api/checkout/calc`)

Calculates the price without creating an order.

```http
POST /api/checkout/calc
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "products": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "userLocation": {
    "latitude": 40.7128,
    "longitude": -74.0060
  }
}
```

Response:
```json
{
  "basePrice": 25.98,
  "sizeFee": 5.00,
  "distanceFee": 2.60,
  "totalPrice": 33.58,
  "distance": 5.2,
  "breakdown": "Base Price: $25.98 | Size Fee: $5.00 | Distance Fee (5.20 km): $2.60"
}
```

**Pricing Logic:**
- Base Price: Product price × quantity
- Size Fee: 
  - SMALL: $1.00 per item
  - MEDIUM: $2.50 per item
  - BIG: $5.00 per item
- Distance Fee: $0.50 per kilometer

### 4. Delivery Guys API (`/api/deliveryguys`)

Returns all delivery guys with their information and distance from user (if location provided).

```http
GET /api/deliveryguys?latitude=40.7128&longitude=-74.0060
Authorization: Bearer <your_jwt_token>
```

Response:
```json
[
  {
    "id": 1,
    "name": "John Smith",
    "age": 28,
    "car": "Honda Civic",
    "nearestLocation": {
      "latitude": 40.7128,
      "longitude": -74.0060,
      "address": "New York, NY"
    },
    "available": true,
    "distanceFromUser": 0.0
  },
  {
    "id": 2,
    "name": "Maria Garcia",
    "age": 32,
    "car": "Toyota Corolla",
    "nearestLocation": {
      "latitude": 40.7589,
      "longitude": -73.9851,
      "address": "Times Square, NY"
    },
    "available": true,
    "distanceFromUser": 5.8
  }
]
```

### 5. Location API (`/api/location`)

#### Update User Location (from Expo/Mobile App)
```http
POST /api/location
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "latitude": 40.7128,
  "longitude": -74.0060,
  "address": "New York, NY"
}
```

Response:
```json
{
  "location": {
    "latitude": 40.7128,
    "longitude": -74.0060,
    "address": "New York, NY"
  },
  "message": "Location updated successfully"
}
```

#### Get User Location
```http
GET /api/location
Authorization: Bearer <your_jwt_token>
```

Response:
```json
{
  "location": {
    "latitude": 40.7128,
    "longitude": -74.0060,
    "address": "New York, NY"
  },
  "message": "Location retrieved successfully"
}
```

## Sample Data

The application automatically creates sample data on first run:

**Test User:**
- Username: `testuser`
- Password: `password123`

**Products:**
- Pizza Margherita (Medium) - $12.99
- Burger Deluxe (Small) - $8.99
- Family Meal Box (Big) - $29.99
- Salad Bowl (Small) - $6.99
- Pasta Carbonara (Medium) - $11.99

**Delivery Guys:**
- John Smith, 28, Honda Civic
- Maria Garcia, 32, Toyota Corolla
- Ahmed Hassan, 25, Ford Focus
- Lisa Chen, 30, Mazda 3

## Integration with Expo/React Native

Example of sending location from Expo app:

```javascript
import * as Location from 'expo-location';

async function updateLocationToBackend() {
  let { status } = await Location.requestForegroundPermissionsAsync();
  if (status !== 'granted') {
    console.log('Permission to access location was denied');
    return;
  }

  let location = await Location.getCurrentPositionAsync({});
  
  const response = await fetch('http://localhost:8080/api/location', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${yourJwtToken}`
    },
    body: JSON.stringify({
      latitude: location.coords.latitude,
      longitude: location.coords.longitude,
      address: 'Your address here'
    })
  });
  
  const data = await response.json();
  console.log(data);
}
```

## Error Handling

All endpoints return appropriate HTTP status codes:
- `200 OK` - Success
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Security

- Passwords are encrypted using BCrypt
- JWT tokens expire after 24 hours
- Refresh tokens expire after 7 days
- All endpoints except `/api/auth/**` require authentication
- CORS enabled for cross-origin requests

## License

This project is open source and available under the MIT License.

