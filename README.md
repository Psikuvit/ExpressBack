# Express Delivery API 🚀

A comprehensive Spring Boot-based delivery service API with JWT authentication, automatic delivery assignment, price calculation, and real-time location tracking.

## 🎯 Quick Overview

This API provides **5 complete endpoints** for building a delivery service application:

1. **`/api/auth`** - Full authentication system with JWT and refresh tokens
2. **`/api/checkout`** - Smart checkout with automatic delivery guy assignment
3. **`/api/checkout/calc`** - Price calculation based on size and distance
4. **`/api/deliveryguys`** - Get all delivery personnel with location tracking
5. **`/api/location`** - User location updates (Expo/React Native compatible)

## ✨ Key Features

- ✅ **JWT Authentication** with refresh tokens
- ✅ **Automatic Delivery Assignment** to nearest available driver
- ✅ **WhatsApp Notifications** - Instant order alerts to delivery guys via Twilio
- ✅ **Smart Price Calculation** based on product size and distance
- ✅ **Real-time Location Tracking** with Haversine distance calculation
- ✅ **Mobile App Ready** - Works seamlessly with Expo/React Native
- ✅ **Complete CRUD Operations** for orders, products, and delivery guys
- ✅ **Sample Data Included** - Ready to test immediately
- ✅ **Rate Limiting** - Prevents abuse and brute force attacks

## 🚀 Quick Start

### Prerequisites
- Java 17+
- PostgreSQL
- Maven (included via wrapper)

### 1. Setup Database
```sql
CREATE DATABASE express_db;
```

### 2. Configure Application
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

### 3. Run Application
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 4. Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

## 📚 Documentation

- **[Quick Start Guide](QUICK_START.md)** - Step-by-step setup instructions
- **[API Documentation](API_DOCUMENTATION.md)** - Complete API reference with examples
- **[Implementation Summary](IMPLEMENTATION_SUMMARY.md)** - Technical details and architecture
- **[Environment Setup](ENVIRONMENT_SETUP.md)** - Configuration and deployment guide
- **[Postman Collection](Express_Delivery_API.postman_collection.json)** - Import and test immediately

## 🏗️ Technology Stack

- **Spring Boot 4.0.3** - Latest Spring framework
- **Spring Security** - JWT authentication
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Database
- **JJWT 0.12.3** - JWT token management
- **Twilio SDK 10.0.0** - WhatsApp integration
- **Bucket4j 8.7.0** - Rate limiting
- **Lombok** - Clean code
- **Maven** - Build tool

## 📱 Mobile Integration

Perfect for Expo/React Native apps:

```javascript
// Update location from mobile app
const updateLocation = async () => {
  const location = await Location.getCurrentPositionAsync({});
  
  await fetch('http://your-server:8080/api/location', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      latitude: location.coords.latitude,
      longitude: location.coords.longitude,
      address: 'User address'
    })
  });
};
```

## 🎮 Testing

### Import Postman Collection
1. Open Postman
2. Import `Express_Delivery_API.postman_collection.json`
3. Test all endpoints with pre-configured requests

### Default Test User
- **Username:** `testuser`
- **Password:** `password123`

## 📦 Sample Data

The application automatically creates:
- **5 Products** (Pizza, Burger, Family Meal Box, Salad, Pasta)
- **4 Delivery Guys** (John, Maria, Ahmed, Lisa)
- **1 Test User** for immediate testing

## 🔒 Security

- Passwords encrypted with BCrypt
- JWT tokens (24h expiry)
- Refresh tokens (7 days expiry)
- CORS enabled
- Stateless authentication
- **Rate Limiting** (Bucket4j)
  - Auth endpoints: 5 requests/minute (prevents brute force)
  - API endpoints: 100 requests/minute (prevents abuse)
  - Per-IP tracking

## 📊 API Endpoints Summary

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/auth/signup` | POST | Register new user | No |
| `/api/auth/login` | POST | Login and get tokens | No |
| `/api/auth/refresh` | POST | Refresh JWT token | No |
| `/api/auth/logout` | POST | Logout user | Yes |
| `/api/checkout` | POST | Create order | Yes |
| `/api/checkout/calc` | POST | Calculate price | Yes |
| `/api/deliveryguys` | GET | Get delivery guys | Yes |
| `/api/location` | POST/GET | Update/Get location | Yes |

## 💡 Example Requests

### Login
```bash
POST /api/auth/login
{
  "username": "testuser",
  "password": "password123"
}
```

### Create Order
```bash
POST /api/checkout
Authorization: Bearer {token}
{
  "products": [
    {"productId": 1, "quantity": 2}
  ],
  "deliveryLocation": {
    "latitude": 40.7128,
    "longitude": -74.0060,
    "address": "New York, NY"
  }
}
```

### Get Delivery Guys
```bash
GET /api/deliveryguys?latitude=40.7128&longitude=-74.0060
Authorization: Bearer {token}
```

## 🗂️ Project Structure

```
Express/
├── src/main/java/me/psikuvit/express/
│   ├── controller/      # REST endpoints (5 controllers)
│   ├── service/         # Business logic
│   ├── repository/      # Data access
│   ├── model/          # JPA entities
│   ├── dto/            # Request/Response objects
│   ├── security/       # JWT & authentication
│   └── config/         # App configuration
├── API_DOCUMENTATION.md
├── QUICK_START.md
└── Express_Delivery_API.postman_collection.json
```

## 🎯 Features in Detail

### 1. Authentication System
- Complete user registration and login
- JWT-based authentication
- Refresh token mechanism
- Secure password hashing

### 2. Smart Checkout
- Validates product availability
- Finds nearest delivery guy automatically
- Calculates distance using Haversine formula
- Creates order with complete tracking

### 3. Dynamic Pricing
- Base product price
- Size-based fees (Small: $1, Medium: $2.5, Big: $5)
- Distance-based delivery fee ($0.50/km)
- Detailed price breakdown

### 4. Delivery Management
- Real-time delivery guy locations
- Availability tracking
- Distance-based sorting
- Automatic assignment

### 5. Location Services
- Mobile app location updates
- GPS coordinate storage
- Address management
- Distance calculations

## 🚢 Production Deployment

See [ENVIRONMENT_SETUP.md](ENVIRONMENT_SETUP.md) for:
- Docker configuration
- Environment variables
- Security best practices
- Production settings

## 📝 License

This project is open source and available under the MIT License.

## 🤝 Contributing

Feel free to submit issues and enhancement requests!

## 📞 Support

For detailed documentation, check out the following files:
- [QUICK_START.md](QUICK_START.md) - Get started quickly
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Complete API reference
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Technical details

---

**Built with ❤️ using Spring Boot 4.0.3**

Ready to use • Production-ready • Mobile-friendly • Well-documented

