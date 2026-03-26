abstract class Sampah implements Exportable {
    private int id;
    private String tipe;
    private double berat;

    public Sampah(int id, String tipe, double berat){
        this.id = id;
        this.tipe = tipe;
        this.berat = berat;
    }

    //getter 
    int getId(){
        return id;
    }

    String getTipe(){
        return tipe;
    }

    double getBerat(){
        return berat;
    }

    //setter
    void setId(int id){
        this.id  = id;
    }

    void setTipe(String tipe){
        this.tipe = tipe;
    }
    
    void setBerat(double berat){
        this.berat = berat;
    }

    public abstract int hitungPoin();

    @Override
    public String toCSV() {
        return String.format("%d,%s,%.2f,%d", id, tipe, berat, hitungPoin());
    }
 
    @Override
    public String toText() {
        return String.format("%-26s | %.2f kg | +%d poin", tipe, berat, hitungPoin());
    }
 
    @Override
    public String toString() { return toText(); }
    

}