import java.util.*;

/*Definition for a single node in the network*/
public class Node {
    private int id;
    private ArrayList<Integer> storedKeys;
    public Node(int id, ArrayList<Integer> storedKeys) {
        this.id = id;
        this.storedKeys = storedKeys;
    }

    public int getId(){
        return this.id;
    }

    public ArrayList<Integer> getStoredKeys(){
        return this.storedKeys;
    }
}
