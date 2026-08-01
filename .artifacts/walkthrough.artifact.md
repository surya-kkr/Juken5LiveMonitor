# Walkthrough - Logika Est. Horsepower Racing (Real-time)

Saya telah memfungsikan indikator **EST. HP** pada tema Racing agar bekerja secara dinamis selama pengujian 0-100km/h berlangsung.

## Perubahan yang Dilakukan

### ViewModel
- **[MonitorViewModel.kt](file:///E:/Project App Juken/Juken5LiveMonitor V.0.0.9/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)**:
    - **Reset HP**: Nilai HP diatur ke `0.0` setiap kali timer disiapkan (Arming).
    - **Kalkulasi Real-time**: Selama fase `RUNNING` (dari start hingga 100km/h), sistem kini menghitung HP instan setiap ada pembaruan data dari GPS (~100ms).
    - **Metode Fisika**: Menggunakan rumus `Power = (Mass * Acceleration) * Velocity`. Akselerasi dihitung dari perubahan kecepatan terhadap waktu antar sinyal GPS.
    - **Peak HP Tracking**: Dashboard akan menampilkan nilai tenaga tertinggi (**Peak HP**) yang berhasil dicapai selama proses akselerasi. Angka ini akan terus diperbarui jika ditemukan titik tenaga yang lebih tinggi saat motor sedang dipacu.
    - **Finalisasi**: Saat menyentuh 100km/h, angka HP akan mengunci pada nilai tertinggi yang dicatat tadi.

## Hasil Verifikasi

### Automated Tests
- Build berhasil tanpa error (`compileDebugKotlin success`).

### Manual Verification
- Anda bisa masuk ke tema **Racing**, klik 0-100km/h, dan mulai memacu kendaraan.
- Anda akan melihat angka **EST. HP** bergerak naik seiring bertambahnya kecepatan dan akselerasi, hingga akhirnya berhenti di angka tenaga puncak saat mencapai 100km/h.
