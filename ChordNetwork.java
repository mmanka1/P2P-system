import java.util.*;

public class ChordNetwork {
    private ArrayList<Integer> processors;
    private int numProcessors;
    private int numKeys;
    private int sizeRing; //2^m identifier space
    private int keySpace; //Key space U

    private int u; //Number of bits to represent identifers in the U universe
    private int m; //Number of bits to represent identifers in M universe
    private Random randomHash;

    private Graph network;

    /**
     * Initialize network
     * @param processors
     * @param numProcessors
     */
    public ChordNetwork(ArrayList<Integer> processors, int numProcessors) {
        this.processors = processors;
        this.numProcessors = numProcessors;
        randomHash = new Random();
    }

    /**
     * Calculate exponential
     * @param base
     * @param exponent
     * @return
     */
    private int exp(int base, int exponent) {
        int i = 0;
        int result = 1;
        while (i < exponent) {
            result = result * base;
            ++i;
        }
        return result;
    }

    /**
     * Set m such that 2^m approximately equals n^3
     * @return
     */
    private void set_m(){
        this.m = (int) (Math.ceil((3*Math.log(this.numProcessors))/Math.log(2)));
    }

    /** 
     * Set u according the number of bits required to store the number of keys
     * @return
    */
    private void set_u(){
        this.u = (int) Math.ceil(Math.log(this.numKeys));
    }

    /**
     * Universal hash function to hash keys
     * @param key
     * @return
     */
    private int hashKey(int key){
        int randomInt = randomHash.nextInt(this.keySpace);
        int a = this.sizeRing * randomInt;
        int b = key % this.sizeRing;
        return ((a*key) + b) % this.sizeRing;
    }

    /**
     * Deterministic hash function to hash processors ids
     * @param id
     * @return
     */ 
    private int hash(int id) {
        return id % sizeRing;
    }

    /**
     * Return index of closest processor to the key, based on a processor's finger table
     * @param k
     * @param fingerTable
     * @return
     */
    private int closestProcessor(int k, int[] fingerTable){
        for (int i = this.m-1; i >= 0; i--) {
            if (hash(fingerTable[i+1]) > hash(fingerTable[i])) {
                if (hashKey(k) >= hash(fingerTable[i]) && hashKey(k) < hash(fingerTable[i+1]))
                    return i;
            } else { 
                if (hash(fingerTable[i]) > hash(fingerTable[i+1])){
                    if ((hashKey(k) >= hash(fingerTable[i]) && hashKey(k) > hash(fingerTable[i+1])) || hashKey(k) < hash(fingerTable[i+1])){
                        return i;
                    } 
                }
            }  
        }
        return -1;
    }

    /**
     * Get successor of a ring identifier based on interval checking
     * @param k
     * @return
     */
    private int[] getSuccessor(int k) {
        int[] result = new int[2];
        for (int i = 0; i < (this.m-1); i++) {
            if (hash(this.processors.get(i+1)) > hash(this.processors.get(i))) {
                if (hashKey(k) > hash(this.processors.get(i)) && hashKey(k) <= hash(this.processors.get(i+1))){
                    result[0] = i+1;
                    result[1] = this.processors.get(i+1);
                    return result;
                } 
            } 
            if (hash(this.processors.get(i+1)) == hash(this.processors.get(i))) {
                if (hashKey(k) == hash(this.processors.get(i+1))) {
                    result[0] = i+1;
                    result[1] = this.processors.get(i+1);
                    return result;
                }  
            }
            if (hash(this.processors.get(i)) > hash(this.processors.get(i+1))) {
                //processor[0] is i+1 and processor[i+1] is i
                if ((hashKey(k) > hash(this.processors.get(i)) && hashKey(k) > hash(this.processors.get(i+1))) || hashKey(k) <= hash(this.processors.get(i+1))){
                    result[0] = i+1;
                    result[1] = this.processors.get(i+1);
                    return result;
                } 
            } 
            if ((i+1) == (this.processors.size() - 1)){
                //processor[0] is i+1 and processor[i+1] is i
                if ((hashKey(k) > hash(this.processors.get(0)) && hashKey(k) > hash(this.processors.get(i+1))) || hashKey(k) <= hash(this.processors.get(0))){
                    result[0] = 0;
                    result[1] = this.processors.get(0);
                    return result;
                } 
            }  
        }
        return null;
    }

    /**
     * Find successor of new node joining existing network
     * @param id
     * @return
     */
    private int findSuccessor(int id){
        //Initialization
        int id_known = this.processors.get(0);
        Node processor;
        String succCurrent = null; //successor for current known processor
        int[] fingerTable = null; //fingerTable for current known processor
        String succ = null;  //successor for new processor
        String[] data;

        String mssg = id_known + "," + "LOOKUP_SUCC" + "," + id;
        String message = null;

        while (true){
            //SEND MESSAGE
            if (mssg != null) {
                //Send mssg
                id = Integer.parseInt(mssg.split(",")[0]);
                processor = network.findNode(id);
                if (processor != null){
                    succCurrent = String.valueOf(processor.getProcSuccessor());
                    fingerTable = processor.getFingerTable();
                } 
            } else {
                if (succ != null) 
                    return Integer.parseInt(succ);
            }

            //RECEIVE MESSAGE
            message = mssg;
            mssg = null;
            if (message != null) {
                data = message.split(",");
                if (data[1].equals("LOOKUP_SUCC")) {
                    int newNodeSuccessor = getSuccessor((Integer.parseInt(data[2])) + 1)[1];
                    //If the successor of the new processor is the same as the successor of the current known processor
                    if (hash(newNodeSuccessor) == hash(Integer.parseInt(succCurrent))) 
                        mssg = data[2] + "," + "FOUND_SUCC" + ","  + newNodeSuccessor;
                    else { //Otherwise, find the processor closest to the key and send a lookup to such processor
                        int indexClosest = closestProcessor(Integer.parseInt(data[2]), fingerTable);
                        if (indexClosest != -1) 
                            if (fingerTable != null)    
                                mssg = fingerTable[indexClosest] + "," + "LOOKUP_SUCC" + "," + data[2];
                    }
                } else {
                    if (data[1].equals("FOUND_SUCC")) 
                        succ = data[2];
                }
            }
        }
    }

    /**
     * Set fingers for a processor's finger table
     * @param id
     * @param onJoin
     * @return
     */
    private int[] setFingers(int id, boolean onJoin){
        int[] fingers = new int[this.m + 1]; 
        try { 
            for (int i = 0; i < this.m; i++)
                if (!onJoin)
                    fingers[i] = getSuccessor(id + exp(2, i))[1];
                else 
                    if (i == 0) {
                        fingers[i] = findSuccessor(id);
                    } else {
                        fingers[i] = findSuccessor(id + exp(2, i));
                    }   
            fingers[m] = id;
        } catch (NullPointerException e){
            System.out.println("No more nodes can be added to the system\n");
            System.exit(0);
        }  
        return fingers;
    }

    /**Initialize Network
     * @param keys
    */
    public void buildNetwork(int[] keys) {
        this.numKeys = keys.length;
        Collections.sort(this.processors); //Sort list of processor ids in increasing order
        set_m(); 
        set_u();
        this.sizeRing = exp(2, this.m);
        this.keySpace = exp(2, this.u);

        //Set keys to associated processors which they should be stored in
        Hashtable<Integer, ArrayList<Integer>> key_dict = new Hashtable<Integer, ArrayList<Integer>>();
        
        for (int j = 0; j < keys.length; j++){
            int k = keys[j];
            int p = getSuccessor(k)[1];

            if (key_dict.get(p) != null) {
                key_dict.get(p).add(k);
            } else {
                key_dict.put(p, new ArrayList<Integer>(Arrays.asList(k)));
            }
        }

        //Get all keys from dictionary as a set object
        Set<Integer> dictKeySet = key_dict.keySet();
        //Iterate over dictionary and create a new processor for each dictionary  
        ArrayList<Node> nodeList = new ArrayList<>();
        for (int processorId: dictKeySet){
            Node node = new Node(processorId, key_dict.get(processorId), setFingers(processorId, false));
            nodeList.add(node);
        }

        //For any processors that do not store keys, also add these processors to the nodelist
        for (int processorId: this.processors){
            if (!dictKeySet.contains(processorId)){
                Node node = new Node(processorId, new ArrayList<Integer>(), setFingers(processorId, false));
                nodeList.add(node);
            }
        }

        //Create peer-to-peer network
        this.network = new Graph(nodeList);
    }

    /**
     * Get network representation
     * @return
     */
    public Graph getNetwork() {
        return this.network;
    }

    /**Return processor id where key was found
     * 
     * @param id
     * @param key
     * @return
    */
    public String findKey(int id, int key) {
        Node processor = network.findNode(id);  //Get processor corresponding to this processor id
        if (processor == null){ //Processor does not exist in the network
            return "Processor not found\n";
        }  
        //Processor has crashed, so set the initial processor to its successor instead
        if (processor.getCrashedStatus()){ 
            int updatedId = processor.getProcSuccessor();
            endProcessor(id, true); //Remove the crashed processor from the system
            id = updatedId;
            processor = network.findNode(id); //Once crashed processor removed, then get the updated successor
        } 

        //Initialization
        String[] data;
        String result = " ";
        int[] fingerTable = processor.getFingerTable();
        int succ = processor.getProcSuccessor();
        ArrayList<Integer> processorEdgeIds = network.findEdges(processor, sizeRing);  //Get edges associated with processor
        String mssg = null;
        String message = null;
        int originalId = id;

        //If this processor has the key
        if (processor.getStoredKeys().contains(key))
            return result = "Key " + key + " found locally at processor " + String.valueOf(processor.getId() + "\n");
        //If processor doesn't have key but key and id mapped to same ring identifer
        if (hash(id) == hashKey(key)){
            //Key should have been stored locally
            return "Key " + key + " not found\n";
        }
         
        //Otherwise, current processor does not have key, so check if successor has the key
        int[] keySuccessor = getSuccessor(key);
        //If the successor of the key is the same as the successor of the current processor, 
        //then the key must be in the segment between current processor and successor
        if (hash(keySuccessor[1]) == hash(succ)) {
            //If processor to send message to is connected to current processor
            if (processorEdgeIds.contains(fingerTable[keySuccessor[0]]))
                mssg = succ + "," + "GET" + "," + key + "," + id;
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
                processor = network.findNode(id);
                //Processor does not exist in the network
                if (processor == null) { 
                    return "Processor not found during search\n"; 
                } 
                //Processor has crashed, so send message to its successor instead
                if (processor.getCrashedStatus()){ 
                    int updatedId = processor.getProcSuccessor();
                    endProcessor(id, true); //Remove the crashed processor from the system
                    id = updatedId;
                    processor = network.findNode(id); //Once crashed processor removed, then get the updated successor
                } 
                fingerTable = processor.getFingerTable();
                succ = processor.getProcSuccessor();
                processorEdgeIds = network.findEdges(processor, sizeRing);  //Get edges associated with node

                //If received end message and the original processor, then return
                if (mssg.split(",")[1].equals("END") && originalId == id){
                    return result;
                }
            } else {
                return "Failure during search\n";
            }

            //RECEIVE MESSAGE
            message = mssg;
            mssg = null;
            if (message != null) {
                data = message.split(",");
                if (data[1].equals("END")) {
                    if (processorEdgeIds.contains(succ))
                        mssg = succ + "," + "END";
                } 
                else {
                    if (data[1].equals("GET")) {
                        //If this processor has the key
                        if (processor.getStoredKeys().contains(Integer.parseInt(data[2]))){
                            if (processorEdgeIds.contains(Integer.parseInt(data[3])))
                                mssg = data[3] + "," + "FOUND" + "," + data[2] + "," + processor.getId();
                        } else { 
                            //If processor doesn't have key but key and id mapped to same ring identifer
                            if (hash(id) == hashKey(key)){ 
                                //Key should have been stored locally
                                result = "Key " + data[2] + " not found\n";
                                if (processorEdgeIds.contains(succ))
                                    //Send end process message successor
                                    mssg = succ + "," + "END";
                            } else {
                                //If message id equals id of original processor, get request travelled back to originating processor, so key not found
                                if (Integer.parseInt(data[3]) == originalId) {
                                    result = "Key " + data[2] + " not found\n";
                                    if (processorEdgeIds.contains(succ)) 
                                        //Send end process message to the other processors
                                        mssg = succ + "," + "END";
                                } else { //Key not found
                                    if (processorEdgeIds.contains(Integer.parseInt(data[3])))
                                        mssg = data[3] + "," + "NOT_FOUND" + "," + data[2] + "," + processor.getId();
                                }
                            }
                        }
                    } else {
                        if (data[1].equals("LOOKUP")) {
                            //If this processor has the key
                            if (processor.getStoredKeys().contains(Integer.parseInt(data[2]))){
                                if (processorEdgeIds.contains(Integer.parseInt(data[3])))
                                    mssg = data[3] + "," + "FOUND" + "," + data[2] + "," + processor.getId();
                            } else {
                                //If processor doesn't have key but key and id mapped to same ring identifer
                                if (hash(id) == hashKey(key)){ 
                                    //Key should have been stored locally
                                    result = "Key " + data[2] + " not found\n";
                                    if (processorEdgeIds.contains(succ))
                                        //Send end process message to successor
                                        mssg = succ + "," + "END";
                                    
                                } else {  //Otherwise, current processor does not have key, so check if successor has the key
                                    //If the successor of the key is the same as the successor of the current processor, 
                                    //then the key must be in the segment between current processor and successor
                                    keySuccessor = getSuccessor(key);
                                    if (hash(keySuccessor[1]) == hash(succ)) {
                                        //If processor to send message to is connected to current processor
                                        if (processorEdgeIds.contains(fingerTable[keySuccessor[0]]))
                                            mssg = succ + "," + "GET" + "," + data[2] + "," + data[3];
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
                                result = "Key " + data[2] + " found at processor " + data[3] + "\n";
                                if (processorEdgeIds.contains(succ))
                                    //Send end process message to successor
                                    mssg = succ + "," + "END";
                            } else {
                                //If not found message is received
                                if (data[1].equals("NOT_FOUND")) { 
                                    result = "Key " + data[2] + " not found\n";
                                    if (processorEdgeIds.contains(succ))
                                        //Send end process message successor
                                        mssg = succ + "," + "END";
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Update finger table entries for all processors
     * @param id
     */
    public void fixFingers(int id) {
        int[] fingerTable;
        for (int proc_id: this.processors){
            if (proc_id != id){
                Node existingProcessor = network.findNode(proc_id);
                if (existingProcessor != null){
                    fingerTable = setFingers(existingProcessor.getId(), false);
                    existingProcessor.setFingerTable(fingerTable);
                }
            }
        }
    }

    /**
     * Get keys to be transferred between processors when a processor joins or leaves the network
     * @param processor
     * @param id
     * @return
     */
    public ArrayList<Integer> getTransferredKeys(Node processor, int id){
        ArrayList<Integer> transferredKeys = new ArrayList<>();
        for (int key: processor.getStoredKeys()){
            //Keys mapped to positions which are in the segment of the processor
            if (hash(processor.getId()) > hash(id)){
                if (hashKey(key) <= hash(id)) 
                    transferredKeys.add(key); //add key to processor
                else {
                    if (hashKey(key) > hash(id) && hashKey(key) > hash(processor.getId()))
                        transferredKeys.add(key); //add key to processor
                }
            } else if (hash(processor.getId()) == hash(id)){
                return new ArrayList<>();
            } else {
                if (hashKey(key) <= hash(id) && hashKey(key) > hash(processor.getId())) 
                    transferredKeys.add(key); //add key to processor
            }
        }
        return transferredKeys;
    }

    /**
     * Add a new processor and return the list of keys which are moved to the new processor
     * @param id
     * @return
     */
    public ArrayList<Integer> addProcessor(int id) {
        Node processor = network.findNode(id);
        //If found processor with the same id that is already in the network, then new processor should not be added
        if (processor != null) {
            return null;
        }

        //Create finger table for new processor that will join the network
        int[] fingerTable = setFingers(id, true);

        //Send a message to successor
        Node successor = network.findNode(fingerTable[0]);
        //When successor receives the message, it checks if it has crashed
        if (successor.getCrashedStatus()){
            int updatedSuccessor = successor.getProcSuccessor(); //Get the id of the successor of the crashed successor
            endProcessor(fingerTable[0], true); //Remove the crashed processor from the system
            successor = network.findNode(updatedSuccessor); //Once crashed processor removed, then get the updated successor
            fingerTable = setFingers(id, true); //If successor crashed, also update the finger table of processor that joined the network
        }

        //Get the keys from the successor that should belong to the new processor
        ArrayList<Integer> transferredKeys = getTransferredKeys(successor, id);

        //Remove keys from successor
        ArrayList<Integer> updatedStoredKeys = new ArrayList<>();
        for (int key: successor.getStoredKeys()) {
            if (!transferredKeys.contains(key))
                updatedStoredKeys.add(key);
        }
        successor.setStoredKeys(updatedStoredKeys);

        //Create new processor and add it to the network as a new processor
        Node newProcessor = new Node(id, transferredKeys, fingerTable);
        network.addNode(newProcessor);

        //Update processor list
        this.processors = network.getAllNodeIds();
        Collections.sort(this.processors); //Sort list of processor ids in increasing order

        //Update finger table of other processors
        fixFingers(id);
        return transferredKeys;
    }

    /**
     * Remove a processor from the system
     * @param id
     * @param isCrashed
     */
    public void endProcessor(int id, boolean isCrashed) {
        Node processor = network.findNode(id);
        if (processor != null) {
            //Remove processor from network
            network.removeNode(processor);
            //Update processor list
            this.processors = network.getAllNodeIds();
            Collections.sort(this.processors); //Sort list of processor ids in increasing order

            //Update finger table of other processors
            fixFingers(id);

            if (!isCrashed){
                //Get the keys from the removed processor and move them to the new appropriate successors
                for (int key: processor.getStoredKeys()){
                    int p = getSuccessor(key)[1];
                    Node succ = network.findNode(p);
                    if (succ != null){
                        //If the successor has crashed
                        if (succ.getCrashedStatus()){
                            int updatedSuccessor = succ.getProcSuccessor(); //Get the id of the successor of the crashed successor
                            endProcessor(p, true); //Remove the crashed processor from the system
                            succ = network.findNode(updatedSuccessor); //Once crashed processor removed, then get the updated successor
                        } else {
                            if (!succ.getStoredKeys().contains(key)) { //If processor for key to be moved to does not already contain the key
                                succ.addStoredKey(key);
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("Node not found\n");
        }
    }

    /**
     * Make a specified processor crash
     * @param id
     */
    public void crashProcessor(int id) {
        Node processor = network.findNode(id);
        if (processor != null) {
            processor.setCrashedStatus(true);
        }
    }
}