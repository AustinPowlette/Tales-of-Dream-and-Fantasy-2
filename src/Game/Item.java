package Game;
import static Game.GameDriver.*;
import static Game.Dictionary.*;
public class Item implements java.io.Serializable {
    static final long serialVersionUID = 1L;

    enum ItemType{
        Pill,
        Array,
        Weapon,
        Clothing,
        Herb,
        Metal,
        Blood,
        Food,
        Wine,
        Poison,
        Instrument

    }
    enum WeaponType{
        Sword,
        Saber,
        Spear,
        Staff,
        Fan
    }
    enum PillType{
        Potential,
        Cultivation
    }
    String name = "";
    //int id = iUID++;
    String maker;
    int makerID;
    String brand;   // the house a pill came from ("Hopkins Pill House"); shown next to the name, not part of it (stacks merge by kind and level)
    int vendorID;
    ItemType itemType;
    WeaponType weaponType;
    PillType pillType;
    int quantity = 1;
    int lvl = -1;
    boolean stackable = false;
    String enchantment = null;
    int enchantBonus = 0;
    boolean heavenSent = false;   // a Heaven-Sent treasure (see Treasure): it is auctioned, never sold to a shop
    int consignorId = -1;         // who put this lot on a city's auction block (paid 90% when it sells); -1 = nobody

    // Constructor for food, wine and poison (stackable goods; see Life)
    public Item(ItemType itemType, int lvl, int quantity, String name) {
        this.itemType = itemType;
        this.lvl = lvl;
        this.quantity = quantity;
        this.name = name;
        stackable = true;
    }
    // Constructor for Herbs and Metals
    public Item(ItemType itemType, int lvl, int quantity) {
        switch (itemType) {
            case Herb:
                name = Naming.herb(lvl);
                break;
            case Metal:
                name = Naming.metal(lvl);
                break;
        }
        this.itemType = itemType;
        this.quantity = quantity;
        this.lvl = lvl;
        stackable = true;
    }
    // Constructor for creating weapons
    public Item(Character maker, WeaponType weaponType, int lvl){
        this.itemType = ItemType.Weapon;
        this.weaponType = weaponType;
        this.lvl = lvl;
        this.maker = maker.name + " " + maker.lastName;
        this.makerID = maker.id;
        stackable = false;
        name = Naming.weapon(lvl, getWeaponType());

    }

    //Constructor for creating Arrays and Clothing
    public Item(Character maker, int lvl, ItemType itemType) {
        this.lvl = lvl;
        this.maker = maker.name + " " + maker.lastName;
        this.makerID = maker.id;
        stackable = false;
        if (itemType == ItemType.Array) {
            this.itemType = ItemType.Array;
            name = Naming.array(lvl, arrayTypes[rand.nextInt(arrayTypes.length)]);
        }
        else if (itemType == ItemType.Instrument) {
            this.itemType = ItemType.Instrument;
            name = Naming.instrument(lvl);
        }
        else if (itemType == ItemType.Clothing) {
            this.itemType = ItemType.Clothing;
            name = Naming.clothing(lvl, clothingNames[rand.nextInt(clothingNames.length)]);
        }

    }

    // Constructor for Blood so far
    public Item(ItemType itemType, int quantity) {
        this.itemType = itemType;
        this.quantity = quantity;
        stackable = true;
        if (itemType == ItemType.Blood) {
            name = "Blood";
        }
    }

    // Constructor for Pills
    public Item(ItemType itemType, PillType pillType, int lvl, int quantity) {
        this.itemType = itemType;
        this.pillType = pillType;
        this.lvl = lvl;
        this.quantity = quantity;
        stackable = true;
        if (pillType != PillType.Potential) {
            name = "" + getPillName() + " Pill";
        }
        else {
            name = "Potential Pill";
        }
    }

    public String getPillType() {
        switch (pillType) {
            case Potential:
                return "Potential";
            case Cultivation:
                return "Cultivation";
            default:
                return "Error in getPillType of Item";
        }
    }
    public String getPillName() {
        switch (lvl) {
            case 1:
                return "Qi Cultivation";
            case 2:
                return "Foundation Stage";
            case 3:
                return "Core Formation";
            case 4:
                return "Soul Wandering";
            case 5:
                return "Nascent Soul";
            case 6:
                return "Ensoulment";
            case 7:
                return "Nihility";
            case 8:
                return "Ascension";
            case 9:
                return "Half-Immortal";
            case 10:
                return "Pseudo Immortal";
            default:
                return "Error in the getPillName method";
        }
    }
    public String getWeaponType() {
        switch (weaponType) {
            case Sword:
                return "Sword";
            case Saber:
                return "Saber";
            case Staff:
                return "Staff";
            case Fan:
                return "Fan";
            case Spear:
                return "Spear";
            default:
                return "Error in getWeaponType of Item";
        }
    }

    public void deconstructor() {
        name = "Shattered relic"; // never null: merge/stack code calls name.equals() on anything found in a bag or pool
        //id = iUID++;
        maker = null;
        makerID = 0;
        vendorID = 0;
        itemType = null;
        weaponType = null;
        pillType = null;
        lvl = 0;
    }
    public String toString() {
        if (itemType == ItemType.Food || itemType == ItemType.Wine || itemType == ItemType.Poison) {
            return name + " x" + quantity;
        }
        if (stackable) {
            return (name + (brand != null && itemType == ItemType.Pill ? " (" + brand + ")" : "") + " Quantity: " + quantity);
        }
        else {
            String enchStr = enchantment != null ? " [" + enchantment + " +" + enchantBonus + "]" : "";
            return (name + " lvl: " + lvl + enchStr);
        }
    }
}
