
---

# 🛒 HayMart - REST API untuk POS Kasir

HayMart adalah sebuah proyek **REST API** berbasis **Spring Boot 3.4.4** yang dirancang untuk mendukung aplikasi **Point of Sale (POS)** modern di berbagai jenis usaha retail.  
Sistem ini menangani seluruh kebutuhan backend untuk otentikasi user, manajemen kasir, pengelolaan produk, transaksi pemesanan, hingga laporan pendapatan.

---

## ✨ Fitur-Fitur API HayMart

### 1. 🔒 Authentication & Authorization

- **Register User**: Endpoint untuk registrasi kasir/admin baru. Akun akan berstatus pending sampai email diverifikasi.
- **Login User**: Mendapatkan JWT Token untuk akses API selanjutnya.
- **Email Verification**: Sistem kirim kode verifikasi ke email user. User harus memverifikasi agar status aktif.
- **Forgot Password**: Kirim OTP ke email untuk melakukan reset password.
- Semua API login/register aman menggunakan **Spring Security + JWT**.

### 2. 👥 Manajemen Kasir

- **Edit Kasir**: Update nama, email, password, dan foto kasir.
- **Data Kasir**: Lihat semua kasir terdaftar (**khusus Admin**).
- **History Pemesanan Kasir**: Admin bisa cek riwayat pemesanan tiap kasir.
- **Update Status Kasir**: Admin bisa aktifkan/nonaktifkan kasir.

### 3. 📦 Manajemen Produk

- **Tambah Produk**: Menambahkan produk baru dengan upload gambar.
- **Edit Produk**: Update nama, harga, stok, kategori produk.
- **List Produk**: Ambil semua produk dengan support filter nama, kategori, harga min/maks, dan sort.
- **Pagination**: Data produk diload dengan sistem halaman (5 produk per halaman).
- **Soft Delete Produk**: Produk dihapus secara soft delete (tidak dihapus permanen).

### 4. 🛍️ Pemesanan Produk

- **Tambah Pemesanan**: Kasir bisa membuat transaksi pembelian produk.
- **Riwayat Pemesanan**: Cek semua pemesanan berdasarkan user atau global.
- **Cetak Struk**: Setelah order selesai, API mengembalikan data struk siap cetak.

### 5. 📑 Laporan Produk

- **Laporan Data Produk**: Menampilkan data seluruh produk yang tersedia.

### 6. 📈 Laporan Pendapatan

- **Pendapatan Harian, Mingguan, Bulanan, Tahunan**: Endpoint untuk melihat total penjualan.
- **Pendapatan Custom Range**: Bisa pilih tanggal mulai dan akhir untuk laporan custom.

---

## 🛠️ Tech Stack

- Java 21
- Spring Boot 3.4.4
- Spring Security + JWT
- JPA + Hibernate (MySQL)
- Swagger (SpringDoc) 2.0.2
- Spring MailSender (untuk Email Verifikasi dan OTP Reset Password)
- Lombok
- Apache POI (export Excel)
- Apache PDFBox (cetak struk PDF)

---

## 📚 Struktur API Endpoint

|  HTTP  | Endpoint                         | Keterangan                      |    Role     |
| :----: | :------------------------------- | :------------------------------ | :---------: |
|  POST  | `/auth/login`                    | Login user                      |   Public    |
|  POST  | `/auth/register`                 | Registrasi user baru            |   Public    |
|  POST  | `/auth/forgot-password`          | Kirim OTP untuk reset password  |   Public    |
|  POST  | `/auth/reset-password`           | Reset password                  |   Public    |
|  GET   | `/auth/verify`                   | Verifikasi email                |   Public    |
|  POST  | `/produk/create`                 | Admin tambah produk             |    Admin    |
|  POST  | `/produk/update/{id}`            | Admin update produk             |    Admin    |
|  GET   | `/produk/get-all-produks`        | List produk semua               | Kasir/Admin |
|  GET   | `/produk/get-produk-page`        | List produk pagination          | Kasir/Admin |
| DELETE | `/produk/delete-produk/{id}`     | Admin hapus produk              |    Admin    |
|  POST  | `/pemesanan/create-pemesanan`    | Kasir buat pemesanan            |    Kasir    |
|  GET   | `/pemesanan/history`             | Kasir lihat history pemesanan   |    Kasir    |
|  GET   | `/pemesanan/struk/{pemesananId}` | Kasir cetak struk               |    Kasir    |
|  PUT   | `/kasir/edit-kasir/{id}`         | Kasir edit data sendiri         |    Kasir    |
|  POST  | `/kasir/update-status/{id}`      | Admin update status kasir       |    Admin    |
|  GET   | `/kasir/get-all-kasir`           | Admin lihat semua kasir         |    Admin    |
|  GET   | `/kasir/history-all-kasir`       | Admin lihat history semua kasir |    Admin    |
|  GET   | `/laporan/laporan-produk`        | Laporan semua produk            |    Admin    |
|  GET   | `/laporan/pendapatan-harian`     | Laporan pendapatan harian       |    Admin    |
|  GET   | `/laporan/pendapatan-mingguan`   | Laporan pendapatan mingguan     |    Admin    |
|  GET   | `/laporan/pendapatan-bulanan`    | Laporan pendapatan bulanan      |    Admin    |
|  GET   | `/laporan/pendapatan-tahunan`    | Laporan pendapatan tahunan      |    Admin    |
|  GET   | `/laporan/pendapatan-permintaan` | Laporan pendapatan custom       |    Admin    |

---

## 🧠 Detail Keamanan

- **JWT Token Authorization**: Semua request ke API harus membawa Bearer Token (kecuali login, register, lupa password, verify).
- **Role Based Access Control (RBAC)**:
  - **ROLE_ADMIN**: Bisa mengakses API manajemen kasir, produk, laporan.
  - **ROLE_KASIR**: Hanya bisa mengakses API pemesanan dan edit diri sendiri.
- **Verifikasi Email**: User baru harus verifikasi email untuk aktif.

---

## 🎯 Target User

- Aplikasi POS Retail (minimarket, apotek, toko fashion, dll)
- Sistem manajemen kasir backoffice

---

## 🚀 Catatan Penting

- **File Upload**: Gambar produk diunggah menggunakan `multipart/form-data`.
- **Swagger UI**: API sudah dokumentasi otomatis di `/swagger-ui.html`.

---
