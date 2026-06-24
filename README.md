# Fashion Commerce Backend

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.2-green)
![SQL Server](https://img.shields.io/badge/SQLServer-2019-red)
![License](https://img.shields.io/badge/license-MIT-blue)

Backend API cho nền tảng thương mại điện tử thời trang theo mô hình marketplace, nơi khách hàng có thể mua sắm từ nhiều shop, chủ shop quản lý sản phẩm và đơn hàng, còn admin theo dõi vận hành toàn hệ thống.

Project được xây dựng bằng Spring Boot 3, Java 21, SQL Server, JWT Security, WebSocket realtime chat, Cloudinary upload ảnh, email OTP và OpenAPI documentation.

---

## Điểm nổi bật

- Multi-vendor marketplace: đơn hàng được tách theo từng shop để mỗi shop xử lý phần đơn của mình.
- Authentication bằng JWT access token và refresh token, mã hóa mật khẩu bằng BCrypt.
- Đăng ký tài khoản qua OTP email, quên mật khẩu và reset mật khẩu có giới hạn thời gian.
- Phân quyền theo role và ownership bằng Spring Security Method Security.
- Quản lý sản phẩm đầy đủ: brand, category, variant size/màu/tồn kho, ảnh sản phẩm theo màu.
- Upload và đồng bộ ảnh với Cloudinary, có lưu `public_id` để xóa/cập nhật ảnh cũ.
- Checkout có snapshot dữ liệu tại thời điểm mua: tên sản phẩm, ảnh, giá và địa chỉ nhận hàng.
- Voucher/discount linh hoạt theo product, shop hoặc order, hỗ trợ phần trăm và số tiền cố định.
- Quản lý vòng đời đơn hàng: pending, confirmed, processing, shipped, delivered, completed, cancelled, return.
- Tự động trừ kho khi shop xác nhận đơn và hoàn kho khi hủy đơn phù hợp.
- Realtime chat giữa khách hàng và shop bằng WebSocket/STOMP, hỗ trợ gửi tin, typing và đánh dấu đã đọc.
- Dashboard analytics cho shop: doanh thu, số đơn, AOV và timeline theo hôm nay/7 ngày/30 ngày.
- API docs bằng Swagger UI và ReDoc.

---

## Tech Stack

| Nhóm | Công nghệ |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| API | Spring Web, RESTful API, OpenAPI/Swagger |
| Security | Spring Security, JWT, BCrypt, Method Security |
| Database | Microsoft SQL Server |
| ORM | Spring Data JPA, Hibernate |
| Realtime | Spring WebSocket, STOMP, SockJS |
| Mapping | MapStruct |
| Boilerplate | Lombok |
| Media Storage | Cloudinary |
| Email | Spring Mail, Gmail SMTP |
| Build Tool | Maven |
| Testing | Spring Boot Test, Spring Security Test |

---

## Kiến trúc tổng quan

```text
Client / Frontend
      |
      v
Controller Layer
REST API + WebSocket endpoint
      |
      v
Service Layer
Business rules, transaction, authorization helpers
      |
      v
Repository Layer
Spring Data JPA queries
      |
      v
SQL Server
```

Project được tổ chức theo layered architecture:

```text
src/main/java/com/trunghieu/fashioncommerce/fashion_commerce_backend
├── config          # OpenAPI, Cloudinary, WebSocket configuration
├── controller      # REST controllers và WebSocket controller
├── dto             # Request/Response DTOs
├── entity          # JPA entities và enums
├── exception       # Global exception handling
├── mapper          # MapStruct mappers
├── repository      # Spring Data JPA repositories
├── scheduler       # Scheduled jobs
├── security        # JWT, filter, UserDetails, ownership security utils
└── service         # Business interfaces và implementations
```

---

## Core Modules

### Auth & User

- Đăng ký tài khoản qua OTP email.
- Xác thực OTP và kích hoạt tài khoản.
- Đăng nhập trả về `accessToken`, `refreshToken` và thông tin user.
- Đổi mật khẩu khi đã đăng nhập.
- Quên mật khẩu, gửi OTP reset, giới hạn số lần nhập sai.
- Admin có thể quản lý trạng thái user: `PENDING`, `ACTIVE`, `INACTIVE`, `BANNED`.

### Product Catalog

- Quản lý product, category, brand.
- Product có nhiều variant theo size, màu và tồn kho.
- Product có nhiều ảnh, ảnh có thể gắn theo màu variant.
- Tìm kiếm sản phẩm, lọc theo shop, category, brand.
- Trạng thái sản phẩm: `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`, `DRAFT`.

### Shop Management

- Customer có thể tạo shop của mình.
- Shop có logo upload dạng multipart.
- Admin duyệt/cập nhật trạng thái shop: `PENDING`, `ACTIVE`, `INACTIVE`, `BANNED`, `REJECTED`.
- Chủ shop chỉ được thao tác với tài nguyên thuộc shop của mình.

### Cart & Checkout

- Giỏ hàng theo user.
- Thêm sản phẩm, cập nhật số lượng, đổi variant, xóa item hoặc clear cart.
- Checkout toàn bộ giỏ hàng hoặc chỉ những cart item được chọn.
- Khi checkout, hệ thống nhóm item theo shop và tạo các `OrderShop` riêng.
- Lưu snapshot địa chỉ để đơn hàng không bị ảnh hưởng khi user sửa địa chỉ sau này.

### Order & Inventory

- Một `Order` tổng có thể chứa nhiều `OrderShop`.
- Mỗi `OrderShop` có danh sách `OrderItem`, trạng thái riêng và shipping riêng.
- Giá, tên sản phẩm và ảnh được đóng băng tại thời điểm mua.
- Kiểm tra tồn kho trước khi tạo đơn.
- Trừ tồn kho khi shop xác nhận đơn.
- Hoàn tồn kho khi đơn bị hủy trong trạng thái phù hợp.
- Hỗ trợ lọc đơn theo trạng thái của `OrderShop`.

### Discount & Voucher

- Discount theo 3 target:
  - `PRODUCT`: áp dụng cho sản phẩm cụ thể.
  - `SHOP`: áp dụng cho toàn shop.
  - `ORDER`: voucher theo mã, áp dụng theo giá trị đơn tối thiểu.
- Hỗ trợ `PERCENT` và `FIXED`.
- Tự chọn mức giảm tốt nhất cho sản phẩm khi checkout.
- Voucher theo từng shop trong cùng một đơn marketplace.

### Payment

- Hỗ trợ phương thức: `COD`, `VNPAY`, `MOMO`.
- Trạng thái payment: `PENDING`, `COMPLETED`, `FAILED`, `REFUND_INITIATED`, `REFUNDED`.
- Online payment result có thể tự chuyển trạng thái order shop sang confirmed hoặc cancelled.
- Với COD, payment được hoàn tất khi giao hàng thành công.

### Realtime Chat

- WebSocket endpoint: `/ws`
- STOMP application prefix: `/app`
- Broker topics: `/topic`, `/queue`
- Gửi tin nhắn realtime theo conversation.
- Typing indicator.
- Mark read message.
- WebSocket inbound channel có interceptor để xác thực user.

### Dashboard & Analytics

- Dashboard theo shop.
- Thống kê doanh thu, số đơn, average order value.
- Timeline doanh thu theo `today`, `7days`, `30days`.
- Lấy recent orders và thống kê trạng thái đơn hàng phục vụ giao diện quản trị shop.

---

## Security

Project sử dụng stateless security:

- JWT authentication filter chạy trước `UsernamePasswordAuthenticationFilter`.
- Session policy: `STATELESS`.
- Password encoder: `BCryptPasswordEncoder`.
- Role-based access control qua `@PreAuthorize`.
- Ownership-based authorization qua `SecurityUtils`, ví dụ:
  - User chỉ xem/sửa dữ liệu của chính mình.
  - Shop owner chỉ quản lý sản phẩm, đơn hàng, voucher của shop mình.
  - Conversation chỉ được xem bởi participant.
  - Admin có quyền quản trị toàn hệ thống.

Public endpoints:

- `/api/auth/**`
- Swagger/OpenAPI/ReDoc
- Một số API đọc public như products, shops, categories, reviews.
- WebSocket handshake `/ws/**`

---

## API Documentation

Sau khi chạy ứng dụng:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- ReDoc: `http://localhost:8080/redoc.html`

OpenAPI đã cấu hình Bearer JWT security scheme, có thể authorize trực tiếp bằng token trong Swagger UI.

---

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
| Orders | `/api/orders` |
| Order Shops | `/api/order-shops` |
| Order Items | `/api/order-items` |
| Order Shipping | `/api/order-shippings` |
| Payments | `/api/payments` |
| Discounts | `/api/discounts` |
| Reviews | `/api/reviews` |
| Conversations | `/api/conversations` |
| Messages | `/api/messages` |
| Dashboard | `/api/dashboard`, `/api/dashboard/shop` |

---

## Database Model Highlights

Các entity chính:

- `User`, `Role`, `PendingRegistration`, `PasswordResetToken`
- `Shop`
- `Product`, `ProductVariant`, `ProductImage`, `ProductBrand`, `Category`
- `Cart`, `CartItem`
- `Order`, `OrderShop`, `OrderItem`, `OrderShipping`
- `Payment`
- `Discount`
- `Conversation`, `Message`
- `Review`
- `ShippingAddress`

Quan hệ nổi bật:

- User có một cart và có thể sở hữu một shop.
- Shop có nhiều product, discount, order shop và conversation.
- Product có nhiều variant, image, review và category.
- Order tổng được tách thành nhiều OrderShop theo từng shop.
- OrderShop có nhiều OrderItem và một OrderShipping.
- Payment gắn one-to-one với Order.
- Conversation gắn giữa User và Shop, Message gắn với Conversation.

---

## Yêu cầu môi trường

- JDK 21+
- Maven 3.9+ hoặc Maven Wrapper có sẵn trong project
- Microsoft SQL Server
- Tài khoản Cloudinary
- Gmail SMTP app password hoặc SMTP credential tương đương

---

## Cấu hình biến môi trường

Ứng dụng đọc cấu hình nhạy cảm từ environment variables:

```env
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=fashion_commerce;encrypt=true;trustServerCertificate=true
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

JWT_SECRET_KEY=your_very_long_secret_key

MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password

CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret
```
---

## Chạy project local

Clone repository:

```bash
git clone <repository-url>
cd fashion-commerce-backend
```

Khởi tạo database SQL Server, sau đó cấu hình các biến môi trường ở trên.

Chạy bằng Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Trên Windows:

```bash
mvnw.cmd spring-boot:run
```

Ứng dụng mặc định chạy tại:

```text
http://localhost:8080
```

---

## Build & Test

Chạy test:

```bash
./mvnw test
```

Build package:

```bash
./mvnw clean package
```

File build nằm trong:

```text
target/
```

---

## Luồng nghiệp vụ tiêu biểu

### Đăng ký và đăng nhập

```text
POST /api/auth/register
        -> lưu PendingRegistration
        -> gửi OTP qua email

POST /api/auth/verify
        -> xác thực OTP
        -> tạo User ACTIVE với role CUSTOMER

POST /api/auth/authenticate
        -> trả về accessToken, refreshToken, user profile
```

### Checkout marketplace

```text
Cart của user
    -> chọn toàn bộ hoặc một số cart item
    -> kiểm tra tồn kho
    -> nhóm item theo shop
    -> áp dụng discount/voucher theo shop
    -> tạo Order tổng
    -> tạo OrderShop cho từng shop
    -> tạo OrderItem snapshot
    -> tạo Payment PENDING
    -> xóa các cart item đã checkout
```

### Xử lý đơn hàng

```text
PENDING
    -> shop confirm
    -> CONFIRMED và trừ tồn kho
    -> PROCESSING / SHIPPED / DELIVERED
    -> customer xác nhận nhận hàng
    -> COMPLETED
```

Khi hủy đơn trong trạng thái phù hợp, hệ thống hoàn kho và cập nhật shipping/payment tương ứng.

### Realtime chat

```text
Client connect /ws
Client send /app/chat.sendMessage
Server broadcast /topic/conversations/{conversationId}

Client send /app/chat.typing
Server broadcast /topic/conversations/{conversationId}/typing

Client send /app/chat.markRead
Server broadcast /topic/conversations/{conversationId}/read
```

---

## Chất lượng code

- Tách lớp rõ ràng: Controller, Service, Repository, Mapper, DTO, Entity.
- Business logic đặt trong Service và được bọc transaction bằng `@Transactional`.
- DTO giúp tránh expose trực tiếp entity ra API.
- MapStruct giảm boilerplate mapping.
- Global exception handler chuẩn hóa lỗi API.
- Validation request bằng Jakarta Validation.
- Method-level authorization giúp bảo vệ tài nguyên theo role và owner.
- Dữ liệu đơn hàng dùng snapshot để đảm bảo tính lịch sử.

---

## Roadmap gợi ý

- Tích hợp gateway thật cho VNPAY/MOMO callback.
- Bổ sung refresh token endpoint và token revocation.
- Thêm migration bằng Flyway hoặc Liquibase.
- Viết integration test cho checkout, discount và order lifecycle.
- Thêm Docker Compose cho SQL Server và backend.
- Bổ sung CI pipeline chạy test/build tự động.

---

## Tác giả

**Trung Hieu**

Backend Developer - Java Spring Boot

Project này thể hiện khả năng thiết kế REST API, xử lý nghiệp vụ marketplace nhiều shop, bảo mật JWT, realtime WebSocket, quản lý media cloud và xây dựng backend có cấu trúc rõ ràng để mở rộng trong môi trường thực tế.
