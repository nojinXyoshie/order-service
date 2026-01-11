# Order Service - Sistem Pembayaran

## Ringkasan Arsitektur

Ini adalah microservice Spring Boot yang menangani pembuatan order dan pemrosesan pembayaran menggunakan HTTP callbacks. Sistem dirancang untuk menangani idempotensi payment gateway, mencegah double charge, dan mengatasi network timeouts.

### Komponen Utama
- **Order Service**: Membuat order dan mengelola status pembayaran
- **Payment Gateway (Eksternal)**: Memproses pembayaran dan mengirim callbacks
- **Notification Service**: Mengirim notifikasi sukses pembayaran
- **H2 Database**: Database in-memory untuk orders, payments, dan notifications

## Penjelasan Alur Pembayaran

1. **Pembuatan Order**
   - Client mengirim `POST /api/orders` dengan customerId dan amount
   - Sistem membuat Order dengan status `PAYMENT_PENDING`
   - Sistem membuat Payment record dengan status `INITIATED`
   - Mengembalikan orderId dan paymentId ke client

2. **Pemrosesan Pembayaran**
   - Payment Service memproses pembayaran (disimulasikan secara eksternal)
   - Payment Gateway mengirim callback ke `POST /api/payments/callback`
   - Sistem memperbarui status pembayaran dan status order sesuai

3. **Notifikasi**
   - Ketika pembayaran berhasil, notifikasi dikirim
   - Sistem memastikan notifikasi hanya dikirim sekali (idempoten)

## Cara Idempotensi Diimplementasikan

### Idempotensi Callback Pembayaran
- Setiap pembayaran memiliki `paymentId` unik
- Sistem memeriksa status pembayaran saat ini sebelum memproses
- Jika status sama dengan request yang masuk, update diabaikan
- Log: `"Idempotent update ignored for payment {} with status {}"`

### Idempotensi Notifikasi
- Sistem memeriksa apakah notifikasi sudah dikirim untuk suatu order
- `notificationRepository.existsByOrderIdAndStatus(orderId, NotificationStatus.SENT)`
- Jika sudah dikirim, pembuatan notifikasi baru dilewati

## Cara Callback Duplikat Ditangani

1. **Pemeriksaan Status**: Sistem membandingkan status yang masuk dengan status pembayaran saat ini
2. **Return Awal**: Jika status tidak berubah, callback diabaikan
3. **Perlindungan Konflik**: Jika pembayaran sudah `SUCCESS`, update yang konflik ditolak
4. **Logging**: Semua percobaan duplikat dicatat untuk audit

```java
if (status == currentStatus) {
    log.info("Idempotent update ignored for payment {} with status {}", payment.getPaymentId(), status);
    return;
}

if (currentStatus == PaymentStatus.SUCCESS) {
    log.warn("Ignoring conflicting update for already successful payment {} with status {}", payment.getPaymentId(), status);
    return;
}
```

## Cara Double Charge Dicegah

1. **Satu Payment per Order**: Setiap order menghasilkan tepat satu payment record
2. **Kontrol Transisi Status**: Hanya transisi `INITIATED` → `SUCCESS/FAILED` yang diizinkan
3. **Pemrosesan Idempoten**: Callback duplikat dengan status yang sama diabaikan
4. **Resolusi Konflik**: Setelah pembayaran `SUCCESS`, tidak ada perubahan status lebih lanjut yang diizinkan
5. **Constraint Database**: Payment records unik berdasarkan paymentId

## Cara Menjalankan Service

### Prasyarat
- Java 17+
- Maven 3.6+

### Langkah-langkah
1. **Clone dan build**
   ```bash
   git clone <repository-url>
   cd order-service
   mvn clean install
   ```

2. **Jalankan aplikasi**
   ```bash
   mvn spring-boot:run
   ```
   
   Aplikasi akan berjalan di `http://localhost:8081`

3. **Akses H2 Console** (opsional)
   - URL: `http://localhost:8081/h2-console`
   - JDBC URL: `jdbc:h2:mem:orderdb`
   - Username: `sa`
   - Password: (kosong)

## Contoh Skenario Test

### 1. Buat Order
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-001","amount":150000}'
```

**Respons:**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "paymentId": "550e8400-e29b-41d4-a716-446655440001",
  "status": "PAYMENT_PENDING",
  "amount": 150000
}
```

### 2. Simulasikan Callback Sukses Pembayaran
```bash
curl -X POST http://localhost:8081/api/payments/callback \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId":"550e8400-e29b-41d4-a716-446655440001",
    "orderId":"550e8400-e29b-41d4-a716-446655440000",
    "status":"SUCCESS",
    "amount":150000
  }'
```

### 3. Cek Status Order
```bash
curl http://localhost:8081/api/orders/550e8400-e29b-41d4-a716-446655440000
```

**Respons yang Diharapkan:**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "cust-001",
  "amount": 150000,
  "status": "PAID",
  "createdAt": "2026-01-11T12:00:00",
  "updatedAt": "2026-01-11T12:01:00"
}
```

### 4. Test Idempotensi (Callback Duplikat)
Kirim callback yang sama lagi - status harus tetap `PAID` dan sistem mencatat idempotent ignore.

### 5. Test Pencegahan Double Charge
Coba kirim callback lain dengan status berbeda - sistem harus menolak jika pembayaran sudah `SUCCESS`.

## API Endpoints

### Manajemen Order
- `POST /api/orders` - Buat order baru
- `GET /api/orders/{orderId}` - Dapatkan detail order

### Callback Pembayaran
- `POST /api/payments/callback` - Terima status pembayaran dari gateway

### Database Console
- `GET /h2-console` - Akses H2 database console

## Keputusan Desain Utama

1. **HTTP Callback daripada Message Broker**: Dipilih untuk kesederhanaan dalam skenario assessment sambil tetap menunjukkan requirement inti
2. **Idempotensi Level Database**: Menggunakan state database untuk memastikan konsistensi
3. **Optimistic Locking**: Field version mencegah masalah modifikasi konkuren
4. **Logging Komprehensif**: Semua perubahan state dicatat untuk debugging dan audit

## Teknologi yang Digunakan

- **Framework**: Spring Boot 3.5.9
- **Database**: H2 (in-memory)
- **Build Tool**: Maven
- **Java Version**: 17
- **Validation**: Jakarta Bean Validation
- **Retry**: Spring Retry (untuk integrasi layanan eksternal di masa depan)