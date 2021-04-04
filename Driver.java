import java.util.ArrayList;

public class Driver {
    public static void main(String[] args) {
        ArrayList<Integer> processors = new ArrayList<>(); //List of processor ids
        processors.add(3);
        processors.add(1);
        processors.add(8);
        processors.add(6);

        int[] keys = {0,1,2,4,5,7,8,9,10,11}; //List of keys

        ChordNetwork p2pNetwork = new ChordNetwork(processors, processors.size());
        p2pNetwork.buildNetwork(keys);

        p2pNetwork.addProcessor(5);
        p2pNetwork.addProcessor(0);
        p2pNetwork.endProcessor(5);
        p2pNetwork.endProcessor(1);
        p2pNetwork.endProcessor(0);  
        // p2pNetwork.endProcessor(3);    

        //Display network nodes and edges as an adjacency list representation
        System.out.println(p2pNetwork.getNetwork().toString());

        // System.out.println(p2pNetwork.findKey(6,7));   
    }
}
