import java.util.*;

/*Graph representation of the network*/
public class Graph {
    private List<EdgeList> adjList = new ArrayList<>();
    public Graph(ArrayList<Node> nodes){
        for (Node n: nodes){
            this.adjList.add(new EdgeList(n, getNodeEdges(nodes, n)));
        } 
    }
    
    //Filter to get just the edges for each node
    private ArrayList<Node> getNodeEdges(ArrayList<Node> nodes, Node node) {
        ArrayList<Node> edges = new ArrayList<>();
        for (Node n: nodes) {
            if (n.getId() != node.getId()) 
                edges.add(n);
        }
        return edges; 
    }

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
