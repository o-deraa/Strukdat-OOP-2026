public abstract class Character {

    //Encapsulation
    // Encapsulation adalah proses menggabungkan atribut dan method dalam satu class
    // serta membatasi akses langsung menggunakan access modifier

    private String name;
    private String weapon;
    private int stamina;
    private int hp;
    private int attack;
    private char rarity;

    public Character(String name, String weapon, int stamina, int hp, int attack, char rarity) {
    this.name = name;
    this.weapon = weapon;
    this.stamina = stamina;
    this.hp = hp;
    this.attack = attack;
    this.rarity = rarity;
}

    // Getter

    public String getName() {
        return name;
    }

    public String getWeapon() {
        return weapon;
    }

    public int getStamina(){
        return stamina;
    }

    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public char getRarity() {
        return rarity;
    }

    // Setter

    public void setName(String name) {
        this.name = name;
    }

    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }

    public void setStamina(int stamina){
        this.stamina = stamina;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public void setRarity(char rarity) {
        this.rarity = rarity;
    }

    //methode untuk menampilkan info character

    
    public void infoDisplay() {
    System.out.println("===== Character Info =====");
    System.out.println("Name   : " + getName());
    System.out.println("Element: " + getElement());
    System.out.println("Weapon : " + getWeapon());
    System.out.println("Rarity : " + getRarity());
}

    // Abstraction
    // Abstraksi menyederhanakan dan menyembunyikan suatu proses yang detail dan kompleks
    // Abstraksi membantu user untuk fokus pada apa yang suatu objek lakukan 
    // -tanpa perlu mempedulikan cara kerjanya

    public abstract void useSkill();
    public abstract void useUltimate();
    public abstract String getElement();
    

    //Pdda class Character, terdapat method abstrak tanpa implementasi
    //Method ini akan diimplementasikan oleh subclass dari Character
}