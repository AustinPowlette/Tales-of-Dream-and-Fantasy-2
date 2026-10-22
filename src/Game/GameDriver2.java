package Game;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.util.concurrent.locks.Lock;

import static Game.Map.*;
import static Game.Map.characters;

public class GameDriver2 {

    static Scanner scan = new Scanner(System.in);
    static Random rand = new Random();
    static final boolean printBreakthroughs = false;
    static final boolean changeMc = true;
    public static int UID = 0;
    //public static int iUID = 0;


    public static void main(String[] args) throws InterruptedException, IOException {
        //tiles[range / 2][range / 2] = new Area(); // Starting Point of the World
        Map map = new Map(); // Starting Point of the World
        //Character[] characters = new Character[startingPopulation];
        int time = 0; // In Weeks
        System.out.println("Would you like to play? Y/n");
        char input = 'Y';
        //char input = scan.next().charAt(0);

        if (input == 'Y' || input == 'y') {
            Character bete = new Character(tiles[range / 2][range / 2]);
            Character ralph = new Character(tiles[range / 2][range / 2]);
            currentCharacter = bete;
            characters.add(bete);
            characters.add(ralph);
            bete.player = 1;
            bete.potential = 100;
            bete.lvl = 60;
            ralph.potential = 100;
            ralph.lvl = 31;
            ralph.equippedWeapon = new Item(ralph, Item.WeaponType.Fan, 1);
            ralph.equippedClothing = new Item(ralph, 1, Item.ItemType.Clothing);
            bete.equippedWeapon = new Item(bete, Item.WeaponType.Saber, 11);
            bete.equippedClothing = new Item(bete, 11, Item.ItemType.Clothing);
            //bete.trait = Character.Traits.Alchemist;
            System.out.println("" + bete + bete.location);
            System.out.println(bete + "'s Potential: " + bete.potential + " Trait: " + bete.getTrait());
            System.out.println("" + ralph + ralph.location);
            System.out.println(ralph + "'s Potential: " + ralph.potential + " Trait: " + ralph.getTrait());

            //Relationship Adding
            bete.interact(ralph);
            //bete.relationshipsList.add(ralph);
            System.out.println(bete.relationshipList.contains(ralph.id).value);
            bete.relationshipList.contains(ralph.id).changeValue(2);
            //ralph.relationshipsList.add(bete);
            ralph.relationshipList.contains(bete.id).changeValue(2);


            bete.battle(ralph);

            System.out.println(bete);
            System.out.println(bete.equippedWeapon);
            System.out.println(bete.equippedClothing);
            System.out.println(ralph);
            System.out.println(ralph.equippedWeapon);
            System.out.println(ralph.equippedClothing);

            //Quest quest1 = new Quest("Random", false, 50, 5, 50);
            //Quest quest2 = new Quest("Test", false, 45, 5, 50);
            //bete.quests.add(quest1);
            //bete.quests.add(quest2);
            //System.out.println(bete.quests);
            //System.out.println(quest1);
            //System.out.println(quest2);

            /*while (true) {
                currentCharacter = bete;
                bete.quest();
                bete.movement(scan.next().charAt(0));
                bete.cultivate();
                System.out.print(bete);
                System.out.println(bete.location);
                System.out.println(bete.backpack + "\n" + bete.materialBag + "\n" + bete.pillBag);
                if (scan.next().charAt(0) == '0') {
                    break;
                }
            }*/

        }
    }
}
