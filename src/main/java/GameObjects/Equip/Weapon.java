package GameObjects.Equip;

public class Weapon {
    private final int id;
    private int attack;;
    private String name;
    private int[] effectList;

    public Weapon(int id, int attack, String name, int[] effectList) {
        this.id = id;
        this.attack = attack;
        this.name = name;
        this.effectList = effectList;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getEffectList() {
        return effectList;
    }

    public void setEffectList(int[] effectList) {
        this.effectList = effectList;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString(){
        if (getEffectList() != null){
            StringBuilder effects = new StringBuilder();
            for (int i:getEffectList())
                effects.append(i).append(":");
            effects.deleteCharAt(effects.length() - 1);
            return getId() + ";" + getAttack() + ";" + getName() + ";" + effects;
        }
        else return getId() + ";" + getAttack() + ";" + getName();
    }
}
