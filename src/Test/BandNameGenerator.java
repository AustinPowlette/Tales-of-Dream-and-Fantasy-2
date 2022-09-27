package Test;

import java.util.*;


public class BandNameGenerator {
    public static void main(String[] args) {
        ArrayList<String> names = new ArrayList<>();
        Random rand = new Random();
        boolean running = true;
        String[] alfaMist = {"Light", "Rust", "Organic", "Attune", "Galaxy", "Energy" };
        String[] inspiration = {"Dream", "Boy", "Blue", "Rest", "Rain", "Song", "Never", "Side", "Ream", "Mist" };
        String[] fanFavorites = {"Moon", "Lace", "Midnight", "Idea", "No", "Sunset", "New" };
        for (String string : inspiration) {
            names.add(string);
        }
        for (String string : fanFavorites) {
            names.add(string);
        }
        for (String string : alfaMist) {
            names.add(string);
        }


        //is for portmanteau
        int one = 0;
        int two = 0;
        /*while (running) {
            String name = "";
            String name1 = names.get(one);
            String name2 = names.get(two++);
            //System.out.println(name1 + " " + name2);
            for (int j = 0; j < name1.length(); j++) {
                for (int k = 0; k < name2.length(); k++) {
                    if (name1.charAt(j) == name2.charAt(k)) {
                        name += name1.substring(0, j) + name2.substring(k) + "\t/";
                    }
                }
            }
            System.out.println(name);
            if (two == names.size()){
                one++;
                two = 0;
            }
            if (one == names.size()) {
                running = false;
            }
        }*/

        //This is where the actual generation happens
        for (int i = 0; i < 50; i++) {
            String name = "";
            int option = rand.nextInt(3);
            // 0 is for combining words without spaces
            if (option == 0) {
                name += (names.get(rand.nextInt(names.size())));
                name += (names.get(rand.nextInt(names.size())).toLowerCase());
            }

            //1 is for combining words with spaces
            if (option == 1) {
                name += (names.get(rand.nextInt(names.size())));
                name += " ";
                name += (names.get(rand.nextInt(names.size())));

            }
            //3 is for having something of something
            if (option == 2) {
                name += (names.get(rand.nextInt(names.size())));
                name += (names.get(rand.nextInt(names.size())));
            }

            //Adds an s or es to the end of the name
            option = rand.nextInt(3);
            if (option == 0 && name.length() > 0) {
                if (name.charAt(name.length() - 1) == 's') {
                    name += "es";
                }
                else {
                    name += "s";
                }
                System.out.printf("The " + name + "\t/ " + name + "\n");
            }
            else {
                System.out.println(name);
            }
        }
    }
}
