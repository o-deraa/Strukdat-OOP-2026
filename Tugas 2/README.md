# Implementasi OOP dalam Manajemen Pengelolaan Sampah di Lingkungan Desa

### Latar Belakang
Persoalan sampah di Bali tampak seperti tak ada ujungnya dari tahun ke tahun. Banyaknya sampah berserakan, tidak adanya sistem pemilahan sampah yang baik, dan kurangnya kesadaran baik dari pemerintah dan masyarakat semakin menambah PR besar pengelolaan sampah di Bali. Sayangnya, lingkungan tempat saya tinggal juga tidak terlepas dari permasalahan tersebut. Setiap saya keluar rumah, pemandangan sampah berserakan yang menyengat akan selalu ada untuk menyapa saya. Ketika hujan, tidak lengkap rasanya tanpa luapan air yang menenuhi jalan, ditambah dengan berseraknya sampah yang menempel di jalan ketika sudah surut. Menurut saya, persoalan sampah ini harus diselesaikan dulu dari lapisan paling bawah, yakni dari kesadaran masyarakat dan lingkungan terkecil, yakni keluarga dan banjar (Di Bali tidak ada RT/RW, sistem yang serupa yakni banjar). Di banjar, harus diciptakan sistem pengelolaan sampah berbasis poin, merit, dan denda untuk memupuk kebiasaan masyarakat dalam bertanggung jawab terhadap sampah yang mereka hasilkan. Sistem tersebut harus mencakup setidaknya hal - hal berikut:

- Pencatatan dan perekapan setoran sampah yang terstruktur
- Transparansi yang jelas terhadap poin yang dikumpulkan tiap warga
- Ada pembeda poin yang jelas antar tipe sampah

Berikut adalah implementasi OOP yang saya rancang untuk permasalahan tersebut:

## Class Diagram
```mermaid
classDiagram
    %% Bagian Interface dan Implementasinya (Behavior Export & Notifikasi)
    class Exportable {
        <<interface>>
        +toCSV() String
        +toText() String
    }
    
    class Notifiable {
        <<interface>>
        +terimaNotifikasi(String pesan) void
        +getKotakMasuk() List~String~
        +adaPesanBelumDibaca() boolean
        +tandaiSudahDibaca() void
    }

    %% Bagian Abstract Class dan Subclass (Kategori Sampah)
    class Sampah {
        <<abstract>>
        #int id
        #String tipe
        #double berat
        +Sampah(int id, String tipe, double berat)
        +hitungPoin()* int
        +toCSV() String
        +toText() String
    }
    
    class SampahOrganik {
        -boolean isCompos
        +SampahOrganik(int id, String tipe, double berat, boolean isCompos)
        +hitungPoin() int
        +toCSV() String
    }
    
    class SampahAnorganik {
        -String material
        +SampahAnorganik(int id, String tipe, double berat, String material)
        +hitungPoin() int
    }
    
    class SampahB3 {
        -String tingkatBahaya
        +SampahB3(int id, String tipe, double berat, String tingkatBahaya)
        +hitungPoin() int
    }
    
    class SampahDaurUlang {
        -String kategori
        +SampahDaurUlang(int id, String tipe, double berat, String kategori)
        +hitungPoin() int
    }

    %% Bagian Class Utama (Entitas Sistem)
    class Warga {
        -int id
        -String nama
        -String tanggalLahir
        -String alamat
        -int poin
        -List~Setoran~ riwayatSetoran
        -List~String~ kotakMasuk
        -boolean adaBelumDibaca
        -int[] MILESTONES
        +Warga(int id, String nama, String tanggalLahir, String alamat)
        +tambahSetoran(Setoran s) void
        +toCSV() String
        +toText() String
        +terimaNotifikasi(String pesan) void
        +getKotakMasuk() List~String~
        +adaPesanBelumDibaca() boolean
        +tandaiSudahDibaca() void
    }
    
    class Setoran {
        -int id
        -Warga warga
        -Sampah sampah
        -String tanggal
        -int poinDiperoleh
        +Setoran(int id, Warga warga, Sampah sampah, String tanggal, int poinDiperoleh)
    }
    
    class Main {
        -List~Warga~ daftarWarga
        +main(String[] args) void
    }

    %% Relasi Realization/Implementation (Interfaces)
    Exportable <|.. Sampah : implements
    Exportable <|.. Warga : implements
    Notifiable <|.. Warga : implements

    %% Relasi Generalization/Inheritance (Garis tegas dengan panah kosong)
    Sampah <|-- SampahOrganik : extends
    Sampah <|-- SampahAnorganik : extends
    Sampah <|-- SampahB3 : extends
    Sampah <|-- SampahDaurUlang : extends

    %% Relasi Aggregation/Association (Behavior & Kepemilikan)
    Warga o-- Setoran : has-a riwayat
    Setoran o-- Warga : belongs-to
    Setoran o-- Sampah : has-a catatan sampah
    Main o-- Warga : has-a data warga

```
