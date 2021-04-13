import java.util.*;

/**
 * Corresponds to each entry in the adjacency list representation of the graph, 
which includes the node itself and the edges connected to it
*/
public class EdgeList {
    private Node node;
    private ArrayList<Node> edges;
    /**
     * 
     * @param node
     * @param edges
     */
    public EdgeList(Node node, ArrayList<Node> edges) {
        this.node = node;
        this.edges = edges;
    }

    /**
     * 
     * @return node
     */
    public Node getNode(){
        return this.node;
    }

    /**
     * 
     * @return edges
     */
    public ArrayList<Node> getEdges(){
        return this.edges;
    }

    /**
     * 
     * @param node
     */
    public void setEdge(Node node){
        this.edges.add(node);   
    }

}
