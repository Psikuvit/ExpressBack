# Environment Configuration Guide

## Database Setup

### PostgreSQL Installation (if needed)

**Windows:**
1. Download from https://www.postgresql.org/download/windows/
2. Install with default settings
3. Remember the password you set for postgres user

**macOS:**
```bash
brew install postgresql
brew services start postgresql
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### Create Database

```bash
# Login to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE express_db;

# Create user (optional)
CREATE USER express_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE express_db TO express_user;

# Exit
\q
```

## Application Properties Configuration

Update `src/main/resources/application.properties`:

```properties
spring.application.name=Express

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/express_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD_HERE
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=your-super-secret-key-change-this-to-something-very-secure-and-random-in-production
jwt.expiration=86400000
jwt.refresh.expiration=604800000

# Server Configuration
server.port=8080
```

## Environment Variables (Alternative Configuration)

Instead of hardcoding credentials, use environment variables:

```bash
# Windows PowerShell
$env:DB_URL="jdbc:postgresql://localhost:5432/express_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your-secret-key"

# Linux/macOS
export DB_URL="jdbc:postgresql://localhost:5432/express_db"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_password"
export JWT_SECRET="your-secret-key"
```

Then update `application.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

## Running the Application

### Using Maven Wrapper (Recommended)

**Windows:**
```powershell
.\mvnw.cmd clean spring-boot:run
```

**Linux/macOS:**
```bash
./mvnw clean spring-boot:run
```

### Using Maven (if installed)

```bash
mvn clean spring-boot:run
```

### Building JAR file

```bash
# Windows
.\mvnw.cmd clean package

# Linux/macOS
./mvnw clean package

# Run the JAR
java -jar target/Express-0.0.1-SNAPSHOT.jar
```

## Verify Installation

Once the application starts, you should see:

```
Started ExpressApplication in X.XXX seconds
Sample products created!
Sample delivery guys created!
Test user created! Username: testuser, Password: password123
```

## Test the API

### Quick Health Check

**Windows PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"testuser","password":"password123"}'
```

**Linux/macOS/Git Bash:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

You should receive a JWT token in the response.

## Common Issues & Solutions

### Issue: "Could not connect to database"
**Solution:** 
- Verify PostgreSQL is running
- Check credentials in application.properties
- Ensure database `express_db` exists

### Issue: "Port 8080 already in use"
**Solution:** 
- Change port in application.properties: `server.port=8081`
- Or stop the process using port 8080

### Issue: "Java version mismatch"
**Solution:** 
- Ensure Java 17 or higher is installed
- Check with: `java -version`
- Download from: https://adoptium.net/

### Issue: "CORS errors from frontend"
**Solution:** 
- Already configured in SecurityConfig to allow all origins
- For production, update CORS settings in SecurityConfig.java

## Production Deployment

### Docker Setup (Optional)

Create `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Create `docker-compose.yml`:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: express_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/express_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres

volumes:
  postgres_data:
```

Run with:
```bash
docker-compose up
```

## Security Best Practices

1. **Change JWT Secret**: Use a strong, random secret (at least 256 bits)
   ```bash
   # Generate secure secret (Linux/macOS)
   openssl rand -base64 64
   ```

2. **Use HTTPS**: In production, always use HTTPS
3. **Environment Variables**: Never commit passwords to git
4. **Database Security**: Use strong passwords and limit access
5. **CORS**: Restrict origins in production

## Next Steps

1. ✅ Set up PostgreSQL database
2. ✅ Update application.properties
3. ✅ Run the application
4. ✅ Test with Postman collection
5. ✅ Integrate with your Expo mobile app
6. ✅ Deploy to production (optional)

## Support

For issues or questions:
1. Check the error logs in the console
2. Review API_DOCUMENTATION.md for endpoint details
3. Verify database connection
4. Ensure all dependencies are installed

Happy coding! 🚀

