package Test;

public class Node implements Comparable<Node>{
    String name;
    int listens;
    public Node(String name, int listens) {
        this.name = name;
        this.listens = listens;
    }

    @Override
    public int compareTo(Node o) {
        if (listens < o.listens) {
            return 1;
        }
        return -1;
    }

    public String toString() {
        return name;
    }
}
