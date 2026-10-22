package Game;

import static Game.GameDriver.rand;
public class Technique implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    enum techniqueType {
        Movement,
        Martial
    }
    String name = "";
    String maker = "";
    techniqueType type;
    int lvl;
    int potential;
    byte elemPlus1;  // the element + 1 (Elements): 0 = none given (the Default Technique follows its owner's path; an old save's technique gets one from its name, see element())


    // Constructor for Innovator to make Technique
    public Technique(techniqueType type, Character maker, int potential) {
        this.type = type;
        int el = Elements.forNewTechnique(maker);
        this.elemPlus1 = (byte) (el + 1);
        this.name = Naming.technique(type, maker, el);
        this.maker = (maker.name + " " + maker.lastName).intern();
        this.lvl = potential;
        this.potential = potential;



    }
    // Constructor to copy technique
    public Technique (Technique technique) {
        this.name = technique.name;
        this.maker = technique.maker;
        this.type = technique.type;
        this.elemPlus1 = technique.elemPlus1;
        this.lvl = 1;
        this.potential = technique.potential;
    }

    // Constructor for default Technique
    public Technique(techniqueType type) {
        this.name = "Default Technique";
        this.maker = "Default Technique";
        this.type = type;
        this.lvl = 1;
        this.potential = 3;
    }

    /** The technique's element 0-4, or -1 for the Default Technique (it follows the path of whoever uses it). */
    int element() {
        if (elemPlus1 > 0) return elemPlus1 - 1;
        if ("Default Technique".equals(name)) return -1;
        elemPlus1 = (byte) (1 + Math.floorMod(name.hashCode(), 5)); // an old save: a stable element from the name
        return elemPlus1 - 1;
    }

    public String getType() {
        switch (type) {
            case Movement:
                return "Movement";
            case Martial:
                return "Martial";
            default:
                return "Error in getType of Technique";
        }
    }
    //This will be used with events to upgrade the technique
    public void upgrade(Character owner) {
        if (this.potential > this.lvl) {
            owner.tempLvlCheck();
            if (rand.nextInt(100) + 1 <= (owner.lvl + owner.potential) / 2) {
                lvl++;
                if ((owner.player > 0 && owner.skipping <= 0) || Game.Map.trackedCharacters.contains(owner.id))
                    System.out.println(this + " was upgraded!");
            }
        }
    }
    public String toString() {
        int el = element();
        return (name + (el >= 0 ? " [" + Elements.name(el) + "]" : "") + " lvl: " + lvl + " Potential: " + potential);
    }
}
