import java.util.*;

/*Definition for a single node in the network*/
public class Node {
    private int id;
    private ArrayList<Integer> storedKeys;
    private int[] fingerTable;
    private boolean isCrashed;
    int successor;

    public Node(int id, ArrayList<Integer> storedKeys, int[] fingerTable) {
        this.id = id;
        this.storedKeys = storedKeys;
        this.fingerTable = fingerTable;
        this.successor = fingerTable[0];
        this.isCrashed = false;
    }

    public int getId(){
        return this.id;
    }

    public ArrayList<Integer> getStoredKeys(){
        return this.storedKeys;
    }

    public void setStoredKeys(ArrayList<Integer> storedKeys){
        this.storedKeys = storedKeys;
    }

    public void addStoredKey(int key){
        this.storedKeys.add(key);
    }

    public void removeStoredKey(int key){
        this.storedKeys.remove(key);
    }

    public int[] getFingerTable(){
        return fingerTable;
    }

    public void setFingerTable(int[] fingerTable){
        this.fingerTable = fingerTable;
        this.successor = fingerTable[0];
    }

    public int getProcSuccessor(){
        return this.successor;
    }

    public boolean getCrashedStatus(){
        return this.isCrashed;
    }
    
    public void setCrashedStatus(boolean status){
        this.isCrashed = status;
    }
}
