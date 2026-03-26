import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


interface Exportable{
    String toCSV();
    String toText();
}

interface Notifiable {
    void terimaNotifikasi(String pesan);
    List<String> getKotakMasuk();
    boolean adaPesanBelumDibaca();
    void tandaiSudahDibaca();
}

public class Main {

    static List<Warga> daftarWarga  = new ArrayList<>();
    static int idWarga = 1;
    static int idSetoran = 1;
    static int idSampah  = 1;
    static Scanner input = new Scanner(System.in);

    //databse
    static final String FILE_WARGA   = "export_warga.csv";
    static final String FILE_SETORAN = "export_setoran.csv";
    static final String FILE_REPORT  = "export_laporan.txt";

    static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    // Baca int dengan validasi, tidak pakai nextInt() agar tidak ghost newline
    static int bacaInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try   { return Integer.parseInt(input.nextLine().trim()); }
            catch (NumberFormatException e) {
                System.out.println("[ERROR] Input harus angka bulat."); }
        }
    }
 
    // Baca double dengan validasi
    static double bacaDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(input.nextLine().trim());
                if (v <= 0) { System.out.println("[ERROR] Harus lebih dari 0."); continue; }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Input harus angka desimal."); }
        }
    }



    static boolean isValidTanggal(String tanggal) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate.parse(tanggal, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }


    static void tambahWarga(){

        System.out.println("========================================");
        System.out.println("        MENAMBAHKAN WARGA BARU");
        System.out.println("========================================");
    
        String nama;
        while (true) {
            System.out.print("Masukkan nama: ");
            nama = input.nextLine();

            if (nama != null && nama.matches("[a-zA-Z ]+")) {
                break;
            } else {
                System.out.println("[TIDAK VALID] Nama hanya boleh huruf dan spasi.\n");
            }
        }

        String tanggalLahir;
        while (true) {
            System.out.print("Masukkan tanggal lahir (DD-MM-YYYY): ");
            tanggalLahir = input.nextLine();

            if (isValidTanggal(tanggalLahir)) {
                break;
            } else {
                System.out.println("[ERROR] Format tanggal tidak valid.\n");
            }
        }

        String alamat;
        while (true) {
            System.out.print("Masukkan alamat: ");
            alamat = input.nextLine();

            if (alamat != null && !alamat.isBlank()) {
                break;
            } else {
                System.out.println("[ERROR] Alamat tidak boleh kosong.\n");
            }
        }

        Warga w = new Warga(idWarga, nama, tanggalLahir, alamat);
        daftarWarga.add(w);

        System.out.println("\n[SUKSES] Warga berhasil ditambahkan!");
        System.out.printf("ID: %d | %s | Lahir: %s | %s%n", idWarga, nama, tanggalLahir, alamat);
        System.out.print("Tekan [Enter] untuk melanjutkan..."); input.nextLine(); // menunggu Enter
    }

    //MENU 2 - LIHAT DAFTAR WARGA
    static void lihatWarga() {
        System.out.println("========================================");
        System.out.println("         DAFTAR WARGA TERDAFTAR");
        System.out.println("========================================");

        if (daftarWarga.isEmpty()) {
            System.out.println("  (Belum ada warga terdaftar)");
        } else {
            System.out.printf("  %-4s %-20s %-14s %-25s %6s %6s%n",
                "ID", "Nama", "Tgl Lahir", "Alamat", "Setor", "Poin");
            System.out.println("  " + "-".repeat(80));
            for (Warga w : daftarWarga) {
                System.out.printf("  %-4d %-20s %-14s %-25s %6d %6d%n",
                    w.getId(), w.getNama(), w.getTanggalLahir(),
                    w.getAlamat(), w.getRiwayatSetoran().size(), w.getPoin());
            }
            System.out.println("  " + "-".repeat(80));
            System.out.printf("  Total warga terdaftar: %d%n", daftarWarga.size());
        }

        System.out.print("\nTekan [Enter] untuk melanjutkan..."); input.nextLine();
    }

    //MENU 3 - UPDATW WARGA

    static void tampilDaftarWarga() {
        if (daftarWarga.isEmpty()) { System.out.println("  (Belum ada warga terdaftar)"); return; }
        for (int i = 0; i < daftarWarga.size(); i++)
            System.out.printf("  %d. %s%n", i + 1, daftarWarga.get(i));
    }

    static Warga pilihWarga(String aksi) {
        if (daftarWarga.isEmpty()) { System.out.println("(Belum ada warga terdaftar)"); return null; }
        System.out.println("\nDaftar warga:");
        tampilDaftarWarga();
        int idx = bacaInt("Pilih nomor warga untuk " + aksi + ": ") - 1;
        if (idx < 0 || idx >= daftarWarga.size()) {
            System.out.println("[ERROR] Nomor tidak valid."); return null;
        }
        return daftarWarga.get(idx);
    }

    static void updateWarga() {
        System.out.println("========================================");
        System.out.println("          UPDATE DATA WARGA");
        System.out.println("========================================");
 
        Warga w = pilihWarga("diupdate");
        if (w == null) { System.out.print("Tekan [Enter] untuk melanjutkan..."); input.nextLine(); return; }
 
        System.out.println("\nData saat ini:");
        System.out.println("Nama          : " + w.getNama());
        System.out.println("Tanggal Lahir : " + w.getTanggalLahir());
        System.out.println("Alamat        : " + w.getAlamat());
        System.out.println("(Tekan [Enter] saja = tidak diubah)");
 
        System.out.print("\nNama baru           : ");
        String nb = input.nextLine().trim();
        if (!nb.isEmpty()) {
            if (nb.matches("[a-zA-Z ]+")) w.setNama(nb);
            else System.out.println("[SKIP] Nama tidak valid, tidak diubah.");
        }
 
        System.out.print("Tanggal lahir baru  : ");
        String tb = input.nextLine().trim();
        if (!tb.isEmpty()) {
            if (isValidTanggal(tb)) w.setTanggalLahir(tb);
            else System.out.println("[SKIP] Tanggal tidak valid, tidak diubah.");
        }
 
        System.out.print("Alamat baru         : ");
        String ab = input.nextLine().trim();
        if (!ab.isEmpty()) w.setAlamat(ab);

        System.out.println("\n[SUKSES] Data diperbarui!");
        System.out.printf("  %s | Lahir: %s | %s%n",
            w.getNama(), w.getTanggalLahir(), w.getAlamat());
        System.out.print("Tekan [Enter] untuk melanjutkan..."); input.nextLine();
    }


    //MENU 4 - HITUNG SAMPAH

    static void hitungSampah() {
        System.out.println("========================================");
        System.out.println("         CATAT SETORAN SAMPAH");
        System.out.println("========================================");

        Warga w = pilihWarga("setor sampah");
        if (w == null) { System.out.print("Tekan [Enter] untuk melanjutkan..."); input.nextLine(); return; }

        System.out.println("\nJenis sampah:");
        System.out.println("  1. Organik      (sisa makanan, daun, dll)");
        System.out.println("  2. Anorganik    (plastik / kaca / logam)");
        System.out.println("  3. B3           (baterai, cat, oli, dll)");
        System.out.println("  4. Daur Ulang   (elektronik / kardus / botol / kertas)");
        int jenis = bacaInt("Pilih jenis [1-4]: ");

        double berat = bacaDouble("Berat (kg): ");

        String tanggal;
        while (true) {
            System.out.print("Tanggal setoran (DD-MM-YYYY): ");
            tanggal = input.nextLine().trim();
            if (isValidTanggal(tanggal)) break;
            System.out.println("[ERROR] Format tidak valid. Contoh: 17-08-2025");
        }

        int sid = idSampah++;
        Sampah sampah;

        switch (jenis) {
            case 1: {
                System.out.print("Bisa dijadikan kompos? (y/n): ");
                boolean k = input.nextLine().trim().equalsIgnoreCase("y");
                sampah = new SampahOrganik(sid, k ? "Organik (kompos)" : "Organik", berat, k);
                break;
            }
            case 2: {
                String mat;
                while (true) {
                    System.out.print("Material (plastik/kaca/logam): ");
                    mat = input.nextLine().trim().toLowerCase();
                    if (List.of("plastik","kaca","logam").contains(mat)) break;
                    System.out.println("[ERROR] Pilih: plastik, kaca, atau logam.");
                }
                sampah = new SampahAnorganik(sid, "Anorganik (" + mat + ")", berat, mat);
                break;
            }
            case 3: {
                String t;
                while (true) {
                    System.out.print("Tingkat bahaya (rendah/sedang/tinggi): ");
                    t = input.nextLine().trim().toLowerCase();
                    if (List.of("rendah","sedang","tinggi").contains(t)) break;
                    System.out.println("[ERROR] Pilih: rendah, sedang, atau tinggi.");
                }
                sampah = new SampahB3(sid, "B3 [" + t + "]", berat, t);
                break;
            }
            case 4: {
                System.out.print("Kategori (elektronik/kardus/botol/kertas): ");
                String kat = input.nextLine().trim().toLowerCase();
                sampah = new SampahDaurUlang(sid, "Daur Ulang (" + kat + ")", berat, kat);
                break;
            }
            default:
                System.out.println("[ERROR] Jenis tidak valid.");
                System.out.print("Tekan [Enter] untuk melanjutkan..."); input.nextLine(); return;
        }

        Setoran setoran = new Setoran(idSetoran++, w, sampah, tanggal);
        w.tambahSetoran(setoran);

        System.out.println("\n[SUKSES] Setoran berhasil dicatat!");
        System.out.printf("  Sampah : %s%n", sampah.toText());
        System.out.printf("  Poin   : +%d (total: %d)%n",
            setoran.getPoinDiperoleh(), w.getPoin());

        if (w.adaPesanBelumDibaca()) {
            System.out.println("----------------------------------------");
            System.out.println("  ** NOTIFIKASI BARU **");
            List<String> kotak = w.getKotakMasuk();
            System.out.println("  " + kotak.get(kotak.size() - 1));
            w.tandaiSudahDibaca();
        }

        System.out.print("Tekan [Enter] untuk melanjutkan..."); input.nextLine();
    }

    //MENU 5-LIHAT LAPORAN
    static void lihatLaporan() {
        while (true) {
            clearScreen();
            System.out.println("========================================");
            System.out.println("      LIHAT DAN CETAK LAPORAN");
            System.out.println("========================================");

            if (daftarWarga.isEmpty()) {
                System.out.println("(belum ada data warga)");
                System.out.print("Tekan [Enter] untuk melanjutkan...");
                input.nextLine();
                return; 
            }

            System.out.println("  1. Laporan semua warga (teks)");
            System.out.println("  2. Ekspor CSV");
            System.out.println("  3. Riwayat setoran per warga");
            System.out.println("  4. Kembali");

            int p = bacaInt("Pilih [1-4]: ");

            switch (p) {
                case 1:
                    laporanTeks();
                    break;
                case 2:
                    laporanCSV();
                    break;
                case 3:
                    laporanPerWarga();
                    break;
                case 4:
                    return; // keluar dari loop & method
                default:
                    System.out.println("[ERROR] Tidak valid.");
            }

            System.out.print("Tekan [Enter] untuk melanjutkan...");
            input.nextLine(); // pause sebelum loop ulang
        }
    }
    
        static void laporanTeks() {
            clearScreen();
            System.out.println("========================================");
            System.out.println("   LAPORAN WARGA - SISTEM SAMPAH");
            System.out.println("========================================");
    
            List<Warga> sorted = new ArrayList<>(daftarWarga);
            sorted.sort((a, b) -> b.getPoin() - a.getPoin());
    
            int rank = 1;
            for (Warga w : sorted) {
                String medal = rank == 1 ? "[1]" : rank == 2 ? "[2]" : rank == 3 ? "[3]" : "   ";
                System.out.printf(" %s %s%n", medal, w.toText());
                rank++;
            }
    
            int    totalPoin  = daftarWarga.stream().mapToInt(Warga::getPoin).sum();
            int    totalSetor = daftarWarga.stream().mapToInt(w -> w.getRiwayatSetoran().size()).sum();
            double totalBerat = daftarWarga.stream()
                .flatMap(w -> w.getRiwayatSetoran().stream())
                .mapToDouble(s -> s.getSampah().getBerat()).sum();
    
            System.out.println("----------------------------------------");
            System.out.printf(" Total: %.2f kg | %d setoran | %d poin%n",
                totalBerat, totalSetor, totalPoin);
    
            if (!sorted.isEmpty())
                System.out.printf(" Warga teraktif: %s (%d poin)%n",
                    sorted.get(0).getNama(), sorted.get(0).getPoin());
        }
    
        static void laporanCSV() {
            try (
                PrintWriter wargaWriter = new PrintWriter(new FileWriter(FILE_WARGA));
                PrintWriter setoranWriter = new PrintWriter(new FileWriter(FILE_SETORAN))
            ) {
                // HEADER WARGA
                wargaWriter.println("id,nama,tanggalLahir,alamat,jumlahSetoran,poin");
                for (Warga w : daftarWarga) {
                    wargaWriter.println(w.toCSV());
                }

                // HEADER SETORAN
                setoranWriter.println("id,idWarga,namaWarga,tipe,berat,tanggal,poin");
                for (Warga w : daftarWarga) {
                    for (Setoran s : w.getRiwayatSetoran()) {
                        setoranWriter.printf("%d,%d,%s,%s,%.2f,%s,%d%n",
                            s.getId(),
                            s.getWarga().getId(),
                            s.getWarga().getNama(),
                            s.getSampah().getTipe(),
                            s.getSampah().getBerat(),
                            s.getTanggal(),
                            s.getPoinDiperoleh()
                        );
                    }
                }

                System.out.println("[INFO] File berhasil dibuat:");
                System.out.println("- " + FILE_WARGA);
                System.out.println("- " + FILE_SETORAN);

            } catch (IOException e) {
                System.out.println("[ERROR] Gagal menulis file: " + e.getMessage());
            }
        }
    
        static void laporanPerWarga() {
            Warga w = pilihWarga("dilihat riwayatnya");
            if (w == null) return;
    
            System.out.println("========================================");
            System.out.printf("  RIWAYAT - %s (ID: %d)%n", w.getNama(), w.getId());
            System.out.println("----------------------------------------");
    
            List<Setoran> riwayat = w.getRiwayatSetoran();
            if (riwayat.isEmpty()) {
                System.out.println("  (belum ada setoran)");
            } else {
                riwayat.forEach(System.out::println);
                System.out.println("----------------------------------------");
                System.out.printf("  Total: %d setoran | %d poin%n",
                    riwayat.size(), w.getPoin());
            }
        }

    // MENU 6-HAPUS WARGA
    static void hapusWarga() {
        System.out.println("========================================");
        System.out.println("           HAPUS DATA WARGA");
        System.out.println("========================================");

        Warga w = pilihWarga("dihapus");
        if (w == null) { System.out.print("Tekan [Enter]..."); input.nextLine(); return; }

        System.out.printf("\nYakin hapus warga '%s' (ID: %d)? (y/n): ", w.getNama(), w.getId());
        String konfirmasi = input.nextLine().trim();
        if (konfirmasi.equalsIgnoreCase("y")) {
            daftarWarga.remove(w);
            System.out.println("[SUKSES] Warga berhasil dihapus.");
        } else {
            System.out.println("[BATAL] Penghapusan dibatalkan.");
        }

        System.out.print("Tekan [Enter] untuk melanjutkan..."); input.nextLine();
    }



static void menu() {
    System.out.println("========================================");
    System.out.println("   SISTEM MANAJEMEN SAMPAH WARGA");
    System.out.println("========================================");
    System.out.println(" 1. Tambahkan Warga");
    System.out.println(" 2. Lihat Warga");
    System.out.println(" 3. Update Data Warga");
    System.out.println(" 4. Hitung Sampah");
    System.out.println(" 5. Lihat dan Cetak Laporan Sampah");
    System.out.println(" 6. Hapus Warga");
    System.out.println(" 7. Keluar");
    System.out.println("========================================");
    System.out.print  ("Pilih opsi [1-7]: ");
}
    public static void main(String[] args) throws Exception {
       while (true) {
            clearScreen();
            menu();
 
            int opsi = bacaInt("");
 
            if (opsi < 1 || opsi > 7) {
                System.out.println("[ERROR] Opsi tidak valid! Masukkan angka [1-6]");
                System.out.print("Tekan [Enter] untuk melanjutkan..."); input.nextLine();
                continue;
            }
 
            clearScreen();
 
            switch (opsi) {
                case 1: tambahWarga();  break;
                case 2: lihatWarga();   break;
                case 3: updateWarga();  break;
                case 4: hitungSampah(); break;
                case 5: lihatLaporan(); break;
                case 6: hapusWarga();   break;
                case 7:
                    System.out.println("Terima kasih. Program selesai.");
                    input.close();
                    System.exit(0);
                    break;
            }
        }
    }
}


