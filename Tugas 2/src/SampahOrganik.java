class SampahOrganik extends Sampah {

    private boolean isCompos;

    public SampahOrganik(int id, String tipe, double berat, boolean isCompos){
        super(id, tipe, berat);
        this.isCompos = isCompos;
    }

    boolean getIsCompos (){
        return isCompos;
    }

    void setIsCompos(boolean isCompos){
        this.isCompos = isCompos;
    }


    @Override
    public int hitungPoin(){
        int base = (int)(getBerat()*5);
        if (isCompos) {
            return base + 20;
        } 

        return base;
    }

    @Override
    public String toCSV() {
        return super.toCSV() + "," + (isCompos ? "bisa_kompos" : "tidak");
    }
    
}
