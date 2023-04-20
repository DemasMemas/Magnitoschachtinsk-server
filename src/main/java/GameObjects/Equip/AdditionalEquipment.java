package GameObjects.Equip;

public class AdditionalEquipment {
    private final int id;
    private final String name;
    public AdditionalEquipment(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public int getId() { return id; }
    @Override
    public String toString(){
        return getId() + ";" + getName();
    }
}

