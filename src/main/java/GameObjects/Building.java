package GameObjects;

import GameObjects.Equip.Weapon;

public class Building {
    public int getId() {
        return id;
    }
    int id;
    int attackBonus;
    int defenceBonus;
    Weapon builtInWeapon;
    boolean health = true;
    public Building(int id){
        this.id = id;
        attackBonus = 0;
        defenceBonus = 0;
        builtInWeapon = null;
        switch (id) {
            case 47 -> defenceBonus = 1;
            case 48 -> defenceBonus = 2;
        }
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public void setAttackBonus(int attackBonus) {
        this.attackBonus = attackBonus;
    }

    public int getDefenceBonus() {
        return defenceBonus;
    }

    public void setDefenceBonus(int defenceBonus) {
        this.defenceBonus = defenceBonus;
    }

    public Weapon getBuiltInWeapon() {
        return builtInWeapon;
    }

    public void setBuiltInWeapon(Weapon builtInWeapon) {
        this.builtInWeapon = builtInWeapon;
    }

    public boolean isHealth() {
        return health;
    }
    public void setHealth(boolean health) {
        this.health = health;
    }
}
