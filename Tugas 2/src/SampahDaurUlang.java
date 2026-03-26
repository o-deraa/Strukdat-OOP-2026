class SampahDaurUlang extends Sampah {
    private String kategori; //elektronik, kardus, botol, kertas, dan lainnya

    public SampahDaurUlang(int id, String tipe, double berat, String kategori){
        super(id, tipe, berat);
        this.kategori = kategori;
    }

    String getKategori(){
        return kategori;
    }

    void setKategori(String kategori){
        this.kategori = kategori;
    }

    @Override
    public int hitungPoin(){
        int base = (int)(getBerat() * 10);
        switch (getKategori().toLowerCase()) {
            case "elektronik":
                return base + 30;
            case "kardus":
                return base + 20;
            case "botol":
                return base + 10;
            case "kertas":
                return base + 5;
            default:
                return base;
        }
    }
    
}
