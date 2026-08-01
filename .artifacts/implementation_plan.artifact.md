# Pembaruan Logika Estimasi Horsepower (HP) Racing

Rencana ini bertujuan untuk memfungsikan indikator "EST. HP" secara dinamis dengan memperhitungkan kecepatan real-time, akselerasi, dan RPM selama pengujian 0-100km/h.

## Perubahan yang Diusulkan

### [Component] ViewModel

#### [MODIFY] [MonitorViewModel.kt](file:///E:/Project App Juken/Juken5LiveMonitor V.0.0.9/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
- **Kalkulasi Real-time**: Mengubah logika di `startGPS` agar menghitung HP secara instan setiap kali menerima update lokasi (saat status `RUNNING`).
    - Rumus Instan: `Power (W) = Massa * Akselerasi (dv/dt) * Kecepatan (v)`.
    - Konversi ke HP: `W / 745.7`.
- **Peak HP Tracking**: Menambahkan variabel untuk menyimpan nilai HP tertinggi (`maxHpDuringRun`) yang dicapai selama pengujian.
- **Integrasi RPM**: Menampilkan HP instan yang berkorelasi dengan RPM saat ini pada dashboard selama pengujian berlangsung.
- **Hasil Akhir**: Saat pengujian selesai (`FINISHED`), nilai HP yang ditampilkan adalah nilai tertinggi (**Peak HP**) yang berhasil dicatat, bukan nilai rata-rata.

## Rencana Verifikasi

### Verifikasi Manual
1. Masuk ke tema **Racing**.
2. Aktifkan timer 0-100km/h.
3. Lakukan akselerasi.
4. Pastikan angka **EST. HP** bergerak naik-turun mengikuti tarikan gas dan perubahan RPM secara real-time.
5. Setelah mencapai 100km/h, pastikan angka tersebut berhenti pada nilai tertinggi yang dicapai selama akselerasi tadi.
