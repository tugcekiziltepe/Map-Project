import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class GraphBuildingHandler extends DefaultHandler {


    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));
    private final GraphDB g;
    private String activeState = "";
    private Way currentWay = new Way();
    private boolean flag = false;
    private Vertex currentVertex; //Temporary vertex to store node

    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equals("node")) {
            // For the case of new nodes, add a new vertex to the graph and save other necessary information
            activeState = "node"; // Set active state to "node"
            long id = Long.parseLong(attributes.getValue("id"));
            double lon = Double.parseDouble(attributes.getValue("lon"));
            double lat = Double.parseDouble(attributes.getValue("lat"));
            g.graph.getAdj().put(id, new ArrayList<Edge>());  //Initialize vertex's adjacency list.
            currentVertex = new Vertex(lat, lon, id); //Create new vertex
            //Add this vertex to the graph
            g.graph.getVertices().put(id, currentVertex);

        } else if (qName.equals("way")) {
            // The first one is encountering a new "way" start tag
            activeState = "way"; //Active state is "way"
            currentWay = new Way(); //Initialize new way
            currentWay.setId(attributes.getValue("id")); //Save way's id.
        } else if (activeState.equals("way") && qName.equals("nd")) {
            // The second scenario is encountering a new "nd" tag while the active state is "way". This
            // tag references a node in the current way by its node id. When this tag is encountered,

            //Add id to the listOfNodes in that way.
            currentWay.getListOfNodes().add(Long.parseLong(attributes.getValue("ref")));
        } else if (activeState.equals("way") && qName.equals("tag")) {
            //If active state is "way" and encountered with "tag"

            String k = attributes.getValue("k");
            String v = attributes.getValue("v");

            if (k.equals("maxspeed")) {
                // Set the speed for the current way
                currentWay.setSpeed(v);
            } else if (k.equals("highway")) {
                //  if the parsed highway type is specified to be allowed in the list ALLOWED_HIGHWAY_TYPES.
                if (ALLOWED_HIGHWAY_TYPES.contains(v)) {
                    //Set flag to true
                    flag = true;
                }
            } else if (k.equals("name")) {
                // Set the name for the current way
                currentWay.setName(v);
            } else if (k.equals("oneway")) {
                // Set the oneway property for the current way
                currentWay.isOneWay = v.equals("yes");
            }

        } else if (activeState.equals("node") && qName.equals("tag") && attributes.getValue("k")
                .equals("name")) {
            // For the case of encountering a "tag" tag with the attribute "name" while the active state is "node"

            currentVertex.setName(attributes.getValue("v")); //Set current vertex's name with not normalized string
            // Put this vertex to ternary search tree with normalized version of name
            String name = GraphDB.normalizeString(attributes.getValue("v")); //Normalize string
            g.tst.put(name, currentVertex);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // Iff the way is marked as a valid, connect the vertices specified by the list of references in the listOfNodes.
        if (qName.equals("way")) {
            if (flag) {
                //If way is oneway
                if (currentWay.isOneWay) {
                    //Traverse listOfNodes and create edges and add them to the graph in one way
                    for (int i = 0; i < currentWay.getListOfNodes().size() - 1; i++) {
                        long firstVertexId = currentWay.getListOfNodes().get(i);
                        long secondVertexId = currentWay.getListOfNodes().get(i + 1);
                        Vertex firstVertex = g.graph.getVertices().get(firstVertexId);
                        Vertex secondVertex = g.graph.getVertices().get(secondVertexId);
                        double weight = g.distance(firstVertex, secondVertex);
                        Edge edge = new Edge(firstVertex, secondVertex, currentWay.speed, weight);
                        g.graph.addEdge(edge);
                    }
                }
                //If way is not oneway
                else {
                    //Traverse listOfNodes and create edges and add them to the graph in two way
                    for (int i = 0; i < currentWay.getListOfNodes().size() - 1; i++) {

                        long firstVertexId = currentWay.getListOfNodes().get(i);
                        long secondVertexId = currentWay.getListOfNodes().get(i + 1);
                        Vertex firstVertex = g.graph.getVertices().get(firstVertexId);
                        Vertex secondVertex = g.graph.getVertices().get(secondVertexId);
                        double weight = g.distance(firstVertex, secondVertex);
                        Edge edge = new Edge(firstVertex, secondVertex, currentWay.speed, weight);
                        g.graph.addEdge(edge);
                        edge = new Edge(secondVertex, firstVertex, currentWay.speed, weight);
                        g.graph.addEdge(edge);
                    }
                }
                flag = false;
            }
        }

    }


    public static class Way {
        private final ArrayList<Long> listOfNodes = new ArrayList<>();
        private String id;
        private String name;
        private String speed;
        private boolean isOneWay;

        //Getter-Setter Methods
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public ArrayList<Long> getListOfNodes() {
            return listOfNodes;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isOneWay() {
            return this.isOneWay;
        }

        public void setOneWay(boolean oneway) {
            this.isOneWay = oneway;
        }
    }
}