class SampahAnorganik extends Sampah {
    private String material;

   public SampahAnorganik(int id, String tipe, double berat, String material){
        super(id, tipe, berat);
        this.material = material;
    }

    String getMaterial(){
        return material;
    }

    void setMaterial(String material){
        this.material = material;
    }

    @Override
    public int hitungPoin(){
        int base = (int) (getBerat() * 8);
        switch (getMaterial().toLowerCase()) {
            case "logam": return base + 20;
            case "kaca":  return base + 10;
            default:      return base;
        }
    }
}