# Express Delivery API - Architecture Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          CLIENT APPLICATIONS                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                 │
│  │   Mobile     │    │   Web App    │    │   Postman    │                 │
│  │ (Expo/React) │    │  (Browser)   │    │   Client     │                 │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘                 │
│         │                   │                   │                          │
│         └───────────────────┼───────────────────┘                          │
│                             │                                              │
│                      HTTP/HTTPS Requests                                   │
│                      (JSON Payload)                                        │
└─────────────────────────────┼────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        API GATEWAY LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │                    SPRING BOOT 4.0.3                               │   │
│  │                    Port: 8080                                      │   │
│  │                    CORS: Enabled                                   │   │
│  └────────────────────────────────────────────────────────────────────┘   │
│                              │                                              │
└──────────────────────────────┼──────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SECURITY LAYER                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │              Spring Security 7.x Configuration                   │     │
│  ├──────────────────────────────────────────────────────────────────┤     │
│  │                                                                   │     │
│  │  ┌────────────────┐         ┌─────────────────┐                │     │
│  │  │RateLimitFilter │ ───────▶│RateLimitService │                │     │
│  │  │  (IP-based)    │         │  (Bucket4j)     │                │     │
│  │  └────────┬───────┘         └─────────────────┘                │     │
│  │           │                                                      │     │
│  │           │  Auth: 5 req/min, API: 100 req/min                 │     │
│  │           ▼                                                      │     │
│  │  ┌────────────────┐         ┌─────────────────┐                │     │
│  │  │ AuthTokenFilter│ ───────▶│    JwtUtils     │                │     │
│  │  │  (JWT Check)   │         │ (Token Verify)  │                │     │
│  │  └────────┬───────┘         └─────────────────┘                │     │
│  │           │                                                      │     │
│  │           ▼                                                      │     │
│  │  ┌────────────────────────┐                                     │     │
│  │  │ UserDetailsServiceImpl │                                     │     │
│  │  │  (Load User Details)   │                                     │     │
│  │  └────────────────────────┘                                     │     │
│  │                                                                   │     │
│  │  Protects: All endpoints except /api/auth/**                    │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└──────────────────────────────┼──────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        CONTROLLER LAYER                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌───────────────────┐  ┌──────────────────┐  ┌──────────────────┐        │
│  │  AuthController   │  │CheckoutController│  │ LocationController│       │
│  │  /api/auth/**     │  │  /api/checkout** │  │  /api/location    │       │
│  ├───────────────────┤  ├──────────────────┤  ├──────────────────┤        │
│  │ - signup()        │  │ - checkout()     │  │ - updateLocation()│       │
│  │ - login()         │  │ - calcPrice()    │  │ - getLocation()   │       │
│  │ - refresh()       │  └──────────────────┘  └──────────────────┘        │
│  │ - logout()        │                                                      │
│  └───────────────────┘  ┌──────────────────────────────────────┐          │
│                         │   DeliveryGuyController              │          │
│                         │   /api/deliveryguys                   │          │
│                         ├──────────────────────────────────────┤          │
│                         │ - getAllDeliveryGuys()                │          │
│                         │   (with location sorting)             │          │
│                         └──────────────────────────────────────┘          │
│                                                                              │
└──────────────────────────────┼──────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SERVICE LAYER                                      │
│                        (Business Logic)                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                   RefreshTokenService                             │     │
│  │  - createRefreshToken()                                           │     │
│  │  - verifyExpiration()                                             │     │
│  │  - deleteByUserId()                                               │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                    CheckoutService                                │     │
│  │  ┌──────────────────────────────────────────────────────┐        │     │
│  │  │ processCheckout()                                     │        │     │
│  │  │  1. Validate products (availability check)           │        │     │
│  │  │  2. Find nearest delivery guy                        │        │     │
│  │  │  3. Calculate distance (Haversine formula)           │        │     │
│  │  │  4. Calculate total price                            │        │     │
│  │  │  5. Create order                                     │        │     │
│  │  │  6. Assign delivery guy                              │        │     │
│  │  │  7. Mark delivery guy as unavailable                 │        │     │
│  │  │  8. Send WhatsApp notification to delivery guy       │        │     │
│  │  └──────────────────────────────────────────────────────┘        │     │
│  │  ┌──────────────────────────────────────────────────────┐        │     │
│  │  │ calculatePrice()                                      │        │     │
│  │  │  - Base price calculation                            │        │     │
│  │  │  - Size fee (Small: $1, Medium: $2.5, Big: $5)      │        │     │
│  │  │  - Distance fee ($0.50/km)                           │        │     │
│  │  │  - Returns breakdown                                 │        │     │
│  │  └──────────────────────────────────────────────────────┘        │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │               DeliveryGuyService                                  │     │
│  │  - getAllDeliveryGuys()                                           │     │
│  │  - Sort by distance from user location                           │     │
│  │  - Calculate distance for each delivery guy                      │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                 LocationService                                   │     │
│  │  - updateUserLocation()                                           │     │
│  │  - getUserLocation()                                              │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │            DistanceCalculationService                             │     │
│  │  ┌──────────────────────────────────────────────────────┐        │     │
│  │  │ calculateDistance(Location from, Location to)        │        │     │
│  │  │  - Uses Haversine Formula                            │        │     │
│  │  │  - Returns distance in kilometers                    │        │     │
│  │  │  - Precision: GPS coordinates (lat/long)             │        │     │
│  │  └──────────────────────────────────────────────────────┘        │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                    WhatsAppService                                │     │
│  │  ┌──────────────────────────────────────────────────────┐        │     │
│  │  │ sendOrderToDeliveryGuy()                             │        │     │
│  │  │  - Sends formatted order details via WhatsApp        │        │     │
│  │  │  - Uses Twilio API                                   │        │     │
│  │  │  - Includes: Order ID, items, price, distance        │        │     │
│  │  │  - Google Maps link for delivery location            │        │     │
│  │  └──────────────────────────────────────────────────────┘        │     │
│  │  ┌──────────────────────────────────────────────────────┐        │     │
│  │  │ sendMessage()                                         │        │     │
│  │  │  - Sends custom WhatsApp messages                    │        │     │
│  │  │  - Generic notification utility                      │        │     │
│  │  └──────────────────────────────────────────────────────┘        │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└──────────────────────────────┼──────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       REPOSITORY LAYER                                      │
│                     (Data Access Objects)                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────┐  ┌───────────────────┐  ┌────────────────────┐      │
│  │ UserRepository   │  │ProductRepository  │  │ OrderRepository    │      │
│  │ (JpaRepository)  │  │ (JpaRepository)   │  │ (JpaRepository)    │      │
│  ├──────────────────┤  ├───────────────────┤  ├────────────────────┤      │
│  │- findByUsername()│  │- findByAvailable()│  │- findByUserId()    │      │
│  │- findByEmail()   │  └───────────────────┘  │- findByDeliveryGuy()│     │
│  │- existsByEmail() │                         └────────────────────┘      │
│  └──────────────────┘                                                       │
│                                                                              │
│  ┌──────────────────────┐  ┌─────────────────────────────────┐            │
│  │DeliveryGuyRepository │  │  RefreshTokenRepository         │            │
│  │   (JpaRepository)    │  │     (JpaRepository)              │            │
│  ├──────────────────────┤  ├─────────────────────────────────┤            │
│  │- findByAvailable()   │  │- findByToken()                  │            │
│  └──────────────────────┘  │- findByUser()                   │            │
│                             │- deleteByUser()                 │            │
│                             └─────────────────────────────────┘            │
│                                                                              │
└──────────────────────────────┼──────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DATA MODEL LAYER                                    │
│                        (JPA Entities)                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                         User Entity                               │     │
│  ├──────────────────────────────────────────────────────────────────┤     │
│  │ - id (Long, PK)                                                   │     │
│  │ - username (String, Unique)                                       │     │
│  │ - email (String, Unique)                                          │     │
│  │ - password (String, Encrypted)                                    │     │
│  │ - roles (Set<String>)                                             │     │
│  │ - location (Location, Embedded)                                   │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                      Product Entity                               │     │
│  ├──────────────────────────────────────────────────────────────────┤     │
│  │ - id (Long, PK)                                                   │     │
│  │ - name (String)                                                   │     │
│  │ - description (String)                                            │     │
│  │ - size (ProductSize: SMALL/MEDIUM/BIG)                           │     │
│  │ - basePrice (Double)                                              │     │
│  │ - available (Boolean)                                             │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                    DeliveryGuy Entity                             │     │
│  ├──────────────────────────────────────────────────────────────────┤     │
│  │ - id (Long, PK)                                                   │     │
│  │ - name (String)                                                   │     │
│  │ - age (Integer)                                                   │     │
│  │ - car (String)                                                    │     │
│  │ - whatsappNumber (String) - with country code                    │     │
│  │ - location (Location, Embedded)                                   │     │
│  │ - available (Boolean)                                             │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                       Order Entity                                │     │
│  ├──────────────────────────────────────────────────────────────────┤     │
│  │ - id (Long, PK)                                                   │     │
│  │ - user (User, ManyToOne)                                          │     │
│  │ - deliveryGuy (DeliveryGuy, ManyToOne)                           │     │
│  │ - items (List<OrderItem>, OneToMany)                             │     │
│  │ - totalPrice (Double)                                             │     │
│  │ - distance (Double)                                               │     │
│  │ - status (OrderStatus: PENDING/ASSIGNED/IN_PROGRESS/DELIVERED)   │     │
│  │ - createdAt (LocalDateTime)                                       │     │
│  │ - deliveryLocation (Location, Embedded)                           │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                     OrderItem Entity                              │     │
│  ├──────────────────────────────────────────────────────────────────┤     │
│  │ - id (Long, PK)                                                   │     │
│  │ - order (Order, ManyToOne)                                        │     │
│  │ - product (Product, ManyToOne)                                    │     │
│  │ - quantity (Integer)                                              │     │
│  │ - price (Double)                                                  │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                   RefreshToken Entity                             │     │
│  ├──────────────────────────────────────────────────────────────────┤     │
│  │ - id (Long, PK)                                                   │     │
│  │ - user (User, OneToOne)                                           │     │
│  │ - token (String, Unique)                                          │     │
│  │ - expiryDate (Instant)                                            │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                    Location (Embeddable)                          │     │
│  ├──────────────────────────────────────────────────────────────────┤     │
│  │ - latitude (Double)                                               │     │
│  │ - longitude (Double)                                              │     │
│  │ - address (String)                                                │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└──────────────────────────────┼──────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        DATABASE LAYER                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐     │
│  │                       PostgreSQL 15+                              │     │
│  ├──────────────────────────────────────────────────────────────────┤     │
│  │                                                                   │     │
│  │  Tables:                                                          │     │
│  │  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐      │     │
│  │  │ users          │  │ products       │  │ orders       │      │     │
│  │  │ user_roles     │  │ delivery_guys  │  │ order_items  │      │     │
│  │  │ refresh_tokens │  └────────────────┘  └──────────────┘      │     │
│  │  └────────────────┘                                              │     │
│  │                                                                   │     │
│  │  Relationships:                                                   │     │
│  │  - User → Order (One-to-Many)                                    │     │
│  │  - DeliveryGuy → Order (One-to-Many)                            │     │
│  │  - Order → OrderItem (One-to-Many)                              │     │
│  │  - Product → OrderItem (One-to-Many)                            │     │
│  │  - User → RefreshToken (One-to-One)                             │     │
│  │                                                                   │     │
│  │  Database: express_db                                            │     │
│  │  Port: 5432                                                      │     │
│  │  Hibernate DDL: update (auto-create/update schema)              │     │
│  └──────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Diagrams

### 1. Authentication Flow (Login)

```
┌─────────┐                                                    ┌──────────┐
│ Client  │                                                    │Database  │
└────┬────┘                                                    └────┬─────┘
     │                                                              │
     │ POST /api/auth/login                                        │
     │ {username, password}                                        │
     ├──────────────────────────▶┌──────────────────┐             │
     │                           │ AuthController   │             │
     │                           └────────┬─────────┘             │
     │                                    │                        │
     │                                    │ authenticate()         │
     │                           ┌────────▼─────────┐             │
     │                           │AuthenticationMgr │             │
     │                           └────────┬─────────┘             │
     │                                    │                        │
     │                                    │ loadUserByUsername()  │
     │                           ┌────────▼─────────┐             │
     │                           │UserDetailsService│             │
     │                           └────────┬─────────┘             │
     │                                    │ Query User            │
     │                                    ├───────────────────────▶│
     │                                    │◀───────────────────────┤
     │                                    │ User + Password        │
     │                           ┌────────▼─────────┐             │
     │                           │  Password Check  │             │
     │                           │  (BCrypt verify) │             │
     │                           └────────┬─────────┘             │
     │                                    │                        │
     │                                    │ generateJwtToken()    │
     │                           ┌────────▼─────────┐             │
     │                           │    JwtUtils      │             │
     │                           └────────┬─────────┘             │
     │                                    │                        │
     │                                    │ createRefreshToken()  │
     │                           ┌────────▼──────────┐            │
     │                           │RefreshTokenService│            │
     │                           └────────┬──────────┘            │
     │                                    │ Save Token            │
     │                                    ├───────────────────────▶│
     │                                    │◀───────────────────────┤
     │◀───────────────────────────────────┤                        │
     │ {token, refreshToken, user info}  │                        │
     │                                                              │
```

### 2. Checkout Flow

```
┌─────────┐                                                    ┌──────────┐
│ Client  │                                                    │Database  │
└────┬────┘                                                    └────┬─────┘
     │                                                              │
     │ POST /api/checkout                                          │
     │ {products[], deliveryLocation}                              │
     │ Authorization: Bearer <token>                               │
     ├──────────────────────────▶┌──────────────────┐             │
     │                           │CheckoutController│             │
     │                           └────────┬─────────┘             │
     │                                    │                        │
     │                                    │ Extract username       │
     │                           ┌────────▼─────────┐             │
     │                           │ CheckoutService  │             │
     │                           └────────┬─────────┘             │
     │                                    │                        │
     │                           ┌────────▼──────────────┐        │
     │                           │ 1. Validate Products  │        │
     │                           │    - Check availability│       │
     │                           │    - Verify IDs        │       │
     │                           └────────┬──────────────┘        │
     │                                    │ Query Products        │
     │                                    ├───────────────────────▶│
     │                                    │◀───────────────────────┤
     │                                    │                        │
     │                           ┌────────▼──────────────┐        │
     │                           │ 2. Find Nearest       │        │
     │                           │    Delivery Guy       │        │
     │                           └────────┬──────────────┘        │
     │                                    │ Get Available         │
     │                                    ├───────────────────────▶│
     │                                    │◀───────────────────────┤
     │                                    │ DeliveryGuys[]        │
     │                           ┌────────▼──────────────┐        │
     │                           │DistanceCalculation   │        │
     │                           │ Service               │        │
     │                           │ (Haversine Formula)   │        │
     │                           └────────┬──────────────┘        │
     │                                    │                        │
     │                           ┌────────▼──────────────┐        │
     │                           │ 3. Calculate Price    │        │
     │                           │    - Base price       │        │
     │                           │    - Size fee         │        │
     │                           │    - Distance fee     │        │
     │                           └────────┬──────────────┘        │
     │                                    │                        │
     │                           ┌────────▼──────────────┐        │
     │                           │ 4. Create Order       │        │
     │                           │    - Set user         │        │
     │                           │    - Assign delivery  │        │
     │                           │    - Add items        │        │
     │                           │    - Set status       │        │
     │                           └────────┬──────────────┘        │
     │                                    │ Save Order            │
     │                                    ├───────────────────────▶│
     │                                    │◀───────────────────────┤
     │                                    │                        │
     │                           ┌────────▼──────────────┐        │
     │                           │ 5. Mark Delivery Guy  │        │
     │                           │    as Unavailable     │        │
     │                           └────────┬──────────────┘        │
     │                                    │ Update Status         │
     │                                    ├───────────────────────▶│
     │                                    │◀───────────────────────┤
     │◀───────────────────────────────────┤                        │
     │ {orderId, items, totalPrice,       │                        │
     │  distance, assignedDeliveryGuy}    │                        │
     │                                                              │
```

### 3. Location Update Flow (From Mobile App)

```
┌──────────┐                                              ┌──────────┐
│Expo App  │                                              │Database  │
└────┬─────┘                                              └────┬─────┘
     │                                                          │
     │ Request GPS Permission                                  │
     │ Get Current Location                                    │
     │ {latitude, longitude}                                   │
     │                                                          │
     │ POST /api/location                                      │
     │ {latitude, longitude, address}                          │
     │ Authorization: Bearer <token>                           │
     ├──────────────────────▶┌──────────────────┐             │
     │                       │LocationController│             │
     │                       └────────┬─────────┘             │
     │                                │                        │
     │                                │ Extract username       │
     │                       ┌────────▼─────────┐             │
     │                       │ LocationService  │             │
     │                       └────────┬─────────┘             │
     │                                │                        │
     │                                │ Find User             │
     │                                ├───────────────────────▶│
     │                                │◀───────────────────────┤
     │                                │                        │
     │                                │ Update Location       │
     │                                ├───────────────────────▶│
     │                                │◀───────────────────────┤
     │◀───────────────────────────────┤                        │
     │ {location, message}            │                        │
     │                                                          │
     │ Location updated successfully!                          │
     │ Now used for:                                           │
     │ - Finding nearest delivery guy                          │
     │ - Distance calculations                                 │
     │ - Price estimates                                       │
     │                                                          │
```

---

## Component Interaction Matrix

| Component | Interacts With | Purpose |
|-----------|----------------|---------|
| **AuthController** | AuthenticationManager, JwtUtils, RefreshTokenService, UserRepository | Handle authentication requests |
| **CheckoutController** | CheckoutService | Process checkout and price calculations |
| **DeliveryGuyController** | DeliveryGuyService | Retrieve delivery guy information |
| **LocationController** | LocationService | Manage user locations |
| **CheckoutService** | ProductRepository, DeliveryGuyRepository, OrderRepository, DistanceCalculationService, WhatsAppService | Orchestrate checkout process and notify delivery guys |
| **RefreshTokenService** | RefreshTokenRepository, UserRepository | Manage refresh tokens |
| **DeliveryGuyService** | DeliveryGuyRepository, DistanceCalculationService | Find and sort delivery guys |
| **LocationService** | UserRepository | Update/retrieve user locations |
| **DistanceCalculationService** | None (Pure calculation) | Calculate distances using Haversine |
| **WhatsAppService** | Twilio API | Send WhatsApp notifications to delivery guys |
| **JwtUtils** | None (Token operations) | Generate and validate JWT tokens |
| **AuthTokenFilter** | JwtUtils, UserDetailsServiceImpl | Intercept and validate requests |
| **UserDetailsServiceImpl** | UserRepository | Load user details for authentication |

---

## Security Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Request Flow                               │
└─────────────────────────────────────────────────────────────────┘

HTTP Request
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  1. AuthTokenFilter (OncePerRequestFilter)                      │
│     - Extract JWT from Authorization header                     │
│     - Validate token using JwtUtils                             │
│     - If valid: load user details                               │
│     - Set SecurityContext with authentication                   │
└─────────────────────┬───────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  2. SecurityFilterChain                                         │
│     - Check if endpoint requires authentication                 │
│     - /api/auth/** → PERMIT ALL                                │
│     - All others → AUTHENTICATED                                │
└─────────────────────┬───────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  3. Controller Method Execution                                 │
│     - SecurityContext populated with user details               │
│     - Controller can access authenticated user                  │
└─────────────────────────────────────────────────────────────────┘

Token Structure:
┌─────────────────────────────────────────────────────────────────┐
│  JWT Token                                                      │
│  ┌───────────┐  ┌─────────────┐  ┌─────────────────┐         │
│  │  Header   │  │   Payload   │  │    Signature     │         │
│  ├───────────┤  ├─────────────┤  ├─────────────────┤         │
│  │ alg: HS256│  │ sub: username│  │ HMACSHA256(     │         │
│  │ typ: JWT  │  │ iat: issued  │  │   base64Url...) │         │
│  └───────────┘  │ exp: expiry  │  └─────────────────┘         │
│                 └─────────────┘                                 │
│                                                                  │
│  Expiration: 24 hours                                           │
│  Secret: Configurable (256-bit minimum)                         │
└─────────────────────────────────────────────────────────────────┘

Refresh Token:
┌─────────────────────────────────────────────────────────────────┐
│  - Stored in database                                           │
│  - UUID format                                                  │
│  - One-to-one with User                                         │
│  - Expiration: 7 days                                           │
│  - Can be used to generate new JWT without re-login            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Price Calculation Algorithm

```
┌─────────────────────────────────────────────────────────────────┐
│                   Price Calculation Formula                     │
└─────────────────────────────────────────────────────────────────┘

Input: List<OrderItem>, User Location

Step 1: Calculate Base Price
┌─────────────────────────────────────────┐
│ basePrice = Σ (product.price × quantity)│
└─────────────────────────────────────────┘

Step 2: Calculate Size Fee
┌──────────────────────────────────────────────────────┐
│ sizeFee = Σ (getSizeFee(product.size) × quantity)   │
│                                                       │
│ getSizeFee(size):                                    │
│   SMALL  → $1.00                                     │
│   MEDIUM → $2.50                                     │
│   BIG    → $5.00                                     │
└──────────────────────────────────────────────────────┘

Step 3: Find Nearest Delivery Guy
┌────────────────────────────────────────────────────┐
│ availableGuys = getAvailableDeliveryGuys()        │
│                                                    │
│ foreach guy in availableGuys:                     │
│   distance = calculateDistance(                   │
│                guy.location,                      │
│                user.location)                     │
│                                                    │
│ nearestGuy = min(distances)                       │
└────────────────────────────────────────────────────┘

Step 4: Calculate Distance Fee
┌─────────────────────────────────────────┐
│ distanceFee = distance × $0.50          │
└─────────────────────────────────────────┘

Step 5: Calculate Total
┌──────────────────────────────────────────────────┐
│ totalPrice = basePrice + sizeFee + distanceFee   │
└──────────────────────────────────────────────────┘

Output: {basePrice, sizeFee, distanceFee, totalPrice, distance}
```

---

## Distance Calculation (Haversine Formula)

```
┌─────────────────────────────────────────────────────────────────┐
│              Haversine Formula Implementation                   │
└─────────────────────────────────────────────────────────────────┘

Input: Location A (lat1, lon1), Location B (lat2, lon2)

Constants:
  EARTH_RADIUS = 6371 km

Step 1: Convert to Radians
┌────────────────────────────────────────────┐
│ latDistance = toRadians(lat2 - lat1)      │
│ lonDistance = toRadians(lon2 - lon1)      │
└────────────────────────────────────────────┘

Step 2: Apply Haversine Formula
┌──────────────────────────────────────────────────────────┐
│ a = sin²(latDistance/2) +                               │
│     cos(lat1) × cos(lat2) × sin²(lonDistance/2)         │
└──────────────────────────────────────────────────────────┘

Step 3: Calculate Angular Distance
┌────────────────────────────────────────────┐
│ c = 2 × atan2(√a, √(1-a))                 │
└────────────────────────────────────────────┘

Step 4: Calculate Distance
┌────────────────────────────────────────────┐
│ distance = EARTH_RADIUS × c                │
└────────────────────────────────────────────┘

Output: distance in kilometers

Accuracy: ±0.5% for distances < 500km
```

---

## Database Schema Relationships

```
┌──────────────────────────────────────────────────────────────────┐
│                    Entity Relationship Diagram                   │
└──────────────────────────────────────────────────────────────────┘

┌─────────────┐              ┌──────────────┐
│    User     │              │RefreshToken  │
├─────────────┤              ├──────────────┤
│ id (PK)     │◀────────────▶│ id (PK)      │
│ username    │   1      1   │ user_id (FK) │
│ email       │              │ token        │
│ password    │              │ expiryDate   │
│ roles       │              └──────────────┘
│ location    │
└──────┬──────┘
       │
       │ 1
       │
       │ *
       ▼
┌─────────────┐
│   Order     │              ┌──────────────┐
├─────────────┤              │ DeliveryGuy  │
│ id (PK)     │  *       1   ├──────────────┤
│ user_id(FK) │◀────────────▶│ id (PK)      │
│ dg_id (FK)  │              │ name         │
│ totalPrice  │              │ age          │
│ distance    │              │ car          │
│ status      │              │ location     │
│ createdAt   │              │ available    │
│ delLocation │              └──────────────┘
└──────┬──────┘
       │
       │ 1
       │
       │ *
       ▼
┌─────────────┐
│  OrderItem  │              ┌──────────────┐
├─────────────┤              │   Product    │
│ id (PK)     │  *       1   ├──────────────┤
│ order_id(FK)│             │ id (PK)      │
│ prod_id(FK) │◀────────────▶│ name         │
│ quantity    │              │ description  │
│ price       │              │ size         │
└─────────────┘              │ basePrice    │
                              │ available    │
                              └──────────────┘

Cascade Rules:
- User deleted → Orders cascade soft delete
- Order deleted → OrderItems cascade delete
- DeliveryGuy deleted → Orders set dg_id = null
- Product deleted → Prevent if in OrderItems
```

---

## Technology Stack Details

```
┌──────────────────────────────────────────────────────────────────┐
│                       Framework Versions                         │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Spring Boot 4.0.3                                        │   │
│  │  ├─ Spring Security 7.x                                  │   │
│  │  ├─ Spring Data JPA 4.x                                  │   │
│  │  ├─ Spring Web MVC                                       │   │
│  │  └─ Spring Validation                                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Security & Authentication                                │   │
│  │  ├─ JJWT 0.12.3 (JWT implementation)                    │   │
│  │  ├─ BCrypt (Password hashing)                           │   │
│  │  └─ Session Management (Stateless)                      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Database                                                 │   │
│  │  ├─ PostgreSQL 15+ (Production)                         │   │
│  │  ├─ Hibernate 6.x (ORM)                                 │   │
│  │  └─ HikariCP (Connection pooling)                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Rate Limiting & Messaging                                │   │
│  │  ├─ Bucket4j 8.7.0 (Rate limiting)                      │   │
│  │  └─ Twilio SDK 10.0.0 (WhatsApp integration)            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Development Tools                                        │   │
│  │  ├─ Lombok (Boilerplate reduction)                      │   │
│  │  ├─ Maven 3.x (Build tool)                              │   │
│  │  └─ Java 17 (Language version)                          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## Deployment Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                    Production Deployment                         │
└──────────────────────────────────────────────────────────────────┘

                        ┌─────────────┐
                        │   Client    │
                        │ (Mobile/Web)│
                        └──────┬──────┘
                               │ HTTPS
                               ▼
                     ┌──────────────────┐
                     │  Load Balancer   │
                     │   (Optional)     │
                     └────────┬─────────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
              ▼               ▼               ▼
       ┌───────────┐   ┌───────────┐   ┌───────────┐
       │ App       │   │ App       │   │ App       │
       │ Instance  │   │ Instance  │   │ Instance  │
       │ (Port     │   │ (Port     │   │ (Port     │
       │  8080)    │   │  8081)    │   │  8082)    │
       └─────┬─────┘   └─────┬─────┘   └─────┬─────┘
             │               │               │
             └───────────────┼───────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   PostgreSQL    │
                    │   Primary       │
                    │   (Master)      │
                    └────────┬────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
                    ▼                 ▼
            ┌──────────────┐  ┌──────────────┐
            │ PostgreSQL   │  │ PostgreSQL   │
            │ Replica 1    │  │ Replica 2    │
            │ (Read Only)  │  │ (Read Only)  │
            └──────────────┘  └──────────────┘

Configuration Files:
┌─────────────────────────────────────────────────────┐
│ application.properties (Dev)                        │
│ application-prod.properties (Production)            │
│ application-test.properties (Testing)               │
└─────────────────────────────────────────────────────┘

Environment Variables (Production):
┌─────────────────────────────────────────────────────┐
│ DB_URL, DB_USERNAME, DB_PASSWORD                    │
│ JWT_SECRET, JWT_EXPIRATION                          │
│ SERVER_PORT, CORS_ALLOWED_ORIGINS                   │
└─────────────────────────────────────────────────────┘
```

---

## API Request/Response Flow Example

### Complete Checkout Request

```
┌────────────────────────────────────────────────────────────────┐
│                    Step-by-Step Flow                           │
└────────────────────────────────────────────────────────────────┘

1. Client Request
   POST /api/checkout
   Headers: {
     "Authorization": "Bearer eyJhbGciOiJIUzI1NiIs...",
     "Content-Type": "application/json"
   }
   Body: {
     "products": [
       {"productId": 1, "quantity": 2},
       {"productId": 3, "quantity": 1}
     ],
     "deliveryLocation": {
       "latitude": 40.7128,
       "longitude": -74.0060,
       "address": "New York, NY"
     }
   }

2. Security Filter validates JWT
   ├─ Extract token from header
   ├─ Verify signature
   ├─ Check expiration
   └─ Load user: "testuser"

3. CheckoutController receives request
   └─ Calls CheckoutService.processCheckout()

4. CheckoutService processes
   ├─ Validates products (IDs: 1, 3)
   │  ├─ Query: SELECT * FROM products WHERE id IN (1,3)
   │  └─ Check: available = true
   │
   ├─ Find nearest delivery guy
   │  ├─ Query: SELECT * FROM delivery_guys WHERE available = true
   │  ├─ Calculate distances for each:
   │  │  ├─ John (40.7128, -74.0060): 0.0 km
   │  │  ├─ Maria (40.7589, -73.9851): 5.8 km
   │  │  ├─ Ahmed (40.7614, -73.9776): 6.2 km
   │  │  └─ Lisa (40.7480, -73.9862): 4.1 km
   │  └─ Nearest: John (0.0 km)
   │
   ├─ Calculate price
   │  ├─ Base: (2 × $12.99) + (1 × $29.99) = $55.97
   │  ├─ Size: (2 × $2.50) + (1 × $5.00) = $10.00
   │  ├─ Distance: 0.0 km × $0.50 = $0.00
   │  └─ Total: $65.97
   │
   ├─ Create order
   │  ├─ INSERT INTO orders (user_id, delivery_guy_id, ...)
   │  ├─ INSERT INTO order_items (order_id, product_id, ...)
   │  └─ Generated order_id: 42
   │
   └─ Update delivery guy
      └─ UPDATE delivery_guys SET available = false WHERE id = 1

5. Response sent to client
   {
     "orderId": 42,
     "message": "Order created successfully...",
     "items": [
       {
         "productId": 1,
         "productName": "Pizza Margherita",
         "size": "MEDIUM",
         "quantity": 2,
         "basePrice": 12.99,
         "available": true
       },
       {
         "productId": 3,
         "productName": "Family Meal Box",
         "size": "BIG",
         "quantity": 1,
         "basePrice": 29.99,
         "available": true
       }
     ],
     "totalPrice": 65.97,
     "distance": 0.0,
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
       "distanceFromUser": 0.0
     }
   }

Total Processing Time: ~200-500ms
Database Queries: 5-7
HTTP Status: 200 OK
```

---

## Error Handling Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                    Exception Handling Flow                     │
└────────────────────────────────────────────────────────────────┘

Exception Types:
┌─────────────────────────────────────────────────────────────┐
│ 1. Authentication Errors                                    │
│    ├─ Invalid credentials → 401 Unauthorized               │
│    ├─ Expired token → 401 Unauthorized                     │
│    └─ Invalid token format → 401 Unauthorized              │
├─────────────────────────────────────────────────────────────┤
│ 2. Validation Errors                                        │
│    ├─ Missing required fields → 400 Bad Request            │
│    ├─ Invalid email format → 400 Bad Request               │
│    └─ Invalid product ID → 400 Bad Request                 │
├─────────────────────────────────────────────────────────────┤
│ 3. Business Logic Errors                                    │
│    ├─ Product not available → 400 Bad Request              │
│    ├─ No delivery guys available → 400 Bad Request         │
│    └─ User not found → 404 Not Found                       │
├─────────────────────────────────────────────────────────────┤
│ 4. Database Errors                                          │
│    ├─ Duplicate username → 409 Conflict                    │
│    ├─ Constraint violation → 400 Bad Request               │
│    └─ Connection failure → 503 Service Unavailable         │
├─────────────────────────────────────────────────────────────┤
│ 5. Server Errors                                            │
│    └─ Unexpected errors → 500 Internal Server Error        │
└─────────────────────────────────────────────────────────────┘

Error Response Format:
{
  "timestamp": "2026-02-22T17:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Product not available",
  "path": "/api/checkout"
}
```

---

## Performance Considerations

```
┌────────────────────────────────────────────────────────────────┐
│                    Performance Optimizations                   │
└────────────────────────────────────────────────────────────────┘

Database:
├─ Connection Pooling (HikariCP)
│  ├─ Maximum pool size: 10
│  ├─ Minimum idle: 5
│  └─ Connection timeout: 30s
│
├─ Indexes
│  ├─ users.username (UNIQUE)
│  ├─ users.email (UNIQUE)
│  ├─ delivery_guys.available
│  └─ orders.user_id, orders.delivery_guy_id
│
└─ Query Optimization
   ├─ Fetch strategies (LAZY/EAGER)
   ├─ Pagination for large datasets
   └─ Selective column fetching

Caching (Future Enhancement):
├─ Product catalog (Redis)
├─ Delivery guy locations (Redis)
└─ User sessions (Redis)

API:
├─ Stateless architecture (horizontal scaling)
├─ JWT validation (no DB lookup per request)
├─ Response compression (GZIP)
└─ Rate Limiting (Bucket4j Token Bucket)
   ├─ Per-IP tracking
   ├─ Auth endpoints: 5 req/min
   └─ API endpoints: 100 req/min
```

---

## Rate Limiting Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                     Rate Limiting System                        │
│                      (Bucket4j - Token Bucket)                  │
└────────────────────────────────────────────────────────────────┘

Implementation:
┌─────────────────────────────────────────────────────────────┐
│ RateLimitFilter (Security Filter)                           │
│  │                                                           │
│  ├─ Intercepts ALL HTTP requests                           │
│  ├─ Extracts client IP (X-Forwarded-For, X-Real-IP)       │
│  ├─ Determines endpoint type (auth vs API)                 │
│  ├─ Consumes token from bucket                             │
│  └─ Returns 429 if limit exceeded                          │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ RateLimitingService                                         │
│  │                                                           │
│  ├─ Manages buckets per IP (ConcurrentHashMap)             │
│  ├─ Token Bucket Algorithm:                                │
│  │   ├─ Auth endpoints: 5 tokens, refill 5/min            │
│  │   └─ API endpoints: 100 tokens, refill 100/min         │
│  └─ Thread-safe bucket resolution                          │
└─────────────────────────────────────────────────────────────┘

Rate Limit Headers:
HTTP/1.1 200 OK
X-Rate-Limit-Remaining: 95

Rate Limit Exceeded Response:
HTTP/1.1 429 Too Many Requests
Content-Type: application/json
{
  "error": "Too many requests",
  "message": "Rate limit exceeded. Please try again later.",
  "status": 429
}

Benefits:
├─ Prevents brute force attacks on /api/auth
├─ Protects against API abuse
├─ Ensures fair resource allocation
├─ Reduces server load from malicious traffic
└─ In-memory implementation (fast, no DB overhead)

Configuration:
├─ Auth endpoints: 5 requests/minute (stricter)
├─ API endpoints: 100 requests/minute (normal usage)
├─ Per-IP tracking (works with proxies)
└─ Automatic token refill
```

---

## WhatsApp Integration Architecture

### Overview
The Express Delivery API integrates with Twilio's WhatsApp Business API to send real-time order notifications to delivery personnel. This ensures immediate communication when orders are assigned.

### Integration Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    WhatsApp Notification Flow                       │
└─────────────────────────────────────────────────────────────────────┘

Customer Places Order
         │
         ▼
┌──────────────────┐
│ CheckoutService  │
└────────┬─────────┘
         │ 1. Process Order
         │ 2. Assign Delivery Guy
         │ 3. Save to Database
         ▼
┌──────────────────┐       ┌──────────────────┐       ┌──────────────┐
│ WhatsAppService  │──────▶│  Twilio API      │──────▶│  WhatsApp    │
└──────────────────┘       └──────────────────┘       └──────────────┘
         │                          │                        │
         │ Build Message            │ API Call              │ Deliver
         │ Format Details           │ (HTTP POST)           │ Message
         ▼                          ▼                        ▼
   Order Details            Authentication         ┌─────────────────┐
   - Order ID               - Account SID          │ Delivery Guy's  │
   - Items List             - Auth Token           │ WhatsApp Device │
   - Total Price            - From Number          └─────────────────┘
   - Distance               - To Number
   - Delivery Address       - Message Body
   - Google Maps Link
```

### Configuration

**Environment Variables (application.properties):**
```properties
# Twilio WhatsApp Configuration
twilio.account.sid=${TWILIO_ACCOUNT_SID:your_account_sid}
twilio.auth.token=${TWILIO_AUTH_TOKEN:your_auth_token}
twilio.whatsapp.from=${TWILIO_WHATSAPP_FROM:whatsapp:+14155238886}
```

**Required Setup:**
1. Twilio Account (twilio.com/whatsapp)
2. WhatsApp Business API enabled
3. Verified sender number
4. Environment variables configured

### Message Format

**WhatsApp Message Template:**
```
🚗 *NEW DELIVERY ORDER* 🚗

Hi [Delivery Guy Name]! 👋

📦 *Order #[Order ID]*
━━━━━━━━━━━━━━━━━━━━

📋 *ORDER ITEMS:*
• [Product Name] ([Size]) x[Quantity] - $[Price]
• [Product Name] ([Size]) x[Quantity] - $[Price]

💰 *Total Price:* $[Total]
📏 *Distance:* [Distance] km

📍 *DELIVERY LOCATION:*
Address: [Full Address]
Coordinates: [Latitude], [Longitude]
🗺️ Google Maps: [Maps Link]

━━━━━━━━━━━━━━━━━━━━
⏰ Please confirm pickup and start delivery ASAP!
Good luck! 🎉
```

### WhatsAppService Component

```java
@Service
public class WhatsAppService {
    // Dependencies
    - Twilio SDK (initialized with credentials)
    - Configuration properties
    
    // Methods
    + sendOrderToDeliveryGuy(deliveryGuy, order, items, location)
      └─ Formats and sends complete order details
    
    + sendMessage(phoneNumber, messageText)
      └─ Generic message sending utility
    
    // Features
    - Automatic initialization (@PostConstruct)
    - Error handling (doesn't fail orders)
    - Logging (success/failure tracking)
    - Message formatting with emojis
    - Google Maps integration
}
```

### DeliveryGuy WhatsApp Integration

**Database Schema Update:**
```sql
ALTER TABLE delivery_guys 
ADD COLUMN whatsapp_number VARCHAR(20) NOT NULL;

-- Example values with country codes
+14155551001  (USA)
+447911123456 (UK)
+971501234567 (UAE)
```

**Entity Model:**
```java
@Entity
public class DeliveryGuy {
    private String whatsappNumber;  // Format: +[country][number]
    // ... other fields
}
```

### Integration Points

| Trigger Point | Service | Action |
|---------------|---------|--------|
| Order Created | CheckoutService | Calls WhatsAppService.sendOrderToDeliveryGuy() |
| Order Assigned | CheckoutService | Sends WhatsApp notification immediately |
| Database Saved | After persist | Ensures order is committed before notification |
| Delivery Guy Selected | findNearestDeliveryGuy() | Uses whatsappNumber from selected delivery guy |

### Error Handling

```
WhatsApp Notification Flow:
┌─────────────────────┐
│ Send Notification   │
└──────────┬──────────┘
           │
     Try to send
           │
           ├─────Success────▶ Log: "Message sent - SID: [MessageSID]"
           │                  Continue with order flow
           │
           └─────Failure────▶ Log: "Failed to send: [Error]"
                              Order still completes successfully
                              (WhatsApp is non-critical)
```

**Failure Scenarios:**
- Invalid phone number → Logged, order continues
- Twilio API down → Logged, order continues
- Network timeout → Logged, order continues
- Invalid credentials → Logged at startup, all messages fail gracefully

### Benefits

✅ **Real-time Communication**
   - Instant notification to delivery personnel
   - No manual checking required
   
✅ **Complete Information**
   - All order details in one message
   - Direct Google Maps link
   - Customer location coordinates

✅ **Professional Format**
   - Structured, easy-to-read messages
   - Emojis for visual clarity
   - Breakdown of items and prices

✅ **Non-blocking**
   - WhatsApp failures don't affect orders
   - Asynchronous messaging
   - Error logging for monitoring

✅ **Scalable**
   - Twilio handles message queue
   - Supports international numbers
   - Rate limiting handled by Twilio

### Sample Delivery Guy Data

```java
DeliveryGuy {
    id: 1,
    name: "John Smith",
    age: 28,
    car: "Honda Civic",
    whatsappNumber: "+14155551001",
    location: {
        latitude: 40.7128,
        longitude: -74.0060,
        address: "New York, NY"
    },
    available: true
}
```

### API Response Update

**GET /api/deliveryguys** now includes:
```json
{
  "id": 1,
  "name": "John Smith",
  "age": 28,
  "car": "Honda Civic",
  "whatsappNumber": "+14155551001",
  "nearestLocation": {
    "latitude": 40.7128,
    "longitude": -74.0060,
    "address": "New York, NY"
  },
  "available": true,
  "distanceFromUser": 5.2
}
```

### Security Considerations

🔒 **Credentials Protection**
   - Stored in environment variables
   - Not committed to version control
   - Separate credentials for dev/prod

🔒 **Phone Number Validation**
   - Must include country code
   - Format: +[country code][number]
   - Validated by Twilio

🔒 **Rate Limiting**
   - Twilio enforces API rate limits
   - Express API rate limiting protects checkout endpoint
   - Prevents abuse

### Dependencies

```xml
<!-- Twilio WhatsApp Integration -->
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>10.0.0</version>
</dependency>
```

---

This architecture provides a complete view of the Express Delivery API system, showing all layers, components, data flows, rate limiting, WhatsApp integration, and technical details.



