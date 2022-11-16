import java.util.*;


public class Router {

    private static List<Vertex> stops = new ArrayList<>();
    private static Vertex start, end;

    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat, double destlon, double destlat) {
        // Return the shortest path between start and end points
        // Return ids of vertices as a linked list

        //Initialize priority queue with Comparator<Item>
        PriorityQueue<Item> unSettledNodes = new PriorityQueue<>(new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                if (o1.weight > o2.weight) return 1;
                else if (o1.weight == o2.weight) return 0;
                else return -1;
            }
        });

        //Initialise other structures
        HashMap<Long, Vertex> settledNodes = new HashMap<>();
        ArrayList<Long> unSettledNodeIds = new ArrayList<>();
        LinkedList<Long> path = new LinkedList<>(); //Initialize path
        Map<Long, Double> distTo = new HashMap<>(g.graph.getVertices().size());
        Map<Long, Edge> edgeTo = new HashMap<>(g.graph.getVertices().size());

        // Use g.closest() to get start and end vertices
        long sourceId = g.closest(stlon, stlat);
        long destinationId = g.closest(destlon, destlat);
        start = g.graph.getVertices().get(sourceId); //Start vertex
        end = g.graph.getVertices().get(destinationId); //End vertex

        //Add source to priority queue
        unSettledNodes.add(new Item(0.0, sourceId));
        //Add sourceId to the Ids
        unSettledNodeIds.add(sourceId);

        //Initialize distTo with positive infinity
        //Initialize edgeTo with null edges
        for (Map.Entry<Long, Vertex> entry : g.graph.getVertices().entrySet()) {
            distTo.put(entry.getKey(), Double.POSITIVE_INFINITY);
            edgeTo.put(entry.getKey(), null);
        }

        //Add sourceId with 0 weight
        distTo.put(sourceId, 0.0);

        //While priority queue has element
        while (unSettledNodes.size() != 0) {
            Long tempId = unSettledNodes.poll().id; //Get min weighted id
            unSettledNodeIds.remove(tempId); //Remove this id from unSettledNodeIds
            settledNodes.put(tempId, g.graph.getVertices().get(tempId)); //Put this into settledNodes
            //For every edges of tempId
            for (Edge edge : g.graph.getAdj().get(tempId)) {
                //Relax this edge
                relax(edge, distTo, edgeTo, unSettledNodes, unSettledNodeIds,  g);
            }
        }

        //Get path using edgeTo
        Long tempId = edgeTo.get(destinationId).getSource().getId();
        path.add(destinationId);
        path.add(tempId);
        while (true && edgeTo.get(tempId) != null) {
            tempId = edgeTo.get(tempId).getSource().getId();
            path.add(tempId); //Add this tempId to the path
            //If we reached sourceId, break
            if (tempId == sourceId) break;
        }
        //Reverse path
        Collections.reverse(path);
        return path;
    }

    private static void relax(Edge edge, Map<Long, Double> distTo, Map<Long, Edge> edgeTo, PriorityQueue<Item> unSettledNodes, ArrayList<Long> unSettledNodeIds, GraphDB graphDB) {
        Long source = edge.getSource().getId(); //source id
        Long destination = edge.getDestination().getId(); //destination id

        if (distTo.get(destination) > (distTo.get(source) + edge.getWeight())) {
            distTo.replace(destination, (distTo.get(source) + edge.getWeight()));
            edgeTo.replace(destination, edge);

            //If unSettledNodeIds has this destination vertex, already decrease key.
            if (unSettledNodeIds.contains(destination)) {
                Item tempItem = new Item();
                for (Item item : unSettledNodes) {
                    if (item.id == destination) {
                        tempItem = item;
                    }
                }
                unSettledNodes.remove(tempItem);
                unSettledNodes.add(new Item((distTo.get(source) + edge.getWeight()), destination));
            }
            //If unSettledNodeIds has this destination vertex, add this into priority queue.
            else {
                unSettledNodes.add(new Item((distTo.get(source) + edge.getWeight()), destination));
                unSettledNodeIds.add(destination);
            }
        }
    }

    public static LinkedList<Long> addStop(GraphDB g, double lat, double lon) {
        // Find the closest vertex to the stop coordinates using g.closest()

        LinkedList<Long> path = new LinkedList<>(); //Initialize path
        long closestPoint = g.closest(lon, lat); //Get closest point
        //Get initial start and end vertices
        Vertex startVertex = start;
        Vertex endVertex = end;

        //Add new stop to stop list
        stops.add(g.graph.getVertices().get(closestPoint));

        //Sort stops according to their distance to startVertex
        Collections.sort(stops, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                if(g.distance(o1, startVertex)  > g.distance(o2, startVertex))  return 1;
                else if (g.distance(o1, startVertex)  == g.distance(o2, startVertex))  return 0;
                else return -1;
            }
        });

        List<Vertex> tempLink = new ArrayList<>();
        tempLink.add(startVertex);
        tempLink.addAll(stops);
        tempLink.add(endVertex);

        for(int i = 0; i < tempLink.size() - 1 ; i ++){
            path.addAll(shortestPath(g, tempLink.get(i).getLng(), tempLink.get(i).getLat(), tempLink.get(i + 1).getLng(), tempLink.get(i +1 ).getLat()));
        }

        //Turn start and end vertex to initial state
        start = startVertex;
        end = endVertex;
        return path;
    }

    public static void clearRoute() {
        start = null;
        end = null;
        stops = new ArrayList<>();
    }

    //Item stores weight and id
    private static class Item {
        double weight = 0;
        Long id;

        public Item(double weight, Long id) {
            this.weight = weight;
            this.id = id;
        }

        public Item() {
        }
    }
}