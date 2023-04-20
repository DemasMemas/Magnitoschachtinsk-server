package GameObjects;

import GameObjects.Equip.*;

public class Person {
    Armor armor;
    Helmet helmet;
    Weapon weapon;
    AdditionalEquipment firstAddEquip;
    AdditionalEquipment secondAddEquip;
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
    public boolean isNotWounded() {
        return health;
    }
    public void setHealth(boolean health) {
        this.health = health;
    }
}
