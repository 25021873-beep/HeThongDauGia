# Hệ thống Đấu giá Trực tuyến

- Drive báo cáo dự án và video demo dự án: https://drive.google.com/drive/folders/1oZUdQMB3YBjX16FvfYjvpyBXz3Yz47Ka?usp=drive_link

## 1. Giới thiệu
- **Mục tiêu dự án:** Xây dựng một hệ thống đấu giá trực tuyến dạng Desktop Application (Client/Server) cho phép người bán đăng sản phẩm và người mua tham gia trả giá theo thời gian thực. Dự án được phát triển phục vụ bài tập lớn môn Lập trình nâng cao - Trường Đại học Công nghệ, Đại học Quốc gia Hà Nội (UET).
- **Công nghệ sử dụng:**
  - Ngôn ngữ: Java 17
  - Giao diện (Frontend): JavaFX (áp dụng mô hình MVC)
  - Giao tiếp mạng: TCP Socket thuần, Gson (JSON parsing)
  - Cơ sở dữ liệu: MySQL
  - Connection Pool: HikariCP
  - Quản lý dự án & Build tool: Maven
  - Kiểm thử: JUnit 5
  - Quản lý mã nguồn: Git (áp dụng Conventional Commits), GitHub Actions (CI/CD) 

## 2. Chức năng hệ thống

### Chức năng bắt buộc
- **Quản lý người dùng:** Đăng ký, đăng nhập. Phân quyền vai trò rõ ràng: Admin, Seller, Bidder.
- **Quản lý sản phẩm đấu giá:** Thêm, sửa, xóa sản phẩm.
- **Tham gia đấu giá:** Đặt giá hợp lệ, cập nhật người dẫn đầu liên tục.
- **Kết thúc phiên đấu giá:** Tự động đóng phiên khi hết thời gian, xác định người thắng cuộc và chuyển đổi trạng thái (OPEN -> RUNNING -> FINISHED).
- **Xử lý lỗi & ngoại lệ:** Xử lý chặt chẽ các trường hợp đặt giá thấp hơn giá hiện tại, đấu giá khi phiên đã đóng, lỗi kết nối hoặc dữ liệu.

### Chức năng nâng cao
- **Auto-Bidding (Đấu giá tự động):** Tự động trả giá thay người dùng, đảm bảo không vượt quá mức giá tối đa (maxBid) và tuân thủ bước giá (increment).
- **Anti-sniping (Gia hạn phiên đấu giá):** Thuật toán tự động gia hạn thêm 120 giây nếu có lượt đặt giá mới xuất hiện trong vòng 60 giây cuối cùng.
- **Realtime Update (Observer nâng cao):** Đồng bộ dữ liệu lập tức tới tất cả client đang xem phiên thông qua Socket và Event-based communication, không dùng cơ chế polling.
- **Xử lý đấu giá đồng thời (Concurrent Bidding):** Sử dụng Transaction Database, ReentrantLock và ConcurrentHashMap để ngăn chặn hoàn toàn các lỗi lost update, rollback giá, đảm bảo có duy nhất một người thắng cuộc.
-**Bid History Visualization (Trực quan hóa lịch sử đấu giá):** Tự động vẽ và cập nhật biểu đồ đường (Line Chart) biểu diễn biến động của mức giá cao nhất theo thời gian thực. Trục X hiển thị mốc thời gian (Timestamp) và trục Y hiển thị mức giá đấu hiện tại; biểu đồ tự động nhảy số liệu mới ngay khi có lượt đặt giá hợp lệ mà không cần tải lại giao diện.

## 3. Kiến trúc và Thiết kế hệ thống
- **Mô hình Kiến trúc:** Kiến trúc Client-Server đa tầng. Phía Client áp dụng MVC (JavaFX + FXML). Phía Server phân tách Controller - Model - DAO/Database. Đảm bảo nguyên tắc chỉ có Server mới được quyền truy cập trực tiếp vào Database.
- **Thiết kế Hướng đối tượng (OOP):**
  - **Encapsulation (Đóng gói):** Sử dụng private/protected kết hợp getter/setter để bảo vệ dữ liệu nhạy cảm của hệ thống.
  - **Inheritance (Kế thừa):** Phân cấp thực thể rõ ràng (User -> Admin/Seller/Bidder; Item -> Electronics/Art/Vehicle).
  - **Polymorphism (Đa hình):** Ghi đè (override) các phương thức in thông tin hoặc xử lý logic theo từng loại đối tượng.
  - **Abstraction (Trừu tượng):** Cấu trúc Abstract Class/Interface cho các thực thể Entity và DAO nền tảng.
- **Design Pattern áp dụng:**
  - **Singleton:** Quản lý tập trung kết nối Database (Connection Pool) và các hệ thống lõi.
  - **Factory Method:** Khởi tạo linh hoạt các thể hiện đối tượng (UserFactory, ItemFactory) khi đọc dữ liệu từ ResultSet.
  - **Observer:** Cơ chế nền tảng xử lý Real-time update cho giá đấu và trạng thái phiên đến các ClientHandler.
- **Chất lượng mã nguồn:** Tuân thủ Google Java Style Guide, chú trọng refactoring để mã nguồn sạch, dễ đọc và loại bỏ code thừa.

## 4. Hướng dẫn cài đặt
- **Yêu cầu:** JDK 17, Maven và MySQL Server (cổng 3306).
- **Thiết lập Database:**
  1. Tạo cơ sở dữ liệu mới với tên: `auction_system`
  2. Chạy file `init_db.sql` để tạo cấu trúc bảng. 
  3. Mở file `DatabaseConnection.java`, cấu hình lại thông tin `db.user` và `db.password` cho khớp với MySQL trên máy của bạn.
- **Khởi chạy:**
  1. Server: Chạy class `org.example.ServerMain` để bật máy chủ ở cổng 8888.
  2. Client: Chạy class `com.auction.client.Launcher` để mở giao diện người dùng.

## 5. Hướng dẫn sử dụng

- **Tài khoản Test:**
  - Admin: admin / 123455
  - Seller: seller / 12345
  - Bidder: bidder / 12345
- **Luồng cơ bản:**
  1. Đăng nhập bằng tài khoản Bidder.
  2. Nạp số dư vào tài khoản.
  3. Chọn một phiên đấu giá đang diễn ra và tiến hành đặt giá.
  4. Mở thêm 1 client khác bằng tài khoản Bidder thứ 2 để thấy giao diện cập nhật giá theo thời gian thực.

## 6. Hình ảnh giao diện

### Trang đăng nhập
<img width="716" height="835" alt="Annotation 2026-06-05 090529" src="https://github.com/user-attachments/assets/927ae383-8bd1-448e-81ca-c064c83514c0" />

### Chi tiết sản phẩm đấu giá và Đặt giá realtime
<img width="1876" height="982" alt="Annotation 2026-06-05 090036" src="https://github.com/user-attachments/assets/6c36d161-0f3c-4230-9987-bbd33c4e0523" />

<img width="1920" height="1031" alt="Annotation 2026-06-04 214114" src="https://github.com/user-attachments/assets/ebc76595-7384-44db-9c40-6e366f8e985a" />

### Trang quản trị
<img width="1907" height="986" alt="Annotation 2026-06-05 091913" src="https://github.com/user-attachments/assets/6eb5c0d6-843e-4700-b68a-170765e8acd6" />


## 7. Thành viên nhóm

| Họ và tên | Nhiệm vụ chính |
| :--- | :--- |
| **Ngô Đức Minh** (Leader) | Phân tích yêu cầu, tích hợp hệ thống, kiểm tra luồng tổng thể, quản lý tiến độ. |
| **Đỗ Trọng Nghĩa** | Thiết kế Entity (áp dụng 4 tính chất OOP), DAO, Factory, thao tác cơ sở dữ liệu MySQL/HikariCP. |
| **Trần Thành Trung** | Phát triển JavaFX Client (chuẩn MVC), Controller giao diện, DTO, hỗ trợ kiểm thử tính năng UI. |
| **Nguyễn Tuấn Dương** | Xử lý Networking (TCP Socket), luồng đồng thời (Concurrency), giao tiếp Real-time (Observer). |

