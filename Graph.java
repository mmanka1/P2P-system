import java.util.*;

/*Graph representation of the network*/
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

    public ArrayList<Node> findEdges(Node node){
        for (EdgeList edgeList: adjList){
            if (edgeList.getNode().equals(node)){
                return edgeList.getEdges();
            }
        }
        return null;
    }

    @Override
    public String toString(){
        String result = "";
        for(EdgeList el: this.adjList){
            result += el.getNode().getId() + ":" + el.getNode().getStoredKeys() + " => ";
            for(Node n: el.getEdges())
                result += n.getId() + ", ";
            result += "\n";
        }
        return result;
    }
}
