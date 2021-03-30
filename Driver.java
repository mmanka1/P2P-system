public class Driver {
    public static void main(String[] args) {
        int[] processors = {3,1,8,6}; //List of processor ids
        int[] keys = {0,1,2,4,5,7,8,9,10,11}; //List of keys

        Network p2pNetwork = new Network(processors.length);
        p2pNetwork.buildNetwork(processors, keys);

        //Display network nodes and edges as an adjacency list representation
        System.out.println(p2pNetwork.getNetwork().toString());
    }
}
