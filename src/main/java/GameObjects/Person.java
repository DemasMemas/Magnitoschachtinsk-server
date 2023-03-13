package GameObjects;

import GameObjects.Equip.*;

public class Person {
    public int getId() {
        return id;
    }
    int id;
    Armor armor;
    Helmet helmet;
    Weapon weapon;
    AdditionalEquipment firstAddEquip;
    AdditionalEquipment secondAddEquip;
    boolean fought = false;
    boolean health = true;
    public String getArmorString(){
        return armor.toString();
    }
    public String getHelmetString(){
        return helmet.toString();
    }
    public String getWeaponString(){
        return weapon.toString();
    }
    public String firstEquipString(){
        return firstAddEquip.toString();
    }
    public String secondEquipString(){
        return secondAddEquip.toString();
    }
    public String getFoughtStatus(){
        return fought ? "1":"0";
    }

    public void setArmor(Armor armor) {
        this.armor = armor;
    }

    public void setHelmet(Helmet helmet) {
        this.helmet = helmet;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public void setFirstAddEquip(AdditionalEquipment firstAddEquip) {
        this.firstAddEquip = firstAddEquip;
    }

    public void setSecondAddEquip(AdditionalEquipment secondAddEquip) {
        this.secondAddEquip = secondAddEquip;
    }

    public void setFought(boolean fought) {
        this.fought = fought;
    }

    public boolean isHealth() {
        return health;
    }

    public void setHealth(boolean health) {
        this.health = health;
    }
}
