# PPPL Backend API Documentation

API Backend untuk sistem manajemen klien dan request layanan dengan fitur AI Lead Scoring.

## Table of Contents

- [Setup & Installation](#setup--installation)
- [Docker Compose (Development)](#docker-compose-development)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
  - [Health Check](#1-health-check)
  - [Auth](#2-auth-endpoints)
  - [Client Form (Public)](#3-client-form-public)
  - [Public Content](#4-public-content)
  - [Public Projects](#5-public-projects)
  - [Admin - Klien Management](#6-klien-management)
  - [Admin - Layanan Management](#7-layanan-management)
  - [Admin - Request Layanan](#8-request-layanan)
  - [Admin - Lead Scoring (AI)](#9-lead-scoring-ai)
  - [Admin - Manager Management](#10-manager-management)
  - [Admin - Karyawan Management](#11-karyawan-management)
  - [Admin - Rekap Meeting](#12-rekap-meeting)
  - [Admin - Dashboard](#13-dashboard)
  - [Admin - Notifications](#14-notifications)
  - [Admin - Projects](#15-projects-admin)
  - [Admin - Content Management](#16-content-management)
  - [Admin - Upload File](#17-upload-file)
- [Git Workflow](#git-workflow)

---

## Setup & Installation

### Prerequisites

- Java JDK 17
- Maven
- Docker Desktop (untuk PostgreSQL, RabbitMQ, Redis)
- PASTIKAN SUDAH ENABLED API DAN BUAT API KEY DI WEBSITE:
  - https://console.cloud.google.com/apis/library/generativelanguage.googleapis.com
  - https://aistudio.google.com/

### Environment Variables

Salin file `.env.example` menjadi `.env` lalu isi nilainya:

```bash
# macOS / Linux
cp .env.example .env

# Windows
copy .env.example .env
```

---

## Docker Compose (Development)

Docker Compose digunakan untuk menjalankan PostgreSQL, RabbitMQ, dan Redis secara bersamaan tanpa perlu install manual.

### Menjalankan semua service

```bash
docker compose up -d
```

Setelah semua container ready, jalankan aplikasi:

```bash
mvn spring-boot:run
```

### Mematikan semua service (data tetap tersimpan)

```bash
docker compose down
```

### Mematikan semua service + hapus semua data

```bash
docker compose down -v
```

### Cek status container

```bash
docker compose ps
```
---

## Authentication

Sebagian besar endpoint memerlukan JWT Bearer Token di header:

```
Authorization: Bearer <your-jwt-token>
```

**Cara mendapatkan token:**
1. Login menggunakan endpoint `POST /api/auth/login`
2. Copy `token` dari response
3. Gunakan token tersebut di header request berikutnya

**Role-based Access:**

- 🟢 **Public** — Tidak perlu auth
- 🔵 **Bearer Token** — Perlu login (semua role)
- 🟠 **MANAGER+** — Perlu role MANAGER atau SUPER_ADMIN
- 🔴 **SUPER_ADMIN** — Hanya SUPER_ADMIN

---

## API Endpoints

### 1. Health Check

#### 1.1 Check Redis Connection

- **Method**: `GET`
- **URL**: `/api/public/health/redis`
- **Auth**: 🟢 No Auth Required
- **Response**: `"Redis Connected Successfully!"`

---

### 2. Auth Endpoints

#### 2.1 Login

- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Auth**: 🟢 No Auth Required
- **Rate Limit**: 5x per 15 menit per IP
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

#### 2.2 Get Current User

- **Method**: `GET`
- **URL**: `/api/auth/me`
- **Auth**: 🔵 Bearer Token Required

#### 2.3 Register Admin

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

#### 2.4 Register Manager (Auto-generate credentials)

- **Method**: `POST`
- **URL**: `/api/admin/register-manager`
- **Auth**: 🔴 SUPER_ADMIN Only
- **Keterangan**: Username & password di-generate otomatis, dikirim via email ke manager
- **Body**:
```json
{
  "namaLengkap": "Jane Smith",
  "email": "jane@company.com",
  "divisi": "Sales"
}
```

#### 2.5 Change Password

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

#### 2.6 Forgot Password

- **Method**: `POST`
- **URL**: `/api/auth/forgot-password`
- **Auth**: 🟢 No Auth Required
- **Rate Limit**: 5x per 15 menit per IP
- **Body**:
```json
{
  "email": "user@example.com"
}
```

#### 2.7 Reset Password

- **Method**: `POST`
- **URL**: `/api/auth/reset-password`
- **Auth**: 🟢 No Auth Required
- **Body**:
```json
{
  "token": "reset-token-dari-email",
  "newPassword": "newpass123"
}
```

#### 2.8 Upload Profile Photo

- **Method**: `POST`
- **URL**: `/api/auth/upload-photo`
- **Auth**: 🔵 Bearer Token Required
- **Body**: `form-data` → key: `file`, value: image file (max 2MB)

#### 2.9 Validate Token

- **Method**: `GET`
- **URL**: `/api/auth/validate`
- **Auth**: 🔵 Bearer Token Required

#### 2.10 Get All Admins

- **Method**: `GET`
- **URL**: `/api/auth/admins`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 2.11 Deactivate Admin

- **Method**: `PUT`
- **URL**: `/api/auth/admins/{id}/deactivate`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 2.12 Activate Admin

- **Method**: `PUT`
- **URL**: `/api/auth/admins/{id}/activate`
- **Auth**: 🔴 SUPER_ADMIN Only

---

### 3. Client Form (Public)

#### 3.1 Submit Form

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
  "message": "Kami membutuhkan solusi CRM",
  "anggaran": "Rp 50.000.000 - Rp 100.000.000",
  "waktuImplementasi": "3-6 bulan"
}
```

#### 3.2 Get Layanan Options

- **Method**: `GET`
- **URL**: `/api/public/form/layanan`
- **Auth**: 🟢 No Auth Required

---

### 4. Public Content

#### 4.1 Get Page Content

- **Method**: `GET`
- **URL**: `/api/public/content/pages/{pageName}`
- **Auth**: 🟢 No Auth Required
- **pageName Options**: sesuai enum `PageName` di backend

---

### 5. Public Projects

#### 5.1 Get All Active Projects

- **Method**: `GET`
- **URL**: `/api/public/projects`
- **Auth**: 🟢 No Auth Required

#### 5.2 Get Featured Projects

- **Method**: `GET`
- **URL**: `/api/public/projects/featured`
- **Auth**: 🟢 No Auth Required

#### 5.3 Search Projects

- **Method**: `POST`
- **URL**: `/api/public/projects/search`
- **Auth**: 🟢 No Auth Required
- **Body**:
```json
{
  "keyword": "CRM",
  "kategori": "DEVELOPMENT"
}
```

#### 5.4 Get Filter Options

- **Method**: `GET`
- **URL**: `/api/public/projects/filter-options`
- **Auth**: 🟢 No Auth Required

---

### 6. Klien Management

#### 6.1 Get All Klien

- **Method**: `GET`
- **URL**: `/api/admin/klien`
- **Auth**: 🔵 Bearer Token Required

#### 6.2 Search Klien

- **Method**: `GET`
- **URL**: `/api/admin/klien/search?keyword=PT&status=SUDAH`
- **Auth**: 🔵 Bearer Token Required

#### 6.3 Get Klien by ID

- **Method**: `GET`
- **URL**: `/api/admin/klien/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 6.4 Create Klien

- **Method**: `POST`
- **URL**: `/api/admin/klien`
- **Auth**: 🔴 SUPER_ADMIN Only
- **Body**:
```json
{
  "namaKlien": "PT Example Corp",
  "emailKlien": "contact@example.com",
  "noTelp": "021-12345678",
  "status": "BELUM"
}
```

#### 6.5 Update Klien

- **Method**: `PUT`
- **URL**: `/api/admin/klien/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only
- **Body**: Sama seperti Create

#### 6.6 Delete Klien

- **Method**: `DELETE`
- **URL**: `/api/admin/klien/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 6.7 Update Klien Status

- **Method**: `PATCH`
- **URL**: `/api/admin/klien/{id}/status?status=SUDAH`
- **Auth**: 🟠 MANAGER+ Required

---

### 7. Layanan Management

#### 7.1 Get All Layanan

- **Method**: `GET`
- **URL**: `/api/admin/layanan`
- **Auth**: 🔵 Bearer Token Required

#### 7.2 Search Layanan

- **Method**: `GET`
- **URL**: `/api/admin/layanan/search?keyword=CRM&kategori=DEVELOPMENT`
- **Auth**: 🔵 Bearer Token Required

#### 7.3 Get Layanan by Kategori

- **Method**: `GET`
- **URL**: `/api/admin/layanan/kategori/{kategori}`
- **Auth**: 🔵 Bearer Token Required
- **kategori Options**: `DEVELOPMENT`, `CONSULTING`, `TRAINING`

#### 7.4 Get Layanan by ID

- **Method**: `GET`
- **URL**: `/api/admin/layanan/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 7.5 Create Layanan

- **Method**: `POST`
- **URL**: `/api/admin/layanan`
- **Auth**: 🔴 SUPER_ADMIN Only
- **Body**:
```json
{
  "namaLayanan": "CRM Implementation",
  "kategori": "DEVELOPMENT",
  "catatan": "Custom CRM solution"
}
```

#### 7.6 Update Layanan

- **Method**: `PUT`
- **URL**: `/api/admin/layanan/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only
- **Body**: Sama seperti Create

#### 7.7 Delete Layanan

- **Method**: `DELETE`
- **URL**: `/api/admin/layanan/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only

---

### 8. Request Layanan

#### 8.1 Get All Requests

- **Method**: `GET`
- **URL**: `/api/admin/request-layanan`
- **Auth**: 🔵 Bearer Token Required

#### 8.2 Get Request by ID

- **Method**: `GET`
- **URL**: `/api/admin/request-layanan/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 8.3 Get by Status

- **Method**: `GET`
- **URL**: `/api/admin/request-layanan/status/{status}`
- **Auth**: 🔵 Bearer Token Required
- **Status Options**: `MENUNGGU_VERIFIKASI`, `DISETUJUI`, `DITOLAK`

#### 8.4 Get Statistics

- **Method**: `GET`
- **URL**: `/api/admin/request-layanan/statistics`
- **Auth**: 🔵 Bearer Token Required

#### 8.5 Get Active Klien

- **Method**: `GET`
- **URL**: `/api/admin/request-layanan/active-klien`
- **Auth**: 🔵 Bearer Token Required

#### 8.6 Approve Request

- **Method**: `POST`
- **URL**: `/api/admin/request-layanan/{id}/approve`
- **Auth**: 🔵 Bearer Token Required

#### 8.7 Reject Request

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

### 9. Lead Scoring (AI)

**Rate Limited**: 15 requests per 2 menit per admin (Redis-based)

#### 9.1 Analyze Single Lead

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

#### 9.2 Batch Analyze All Pending

- **Method**: `POST`
- **URL**: `/api/admin/lead-scoring/analyze-all`
- **Auth**: 🔵 Bearer Token Required

#### 9.3 Get All Lead Results

- **Method**: `GET`
- **URL**: `/api/admin/lead-scoring/results`
- **Auth**: 🔵 Bearer Token Required
- **Cache**: 1 jam

#### 9.4 Get by Priority

- **Method**: `GET`
- **URL**: `/api/admin/lead-scoring/results/priority/{priority}`
- **Auth**: 🔵 Bearer Token Required
- **Priority Options**: `HOT`, `WARM`, `COLD`

#### 9.5 Get Statistics

- **Method**: `GET`
- **URL**: `/api/admin/lead-scoring/statistics`
- **Auth**: 🔵 Bearer Token Required

#### 9.6 Get Rate Limit Info

- **Method**: `GET`
- **URL**: `/api/admin/lead-scoring/rate-limit-info`
- **Auth**: 🔵 Bearer Token Required

---

### 10. Manager Management

#### 10.1 Get All Managers

- **Method**: `GET`
- **URL**: `/api/admin/manager`
- **Auth**: 🔵 Bearer Token Required

#### 10.2 Search Managers

- **Method**: `GET`
- **URL**: `/api/admin/manager/search?keyword=Jane&divisi=Sales`
- **Auth**: 🔵 Bearer Token Required

#### 10.3 Get Divisi List

- **Method**: `GET`
- **URL**: `/api/admin/manager/divisi`
- **Auth**: 🔵 Bearer Token Required

#### 10.4 Get Divisi from Layanan

- **Method**: `GET`
- **URL**: `/api/admin/manager/divisi-layanan`
- **Auth**: 🔵 Bearer Token Required

#### 10.5 Get Manager by ID

- **Method**: `GET`
- **URL**: `/api/admin/manager/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 10.6 Create Manager

- **Method**: `POST`
- **URL**: `/api/admin/manager`
- **Auth**: 🔴 SUPER_ADMIN Only
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

#### 10.7 Update Manager

- **Method**: `PUT`
- **URL**: `/api/admin/manager/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only
- **Body**: Sama seperti Create

#### 10.8 Delete Manager

- **Method**: `DELETE`
- **URL**: `/api/admin/manager/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only
- **Keterangan**: Menghapus manager beserta akun login-nya

---

### 11. Karyawan Management

#### 11.1 Get All Karyawan

- **Method**: `GET`
- **URL**: `/api/admin/karyawan`
- **Auth**: 🔵 Bearer Token Required

#### 11.2 Search Karyawan

- **Method**: `GET`
- **URL**: `/api/admin/karyawan/search?keyword=Bob&idManager=1`
- **Auth**: 🔵 Bearer Token Required

#### 11.3 Get Karyawan by Manager

- **Method**: `GET`
- **URL**: `/api/admin/karyawan/manager/{idManager}`
- **Auth**: 🔵 Bearer Token Required

#### 11.4 Get Karyawan by ID

- **Method**: `GET`
- **URL**: `/api/admin/karyawan/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 11.5 Create Karyawan

- **Method**: `POST`
- **URL**: `/api/admin/karyawan`
- **Auth**: 🔴 SUPER_ADMIN Only
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

#### 11.6 Update Karyawan

- **Method**: `PUT`
- **URL**: `/api/admin/karyawan/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only
- **Body**: Sama seperti Create

#### 11.7 Delete Karyawan

- **Method**: `DELETE`
- **URL**: `/api/admin/karyawan/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only

---

### 12. Rekap Meeting

#### 12.1 Get All Rekap

- **Method**: `GET`
- **URL**: `/api/admin/rekap`
- **Auth**: 🟠 MANAGER+ Required

#### 12.2 Search Rekap

- **Method**: `GET`
- **URL**: `/api/admin/rekap/search?keyword=PT&status=SELESAI`
- **Auth**: 🟠 MANAGER+ Required

#### 12.3 Get Rekap by Klien

- **Method**: `GET`
- **URL**: `/api/admin/rekap/klien/{idKlien}`
- **Auth**: 🟠 MANAGER+ Required

#### 12.4 Get Rekap by Manager

- **Method**: `GET`
- **URL**: `/api/admin/rekap/manager/{idManager}`
- **Auth**: 🟠 MANAGER+ Required

#### 12.5 Get Rekap by ID

- **Method**: `GET`
- **URL**: `/api/admin/rekap/{id}`
- **Auth**: 🟠 MANAGER+ Required

#### 12.6 Create Rekap

- **Method**: `POST`
- **URL**: `/api/admin/rekap`
- **Auth**: 🟠 MANAGER+ Required
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

#### 12.7 Update Rekap

- **Method**: `PUT`
- **URL**: `/api/admin/rekap/{id}`
- **Auth**: 🟠 MANAGER+ Required
- **Body**: Sama seperti Create

#### 12.8 Delete Rekap

- **Method**: `DELETE`
- **URL**: `/api/admin/rekap/{id}`
- **Auth**: 🔴 SUPER_ADMIN Only

---

### 13. Dashboard

#### 13.1 Monthly Lead Stats

- **Method**: `GET`
- **URL**: `/api/admin/dashboard/monthly-lead-stats?months=6`
- **Auth**: 🔵 Bearer Token Required

#### 13.2 Lead Trend

- **Method**: `GET`
- **URL**: `/api/admin/dashboard/lead-trend?months=6`
- **Auth**: 🔵 Bearer Token Required

#### 13.3 Conversion Rate

- **Method**: `GET`
- **URL**: `/api/admin/dashboard/conversion-rate?months=6`
- **Auth**: 🔵 Bearer Token Required

#### 13.4 Recent Activities

- **Method**: `GET`
- **URL**: `/api/admin/dashboard/recent-activities?limit=10`
- **Auth**: 🔵 Bearer Token Required

---

### 14. Notifications

#### 14.1 Get All Notifications

- **Method**: `GET`
- **URL**: `/api/admin/notifications`
- **Auth**: 🔵 Bearer Token Required

#### 14.2 Get Recent (10 latest)

- **Method**: `GET`
- **URL**: `/api/admin/notifications/recent`
- **Auth**: 🔵 Bearer Token Required

#### 14.3 Get Unread

- **Method**: `GET`
- **URL**: `/api/admin/notifications/unread`
- **Auth**: 🔵 Bearer Token Required

#### 14.4 Get Unread Count

- **Method**: `GET`
- **URL**: `/api/admin/notifications/unread/count`
- **Auth**: 🔵 Bearer Token Required

#### 14.5 Mark as Read

- **Method**: `PUT`
- **URL**: `/api/admin/notifications/{id}/read`
- **Auth**: 🔵 Bearer Token Required

#### 14.6 Mark All as Read

- **Method**: `PUT`
- **URL**: `/api/admin/notifications/read-all`
- **Auth**: 🔵 Bearer Token Required

#### 14.7 Delete Notification

- **Method**: `DELETE`
- **URL**: `/api/admin/notifications/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 14.8 Test Real-time Notification (RabbitMQ + WebSocket)

- **Method**: `POST`
- **URL**: `/api/admin/notifications/test-realtime`
- **Auth**: 🔵 Bearer Token Required

#### 14.9 Test Email

- **Method**: `POST`
- **URL**: `/api/admin/notifications/test-email?to=test@example.com`
- **Auth**: 🔴 SUPER_ADMIN Only

#### 14.10 Cleanup Old Notifications

- **Method**: `DELETE`
- **URL**: `/api/admin/notifications/cleanup`
- **Auth**: 🔴 SUPER_ADMIN Only

---

### 15. Projects (Admin)

#### 15.1 Get All Projects

- **Method**: `GET`
- **URL**: `/api/admin/projects`
- **Auth**: 🔵 Bearer Token Required

#### 15.2 Get Project by ID

- **Method**: `GET`
- **URL**: `/api/admin/projects/{id}`
- **Auth**: 🔵 Bearer Token Required

#### 15.3 Create Project

- **Method**: `POST`
- **URL**: `/api/admin/projects`
- **Auth**: 🔵 Bearer Token Required
- **Body**:
```json
{
  "namaProject": "CRM Development",
  "deskripsi": "Custom CRM untuk PT Maju Jaya",
  "kategori": "DEVELOPMENT",
  "isActive": true,
  "isFeatured": false
}
```

#### 15.4 Update Project

- **Method**: `PUT`
- **URL**: `/api/admin/projects/{id}`
- **Auth**: 🔵 Bearer Token Required
- **Body**: Sama seperti Create

#### 15.5 Toggle Active

- **Method**: `PATCH`
- **URL**: `/api/admin/projects/{id}/toggle-active`
- **Auth**: 🔵 Bearer Token Required

#### 15.6 Toggle Featured

- **Method**: `PATCH`
- **URL**: `/api/admin/projects/{id}/toggle-featured`
- **Auth**: 🔵 Bearer Token Required

#### 15.7 Delete Project

- **Method**: `DELETE`
- **URL**: `/api/admin/projects/{id}`
- **Auth**: 🔵 Bearer Token Required

---

### 16. Content Management

#### 16.1 Get All Content (Grouped by Page)

- **Method**: `GET`
- **URL**: `/api/admin/content/all`
- **Auth**: 🔵 Bearer Token Required

#### 16.2 Get Content by Page

- **Method**: `GET`
- **URL**: `/api/admin/content/pages/{pageName}`
- **Auth**: 🔵 Bearer Token Required

#### 16.3 Create Content

- **Method**: `POST`
- **URL**: `/api/admin/content/content`
- **Auth**: 🔵 Bearer Token Required
- **Body**:
```json
{
  "pageName": "HOME",
  "contentKey": "hero_title",
  "contentValue": "Selamat Datang di Pandigi",
  "isActive": true
}
```

#### 16.4 Update Content

- **Method**: `PUT`
- **URL**: `/api/admin/content/{idContent}`
- **Auth**: 🔵 Bearer Token Required
- **Body**: Sama seperti Create

#### 16.5 Bulk Update Page Content

- **Method**: `PUT`
- **URL**: `/api/admin/content/pages/{pageName}/bulk`
- **Auth**: 🔵 Bearer Token Required

#### 16.6 Toggle Active Status

- **Method**: `PATCH`
- **URL**: `/api/admin/content/{idContent}/toggle`
- **Auth**: 🔵 Bearer Token Required

#### 16.7 Delete Content

- **Method**: `DELETE`
- **URL**: `/api/admin/content/{idContent}`
- **Auth**: 🔵 Bearer Token Required

---

### 17. Upload File

#### 17.1 Upload Image

- **Method**: `POST`
- **URL**: `/api/admin/upload/image`
- **Auth**: 🔵 Bearer Token Required
- **Body**: `form-data` → key: `file`, value: image file

---

---

## Git Workflow

### Initial Setup

```bash
git clone https://github.com/nxstray/pandigi-backend.git
cd pandigi-backend
mvn clean install -DskipTests
```

### Daily Development

```bash
# 1. Pull latest changes
git pull origin main

# 2. Buat feature branch
git checkout -b feature/nama-fitur

# 3. Jalankan services
docker compose up -d

# 4. Jalankan aplikasi
mvn spring-boot:run

# 5. Stage & commit
git add .
git commit -m "feat: tambah endpoint baru"

# 6. Push & buat Pull Request
git push origin feature/nama-fitur
```
---