class Setoran{
    private int id;
    private Warga warga;
    private Sampah sampah;
    private String tanggal;
    private int poinDiperoleh;
    public Setoran(int id, Warga warga, Sampah sampah, String tanggal) {
        this.id = id;
        this.warga = warga;
        this.sampah = sampah;
        this.tanggal = tanggal;
        this.poinDiperoleh = sampah.hitungPoin();
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Warga getWarga() {
        return warga;
    }
    public void setWarga(Warga warga) {
        this.warga = warga;
    }

    public Sampah getSampah() {
        return sampah;
    }
    public void setSampah(Sampah sampah) {
        this.sampah = sampah;
    }

    public String getTanggal() {
        return tanggal;
    }
    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public int getPoinDiperoleh() {
        return poinDiperoleh;
    }
    public void setPoinDiperoleh(int poinDiperoleh) {
        this.poinDiperoleh = poinDiperoleh;
    }
    

    
}
