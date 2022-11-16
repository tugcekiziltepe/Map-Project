import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;


public class GraphDB {

    public Graph graph = new Graph();
    public TST<Vertex> tst = new TST<>();

    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    static String normalizeString(String s) {
        String regex = "/\\s+|[^a-zA-Z]+|\\W|\\r/"; //Match all strings that are not alphabetical
        return s.replaceAll(regex, "").toLowerCase();
    }

    private void clean() {
        // Remove the vertices with no incoming and outgoing connections from your graph
        ArrayList<Long> removedVertices = new ArrayList<>();
        HashSet<Long> notRemovedVertices = new HashSet<>();

        //Get vertices which have incoming or outgoing edges
        for (Map.Entry<Long, ArrayList<Edge>> entry : graph.getAdj().entrySet()) {
            for (Edge edge : entry.getValue()) {
                notRemovedVertices.add(edge.getSource().getId());
                notRemovedVertices.add(edge.getDestination().getId());
            }
        }
        //Find other vertices which do not have incoming or outgoing edges
        for (Map.Entry<Long, ArrayList<Edge>> entry : graph.getAdj().entrySet()) {
            if (!notRemovedVertices.contains(entry.getKey())) {
                removedVertices.add(entry.getKey());
            }
        }

        //Remove these vertices
        for (Long id : removedVertices) {
            graph.getAdj().remove(id);
            graph.getVertices().remove(id);
        }
    }

    public double distance(Vertex v1, Vertex v2) {
        // Return the euclidean distance between two vertices
        return Math.sqrt(Math.pow(v1.getLng() - v2.getLng(), 2) + Math.pow((v1.getLat() - v2.getLat()), 2));
    }


    public long closest(double lon, double lat) {
        // Returns the closest vertex to the given latitude and longitude values
        //Get first vertex in vertices list
        Vertex firstVertex = (Vertex) graph.getVertices().values().toArray()[0];
        long resultId = firstVertex.getId(); //Initially result is first index.
        //Get square longitude difference of two coordinates
        double lonDifference = Math.pow(firstVertex.getLng() - lon, 2);
        //Get square latitude difference of two coordinates
        double latDifference = Math.pow(firstVertex.getLat() - lat, 2);
        //Get distance by taking square of distances
        double distance = Math.sqrt(lonDifference + latDifference);

        //For every vertex in graph
        for (Map.Entry<Long, Vertex> entry : graph.getVertices().entrySet()) {
            //Calculate temp distance
            double tempLonDifference = Math.pow(entry.getValue().getLng() - lon, 2);
            double tempLatDifference = Math.pow(entry.getValue().getLat() - lat, 2);
            double lastDistance = Math.sqrt(tempLonDifference + tempLatDifference);
            //If lastly found distance is greater than current distance
            if (lastDistance <= distance) {
                //Update distance
                distance = lastDistance;
                //Update result id
                resultId = entry.getKey();
            }
        }
        return resultId;
    }

    double lon(long v) {
        // Returns the longitude of the given vertex, v is the vertex id(
        return graph.getVertices().get(v).getLng();
    }


    double lat(long v) {
        // Returns the latitude of the given vertex, v is the vertex id
        return graph.getVertices().get(v).getLat();
    }
}