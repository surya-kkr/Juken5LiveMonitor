# Walkthrough - Implementasi Tema KKRXAG

Saya telah mengintegrasikan tema **KKRXAG** ke dalam pilihan Settings dan menghubungkan parameter data ECU sesuai dengan permintaan Anda tanpa mengubah tata letak UI yang sudah ada.

## Perubahan Utama

### 1. Pilihan Tema di Settings
Saya menambahkan tombol **KKRXAG** di `SettingsDialog`. Agar tampilan tetap rapi, pilihan UI sekarang disusun dalam 2 baris:
- Baris 1: Classic & Modern
- Baris 2: Racing & KKRXAG

### 2. Integrasi Navigasi Dashboard
Dashboard KKRXAG sekarang resmi terdaftar di `MonitorLandscape.kt`. Ketika Anda memilih tema KKRXAG (indeks 3), aplikasi akan menampilkan antarmuka KKRXAG.

### 3. Pemetaan Data ECU pada KKRXAG
Saya menyesuaikan data yang ditampilkan pada komponen KKRXAG dengan merujuk pada logika tema Racing:
- **Bar Atas**: Menampilkan **AFR**, **TPS**, **Battery**, dan **Engine Temp** secara real-time.
- **Grid Bawah (Kiri)**: Slot data telah diperbarui untuk menampilkan:
    - **OIL TEMP** (diambil dari Engine Oil Temp)
    - **AIR TEMP** (diambil dari Intake Air Temp)
    - **IGNITION** (menampilkan derajat pengapian)
    - **INJECTOR** (menampilkan durasi injeksi dalam ms)
- **Grid Bawah (Kanan)**: Tetap menampilkan data performa seperti **0-100 KM/H**, **EST. HP**, **TOP SPEED**, dan **AVG SPEED**.

### 4. Fitur Interaktif 0-100 KM/H
Sama seperti tema Racing, fitur timer 0-100 pada tema KKRXAG kini interaktif:
- Klik pada label "0-100 KM/H" untuk menyiapkan timer.
- Warna teks akan berubah sesuai status (Kuning untuk READY, Hijau untuk RUNNING).

## Verifikasi yang Dilakukan
- Memastikan semua komponen di package `kkrxag` menggunakan komponen lokalnya sendiri.
- Memastikan tidak ada pergeseran posisi pada elemen-elemen grafis (Gauge RPM, Bottom Grid, dll).
- Memverifikasi parameter data yang diminta sudah terpetakan ke variabel ECU yang benar.

Anda sekarang dapat mencoba tema baru ini melalui menu Settings (ikon Gear) di dashboard.
