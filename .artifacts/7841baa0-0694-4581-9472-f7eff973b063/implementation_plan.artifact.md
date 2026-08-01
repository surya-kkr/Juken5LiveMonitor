# Implementasi Tema KKRXAG (Tanpa Perubahan Tata Letak)

Menambahkan tema **KKRXAG** ke dalam pilihan pengaturan dan memetakan data ECU ke komponen yang sudah ada tanpa mengubah struktur atau posisi UI.

## User Review Required

> [!IMPORTANT]
> - Struktur visual `KkrxagMonitor` tidak akan berubah (posisi gauge, grid, dan bar tetap).
> - Hanya label dan sumber data pada slot yang tersedia yang akan disesuaikan dengan permintaan Anda.

## Proposed Changes

### [app] Settings & Navigation

#### [MODIFY] [SettingsDialog.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/components/SettingsDialog.kt)
- Menambahkan tombol **KKRXAG** pada bagian pemilihan UI.
- Mengatur tata letak tombol agar tetap rapi (menggunakan 2 baris: Classic/Modern dan Racing/KKRXAG) untuk menjaga konsistensi posisi.

#### [MODIFY] [MonitorLandscape.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/MonitorLandscape.kt)
- Mengaktifkan `KkrxagMonitor` ketika `selectedUi == 3`.

### [app] KKRXAG Theme Data Mapping

#### [MODIFY] [KkrxagMonitor.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/kkrxag/KkrxagMonitor.kt)
- Menghubungkan semua data yang diperlukan dari `MonitorViewModel` dan `ECULiveData` ke komponen KKRXAG.

#### [MODIFY] [KkrRacingBottomGrid.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/kkrxag/KkrRacingBottomGrid.kt)
- Memetakan data pada 4 slot kiri:
    1. **OIL TEMP** (menggunakan `liveData.eot`)
    2. **AIR TEMP** (menggunakan `liveData.iat`)
    3. **IGNITION** (menggunakan `liveData.ignition`)
    4. **INJECTOR** (menggunakan `liveData.injector`)
- Mempertahankan slot kanan untuk performa (0-100, Top Speed, HP, Avg Speed) namun memastikan fungsi 0-100 interaktif seperti di tema Racing.

#### [MODIFY] [KkrRacingTopBar.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/kkrxag/KkrRacingTopBar.kt)
- Memastikan BATT, TEMP, TPS, dan AFR terhubung ke sumber data yang benar (sudah ada, tinggal verifikasi).

## Verification Plan

### Manual Verification
- Cek dialog Settings: Pastikan tombol KKRXAG muncul dan tidak merusak layout tombol lainnya.
- Aktifkan KKRXAG: Pastikan semua angka (AFR, TPS, BATT, TEMP, IGN, INJ, dll) bergerak sesuai data ECU.
- Uji Klik 0-100: Pastikan label "0-100 KM/H" bisa diklik untuk memulai timer.
