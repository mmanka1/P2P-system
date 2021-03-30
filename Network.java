import java.util.*;

public class Network {
    private int[] processors;
    private int sizeRing;
    private int m; //exponent to calculate sizeRing
    private Graph network;

    public Network(int[] processors, int numProcessors) {
        this.processors = processors;
        this.m = numProcessors;
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

    //Return index of closest processor to the key, based on a processor's finger table
    private int closestProcessor(int k, int[] fingerTable){
        for (int i = 0; i < (fingerTable.length-1); i++) {
            if (hash(fingerTable[i+1]) > hash(fingerTable[i])) {
                if (k >= hash(fingerTable[i]) && k < hash(fingerTable[i+1]))
                    return i;
            } 
            else { //hp(fingerTable[i]) >= hp(fingerTable[i+1])
                if ((k >= hash(fingerTable[i]) && k > hash(fingerTable[i+1])) || k < hash(fingerTable[i+1])){
                    return i;
                } 
            }  
        }
        return -1;
    }

    private int[] getSuccessor(int k) {
        int[] result = new int[2];
        for (int i = 0; i < (this.m-1); i++) {
            if (hash(this.processors[i+1]) > hash(this.processors[i])) {
                if (k > hash(this.processors[i]) && k <= hash(this.processors[i+1])){
                    result[0] = i+1;
                    result[1] = hash(this.processors[i+1]);
                    return result;
                } 
            } 
            if (hash(this.processors[i+1]) == hash(this.processors[i])) {
                if (k == hash(this.processors[i+1])) {
                    result[0] = i+1;
                    result[1] = hash(this.processors[i+1]);
                    return result;
                }  
            } 
            if ((i+1) == (this.processors.length - 1)){
                //processor[0] is i+1 and processor[i+1] is i
                if ((k > hash(this.processors[0]) && k > hash(this.processors[i+1])) || k < hash(this.processors[i+1])){
                    result[0] = 0;
                    result[1] = hash(this.processors[0]);
                    return result;
                } 
            }  
        }
        return null;
    }

    private int[] getFingers(int id){
        int[] fingers = new int[this.m + 1];
        for (int i = 0; i < m; i++){
            fingers[i] = getSuccessor((hash(id) + exp(2, i)) % this.sizeRing)[1];
        }
        fingers[m] = id;
        return fingers;
    }

    public void buildNetwork(int[] keys) {
        Arrays.sort(this.processors); //Sort list of processor ids in increasing order
        this.sizeRing = exp(2, this.m);

        //Set keys to associated processors which they should be stored in
        Hashtable<Integer, ArrayList<Integer>> key_dict = new Hashtable<Integer, ArrayList<Integer>>();
        
        for (int j = 0; j < keys.length; j++){
            int k = hash(keys[j]);
            int hp = getSuccessor(k)[1];

            if (key_dict.get(hp) != null) {
                key_dict.get(hp).add(k);
            } else {
                key_dict.put(hp, new ArrayList<Integer>(Arrays.asList(k)));
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

    private boolean processorExists(int id){
        for (int identifier: this.processors){
            if (id == identifier) 
                return true;
        }
        return false;
    }

    //Return processor id where key was found
    //Params: 
    //(1) id of processor to start search at 
    //(2) key to search for
    public String findKey(int id, int key) {
        if (!processorExists(id)){
            return "Processor not found";
        } else { //Processor exists in the network
            String result = " ";
            int[] fingerTable = getFingers(id);

            //Initialization
            String[] data;
            int succ;
            String mssg = null;
            String message = null;
            int originalId = id;

            Node processor = network.findNode(id);  //Get processor corresponding to this processor id
            ArrayList<Integer> processorEdgeIds = new ArrayList<>();  //Store edges associated with node

            if (processor != null){
                //Get edges associated with node
                for (Node n: network.findEdges(processor))
                    processorEdgeIds.add(n.getId());

                //If this processor has the key
                if (processor.getStoredKeys().contains(key))
                    return result = "Key " + key + " found locally at processor " + String.valueOf(processor.getId());
                
                //Otherwise, current processor does not have key, so check if successor has the key
                succ = fingerTable[0];
                //If the successor of the key is the same as the successor of the current processor, 
                //then the key must be in the segment between current processor and successor
                int[] keySuccessor = getSuccessor(key);
                if (hash(keySuccessor[1]) == hash(succ)) {
                    //If processor to send message to is connected to current processor
                    if (processorEdgeIds.contains(fingerTable[keySuccessor[0]]))
                        mssg = fingerTable[0] + "," + "GET" + "," + key + "," + id;
                } else { //Otherwise, find the processor closest to the key and send a lookup to such processor
                    int indexClosest = closestProcessor(key, fingerTable);
                    if (indexClosest != -1) {
                        if (processorEdgeIds.contains(fingerTable[indexClosest])) 
                            mssg = fingerTable[indexClosest] + "," + "LOOKUP" + "," + key + "," + id;
                    }
                }

                while(true){
                    //SEND MESSAGE
                    if (mssg != null) {
                        //Send mssg
                        id = Integer.parseInt(mssg.split(",")[0]);
                        fingerTable = getFingers(id);
                        processor = network.findNode(id);
                        processorEdgeIds = new ArrayList<>();
                        for (Node n: network.findEdges(processor)) //Get edges associated with node
                            processorEdgeIds.add(n.getId());

                        //If received end message and the original processor, then return
                        if (mssg.split(",")[1].equals("END") && originalId == id){
                            return result;
                        }
                    }
    
                    //RECEIVE MESSAGE
                    message = mssg;
                    mssg = null;
                    if (message != null) {
                        data = message.split(",");
                        if (data[1].equals("END")) {
                            if (processorEdgeIds.contains(fingerTable[0]))
                                mssg = fingerTable[0] + "," + "END";
                        } 
                        else {
                            if (data[1].equals("GET")) {
                                //If this processor has the key
                                if (processor.getStoredKeys().contains(Integer.parseInt(data[2]))){
                                    if (processorEdgeIds.contains(Integer.parseInt(data[3])))
                                        mssg = data[3] + "," + "FOUND" + "," + data[2] + "," + id;
                                } else { 
                                    //If there is another processor pj mapped to current processor pi, forward get request to pj
                                    if (hash(fingerTable[0]) == hash(id)) {
                                        if (processorEdgeIds.contains(fingerTable[0])) 
                                            mssg = fingerTable[0] + "," + "GET" + "," + data[2] + "," + data[3];
                                    } else {
                                        //If message id equals id of original processor, get request travelled back to originating processor, so key not found
                                        if (Integer.parseInt(data[3]) == originalId)
                                            result = "Key " + data[2] + " not found";
                                            if (processorEdgeIds.contains(fingerTable[0]))
                                                //Send end process message to the other processors
                                                mssg = fingerTable[0] + "," + "END";
                                        else { //Key not found
                                            if (processorEdgeIds.contains(Integer.parseInt(data[3])))
                                                mssg = data[3] + "," + "NOT_FOUND" + "," + data[2] + "," + id;
                                        }
                                    }
                                }
                            } else {
                                if (data[1].equals("LOOKUP")) {
                                    //If this processor has the key
                                    if (processor.getStoredKeys().contains(Integer.parseInt(data[2]))){
                                        if (processorEdgeIds.contains(Integer.parseInt(data[3])))
                                            mssg = data[3] + "," + "FOUND" + "," + data[2] + "," + id;
                                    } else {
                                        //If there is another processor pj mapped to current processor pi, forward get request to pj
                                        if (hash(fingerTable[0]) == hash(id)) {
                                            if (processorEdgeIds.contains(fingerTable[0])) 
                                                mssg = fingerTable[0] + "," + "GET" + "," + data[2] + "," + data[3];
                                        } else {  //Otherwise, current processor does not have key, so check if successor has the key
                                            succ = fingerTable[0];
                                            //If the successor of the key is the same as the successor of the current processor, 
                                            //then the key must be in the segment between current processor and successor
                                            keySuccessor = getSuccessor(key);
                                            if (hash(keySuccessor[1]) == hash(succ)) {
                                                //If processor to send message to is connected to current processor
                                                if (processorEdgeIds.contains(fingerTable[keySuccessor[0]]))
                                                    mssg = fingerTable[0] + "," + "GET" + "," + data[2] + "," + data[3];
                                            } else { //Otherwise, find the processor closest to the key and send a lookup to such processor
                                                int indexClosest = closestProcessor(key, fingerTable);
                                                if (indexClosest != -1) {
                                                    if (processorEdgeIds.contains(fingerTable[indexClosest])) 
                                                        mssg = fingerTable[indexClosest] + "," + "LOOKUP" + "," + data[2] + "," + data[3];
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (data[1].equals("FOUND")) {
                                        result = "Key " + data[2] + " found at processor " + data[3];
                                        if (processorEdgeIds.contains(fingerTable[0]))
                                            //Send end process message to the other processors
                                            mssg = fingerTable[0] + "," + "END";
                                    } else {
                                        //If not found message is received
								        if (data[1].equals("NOT_FOUND")) { 
                                            result = "Key " + data[2] + " not found";
                                            if (processorEdgeIds.contains(fingerTable[0]))
                                                //Send end process message to the other processors
                                                mssg = fingerTable[0] + "," + "END";
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return "processor does not exist";
        }
    }

    //Add a new processor and return the list of keys which are moved to the new processor
    // public ArrayList<Integer> addProcessor(Node processor) {
    //     //TODO
    // }

    //Remove a processor from the system
    public void endProcessor(Node processor) {
        //TODO
    }

    //Make a specified processor crash
    public void crashProcessor(Node processor) {
        //TODO
    }
}
