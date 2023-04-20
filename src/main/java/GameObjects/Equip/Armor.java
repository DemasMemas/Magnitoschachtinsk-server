package GameObjects.Equip;

public class Armor {
    private int id;
    private int defence;
    private int maxDefence;
    private final String name;
    private int[] effectList;

    public Armor(int id, int defence, String name, int[] effect) {
        this.id = id;
        this.maxDefence = defence;
        this.defence = maxDefence;
        this.name = name;
        this.effectList = effect;
    }

    public int getDefence() {
        return defence;
    }

    public void setDefence(int defence) {
        this.defence = defence;
    }

    public String getName() {
        return name;
    }

    public int[] getEffect() {
        return effectList;
    }

    public void setEffect(int[] effect) {
        this.effectList = effect;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMaxDefence() {
        return maxDefence;
    }

    public void setMaxDefence(int maxDefence) {
        this.maxDefence = maxDefence;
    }

    @Override
    public String toString() {
        StringBuilder effects = new StringBuilder();
        for (int i : getEffect())
            effects.append(i).append(":");
        if (effects.length() > 0) effects.deleteCharAt(effects.length() - 1);
        return getId() + ";" + getDefence() + ";" + getName() + ";" + effects;
    }
}
