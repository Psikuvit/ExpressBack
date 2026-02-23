# Express Delivery API - Quick Start Guide

## Overview
You've successfully created a comprehensive delivery service API with 5 main endpoints:

### API Endpoints Summary:

1. **`/api/auth`** - Authentication system with JWT & refresh tokens
   - POST /api/auth/signup - Register new user
   - POST /api/auth/login - Login and get JWT token
   - POST /api/auth/refresh - Refresh expired token
   - POST /api/auth/logout - Logout user

2. **`/api/checkout`** - Order checkout with automatic delivery assignment
   - POST /api/checkout - Create order, validate products, assign nearest delivery guy

3. **`/api/checkout/calc`** - Price calculation without creating order
   - POST /api/checkout/calc - Calculate total price based on products, size, and distance

4. **`/api/deliveryguys`** - Get all delivery guys with locations
   - GET /api/deliveryguys - Returns all delivery guys sorted by distance (if location provided)

5. **`/api/location`** - User location management (for Expo/mobile apps)
   - POST /api/location - Update user location from mobile app
   - GET /api/location - Get current user location

## Getting Started

### 1. Setup PostgreSQL Database
```sql
CREATE DATABASE express_db;
```

### 2. Update Database Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/express_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

### 3. Run the Application
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

The app will:
- Start on port 8080
- Automatically create database tables
- Seed sample data (products, delivery guys, test user)

### 4. Test the API

#### Sample Request Flow:

**Step 1: Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Step 2: Update Location (from mobile app)**
```bash
curl -X POST http://localhost:8080/api/location \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "latitude": 40.7128,
    "longitude": -74.0060,
    "address": "New York, NY"
  }'
```

**Step 3: Get Available Delivery Guys**
```bash
curl -X GET "http://localhost:8080/api/deliveryguys?latitude=40.7128&longitude=-74.0060" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Step 4: Calculate Price**
```bash
curl -X POST http://localhost:8080/api/checkout/calc \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "products": [
      {"productId": 1, "quantity": 2}
    ],
    "userLocation": {
      "latitude": 40.7128,
      "longitude": -74.0060
    }
  }'
```

**Step 5: Checkout**
```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "products": [
      {"productId": 1, "quantity": 2}
    ],
    "deliveryLocation": {
      "latitude": 40.7128,
      "longitude": -74.0060,
      "address": "123 Main St, New York, NY"
    }
  }'
```

## Sample Data Created Automatically

**Test User:**
- Username: `testuser`
- Password: `password123`

**Products (5 items):**
- Pizza Margherita (Medium) - $12.99
- Burger Deluxe (Small) - $8.99
- Family Meal Box (Big) - $29.99
- Salad Bowl (Small) - $6.99
- Pasta Carbonara (Medium) - $11.99

**Delivery Guys (4 drivers):**
- John Smith, 28, Honda Civic
- Maria Garcia, 32, Toyota Corolla
- Ahmed Hassan, 25, Ford Focus
- Lisa Chen, 30, Mazda 3

## Features Implemented

✅ JWT Authentication with refresh tokens
✅ Session management
✅ Product validation and availability checks
✅ Automatic nearest delivery guy assignment
✅ Distance calculation using Haversine formula
✅ Dynamic price calculation based on:
   - Product base price
   - Product size (Small: +$1, Medium: +$2.5, Big: +$5 per item)
   - Distance ($0.50 per km)
✅ Location tracking from mobile apps (Expo compatible)
✅ CORS enabled for cross-origin requests
✅ PostgreSQL database with JPA
✅ Comprehensive error handling

## Mobile App Integration (Expo Example)

```javascript
import * as Location from 'expo-location';

// Get and send location to backend
const updateLocation = async (token) => {
  const { status } = await Location.requestForegroundPermissionsAsync();
  if (status !== 'granted') return;

  const location = await Location.getCurrentPositionAsync({});
  
  await fetch('http://YOUR_SERVER:8080/api/location', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      latitude: location.coords.latitude,
      longitude: location.coords.longitude,
      address: 'User address'
    })
  });
};
```

## Architecture

```
├── controller/      # REST API endpoints
├── service/         # Business logic
├── repository/      # Database access
├── model/           # Entity classes
├── dto/             # Data transfer objects
├── security/        # JWT & authentication
└── config/          # Configuration & data initialization
```

## Important Notes

1. **Security**: Change the JWT secret in `application.properties` for production
2. **Database**: Tables are auto-created on first run (hibernate ddl-auto=update)
3. **CORS**: Currently allows all origins - restrict for production
4. **Tokens**: JWT expires in 24h, Refresh token in 7 days
5. **Distance**: Calculated in kilometers using lat/long coordinates

## Next Steps

1. Set up your PostgreSQL database
2. Update application.properties with your credentials
3. Run the application
4. Test with the provided test user
5. Integrate with your Expo mobile app
6. Customize products and delivery guys as needed

For detailed API documentation, see `API_DOCUMENTATION.md`

