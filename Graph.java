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

    public Node findNode(int id, int size){
        for (Node n: nodes){
            if ((n.getId() % size) == id || n.getId() == id) {
                return n;
            }
        }
        return null; //Node not found
    }

    public void addNode(Node node){
        this.adjList.add(new EdgeList(node, setNodeEdges(node)));
        this.nodes.add(node);
        for (EdgeList edgeList: this.adjList) {
            if (!edgeList.getNode().equals(node)){
                edgeList.setEdge(node);
            }
        }
    }

    public void removeNode(Node node){
        //Remove node from nodes
        for(Node n: this.nodes){
            if (node.equals(n)){
                this.nodes.remove(n);
                break;
            }
        }
        //Remove edgelist from adjacency list containing the node
        for(EdgeList edgeList: this.adjList){
            if (edgeList.getNode().equals(node)){
                this.adjList.remove(edgeList);
                break;
            }
        }
        //For the remaining edgelist entries, remove the node from the other edgelist edges
        for(EdgeList edgeList: this.adjList){
            //If edges contains the node to be removed, then iterate over that list of edges to find such node
            if (edgeList.getEdges().contains(node)){
                for (Node edge: edgeList.getEdges()){
                    //Once node found in the list, remove it
                    if (edge.equals(node)){
                        edgeList.getEdges().remove(edge);
                        break;
                    }
                }
            }
        }
    }

    //Return the ids of all nodes in the graph
    public ArrayList<Integer> getAllNodeIds(){
        ArrayList<Integer> nodeIds = new ArrayList<>();
        for (Node n: this.nodes) {
            nodeIds.add(n.getId());
        }
        return nodeIds;
    }


    //Return the edges as integer ids connected to the specified node
    public ArrayList<Integer> findEdges(Node node, int size){
        ArrayList<Integer> edgeIds = new ArrayList<>();
        for (EdgeList edgeList: this.adjList){
            if (edgeList.getNode().equals(node)){
                for (Node n: edgeList.getEdges()){
                    edgeIds.add(n.getId() % size);
                }
            }
        }
        return edgeIds;
    }

    @Override
    public String toString(){
        String result = "";
        for(EdgeList el: this.adjList){
            result += el.getNode().getId() + ";" + el.getNode().getProcSuccessor() + ":" + el.getNode().getStoredKeys() + " => ";
            for(Node n: el.getEdges())
                result += n.getId() + ", ";
            result += "\n";
        }
        for(EdgeList el: this.adjList){
            result += el.getNode().getId() + ":";
            for(int finger: el.getNode().getFingerTable()) 
                result += finger + ", ";
            result += " => ";
            for(Node n: el.getEdges())
                result += n.getId() + ", ";
            result += "\n";
        }
        return result;
    }
}