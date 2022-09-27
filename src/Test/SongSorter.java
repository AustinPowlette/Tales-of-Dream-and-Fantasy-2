package Test;
import java.util.ArrayList;
import java.util.Comparator;

public class SongSorter {
    public static void main(String[] args) {
        ArrayList<Node> list = new ArrayList<>();
        list.add(new Node("Up & Down", 1817530));
        list.add(new Node("BNR", 3893483));
        list.add(new Node("Seeds", 1284009));
        list.add(new Node("L.A.", 1102691));
        list.add(new Node("Gone", 2180782));
        list.add(new Node("Retreat!", 1023097));
        list.add(new Node("Trophy", 4731341));
        list.add(new Node("Balloon", 4035972));
        list.add(new Node("Tunnel (all that you had)", 1117478));
        list.add(new Node("Ice Melt", 1501236));
        list.add(new Node("Cracking", 4748596));
        list.add(new Node("Nina", 9519750));
        list.add(new Node("Ghostride", 39864433));
        list.add(new Node("Fall Down", 7602575));
        list.add(new Node("M.R.", 3930559));
        list.add(new Node("The Letter", 2703027));
        list.add(new Node("Part III", 14856827));
        list.add(new Node("And It Never Ends", 2802490));
        list.add(new Node("Faces", 3015849));
        list.add(new Node("Jinx", 3909697));
        list.add(new Node("Plants", 36643806));
        list.add(new Node("Recently Played", 14422059));
        list.add(new Node("Thirty-Nine", 5264908));
        list.add(new Node("Locket", 81662562));
        list.add(new Node("Bones", 25395962));
        list.add(new Node("Vinta", 11366321));
        list.add(new Node("So Tired", 13126494));

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (list.get(j).listens < list.get(j+1).listens) {
                    Node bigger = list.get(j + 1);
                    Node smaller = list.get(j);
                    list.set(j, bigger);
                    list.set(j+1, smaller);
                }
            }
        }
        System.out.println(list);

    }
}
