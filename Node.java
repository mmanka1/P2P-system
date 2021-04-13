import java.util.*;

/** 
 * Definition for a single node in the network
*/
public class Node {
    private int id;
    private ArrayList<Integer> storedKeys;
    private int[] fingerTable;
    private boolean isCrashed;
    int successor;

    /**
     * 
     * @param id
     * @param storedKeys
     * @param fingerTable
     */
    public Node(int id, ArrayList<Integer> storedKeys, int[] fingerTable) {
        this.id = id;
        this.storedKeys = storedKeys;
        this.fingerTable = fingerTable;
        this.successor = fingerTable[0];
        this.isCrashed = false;
    }

    /**
     * 
     * @return id
     */
    public int getId(){
        return this.id;
    }

    /**
     * 
     * @return storedKeys
     */
    public ArrayList<Integer> getStoredKeys(){
        return this.storedKeys;
    }

    /**
     * 
     * @param storedKeys
     */
    public void setStoredKeys(ArrayList<Integer> storedKeys){
        this.storedKeys = storedKeys;
    }

    /**
     * 
     * @param key
     */
    public void addStoredKey(int key){
        this.storedKeys.add(key);
    }

    /**
     * 
     * @param key
     */
    public void removeStoredKey(int key){
        this.storedKeys.remove(key);
    }

    /**
     * 
     * @return
     */
    public int[] getFingerTable(){
        return fingerTable;
    }

    /**
     * 
     * @param fingerTable
     */
    public void setFingerTable(int[] fingerTable){
        this.fingerTable = fingerTable;
        this.successor = fingerTable[0];
    }

    /**
     * 
     * @return
     */
    public int getProcSuccessor(){
        return this.successor;
    }

    /**
     * 
     * @return
     */
    public boolean getCrashedStatus(){
        return this.isCrashed;
    }
    
    /**
     * 
     * @param status
     */
    public void setCrashedStatus(boolean status){
        this.isCrashed = status;
    }
}
