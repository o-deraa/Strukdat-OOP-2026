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
    class Exportable {
        <<interface>>
        +toCSV() String
        +toText() String
    }
    
    class Notifiable {
        <<interface>>
        +terimaNotifikasi(pesan: String)
        +getKotakMasuk() List~String~
        +adaPesanBelumDibaca() boolean
        +tandaiSudahDibaca()
    }
    
    class Sampah {
        <<abstract>>
        -id: int
        -tipe: String
        -berat: double
        +hitungPoin()* int
        +toCSV() String
        +toText() String
    }
    
    class SampahOrganik {
        -isCompos: boolean
        +hitungPoin() int
        +toCSV() String
    }
    
    class SampahAnorganik {
        -material: String
        +hitungPoin() int
    }
    
    class SampahB3 {
        -tingkatBahaya: String
        +hitungPoin() int
    }
    
    class SampahDaurUlang {
        -kategori: String
        +hitungPoin() int
    }
    
    class Warga {
        -id: int
        -nama: String
        -tanggalLahir: String
        -alamat: String
        -poin: int
        -riwayatSetoran: List~Setoran~
        -kotakMasuk: List~String~
        -adaBelumDibaca: boolean
        -MILESTONES: int[]
        +tambahSetoran(s: Setoran)
        +toCSV() String
        +toText() String
    }
    
    class Setoran {
        -id: int
        -warga: Warga
        -sampah: Sampah
        -tanggal: String
        -poinDiperoleh: int
    }
    
    class Main {
        -daftarWarga: List~Warga~
        +main(args: String[])
    }

    Exportable <|.. Sampah
    Exportable <|.. Warga
    Notifiable <|.. Warga
    Sampah <|-- SampahOrganik
    Sampah <|-- SampahAnorganik
    Sampah <|-- SampahB3
    Sampah <|-- SampahDaurUlang
    Warga "1" --> "*" Setoran : memiliki riwayat
    Setoran "1" --> "1" Warga : milik
    Setoran "1" --> "1" Sampah : mencatat
    Main --> "*" Warga : mengelola

```
