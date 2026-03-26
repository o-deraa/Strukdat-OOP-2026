class SampahB3 extends Sampah{
    private String tingkatBahaya;

    public SampahB3(int id, String tipe, double berat, String tingkatBahaya){
        super(id, tipe, berat);
        this.tingkatBahaya = tingkatBahaya;
    }

    String getTingkatBahaya(){
        return tingkatBahaya;
    }

    void setTingkatBahaya(String tingkatBahaya){
        this.tingkatBahaya = tingkatBahaya;
    }

    public int hitungPoin(){
        int base = (int)(getBerat() * 10);
        switch (getTingkatBahaya().toLowerCase()) {
            case "tinggi":
                return base * 3;
            case "sedang":
                return base * 2 ;
            default:
                return base;
        }
    }
}
