package Game;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.util.concurrent.locks.Lock;
import javax.swing.SwingUtilities;
import static Game.Map.*;

public class GameDriver {

    static Scanner scan = new Scanner(System.in);
    static Random rand = new Random();
    static final boolean printBreakthroughs = false;
    static final boolean changeMc = true;
    public static int UID = 0;
    static boolean guiEnabled = false;
    static MapViewer mapViewer = null;
    //public static int iUID = 0;


    public static void main(String[] args) throws InterruptedException, IOException {
        //tiles[range / 2][range / 2] = new Area(); // Starting Point of the World
        Map map = new Map(); // Starting Point of the World
        //Character[] characters = new Character[startingPopulation];
        int time = 0; // In Weeks
        System.out.println("Enable map viewer? Y/n");
        char guiInput = scan.next().charAt(0);
        if (guiInput == 'Y' || guiInput == 'y') {
            guiEnabled = true;
            try {
                SwingUtilities.invokeAndWait(() -> mapViewer = new MapViewer());
            } catch (Exception e) {
                System.out.println("Map viewer unavailable: " + e.getMessage());
                guiEnabled = false;
            }
        }
        System.out.println("Would you like to play? Y/n");
        char input = scan.next().charAt(0);

        if (input == 'Y' || input == 'y') {
            System.out.println("Character setup: (C)ustomize, (F)ollow mode, Enter for default Alchemist");
            char mcOpt = scan.next().charAt(0);
            Character bete = new Character(tiles[range / 2][range / 2]);
            currentCharacter = bete;
            characters.add(bete);
            population++; // Don't forget to add player characters to population
            bete.player = 1;
            if (mcOpt == 'C' || mcOpt == 'c') {
                System.out.println("Trait: A=Alchemist B=Blacksmith T=Tailor S=Scribe I=Innovative L=Leader M=Merchant R=Runner");
                char tc = scan.next().charAt(0);
                switch (tc) {
                    case 'A': case 'a': bete.trait = Character.Traits.Alchemist;   break;
                    case 'B': case 'b': bete.trait = Character.Traits.Blacksmith;  break;
                    case 'T': case 't': bete.trait = Character.Traits.Tailor;      break;
                    case 'S': case 's': bete.trait = Character.Traits.Scribe;      break;
                    case 'I': case 'i': bete.trait = Character.Traits.Innovative;  break;
                    case 'L': case 'l': bete.trait = Character.Traits.Leader;      break;
                    case 'M': case 'm': bete.trait = Character.Traits.Merchant;    break;
                    case 'R': case 'r': bete.trait = Character.Traits.Runner;      break;
                    default:            bete.trait = Character.Traits.Innovative;  break;
                }
                System.out.println("Potential (1-100):");
                try { bete.potential = Math.min(100, Math.max(1, Integer.parseInt(scan.next()))); } catch (Exception e) { bete.potential = 100; }
                System.out.println("Affiliation: G=Good N=Neutral E=Evil");
                char ac = scan.next().charAt(0);
                if (ac == 'G' || ac == 'g') bete.affiliation = "Good";
                else if (ac == 'E' || ac == 'e') bete.affiliation = "Evil";
                else bete.affiliation = "Neutral";
            } else if (mcOpt == 'F' || mcOpt == 'f') {
                bete.player = 2; bete.potential = 100; bete.trait = Character.Traits.Innovative;
            } else {
                bete.potential = 100; bete.trait = Character.Traits.Alchemist;
            }
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
                // Reset previous hotspots
                for (Area area : hotspotAreas) { area.lvl = area.baseLvl; area.hotspot = false; }
                hotspotAreas.clear();
                // Pick 3-5 new hotspot areas (non-city, not Originis)
                ArrayList<Area> eligible = new ArrayList<>();
                for (Area area : createdAreas) { if (!area.city && area.lvl > 0) eligible.add(area); }
                int hotCount = rand.nextInt(3) + 3;
                for (int _h = 0; _h < hotCount && !eligible.isEmpty(); _h++) {
                    Area pick = eligible.remove(rand.nextInt(eligible.size()));
                    pick.baseLvl = pick.lvl;
                    pick.lvl += rand.nextInt(20) + 10;
                    pick.hotspot = true;
                    hotspotAreas.add(pick);
                }
                // Resource disputes: sects competing for the same dungeon/hotspot areas escalate squabbles
                ArrayList<Organization> sectList = new ArrayList<>();
                for (Area area : createdAreas) {
                    if (area.sect != null && !sectList.contains(area.sect)) sectList.add(area.sect);
                }
                for (int _s1 = 0; _s1 < sectList.size(); _s1++) {
                    for (int _s2 = _s1 + 1; _s2 < sectList.size(); _s2++) {
                        Organization s1 = sectList.get(_s1);
                        Organization s2 = sectList.get(_s2);
                        if (s1.isAlly(s2) || s1.isAtWar() || s2.isAtWar()) continue;
                        if (s1.master == null || s2.master == null) continue;
                        for (Area a : s1.master.knownLocations) {
                            if (a == null || (!a.hotspot && a.dungeonType == null)) continue;
                            if (s2.master.knownLocations.contains(a)) {
                                s1.squabbles += a.hotspot ? 2 : 1; // hotspot disputes escalate faster
                                if (s1.squabbles >= 4) s1.declareWar(s2);
                                break;
                            }
                        }
                    }
                }
                // Succession crisis: tick down crisis timers
                for (Organization s : sectList) {
                    if (s.crisisTimer > 0) s.crisisTimer--;
                }
                // Sect quest boards refresh yearly
                for (Organization s : sectList) {
                    s.postMissionsYearly();
                }
                // City taxation: owners collect 1% of population as yearly ss income
                for (Area area : createdAreas) {
                    if (area.city && area.cityOwner != null && !area.cityOwner.dead) {
                        area.cityOwner.ss += area.population / 100;
                        area.cityOwner.convertCurrency();
                    }
                }
                // Beast Tide: overflow Beast Lairs spread monsters to neighbors and threaten nearby cities
                int[] btDx = {-1, 1, 0, 0};
                int[] btDy = {0, 0, -1, 1};
                for (Area area : new ArrayList<>(createdAreas)) {
                    if (area.dungeonType == Area.DungeonType.BEAST_LAIR && area.monsters > area.lvl * 2000) {
                        int spread = area.monsters / 4;
                        for (int _d = 0; _d < 4; _d++) {
                            int nx = area.x + btDx[_d], ny = area.y + btDy[_d];
                            if (nx >= 0 && nx < range && ny >= 0 && ny < range && tiles[nx][ny] != null) {
                                Area nbr = tiles[nx][ny];
                                nbr.monsters += spread / 4;
                                if (nbr.city && nbr.population > 0) {
                                    long casualties = Math.min(nbr.population / 10, spread * 100L);
                                    nbr.population = (int) Math.max(1, nbr.population - casualties);
                                    System.out.println("Beast Tide erupts from " + area.name + " and strikes " + nbr.name + "! -" + casualties + " population");
                                }
                            }
                        }
                        area.monsters = area.lvl * 1000;
                    }
                }
                // Celestial Trial: once per year, spawn a temporary high-level trial site
                if (!createdAreas.isEmpty()) {
                    ArrayList<Area> trialElig = new ArrayList<>();
                    for (Area area : createdAreas) {
                        if (!area.city && area.dungeonType == null && area.celestialTrialTimer == 0 && area.lvl >= 20) trialElig.add(area);
                    }
                    if (!trialElig.isEmpty()) {
                        Area trialArea = trialElig.get(rand.nextInt(trialElig.size()));
                        trialArea.dungeonType = Area.DungeonType.CELESTIAL_TRIAL;
                        trialArea.celestialTrialTimer = 10;
                        // Notify characters within 15 tiles
                        for (Character cc : characters) {
                            if (cc == null || cc.dead || !cc.born) continue;
                            if (Math.abs(cc.location.x - trialArea.x) + Math.abs(cc.location.y - trialArea.y) <= 15) {
                                if (!cc.knownLocations.contains(trialArea)) cc.knownLocations.add(trialArea);
                            }
                        }
                        System.out.println("A Celestial Trial has opened at " + trialArea.name + " [" + trialArea.x + "," + trialArea.y + "]! It will close in 10 ticks.");
                    }
                }
                // Heaven-Sent Treasure: every 3 years, a powerful technique descends into the world
                if ((time / 52) % 3 == 2) {
                    ArrayList<Area> htElig = new ArrayList<>();
                    for (Area area : createdAreas) {
                        if (!area.city && area.heavenlyTechnique == null) htElig.add(area);
                    }
                    if (!htElig.isEmpty()) {
                        Area htArea = htElig.get(rand.nextInt(htElig.size()));
                        Character phantom = characters.get(rand.nextInt(characters.size()));
                        if (phantom == null) phantom = characters.get(0);
                        Technique heavenTech = new Technique(
                            rand.nextInt(2) == 0 ? Technique.techniqueType.Martial : Technique.techniqueType.Movement,
                            phantom, Math.max(5, rand.nextInt(10) + 5));
                        heavenTech.name = "Heaven-Sent " + heavenTech.name;
                        htArea.heavenlyTechnique = heavenTech;
                        // Broadcast to all characters within 20 tiles
                        for (Character cc : characters) {
                            if (cc == null || cc.dead || !cc.born) continue;
                            if (Math.abs(cc.location.x - htArea.x) + Math.abs(cc.location.y - htArea.y) <= 20) {
                                if (!cc.knownLocations.contains(htArea)) cc.knownLocations.add(htArea);
                            }
                        }
                        System.out.println("Heaven's Will descends! A heavenly technique has appeared at " + htArea.name + " [" + htArea.x + "," + htArea.y + "]!");
                    }
                }
                //scan.nextLine();
            }
            // Tournament timer countdown and resolution (every tick)
            for (Area area : createdAreas) {
                if (area.tournamentTimer > 0) {
                    area.tournamentTimer--;
                    if (area.tournamentTimer == 0) {
                        area.runTournament();
                    }
                }
                // Celestial Trial despawn
                if (area.celestialTrialTimer > 0) {
                    area.celestialTrialTimer--;
                    if (area.celestialTrialTimer == 0) {
                        area.dungeonType = null;
                        System.out.println("The Celestial Trial at " + area.name + " has closed.");
                    }
                }
            }

            // Ages and Cultivates Players
            for (Character character : characters) {
                if (!character.dead) {
                    character.time();
                    if (character.born) {
                        character.cultivate();
                        if (character.sect != null && character.sect.founder.lvl > character.lvl) {
                            int bonus = Math.max(character.sect.founder.majorLvl, character.sect.sectLvl() / 10);
                            for (int i = 0; i < bonus; i++) {
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
                    if (character.player >= 1 && character.age >= 8) {
                        if (character.player == 2) {
                            character.movement();
                            System.out.println("[Follow] " + character + character.location);
                        } else {
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
                                case 'i':
                                    System.out.println("=== " + character.name + " " + character.lastName + " ===");
                                    System.out.println("Level: " + character.lvl + "  (Major: " + character.majorLvl + "  Minor: " + character.minorLvl + ")");
                                    System.out.println("Cultivation: " + (int)character.cultivation + " / " + (int)character.cultivationBaseMax);
                                    System.out.println("Potential: " + character.potential + "  Age: " + character.age + " / " + character.maxAge);
                                    System.out.println("Affiliation: " + character.affiliation + "  Trait: " + character.getTrait());
                                    System.out.println("Profession Lvl: " + character.professionLvl);
                                    System.out.println("Wealth: " + character.ss + " SS  " + character.sc + " SC  " + character.dc + " DC  " + character.ij + " IJ");
                                    System.out.println("Weapon: " + (character.equippedWeapon != null ? character.equippedWeapon : "None"));
                                    System.out.println("Armor:  " + (character.equippedClothing != null ? character.equippedClothing : "None"));
                                    System.out.println("Martial Technique: " + character.martialTechnique + "  Movement Technique: " + character.movementTechnique);
                                    System.out.println("Kills: " + character.kills + "  Items Crafted: " + character.itemsCrafted + "  Sect Points: " + character.sectPoints);
                                    if (character.sect != null) System.out.println("Sect: " + character.sect + " [" + character.sect.getRank(character) + "]  Sect Lvl: " + character.sect.sectLvl() + "  Treasury: " + character.sect.treasury.size() + " items");
                                    character.movement('x');
                                    break;
                                case 'l':
                                    System.out.println("=== Known Locations (" + character.knownLocations.size() + ") ===");
                                    for (Area area : character.knownLocations) {
                                        if (area != null) {
                                            String info = area.name + " [" + area.x * 100 + ", " + area.y * 100 + "]";
                                            if (area.city) info += " (City)";
                                            if (area.sect != null) info += " [Sect: " + area.sect.name + "]";
                                            if (area.cityOwner != null) info += " [Owner: " + area.cityOwner.name + "]";
                                            System.out.println(info);
                                        }
                                    }
                                    character.movement('x');
                                    break;
                                case 'r':
                                    System.out.println("=== Relationships ===");
                                    for (Relationship rel : character.relationshipList.relationships) {
                                        Character friend = characters.get(rel.friendID);
                                        if (friend != null && !friend.dead) {
                                            String label = rel.value >= 3 ? "Best Friend" :
                                                           rel.value == 2 ? "Close Friend" :
                                                           rel.value == 1 ? "Friend" :
                                                           rel.value == 0 ? "Acquaintance" :
                                                           rel.value == -1 ? "Dislike" :
                                                           rel.value == -2 ? "Rival" : "Arch-Enemy";
                                            System.out.println(friend.name + " " + friend.lastName + " — " + label + " (" + rel.value + ")");
                                        }
                                    }
                                    if (character.spouse != null) System.out.println("Spouse: " + character.spouse.name + " " + character.spouse.lastName);
                                    character.movement('x');
                                    break;
                                case 'o':
                                    System.out.println("Auto-move options:");
                                    System.out.println("  x — Wait N turns (quest auto-runs)");
                                    System.out.println("  w/a/s/d — Move in direction N turns");
                                    System.out.println("  c — Move toward coordinate N turns");
                                    System.out.println("  m — Random movement N turns");
                                    char moveOpt = scan.next().charAt(0);
                                    switch (moveOpt) {
                                        case 'x':
                                            character.movement = 'x';
                                            System.out.println("How many turns?");
                                            character.skipping = scan.nextInt() - 1;
                                            character.movement('x');
                                            break;
                                        case 'w': case 'a': case 's': case 'd':
                                            character.movement = moveOpt;
                                            System.out.println("How many turns?");
                                            character.skipping = scan.nextInt() - 1;
                                            character.movement(moveOpt);
                                            break;
                                        case 'c':
                                            System.out.println("Target tile X Y (0-" + (Map.range-1) + ", e.g. '15 15'):");
                                            character.autoX = scan.nextInt();
                                            character.autoY = scan.nextInt();
                                            character.movement = 'c';
                                            System.out.println("How many turns?");
                                            character.skipping = scan.nextInt() - 1;
                                            // first step toward coord
                                            int dx0 = character.autoX - character.location.x;
                                            int dy0 = character.autoY - character.location.y;
                                            if (dx0 == 0 && dy0 == 0) { character.autoX = -1; character.autoY = -1; character.movement = 'x'; character.movement('x'); }
                                            else if (Math.abs(dx0) >= Math.abs(dy0)) character.movement(dx0 > 0 ? 'd' : 'a');
                                            else character.movement(dy0 > 0 ? 's' : 'w');
                                            break;
                                        case 'm':
                                            character.movement = 'm';
                                            System.out.println("How many turns?");
                                            character.skipping = scan.nextInt() - 1;
                                            character.movement();
                                            break;
                                        default:
                                            character.movement('x');
                                    }
                                    break;
                                default:
                                    character.movement('x');
                                    break;
                            }
                        } else {
                            character.skipping--;
                            if (character.movement == 'm') {
                                character.movement();
                            } else if (character.movement == 'c' && character.autoX >= 0) {
                                int dxc = character.autoX - character.location.x;
                                int dyc = character.autoY - character.location.y;
                                if (dxc == 0 && dyc == 0) {
                                    character.autoX = -1; character.autoY = -1;
                                    character.movement = 'x';
                                    character.movement('x');
                                } else if (Math.abs(dxc) >= Math.abs(dyc)) {
                                    character.movement(dxc > 0 ? 'd' : 'a');
                                } else {
                                    character.movement(dyc > 0 ? 's' : 'w');
                                }
                            } else {
                                character.movement(character.movement);
                            }
                        }
                            if (character.skipping <= 0) {
                                System.out.println("" + character + character.location);
                            }
                        } // end else (non-followMode)
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
            monstersList.removeAll(awakenedMonsters);
            awakenedMonsters.clear();
            tempCharacters.clear();
            remCharacters.clear();
            if (guiEnabled && mapViewer != null) mapViewer.refresh();
            Thread.sleep(10000);
        }
    }
}
