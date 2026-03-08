
//Di sini saya membuat subclass baru untuk karakter dengan tipe elemen Geo

public class Geo extends Character {

    public Geo(String name, String weapon, int stamina, int hp, int attack, char rarity) {
        super(name, weapon, stamina, hp, attack, rarity);
        System.out.println("[GEO Character sucesfully created]");
    }

    @Override
    public void useSkill() {
        int newStamina = getStamina() + 30;
        setStamina(newStamina);
        System.out.println(getName() + " menggunakan elemental skil! Stamina bertambah");

    }

    @Override
    public void useUltimate() {
        int newStamina = getStamina() * 5;
        setStamina(newStamina);
        int newHp = getHp() + 2000;
        setHp(newHp);
        System.out.println(getName() + " menggunakan elemental burst dignunakan! Stamina dan HP bertambah secara drastis");
    }

    @Override
    public String getElement() {
        return "Geo";
    }
    
}
