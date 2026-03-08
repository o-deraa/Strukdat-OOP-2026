public class Main {
    public static void main(String[] args) throws Exception{

        Character pyro_1 = new Pyro("Hu Tao", "Polearmn", 100, 3400, 200, 'S');
        Character geo_1 = new Geo("Noelle", "Claymore", 100, 3000, 150, 'A');

        pyro_1.infoDisplay();
        geo_1.infoDisplay();

        System.out.println();

        pyro_1.useSkill();
        pyro_1.useUltimate();

        System.out.println();

        geo_1.useSkill();
        geo_1.useUltimate();

        //Polymorphism tambahan*
        //Implementasi di atas semakin memperjelas konsep polymorphism
        //Dua class berbeda dapat menggunakan methode dengan nama yang sama
        //-karena berasal dari superclass yang sama
        //Namun isi dari method tersebut disesuaikan dengan kebutuhan tiap class
    }
}
