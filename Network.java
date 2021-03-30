import java.util.*;

public class Network {
    private int sizeRing;
    private Graph network;

    public Network(int size) {
        this.sizeRing = size;
    }
    
    private int hash(int id){
        return id % sizeRing;
    }

    private int exp(int base, int exponent) {
        int i = 0;
        int result = 1;

        while (i < exponent) {
            result = result * base;
            ++i;
        }
        return result;
    }

    public void buildNetwork(int[] processors, int[] keys) {
        Arrays.sort(processors); //Sort list of processor ids in increasing order
        this.sizeRing = exp(2, processors.length);

        //Set keys to associated processors which they should be stored in
        Hashtable<Integer, ArrayList<Integer>> key_dict = new Hashtable<Integer, ArrayList<Integer>>();
        for (int j = 0; j < keys.length; j++){
            int k = hash(keys[j]);
            for (int i = 0; i < (processors.length - 1); i++) {
                if (hash(processors[i+1]) > hash(processors[i])) {
                    if (k > hash(processors[i]) && k <= hash(processors[i+1])){
                        if (key_dict.get(hash(processors[i+1])) != null) {
                            key_dict.get(hash(processors[i+1])).add(k);
                        } else {
                            key_dict.put(hash(processors[i+1]), new ArrayList<Integer>(Arrays.asList(k)));
                        }
                        break;
                    } 
                } 
                if (hash(processors[i+1]) == hash(processors[i])) {
                    if (k == hash(processors[i+1])){
                        if (key_dict.get(hash(processors[i+1])) != null) {
                            key_dict.get(hash(processors[i+1])).add(k);
                        } else {
                            key_dict.put(hash(processors[i+1]), new ArrayList<Integer>(Arrays.asList(k)));
                        }
                        break;
                    }  
                } 
                if ((i+1) == (processors.length - 1)){
                    //processor[0] is i+1 and processor[i+1] is i
                    if ((k > hash(processors[0]) && k > hash(processors[i+1])) || k < hash(processors[i+1])){
                        if (key_dict.get(hash(processors[0])) != null) {
                            key_dict.get(hash(processors[0])).add(k);
                        } else {
                            key_dict.put(hash(processors[0]), new ArrayList<Integer>(Arrays.asList(k)));
                        }
                    } 
                }  
            }
        }

        //Get all keys from dictionary as a set object
        Set<Integer> dictKeySet = key_dict.keySet();
        //Iterate over dictionary and create a new node for each dictionary  
        ArrayList<Node> nodeList = new ArrayList<>();
        for (int key: dictKeySet){
            Node node = new Node(key, key_dict.get(key));
            nodeList.add(node);
        }
        //Create peer-to-peer network
        this.network = new Graph(nodeList);
    }

    public Graph getNetwork() {
        return this.network;
    }

    //Return processor where key was found, and return the id as a string
    public String findKey() {
        String str = "";
        return str;
    }

    //Add a new processor and return the list of keys which are moved to the new processor
    public ArrayList<Integer> addProcessor(Node processor) {
        //TODO
    }

    //Remove a processor from the system
    public void endProcessor(Node processor) {
        //TODO
    }

    //Make a specified processor crash
    public void crashProcessor(Node processor) {
        //TODO
    }
}
