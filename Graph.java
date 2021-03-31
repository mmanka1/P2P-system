import java.util.*;

/*Graph representation of the network using adjacency list*/
public class Graph {
    private List<EdgeList> adjList = new ArrayList<>();
    private ArrayList<Node> nodes;

    public Graph(ArrayList<Node> nodes){
        this.nodes = nodes;
        for (Node n: nodes){
            this.adjList.add(new EdgeList(n, setNodeEdges(n)));
        } 
    }
    
    //Filter to get just the edges for each node
    private ArrayList<Node> setNodeEdges(Node node) {
        ArrayList<Node> edges = new ArrayList<>();
        for (Node n: this.nodes) {
            if (n.getId() != node.getId()) 
                edges.add(n);
        }
        return edges; 
    }

    public Node findNode(int id){
        for (Node n: nodes){
            if (n.getId() == id) {
                return n;
            }
        }
        return null; //Node not found
    }

    //Return the edges as integer ids connected to the specified node
    public ArrayList<Integer> findEdges(Node node){
        ArrayList<Integer> edgeIds = new ArrayList<>();
        for (EdgeList edgeList: adjList){
            if (edgeList.getNode().equals(node)){
                for (Node n: edgeList.getEdges()){
                    edgeIds.add(n.getId());
                }
            }
        }
        return edgeIds;
    }

    @Override
    public String toString(){
        String result = "";
        for(EdgeList el: this.adjList){
            result += el.getNode().getId() + ":" + el.getNode().getFingerTable()[2] + " => ";
            for(Node n: el.getEdges())
                result += n.getId() + ", ";
            result += "\n";
        }
        return result;
    }
}
