import java.util.*;

public class ChordNetwork {
    private ArrayList<Integer> processors;
    private int sizeRing;
    private int m; //exponent to calculate sizeRing
    private Graph network;

    public ChordNetwork(ArrayList<Integer> processors, int numProcessors) {
        this.processors = processors;
        this.m = numProcessors;
    }
    
    public int hash(int id){
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
        for (int i = this.m-1; i >= 0; i--) {
            if (hash(fingerTable[i+1]) > hash(fingerTable[i])) {
                if (hash(k) >= hash(fingerTable[i]) && hash(k) < hash(fingerTable[i+1]))
                    return i;
            } 
            else { 
                if (hash(fingerTable[i]) > hash(fingerTable[i+1])){
                    if ((hash(k) >= hash(fingerTable[i]) && hash(k) > hash(fingerTable[i+1])) || hash(k) < hash(fingerTable[i+1])){
                        return i;
                    } 
                }
            }  
        }
        return -1;
    }

    private int[] getSuccessor(int k) {
        int[] result = new int[2];
        for (int i = 0; i < (this.m-1); i++) {
            if (hash(this.processors.get(i+1)) > hash(this.processors.get(i))) {
                if (hash(k) > hash(this.processors.get(i)) && hash(k) <= hash(this.processors.get(i+1))){
                    result[0] = i+1;
                    result[1] = hash(this.processors.get(i+1));
                    return result;
                } 
            } 
            if (hash(this.processors.get(i+1)) == hash(this.processors.get(i))) {
                if (hash(k) == hash(this.processors.get(i+1))) {
                    result[0] = i+1;
                    result[1] = hash(this.processors.get(i+1));
                    return result;
                }  
            }
            if (hash(this.processors.get(i)) > hash(this.processors.get(i+1))) {
                //processor[0] is i+1 and processor[i+1] is i
                if ((hash(k) > hash(this.processors.get(i)) && hash(k) > hash(this.processors.get(i+1))) || hash(k) <= hash(this.processors.get(i+1))){
                    result[0] = i+1;
                    result[1] = hash(this.processors.get(i+1));
                    return result;
                } 
            } 
            if ((i+1) == (this.processors.size() - 1)){
                //processor[0] is i+1 and processor[i+1] is i
                if ((hash(k) > hash(this.processors.get(0)) && hash(k) > hash(this.processors.get(i+1))) || hash(k) <= hash(this.processors.get(0))){
                    result[0] = 0;
                    result[1] = hash(this.processors.get(0));
                    return result;
                } 
            }  
        }
        return null;
    }

    private int findSuccessor(int id){
        //Initialization
        int id_known = this.processors.get(0);
        Node processor;
        String succCurrent = null; //successor for current known processor
        int[] fingerTable = null; //fingerTable for current known processor
        
        String succ = null;  //successor for new node

        String[] data;
        String mssg = id_known + "," + "LOOKUP_SUCC" + "," + id;
        String message = null;

        while (true){
            //SEND MESSAGE
            if (mssg != null) {
                //Send mssg
                id = Integer.parseInt(mssg.split(",")[0]);
                processor = network.findNode(id, this.sizeRing);
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
                    int[] newNodeSuccessor = getSuccessor(Integer.parseInt(data[2]));
                    //If the successor of the new node is the same as the successor of the current known processor
                    if (hash(newNodeSuccessor[1]) == hash(Integer.parseInt(succCurrent))) 
                        mssg = data[2] + "," + "FOUND_SUCC" + ","  + newNodeSuccessor[1];
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

    private int[] setFingers(int id, boolean newFingers){
        int[] fingers;
        if (newFingers) {
            fingers = new int[this.m + 2];
            fingers[0] = findSuccessor(id);
            for (int i = 1; i <= this.m; i++)
                fingers[i] = findSuccessor((hash(id) + exp(2, i)) % this.sizeRing);
            fingers[m+1] = hash(id);
        } else {
            fingers = new int[this.m + 1];
            for (int i = 0; i < this.m; i++)
                fingers[i] = getSuccessor((hash(id) + exp(2, i)) % this.sizeRing)[1];
            fingers[m] = hash(id);
        }
        return fingers;
    }

    public void buildNetwork(int[] keys) {
        Collections.sort(this.processors); //Sort list of processor ids in increasing order
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
        for (int processorId: dictKeySet){
            Node node = new Node(processorId, key_dict.get(processorId), setFingers(processorId, false));
            nodeList.add(node);
        }
        //Create peer-to-peer network
        this.network = new Graph(nodeList);
    }

    public Graph getNetwork() {
        return this.network;
    }

    //Return processor id where key was found
    //Params: 
    //(1) id of processor to start search at 
    //(2) key to search for
    public String findKey(int id, int key) {
        Node processor = network.findNode(id, this.sizeRing);  //Get processor corresponding to this processor id
        if (processor == null){ //Processor does not exist in the network
            return "Processor not found";
        }  
        //Processor has crashed, so set the initial processor to its successor instead
        if (processor.getCrashedStatus()){ 
            int updatedId = processor.getProcSuccessor();
            endProcessor(id, true); //Remove the crashed processor from the system
            id = updatedId;
            processor = network.findNode(id, this.sizeRing); //Once crashed processor removed, then get the updated successor
        } 

        //Initialization
        String[] data;
        String result = " ";
        int[] fingerTable = processor.getFingerTable();
        int succ = processor.getProcSuccessor();
        ArrayList<Integer> processorEdgeIds = network.findEdges(processor, sizeRing);  //Get edges associated with node
        String mssg = null;
        String message = null;
        int originalId = hash(id);

        //If this processor has the key
        if (processor.getStoredKeys().contains(key))
            return result = "Key " + key + " found locally at processor " + String.valueOf(processor.getId() + "\n");
        //If processor doesn't have key but key and id mapped to same ring identifer
        if (hash(id) == hash(key)){
            //Key should have been stored locally
            return "Key " + key + " not found\n";
        }
         
        //Otherwise, current processor does not have key, so check if successor has the key
        int[] keySuccessor = getSuccessor(key);
        //If the successor of the key is the same as the successor of the current processor, 
        //then the key must be in the segment between current processor and successor
        if (keySuccessor[1] == succ) {
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
                processor = network.findNode(id, this.sizeRing);
                //Processor does not exist in the network
                if (processor == null) { 
                    return "Processor not found during search\n"; 
                } 
                //Processor has crashed, so send message to its successor instead
                if (processor.getCrashedStatus()){ 
                    int updatedId = processor.getProcSuccessor();
                    endProcessor(id, true); //Remove the crashed processor from the system
                    originalId = hash(originalId); //Once crashed processor is removed, recompute the original id based on the size of the ring
                    id = updatedId;
                    processor = network.findNode(id, this.sizeRing); //Once crashed processor removed, then get the updated successor
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
                            if (processorEdgeIds.contains(hash(Integer.parseInt(data[3]))))
                                mssg = data[3] + "," + "FOUND" + "," + data[2] + "," + processor.getId();
                        } else { 
                            //If processor doesn't have key but key and id mapped to same ring identifer
                            if (hash(id) == hash(key)){ 
                                //Key should have been stored locally
                                result = "Key " + data[2] + " not found\n";
                                if (processorEdgeIds.contains(succ))
                                    //Send end process message successor
                                    mssg = succ + "," + "END";
                            } else {
                                //If message id equals id of original processor, get request travelled back to originating processor, so key not found
                                if (hash(Integer.parseInt(data[3])) == originalId) {
                                    result = "Key " + data[2] + " not found\n";
                                    if (processorEdgeIds.contains(succ)) 
                                        //Send end process message to the other processors
                                        mssg = succ + "," + "END";
                                } else { //Key not found
                                    if (processorEdgeIds.contains(hash(Integer.parseInt(data[3]))))
                                        mssg = data[3] + "," + "NOT_FOUND" + "," + data[2] + "," + processor.getId();
                                }
                            }
                        }
                    } else {
                        if (data[1].equals("LOOKUP")) {
                            //If this processor has the key
                            if (processor.getStoredKeys().contains(Integer.parseInt(data[2]))){
                                if (processorEdgeIds.contains(hash(Integer.parseInt(data[3]))))
                                    mssg = data[3] + "," + "FOUND" + "," + data[2] + "," + processor.getId();
                            } else {
                                //If processor doesn't have key but key and id mapped to same ring identifer
                                if (hash(id) == hash(key)){ 
                                    //Key should have been stored locally
                                    result = "Key " + data[2] + " not found\n";
                                    if (processorEdgeIds.contains(succ))
                                        //Send end process message to successor
                                        mssg = succ + "," + "END";
                                    
                                } else {  //Otherwise, current processor does not have key, so check if successor has the key
                                    //If the successor of the key is the same as the successor of the current processor, 
                                    //then the key must be in the segment between current processor and successor
                                    keySuccessor = getSuccessor(key);
                                    if (keySuccessor[1] == succ) {
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

    //Update finger table entries for all processors
    public void fixFingers(int id) {
        int[] fingerTable;
        for (int proc_id: this.processors){
            if (proc_id != id){
                Node existingProcessor = network.findNode(proc_id, this.sizeRing);
                if (existingProcessor != null){
                    fingerTable = setFingers(existingProcessor.getId(), false);
                    existingProcessor.setFingerTable(fingerTable);
                }
            }
        }
    }

    //Get keys to be transferred between processors when a processor joins or leaves the network
    public ArrayList<Integer> getTransferredKeys(Node processor, int id){
        ArrayList<Integer> transferredKeys = new ArrayList<>();
        for (int key: processor.getStoredKeys()){
            //Keys mapped to positions which are in the segment of the node
            if (hash(processor.getId()) > hash(id)){
                if (hash(key) <= hash(id)) 
                    transferredKeys.add(key); //add key to node
                else {
                    if (hash(key) > hash(id) && hash(key) > hash(processor.getId()))
                        transferredKeys.add(key); //add key to node
                }
            } else if (hash(processor.getId()) == hash(id)){
                return new ArrayList<>();
            } else {
                if (hash(key) <= hash(id) && hash(key) > hash(processor.getId())) 
                    transferredKeys.add(key); //add key to node
            }
        }
        return transferredKeys;
    }

    //Add a new processor and return the list of keys which are moved to the new processor
    public ArrayList<Integer> addProcessor(int id) {
        //Create finger table for new processor that will join the network
        int[] fingerTable = setFingers(id, true);
        Node successor = network.findNode(fingerTable[0], this.sizeRing);

        //If the successor has crashed
        if (successor.getCrashedStatus()){
            int updatedSuccessor = successor.getProcSuccessor(); //Get the id of the successor of the crashed successor
            endProcessor(fingerTable[0], true); //Remove the crashed processor from the system
            successor = network.findNode(updatedSuccessor, this.sizeRing); //Once crashed processor removed, then get the updated successor
            fingerTable = setFingers(id, true); //If successor crashed, also update the finger table of processor that joined the network
        }

        //Get the keys from the successor that should belong to the new processor
        ArrayList<Integer> transferredKeys = getTransferredKeys(successor, id);

        //Remove keys from successor and store them in the new processor
        ArrayList<Integer> updatedStoredKeys = new ArrayList<>();
        for (int key: successor.getStoredKeys()) {
            if (!transferredKeys.contains(key))
                updatedStoredKeys.add(key);
        }
        successor.setStoredKeys(updatedStoredKeys);

        //Create new node and add it to the network as a new processor
        Node newProcessor = new Node(id, transferredKeys, fingerTable);
        network.addNode(newProcessor);

        //Update processor list
        this.processors = network.getAllNodeIds();
        Collections.sort(this.processors); //Sort list of processor ids in increasing order

        //Update the number of processors to be in the network
        this.m++;
        //Update the size of the ring
        //If the new size of the ring is equal to or smaller than the largest processor id, then increase m to avoid collisions
        if (exp(2, this.m) <= this.processors.get(this.m-1))
        this.sizeRing = exp(2, this.m) + this.processors.get(this.m-1); //Ensure that the size of the ring is larger than largest processor id
        else this.sizeRing = exp(2, this.m);

        //Update finger table of other processors
        fixFingers(id);
        return transferredKeys;
    }

    //Remove a processor from the system
    public void endProcessor(int id, boolean isCrashed) {
        Node processor = network.findNode(id, this.sizeRing);
        if (processor != null) {
            //Remove processor from network
            network.removeNode(processor);
            //Update processor list
            this.processors = network.getAllNodeIds();
            Collections.sort(this.processors); //Sort list of processor ids in increasing order

            //Update size of the ring and the nodes in the network
            this.m--;
            //If the new size of the ring is equal to or smaller than the largest processor id, then increase m to avoid collisions
            if (exp(2, this.m) <= this.processors.get(this.m-1))
                this.sizeRing = exp(2, this.m) + this.processors.get(this.m-1); //Ensure that the size of the ring is larger than largest processor id
            else this.sizeRing = exp(2, this.m);

            //Update finger table of other nodes
            fixFingers(id);

            if (!isCrashed){
                //Get the keys from the removed processor and move them to the new appropriate successors
                for (int key: processor.getStoredKeys()){
                    int hp = getSuccessor(key)[1];
                    Node succ = network.findNode(hp, this.sizeRing);
                    if (succ != null){
                        //If the successor has crashed
                        if (succ.getCrashedStatus()){
                            int updatedSuccessor = succ.getProcSuccessor(); //Get the id of the successor of the crashed successor
                            endProcessor(hp, true); //Remove the crashed processor from the system
                            succ = network.findNode(updatedSuccessor, this.sizeRing); //Once crashed processor removed, then get the updated successor
                        } else {
                            if (!succ.getStoredKeys().contains(key)) { //If processor for key to be moved to does not already contain the key
                                succ.addStoredKey(key);
                            }
                        }
                    }
                }
            }
        }
    }

    //Make a specified processor crash
    public void crashProcessor(int id) {
        Node processor = network.findNode(id, this.sizeRing);
        if (processor != null) {
            processor.setCrashedStatus(true);
        }
    }
}