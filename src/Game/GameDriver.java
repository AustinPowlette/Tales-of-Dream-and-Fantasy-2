package Game;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.util.concurrent.locks.Lock;
import static Game.Map.*;

public class GameDriver {

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
        char input = scan.next().charAt(0);

        if (input == 'Y' || input == 'y') {
            Character bete = new Character(tiles[range / 2][range / 2]);
            currentCharacter = bete;
            characters.add(bete);
            population++; // Don't forget to add player characters to population
            bete.player = true;
            bete.potential = 100;
            bete.trait = Character.Traits.Alchemist;
            System.out.println("" + bete + bete.location);
            System.out.println(bete + "'s Potential: " + bete.potential + " Trait: " + bete.getTrait());

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
        // Character Creation
        for (int i = 0; i < startingPopulation; i++) {
            characters.add(new Character(tiles[range / 2][range / 2]));
        }


        while (true) {
            //System.out.println("It's day: " + (time/24 + 1));
            if ((time) % 52 == 0) {
                System.out.println("It's year: " + (((time / 52)) + 1) + " Population: " + population + " " + monstersList);
                //scan.nextLine();
            }

            // Ages and Cultivates Players
            for (Character character : characters) {
                if (!character.dead) {
                    character.time();
                    if (character.born) {
                        character.cultivate();
                        if (character.sect != null && character.sect.founder.lvl > character.lvl) {
                            for (int i = 0; i < character.sect.founder.majorLvl; i++) {
                                character.cultivate();
                            }
                        }
                    }
                }
            }

            // Causes Players to Move and Go Through Events

            for (Character character : characters) {
                currentCharacter = character;
                if (!character.dead && character.born) {
                    if (character.player == true && character.age >= 8) {
                        //character.movement(scan.next().charAt(0));
                        if (character.skipping <= 0) {
                            System.out.println(character.backpack + "\n" + character.materialBag + "\n" + character.pillBag); // Change .backpack to just character
                            if (character.spouse != null) {
                                System.out.println("" + character.spouse + character.spouse.location);
                            }

                            switch (scan.next().charAt(0)) {
                                case 'w':
                                    character.movement('w');
                                    break;
                                case 'a':
                                    character.movement('a');
                                    break;
                                case 's':
                                    character.movement('s');
                                    break;
                                case 'd':
                                    character.movement('d');
                                    break;
                                case 'o':
                                    System.out.println("Welcome to Options!\nInput 'x' to skip turns without movement!\nInput 'm' to skip turns with movement");
                                    switch (scan.next().charAt(0)) {
                                        case 'x':
                                            character.movement = 'x';
                                            System.out.println("How many turns do you want to skip?");
                                            character.skipping = scan.nextInt() - 1;
                                            character.movement('x');
                                            break;
                                        case 'm':
                                            character.movement = 'm';
                                            System.out.println("How many turns do you want to skip?");
                                            character.skipping = scan.nextInt() - 1;
                                            character.movement();
                                            break;
                                    }
                                default:
                                    character.movement('x');
                                    break;
                            }
                        } else {
                            character.skipping--;
                            if (character.movement == 'm') {
                                character.movement();
                            } else {
                                character.movement(character.movement);
                            }
                        }
                        if (character.skipping <= 0) {
                            System.out.println("" + character + character.location);
                        }
                        //System.out.println(character + " is doing their quest!");
                        character.quest();
                    }
                    else if (character.age >= 8) {
                        //System.out.println(character + " is doing their quest!!");
                        character.movement();
                        character.quest();
                    }
                    else {
                        //System.out.println(character + " is doing their quest!!!");
                        character.quest();
                    }

                    // For Mothers
                    if (character.gender.equals("Female") && !character.children.isEmpty()) {
                        for (Character child : character.children) {
                            if (child.age < 8) {
                                child.location.characters.remove(child);
                                child.location = child.mother.location;
                                if (!child.location.characters.contains(child)) {
                                    child.location.characters.add(child); // Adds This Character to the Character List of the New Location
                                }
                                if (!child.knownLocations.contains(child.location)) {
                                    child.knownLocations.add(child.location); // Adds New Location to knownLocations
                                }
                            }
                        }
                    }
                }
            }


            // Ages the areas and adds the tempCharacters list to the characters list.

            for (Area area : createdAreas) {
                area.age();
            }
            for (Character monster : monstersList) {
                if (!monster.location.characters.isEmpty()) {
                    Character target = monster.location.characters.get(rand.nextInt(monster.location.characters.size()));
                    monster.battleCheck(target);
                }
                monster.movement();
            }
            time++;

            characters.addAll(tempCharacters);
            population += tempCharacters.size();

            //characters.removeAll(remCharacters); // Old removal method
            if (!remCharacters.isEmpty()) {
                for (Character character : remCharacters) {
                    characters.set(character.id, null); // Sets the index to null
                    population--;
                }
            }
            monstersList.removeAll(remCharacters);
            tempCharacters.clear();
            remCharacters.clear();
            Thread.sleep(10000);
        }
    }
}
