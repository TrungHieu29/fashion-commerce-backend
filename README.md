# Fashion Commerce Backend

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-supported-blue)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

Backend API for a fashion commerce marketplace. The system supports customers buying from multiple shops, shop owners managing products and orders, admins managing users/shops, realtime chat, Cloudinary image uploads, Brevo email OTP, and Swagger/ReDoc API documentation.

## Tech Stack

| Group | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| API | Spring Web, REST API, OpenAPI/Swagger, ReDoc |
| Security | Spring Security, JWT, BCrypt, Method Security |
| Database | PostgreSQL |
| ORM | Spring Data JPA, Hibernate |
| Realtime | Spring WebSocket, STOMP, SockJS |
| Mapping | MapStruct |
| Boilerplate | Lombok |
| Media | Cloudinary |
| Email | Brevo SMTP API |
| Build | Maven Wrapper, Docker |

## Main Features

- JWT authentication with access token and refresh token.
- Account registration with email OTP, resend OTP, forgot password, and reset password.
- Role-based authorization with `ADMIN`, `CUSTOMER`, plus ownership checks using `@PreAuthorize`.
- Shop management, shop status management, and multipart logo upload.
- Product, brand, category, variant, and product image management.
- Product search/filter by keyword, shop, category, and brand.
- Cart and multi-shop checkout. One checkout can create multiple `OrderShop` records.
- Order snapshots for product name, image, price, and shipping address at purchase time.
- Discount/voucher support for `PRODUCT`, `SHOP`, and `ORDER`; supports `PERCENT` and `FIXED`.
- Payment status support for `COD`, `VNPAY`, and `MOMO`.
- Notifications, wishlist, reviews, and shipping addresses.
- Realtime chat over WebSocket/STOMP: send message, typing indicator, and mark read.
- Shop dashboard and analytics.
- Dockerfile for Render or similar container-based deployment.

## Project Structure

```text
src/main/java/com/trunghieu/fashioncommerce/fashion_commerce_backend
|-- config          # Cloudinary, OpenAPI, WebSocket
|-- controller      # REST controllers and WebSocket controller
|-- dto             # Request/Response DTOs
|-- entity          # JPA entities and enums
|-- exception       # Global exception handler
|-- mapper          # MapStruct mappers
|-- repository      # Spring Data JPA repositories
|-- scheduler       # Scheduled jobs
|-- security        # JWT, filter, UserDetails, SecurityUtils
`-- service         # Business interfaces and implementations
```

Main request flow:

```text
Frontend
  -> Controller
  -> Service
  -> Repository
  -> PostgreSQL
```

## API Documentation

After starting the app locally:

```text
Swagger UI:   http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
ReDoc:        http://localhost:8080/redoc.html
```

Swagger is configured with Bearer JWT. Use the `Authorize` button and paste an access token to test secured endpoints.

## API Groups

| Domain | Base endpoint |
| --- | --- |
| Auth | `/api/auth` |
| Users | `/api/users` |
| Roles | `/api/roles` |
| Shops | `/api/shops` |
| Products | `/api/products` |
| Product Images | `/api/product-images` |
| Product Variants | `/api/product-variants` |
| Product Brands | `/api/product-brands` |
| Categories | `/api/categories` |
| Cart | `/api/carts` |
| Wishlist | `/api/wishlist` |
| Orders | `/api/orders` |
| Order Shops | `/api/order-shops` |
| Order Items | `/api/order-items` |
| Order Shipping | `/api/order-shippings` |
| Payments | `/api/payments` |
| Discounts | `/api/discounts` |
| Reviews | `/api/reviews` |
| Conversations | `/api/conversations` |
| Messages | `/api/messages` |
| Notifications | `/api/notifications` |
| Dashboard | `/api/dashboard`, `/api/dashboard/shop` |

## WebSocket

```text
Handshake endpoint: /ws
Application prefix: /app
Broker prefixes:    /topic, /queue
User prefix:        /user
```

Main message mappings:

```text
/app/chat.sendMessage
/app/chat.typing
/app/chat.markRead
```

Server broadcasts by conversation:

```text
/topic/conversations/{conversationId}
/topic/conversations/{conversationId}/typing
/topic/conversations/{conversationId}/read
```

## Security

The backend uses stateless JWT security:

- `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`.
- Session policy is `STATELESS`.
- Passwords are hashed with `BCryptPasswordEncoder`.
- Main public APIs:
  - `/api/auth/**`
  - Swagger/ReDoc/OpenAPI
  - WebSocket handshake `/ws/**`
  - Some public read APIs such as products, shops, categories, and reviews.
- Other APIs require JWT authentication.
- Current CORS allowlist:
  - `http://localhost:5173`
  - `https://*.vercel.app`

## Environment Variables

The app reads sensitive configuration from environment variables:

```env
PORT=8080

DB_URL=jdbc:postgresql://<host>:5432/<database>
DB_USERNAME=postgres
DB_PASSWORD=your_database_password

JWT_SECRET_KEY=your_long_jwt_secret_key

BREVO_API_KEY=your_brevo_api_key

CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret
```

Optional tuning variables:

```env
JPA_SHOW_SQL=false
HIBERNATE_FORMAT_SQL=false
HIBERNATE_DEFAULT_BATCH_FETCH_SIZE=50
```

SQL logging is disabled by default for production because cloud logging can slow down requests. Enable it only when debugging locally:

```env
JPA_SHOW_SQL=true
HIBERNATE_FORMAT_SQL=true
```

## Run Locally

Requirements:

- JDK 21+
- PostgreSQL or hosted PostgreSQL such as Supabase
- Cloudinary account
- Brevo API key

Clone the project:

```bash
git clone <repository-url>
cd fashion-commerce-backend
```

Run with Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The app runs at:

```text
http://localhost:8080
```

## Build And Test

Run tests:

```bash
./mvnw test
```

Build jar:

```bash
./mvnw clean package
```

Build Docker image:

```bash
docker build -t fashion-commerce-backend .
```

Run Docker container:

```bash
docker run --env-file .env -p 8080:8080 fashion-commerce-backend
```

## Deployment Notes

The backend can be deployed with the current Dockerfile. On Render or another container platform, configure all required environment variables in the deployment dashboard.

For Render/Supabase:

- Choose a backend region close to the database when possible.
- Do not enable `JPA_SHOW_SQL=true` in production.
- On free tiers, the first request can be slow because of cold start.
- If product APIs are slow, check N+1 queries, SQL logging, and network latency between Render and Supabase.

## Main Business Flows

Registration:

```text
POST /api/auth/register
  -> save PendingRegistration
  -> send OTP through Brevo

POST /api/auth/verify
  -> verify OTP
  -> create ACTIVE User

POST /api/auth/authenticate
  -> return accessToken, refreshToken, and user profile
```

Marketplace checkout:

```text
Cart
  -> select checkout items
  -> validate stock
  -> group items by shop
  -> apply discount/voucher
  -> create parent Order
  -> create OrderShop for each shop
  -> create OrderItem snapshots
  -> create PENDING Payment
  -> remove checked-out items from cart
```

Order lifecycle:

```text
PENDING
  -> CONFIRMED
  -> PROCESSING
  -> SHIPPED
  -> DELIVERED
  -> COMPLETED
```

Side paths:

```text
CANCELLED
RETURN_REQUESTED
RETURNED
```

## Main Entities

- `User`, `Role`, `PendingRegistration`, `PasswordResetToken`
- `Shop`
- `Product`, `ProductVariant`, `ProductImage`, `ProductBrand`, `Category`
- `Cart`, `CartItem`, `WishlistItem`
- `Order`, `OrderShop`, `OrderItem`, `OrderShipping`
- `Payment`
- `Discount`
- `Conversation`, `Message`
- `Review`
- `ShippingAddress`
- `Notification`

## Important Enums

- `UserStatus`: `PENDING`, `ACTIVE`, `INACTIVE`, `BANNED`
- `ShopStatus`: `PENDING`, `ACTIVE`, `INACTIVE`, `BANNED`, `REJECTED`
- `ProductStatus`: `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`, `DRAFT`
- `OrderStatus`: `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `COMPLETED`, `CANCELLED`, `RETURN_REQUESTED`, `RETURNED`
- `PaymentStatus`: `PENDING`, `COMPLETED`, `FAILED`, `REFUND_INITIATED`, `REFUNDED`
- `ShippingStatus`: `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `RETURNED`
- `DiscountTarget`: `PRODUCT`, `SHOP`, `ORDER`
- `DiscountType`: `PERCENT`, `FIXED`
- `DiscountStatus`: `ACTIVE`, `INACTIVE`, `EXPIRED`

## Current Notes

- `spring.jpa.hibernate.ddl-auto=update` is used for development convenience. For real production, prefer Flyway or Liquibase migrations.
- SQL logging is disabled by default and Hibernate batch fetch size is enabled to reduce extra lazy-loading queries.
- Redis/cache is not currently added. If performance needs more work, measure slow requests first, then cache high-read APIs such as products, categories, and brands.
- Maven Wrapper should be working before running build/test locally. If the wrapper fails on Windows, install global Maven or inspect the wrapper script.

## Roadmap

- Optimize product/discount queries to reduce N+1 behavior.
- Add integration tests for auth, checkout, discount, and order lifecycle.
- Add Flyway or Liquibase migrations.
- Add Redis cache for high-read data when traffic grows.
- Complete real callbacks for VNPAY/MOMO.
- Add CI pipeline for automatic test/build.

## Author

**Trung Hieu**

Backend Developer - Java Spring Boot
