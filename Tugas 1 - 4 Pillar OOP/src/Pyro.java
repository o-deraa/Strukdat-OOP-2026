//Inheritance
//Inheritance adalah konsep untuk membuat class baru (subclass) dari class yang sudah ada (superlass)
//Class utama disebut superclass dan class turunannya disebut subclass
//Subclass mewarisi atribut dan methode dari superclass sehingga mengurangi duplikasi

//Di sini saya membuat subclass dari class Character
//yaitu class untuk tipe karakter berelemen Pyro

public class Pyro extends Character {

    public Pyro(String name, String weapon, int stamina, int hp, int attack, char rarity) {
        super(name, weapon, stamina, hp, attack, rarity);
        System.out.println("[PYRO Character sucesfully created]");
    }

    //POLYMORPHISM
    //Polymorphism memungkinkan objek memiliki banyak bentuk
    //Subclass dengan superclass yang sama dapat mewarisi method dengan nama yang sama
    //tapi, method bisa berperilaku berbeda tergantung kebutuhan dan keinginan pengguna

    //Di sini, saya mengimplementasikan methode useSkill dan use Ultimate
    //yang sebelumnya ada di superclass Character

    @Override
    public void useSkill() {
        int newAttack = (int) (getAttack() * 2);
        setAttack(newAttack);
        System.out.println(getName() + " menggunakan elemental skill ! Attack berhasil ditingkatkan");
        System.out.println("Seranganmu akan menghasilkan damage pyro!");
    }

    @Override
    public void useUltimate() {
        int newAttack = getAttack() * 4;
        setAttack(newAttack);
        int newHp = getHp() * 2;
        setHp(newHp);
        System.out.println(getName() + " menggunakan elemental burst! Attack dan HP meningkat secara drastis");
        System.out.println("Seranganmu akan menghasilkan pyro damage area yang besar!");
    }

    @Override
    public String getElement() {
        return "Pyro";
    }

    //Perlu diingat bahwa modifikasi method ini saya sesuaikan khusus untuk subclass Pyro
    //Pada subclass elemen lain, pengimpentasiannya akan berbeda
    //Inilah inti dari polymorphism, di mana kita bisa menggunakan objek dari class yang berbeda
    //-dengan cara yang sama
}