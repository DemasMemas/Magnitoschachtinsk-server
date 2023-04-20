package GameObjects;

public class Building {
    int id;
    int defenceBonus;
    public Building(int id){
        this.id = id;
        defenceBonus = 0;
        switch (id) {
            case 47 -> defenceBonus = 1;
            case 48 -> defenceBonus = 2;
        }
    }
}
