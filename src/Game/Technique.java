package Game;

import static Game.GameDriver.rand;
public class Technique {
    enum techniqueType {
        Movement,
        Martial
    }
    String name = "";
    Character maker;
    techniqueType type;
    int lvl;
    int potential;


    // Constructor for Innovator to make Technique
    public Technique(techniqueType type, Character maker, int potential) {
        this.maker = maker;
        this.type = type;
        this.name = ("" + maker + "'s " + getType() + " Technique");
        this.lvl = potential;
        this.potential = potential;


    }
    // Constructor to copy technique
    public Technique (Technique technique) {
        this.name = technique.name;
        this.maker = technique.maker;
        this.type = technique.type;
        this.lvl = 1;
        this.potential = technique.potential;
    }

    // Constructor for default Technique
    public Technique(techniqueType type) {
        this.name = "Default Technique";
        this.type = type;
        this.maker = null;
        this.lvl = 1;
        this.potential = 1;
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
            if (rand.nextInt(100) + 1 <= (owner.tempLvl + owner.potential) / 2) {
                lvl++;
                System.out.println(this + " was upgraded!");
            }
        }
    }
    public String toString() {
        return (name + " lvl: " + lvl + " Potential: " + potential);
    }
}
