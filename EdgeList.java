import java.util.*;

/*Corresponds to each entry in the adjacency list representation of the graph, 
which includes the node itself and the edges connected to it*/
public class EdgeList {
    private Node node;
    private ArrayList<Node> edges;
    public EdgeList(Node node, ArrayList<Node> edges) {
        this.node = node;
        this.edges = edges;
    }

    public Node getNode(){
        return this.node;
    }

    public ArrayList<Node> getEdges(){
        return this.edges;
    }
}
