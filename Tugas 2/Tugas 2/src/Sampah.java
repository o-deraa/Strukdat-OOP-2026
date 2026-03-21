abstract public class Sampah {
    private int id;
    private String tipe;
    private int berat;

    Sampah(int id, String tipe, int berat){
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

    int getBerat(){
        return berat;
    }

    //setter
    void setId(int id){
        this.id  = id;
    }

    void setTipe(String tipe){
        this.tipe = tipe;
    }
    
    void setBerat(int berat){
        this.berat = berat;
    }

    public abstract int hitungPoin();

}