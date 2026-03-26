import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

class Warga implements Exportable, Notifiable{
    private int id; 
    private String nama;
    private String tanggalLahir;
    private String alamat;
    private int poin;
    private List<Setoran> riwayatSetoran;

    // Notifiable
    private List<String> kotakMasuk;
    private boolean      adaBelumDibaca;
    private static final int[] MILESTONES = {50, 100, 200, 500};

    
    public Warga(int id, String nama, String tanggalLahir, String alamat) {
        this.id = id;
        this.nama = nama;
        this.tanggalLahir = tanggalLahir;
        this.alamat = alamat;
        this.poin = 0;
        this.riwayatSetoran = new ArrayList<>();
        this.kotakMasuk     = new ArrayList<>();
        this.adaBelumDibaca = false;

        
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }


    public String getTanggalLahir() {
        return tanggalLahir;
    }

    public void setTanggalLahir(String tanggalLahir) {
        this.tanggalLahir = tanggalLahir;
    }


    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }


    public int getPoin() {
        return poin;
    }

    public void setPoin(int poin) {
        this.poin = poin;
    }


    public List<Setoran> getRiwayatSetoran() {
        return riwayatSetoran;
    }

    public void setRiwayatSetoran(List<Setoran> riwayatSetoran) {
        this.riwayatSetoran = riwayatSetoran;
    }

    public void tambahSetoran(Setoran s) {
        int poinSebelum = poin;
        riwayatSetoran.add(s);
        poin += s.getPoinDiperoleh();
 
        for (int milestone : MILESTONES) {
            if (poinSebelum < milestone && poin >= milestone) {
                terimaNotifikasi(String.format(
                    "Selamat! Kamu meraih %d poin. " +
                    "Tukarkan poin di Banjar setiap awal Bulan.", milestone));
            }
        }
    }

    //nptifable
    @Override
    public void terimaNotifikasi(String pesan) {
        kotakMasuk.add("[" + LocalDate.now() + "] " + pesan);
        adaBelumDibaca = true;
    }
 
    @Override public List<String> getKotakMasuk()     { return kotakMasuk; }
    @Override public boolean adaPesanBelumDibaca()     { return adaBelumDibaca; }
    @Override public void tandaiSudahDibaca()          { adaBelumDibaca = false; }


    //exportable
    @Override
    public String toCSV() {
        return String.format("%d,%s,%s,%s,%d,%d",
            id, nama, tanggalLahir, alamat, riwayatSetoran.size(), poin);
    }
 
    @Override
    public String toText() {
        String notif = adaBelumDibaca ? "[!]" : "   ";
        return String.format("[%d] %s %-20s | %-12s | %2d setoran | %4d poin",
            id, notif, nama, alamat, riwayatSetoran.size(), poin);
    }
 
    @Override public String toString() { return toText(); }
    
    
}

