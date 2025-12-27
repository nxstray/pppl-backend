# PPPL Backend API Documentation
API Backend untuk sistem manajemen klien dan request layanan dengan fitur AI Lead Scoring.

## Table of Contents
- [Setup & Installation](#setup--installation)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
  - [Auth](#1-auth-endpoints)
  - [Client Form (Public)](#2-client-form-public)
  - [Klien Management](#3-klien-management)
  - [Layanan Management](#4-layanan-management)
  - [Request Layanan](#5-request-layanan)
  - [Lead Scoring (AI)](#6-lead-scoring-ai)
  - [Manager Management](#7-manager-management)
  - [Karyawan Management](#8-karyawan-management)
  - [Rekap Meeting](#9-rekap-meeting)
  - [Notifications](#10-notifications)
- [Git Workflow](#git-workflow)

---

## Setup & Installation
### Prerequisites
- Java JDK 17
- PostgreSQL
- Maven
- RabbitMQ (untuk notifikasi real-time)
- PASTIKAN SUDAH ENABLED API DAN BUAT API KEY DI WEBSITE 
*https://console.cloud.google.com/apis/library/generativelanguage.googleapis.com?project=idyllic-cabinet-481618-u5*
*https://aistudio.google.com/*

### Environment Variables
```bash
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Gemini AI
gemini.api.key=your-gemini-api-key

# Email (optional)
spring.mail.host=smtp.gmail.com
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### Run Application
```bash
mvn spring-boot:run
```

---

## Authentication

Sebagian besar endpoint memerlukan JWT Bearer Token di header:

```js
Authorization: Bearer <your-jwt-token>
```

**Cara mendapatkan token:**
1. Login menggunakan endpoint `/api/auth/login`
2. Copy `token` dari response
3. Gunakan token tersebut di header request berikutnya

**Role-based Access:**
- 🟢 **Public**: Tidak perlu auth
- 🔵 **ADMIN/MANAGER**: Perlu role ADMIN, SUPER_ADMIN, atau MANAGER
- 🔴 **SUPER_ADMIN**: Hanya SUPER_ADMIN

---

## API Endpoints

### 1. Auth Endpoints
#### 1.1 Login (Public)
- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Auth**: 🟢 No Auth Required
- **Body**:
```json
{
  "username": "admin",
  "password": "password123"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "Login berhasil",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "admin",
    "role": "SUPER_ADMIN"
  }
}
```

#### 1.2 Get Current User
- **Method**: `GET`
- **URL**: `/api/auth/me`
- **Auth**: 🔵 Bearer Token Required

#### 1.3 Register Admin
- **Method**: `POST`
- **URL**: `/api/auth/register`
- **Auth**: 🔴 SUPER_ADMIN Only
- **Body**:
```json
{
  "username": "newadmin",
  "password": "password123",
  "namaLengkap": "John Doe",
  "email": "john@example.com",
  "role": "ADMIN"
}
```

#### 1.4 Change Password
- **Method**: `POST`
- **URL**: `/api/auth/change-password`
- **Auth**: 🔵 Bearer Token Required
- **Body**:
```json
{
  "oldPassword": "oldpass123",
  "newPassword": "newpass123"
}
```

#### 1.5 Upload Profile Photo
- **Method**: `POST`
- **URL**: `/api/auth/upload-photo`
- **Auth**: 🔵 Bearer Token Required
- **Body**: Form-data
  - Key: `file` (File)
  - Value: Select image file (max 2MB)

#### 1.6 Validate Token
- **Method**: `GET`
- **URL**: `/api/auth/validate`
- **Auth**: 🔵 Bearer Token Required

#### 1.7 Get All Admins
- **Method**: `GET`
- **URL**: `/api/auth/admins`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 1.8 Deactivate Admin
- **Method**: `PUT`
- **URL**: `/api/auth/admins/{id}/deactivate`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 1.9 Activate Admin
- **Method**: `PUT`
- **URL**: `/api/auth/admins/{id}/activate`
- **Auth**: 🔴 SUPER_ADMIN Only

---

### 2. Client Form (Public)
#### 2.1 Submit Form
- **Method**: `POST`
- **URL**: `/api/public/form/submit`
- **Auth**: 🟢 No Auth Required
- **Body**:
```json
{
  "fullName": "John Doe",
  "email": "john@company.com",
  "phoneNumber": "081234567890",
  "perusahaan": "PT Maju Jaya",
  "idLayanan": 1,
  "message": "Kami membutuhkan solusi CRM untuk perusahaan kami",
  "anggaran": "Rp 50.000.000 - Rp 100.000.000",
  "waktuImplementasi": "3-6 bulan"
}
```

#### 2.2 Get Layanan Options
- **Method**: `GET`
- **URL**: `/api/public/form/layanan`
- **Auth**: 🟢 No Auth Required

---

### 3. Klien Management

#### 3.1 Get All Klien
- **Method**: `GET`
- **URL**: `/api/admin/klien`
- **Auth**: 🔵 Bearer Token Required

#### 3.2 Get Klien by ID
- **Method**: `GET`
- **URL**: `/api/admin/klien/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 3.3 Create Klien
- **Method**: `POST`
- **URL**: `/api/admin/klien`
- **Auth**: 🔵 Bearer Token Required (ADMIN/SUPER_ADMIN)
- **Body**:
```json
{
  "namaKlien": "PT Example Corp",
  "emailKlien": "contact@example.com",
  "noTelp": "021-12345678",
  "status": "BELUM"
}
```

#### 3.4 Update Klien
- **Method**: `PUT`
- **URL**: `/api/admin/klien/{id}`
- **Auth**: 🔵 Bearer Token Required (ADMIN/SUPER_ADMIN)
- **Body**: Same as Create

#### 3.5 Delete Klien
- **Method**: `DELETE`
- **URL**: `/api/admin/klien/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 3.6 Search Klien
- **Method**: `GET`
- **URL**: `/api/admin/klien/search?keyword=PT&status=SUDAH`
- **Auth**: 🔵 Bearer Token Required

#### 3.7 Update Klien Status
- **Method**: `PATCH`
- **URL**: `/api/admin/klien/{id}/status?status=SUDAH`
- **Auth**: 🔵 Bearer Token Required

---

### 4. Layanan Management

#### 4.1 Get All Layanan
- **Method**: `GET`
- **URL**: `/api/admin/layanan`
- **Auth**: 🔵 Bearer Token Required

#### 4.2 Get Layanan by ID
- **Method**: `GET`
- **URL**: `/api/admin/layanan/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 4.3 Create Layanan
- **Method**: `POST`
- **URL**: `/api/admin/layanan`
- **Auth**: 🔵 Bearer Token Required (ADMIN/SUPER_ADMIN)
- **Body**:
```json
{
  "namaLayanan": "CRM Implementation",
  "kategori": "DEVELOPMENT",
  "catatan": "Custom CRM solution"
}
```
**Kategori Options**: `DEVELOPMENT`, `CONSULTING`, `TRAINING`

#### 4.4 Update Layanan
- **Method**: `PUT`
- **URL**: `/api/admin/layanan/{id}`
- **Auth**: 🔵 Bearer Token Required (ADMIN/SUPER_ADMIN)

#### 4.5 Delete Layanan
- **Method**: `DELETE`
- **URL**: `/api/admin/layanan/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 4.6 Search Layanan
- **Method**: `GET`
- **URL**: `/api/admin/layanan/search?keyword=CRM&kategori=DEVELOPMENT`
- **Auth**: 🔵 Bearer Token Required

#### 4.7 Get by Kategori
- **Method**: `GET`
- **URL**: `/api/admin/layanan/kategori/DEVELOPMENT`
- **Auth**: 🔵 Bearer Token Required

---

### 5. Request Layanan

#### 5.1 Get All Requests
- **Method**: `GET`
- **URL**: `/api/admin/request-layanan`
- **Auth**: 🔵 Bearer Token Required

#### 5.2 Get Request by ID
- **Method**: `GET`
- **URL**: `/api/admin/request-layanan/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 5.3 Get by Status
- **Method**: `GET`
- **URL**: `/api/admin/request-layanan/status/MENUNGGU_VERIFIKASI`
- **Auth**: 🔵 Bearer Token Required

**Status Options**: `MENUNGGU_VERIFIKASI`, `DISETUJUI`, `DITOLAK`

#### 5.4 Get Statistics
- **Method**: `GET`
- **URL**: `/api/admin/request-layanan/statistics`
- **Auth**: 🔵 Bearer Token Required

#### 5.5 Get Active Klien
- **Method**: `GET`
- **URL**: `/api/admin/request-layanan/active-klien`
- **Auth**: 🔵 Bearer Token Required

#### 5.6 Approve Request
- **Method**: `POST`
- **URL**: `/api/admin/request-layanan/{id}/approve`
- **Auth**: 🔵 Bearer Token Required

#### 5.7 Reject Request
- **Method**: `POST`
- **URL**: `/api/admin/request-layanan/{id}/reject`
- **Auth**: 🔵 Bearer Token Required
- **Body**:
```json
{
  "keterangan": "Budget tidak sesuai dengan scope project"
}
```

---

### 6. Lead Scoring (AI)

**Rate Limited**: 15 requests per 2 minutes per admin

#### 6.1 Analyze Single Lead
- **Method**: `POST`
- **URL**: `/api/admin/lead-scoring/analyze/{idRequest}`
- **Auth**: 🔵 Bearer Token Required
- **Response**:
```json
{
  "success": true,
  "message": "Lead berhasil dianalisa. Remaining requests: 14/15",
  "data": {
    "idRequest": 1,
    "skor": "HOT",
    "kategori": "Enterprise",
    "alasan": "Budget tinggi dan timeline realistis...",
    "confidence": 0.85
  }
}
```

#### 6.2 Batch Analyze All Pending
- **Method**: `POST`
- **URL**: `/api/admin/lead-scoring/analyze-all`
- **Auth**: 🔵 Bearer Token Required

#### 6.3 Get All Lead Results
- **Method**: `GET`
- **URL**: `/api/admin/lead-scoring/results`
- **Auth**: 🔵 Bearer Token Required

#### 6.4 Get by Priority
- **Method**: `GET`
- **URL**: `/api/admin/lead-scoring/results/priority/HOT`
- **Auth**: 🔵 Bearer Token Required

**Priority Options**: `HOT`, `WARM`, `COLD`

#### 6.5 Get Statistics
- **Method**: `GET`
- **URL**: `/api/admin/lead-scoring/statistics`
- **Auth**: 🔵 Bearer Token Required

#### 6.6 Get Rate Limit Info
- **Method**: `GET`
- **URL**: `/api/admin/lead-scoring/rate-limit-info`
- **Auth**: 🔵 Bearer Token Required

---

### 7. Manager Management
#### 7.1 Get All Managers
- **Method**: `GET`
- **URL**: `/api/admin/manager`
- **Auth**: 🔵 Bearer Token Required

#### 7.2 Get Manager by ID
- **Method**: `GET`
- **URL**: `/api/admin/manager/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 7.3 Create Manager
- **Method**: `POST`
- **URL**: `/api/admin/manager`
- **Auth**: 🔵 Bearer Token Required (ADMIN/SUPER_ADMIN)
- **Body**:
```json
{
  "namaManager": "Jane Smith",
  "emailManager": "jane@company.com",
  "noTelp": "081234567890",
  "divisi": "Sales",
  "tglMulai": "2024-01-15"
}
```

#### 7.4 Update Manager
- **Method**: `PUT`
- **URL**: `/api/admin/manager/{id}`
- **Auth**: 🔵 Bearer Token Required (ADMIN/SUPER_ADMIN)

#### 7.5 Delete Manager
- **Method**: `DELETE`
- **URL**: `/api/admin/manager/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 7.6 Search Managers
- **Method**: `GET`
- **URL**: `/api/admin/manager/search?keyword=Jane&divisi=Sales`
- **Auth**: 🔵 Bearer Token Required

#### 7.7 Get Divisi List
- **Method**: `GET`
- **URL**: `/api/admin/manager/divisi`
- **Auth**: 🔵 Bearer Token Required

---

### 8. Karyawan Management

#### 8.1 Get All Karyawan
- **Method**: `GET`
- **URL**: `/api/admin/karyawan`
- **Auth**: 🔵 Bearer Token Required

#### 8.2 Get Karyawan by ID
- **Method**: `GET`
- **URL**: `/api/admin/karyawan/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 8.3 Create Karyawan
- **Method**: `POST`
- **URL**: `/api/admin/karyawan`
- **Auth**: 🔵 Bearer Token Required (ADMIN/SUPER_ADMIN)
- **Body**:
```json
{
  "namaKaryawan": "Bob Johnson",
  "emailKaryawan": "bob@company.com",
  "noTelp": "081234567890",
  "jabatanPosisi": "Sales Executive",
  "idManager": 1
}
```

#### 8.4 Update Karyawan
- **Method**: `PUT`
- **URL**: `/api/admin/karyawan/{id}`
- **Auth**: 🔵 Bearer Token Required (ADMIN/SUPER_ADMIN)

#### 8.5 Delete Karyawan
- **Method**: `DELETE`
- **URL**: `/api/admin/karyawan/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 8.6 Search Karyawan
- **Method**: `GET`
- **URL**: `/api/admin/karyawan/search?keyword=Bob&idManager=1`
- **Auth**: 🔵 Bearer Token Required

#### 8.7 Get by Manager
- **Method**: `GET`
- **URL**: `/api/admin/karyawan/manager/{idManager}`
- **Auth**: 🔵 Bearer Token Required

---

### 9. Rekap Meeting

#### 9.1 Get All Rekap
- **Method**: `GET`
- **URL**: `/api/admin/rekap`
- **Auth**: 🔵 Bearer Token Required

#### 9.2 Get Rekap by ID
- **Method**: `GET`
- **URL**: `/api/admin/rekap/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 9.3 Create Rekap
- **Method**: `POST`
- **URL**: `/api/admin/rekap`
- **Auth**: 🔵 Bearer Token Required
- **Body**:
```json
{
  "idKlien": 1,
  "idManager": 1,
  "idLayanan": 1,
  "tglMeeting": "2024-01-20",
  "hasil": "Client setuju untuk lanjut ke tahap proposal",
  "status": "MASIH_JALAN",
  "catatan": "Follow up dalam 3 hari"
}
```

**Status Options**: `MASIH_JALAN`, `SELESAI`, `BATAL`

#### 9.4 Update Rekap
- **Method**: `PUT`
- **URL**: `/api/admin/rekap/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 9.5 Delete Rekap
- **Method**: `DELETE`
- **URL**: `/api/admin/rekap/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 9.6 Search Rekap
- **Method**: `GET`
- **URL**: `/api/admin/rekap/search?keyword=PT&status=SELESAI`
- **Auth**: 🔵 Bearer Token Required

#### 9.7 Get by Klien
- **Method**: `GET`
- **URL**: `/api/admin/rekap/klien/{idKlien}`
- **Auth**: 🔵 Bearer Token Required

#### 9.8 Get by Manager
- **Method**: `GET`
- **URL**: `/api/admin/rekap/manager/{idManager}`
- **Auth**: 🔵 Bearer Token Required

---

### 10. Notifications

#### 10.1 Get All Notifications
- **Method**: `GET`
- **URL**: `/api/admin/notifications`
- **Auth**: 🔵 Bearer Token Required

#### 10.2 Get Recent (10 latest)
- **Method**: `GET`
- **URL**: `/api/admin/notifications/recent`
- **Auth**: 🔵 Bearer Token Required

#### 10.3 Get Unread
- **Method**: `GET`
- **URL**: `/api/admin/notifications/unread`
- **Auth**: 🔵 Bearer Token Required

#### 10.4 Get Unread Count
- **Method**: `GET`
- **URL**: `/api/admin/notifications/unread/count`
- **Auth**: 🔵 Bearer Token Required

#### 10.5 Mark as Read
- **Method**: `PUT`
- **URL**: `/api/admin/notifications/{id}/read`
- **Auth**: 🔵 Bearer Token Required

#### 10.6 Mark All as Read
- **Method**: `PUT`
- **URL**: `/api/admin/notifications/read-all`
- **Auth**: 🔵 Bearer Token Required

#### 10.7 Delete Notification
- **Method**: `DELETE`
- **URL**: `/api/admin/notifications/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 10.8 Test Real-time Notification
- **Method**: `POST`
- **URL**: `/api/admin/notifications/test-realtime`
- **Auth**: 🔵 Bearer Token Required

#### 10.9 Test Email (SUPER_ADMIN)
- **Method**: `POST`
- **URL**: `/api/admin/notifications/test-email?to=test@example.com`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 10.10 Cleanup Old Notifications
- **Method**: `DELETE`
- **URL**: `/api/admin/notifications/cleanup`
- **Auth**: 🔴 SUPER_ADMIN Only

---

## Git Workflow
### Initial Setup
```bash
# Clone repository
git clone https://github.com/nxstray/pppl-backend.git
cd pppl-backend

# Install dependencies
mvn clean install -DskipTests
```

### Daily Development
```bash
# 1. Pull latest changes
git pull origin main

# 2. Create feature branch
git checkout -b feature/nama-fitur

# 3. Make changes and test

# 4. Stage changes
git add .

# 5. Commit with meaningful message
git commit -m "feat: tambah endpoint upload foto profil"

# 6. Push to remote
git push origin feature/nama-fitur

# 7. Create Pull Request di GitHub
```

### Commit Message Convention
feat: fitur baru
fix: perbaikan bug
docs: update dokumentasi
refactor: refactoring code
test: tambah unit test
chore: update dependencies

### Example Commits
git commit -m "feat: tambah endpoint lead scoring dengan AI"
git commit -m "fix: perbaiki validasi email di klien controller"
git commit -m "docs: update API documentation untuk auth endpoints"
git commit -m "refactor: optimize query di request layanan service"


### Import Collection Steps:
1. Buat Collection baru: (bebas)
2. Setup Environment Variables:
   - `base_url`: `http://localhost:8083`
   - `token`: (akan di-set setelah login)

### Test Flow:
1. Login → Copy token
2. Set token ke .env
3. Test protected endpoints


---

## Important Notes
1. **CORS**: Frontend harus running di `http://localhost:4200`
2. **Rate Limiting**: Lead Scoring API limited to 15 req/2 min
3. **File Upload**: Max 2MB untuk foto profil
4. **JWT Expiration**: Token berlaku 24 jam (86400000 ms)
5. **Database**: Gunakan PostgreSQL, bukan H2 untuk production

---

## Support
Jika ada issue atau pertanyaan:
1. Buat issue di GitHub