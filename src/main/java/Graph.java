import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
    private HashMap<Long, ArrayList<Edge>> adj; //Adjacency List with ID and directed edges
    private HashMap<Long, Vertex> vertices; //Vertices in the graph

    public Graph() {
        setAdj(new HashMap<Long, ArrayList<Edge>>());
        setVertices(new HashMap<Long, Vertex>());
    }

    //This method adds given edge to graph
    public void addEdge(Edge edge) {
        //If source vertex is not in the vertices, add this to the vertices
        if (!getVertices().containsKey(edge.getSource().getId()))
            getVertices().put(edge.getSource().getId(), edge.getSource());
        //If destination vertex is not in the vertices, add this to the vertices
        if (!getVertices().containsKey(edge.getDestination().getId()))
            getVertices().put(edge.getDestination().getId(), edge.getDestination());
        getAdj().get(edge.getSource().getId()).add(0, edge);

    }

    //Getter-Setter
    public HashMap<Long, ArrayList<Edge>> getAdj() {
        return adj;
    }

    public void setAdj(HashMap<Long, ArrayList<Edge>> adj) {
        this.adj = adj;
    }

    public HashMap<Long, Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(HashMap<Long, Vertex> vertices) {
        this.vertices = vertices;
    }
}