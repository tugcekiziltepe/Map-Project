import java.util.*;


public class QuadTree {
    private final String imageRoot;
    public QTreeNode root;

    public QuadTree(String imageRoot) {
        // Instantiate the root element of the tree with depth 0
        // Use the ROOT_ULLAT, ROOT_ULLON, ROOT_LRLAT, ROOT_LRLON static variables of MapServer class
        root = new QTreeNode("root", MapServer.ROOT_ULLAT, MapServer.ROOT_ULLON, MapServer.ROOT_LRLAT, MapServer.ROOT_LRLON, 0);
        // Save the imageRoot value to the instance variable
        this.imageRoot = imageRoot;
        // Call the build method with depth 1
        build(root, 1);
    }

    public void build(QTreeNode subTreeRoot, int depth) {
        // Recursive method to build the tree as instructed
        String name = subTreeRoot.getName();
        //If depth is greater than 7
        if (depth > 7) return;
        //If name is "root", create empty string to name child nodes.
        if (subTreeRoot.getName().equals(root.getName())) {
            name = "";
        }

        //Create NW Node using subTreeRoot's Latitudes and Longitudes
        subTreeRoot.NW = new QTreeNode(name.concat("1"), subTreeRoot.getUpperLeftLatitude(), subTreeRoot.getUpperLeftLongtitude(),
                (subTreeRoot.getLowerRightLatitude() + subTreeRoot.getUpperLeftLatitude()) / 2,
                (subTreeRoot.getLowerRightLongtitude() + subTreeRoot.getUpperLeftLongtitude()) / 2, depth);
        //Create NE Node using subTreeRoot's Latitudes and Longitudes
        subTreeRoot.NE = new QTreeNode(name.concat("2"), subTreeRoot.getUpperLeftLatitude(),
                (subTreeRoot.getUpperLeftLongtitude() + subTreeRoot.getLowerRightLongtitude()) / 2,
                (subTreeRoot.getLowerRightLatitude() + subTreeRoot.getUpperLeftLatitude()) / 2, subTreeRoot.getLowerRightLongtitude(), depth);
        //Create SW Node using subTreeRoot's Latitudes and Longitudes
        subTreeRoot.SW = new QTreeNode(name.concat("3"), (subTreeRoot.getUpperLeftLatitude() + subTreeRoot.getLowerRightLatitude()) / 2,
                subTreeRoot.getUpperLeftLongtitude(),
                subTreeRoot.getLowerRightLatitude(), (subTreeRoot.getLowerRightLongtitude() + subTreeRoot.getUpperLeftLongtitude()) / 2, depth);
        //Create SE Node using subTreeRoot's Latitudes and Longitudes
        subTreeRoot.SE = new QTreeNode(name.concat("4"),
                (subTreeRoot.getUpperLeftLatitude() + subTreeRoot.getLowerRightLatitude()) / 2, (subTreeRoot.getUpperLeftLongtitude() + subTreeRoot.getLowerRightLongtitude()) / 2,
                subTreeRoot.getLowerRightLatitude(), subTreeRoot.getLowerRightLongtitude(), depth);

        //Call recursively build method with child nodes and depth + 1
        build(subTreeRoot.NW, depth + 1);
        build(subTreeRoot.NE, depth + 1);
        build(subTreeRoot.SW, depth + 1);
        build(subTreeRoot.SE, depth + 1);
    }

    public Map<String, Object> search(Map<String, Double> params) {
        /*
         * Parameters are:
         * "ullat": Upper left latitude of the query box
         * "ullon": Upper left longitude of the query box
         * "lrlat": Lower right latitude of the query box
         * "lrlon": Lower right longitude of the query box
         * */

        // Instantiate a QTreeNode to represent the query box defined by the parameters
        QTreeNode queryBox = new QTreeNode("", params.get("ullat"), params.get("ullon"), params.get("lrlat"), params.get("lrlon"), 0);
        ArrayList<QTreeNode> list = new ArrayList<QTreeNode>(); //Initialize ArrayList
        // Calculate the lonDpp value of the query box
        double lonDPP = (queryBox.getLowerRightLongtitude() - queryBox.getUpperLeftLongtitude()) / params.get("w");
        // Call the search() method with the query box and the lonDpp value
        search(queryBox, root, lonDPP, list);
        // Call and return the result of the getMap() method to return the acquired nodes in an appropriate way
        return getMap(list);

    }

    private Map<String, Object> getMap(ArrayList<QTreeNode> list) {
        Map<String, Object> map = new HashMap<>();

        // Check if the root intersects with the given query box
        //If list has no element query success is false.
        if (list.size() == 0) {
            map.put("query_success", false);
            return map;
        }

        // Use the get2D() method to get organized images in a 2D array
        map.put("render_grid", get2D(list));

        // Upper left latitude of the retrieved grid
        map.put("raster_ul_lat", list.get(0).getUpperLeftLatitude());

        // Upper left longitude of the retrieved grid
        map.put("raster_ul_lon", list.get(0).getUpperLeftLongtitude());

        // Upper lower right latitude of the retrieved grid
        map.put("raster_lr_lat", list.get(list.size() - 1).getLowerRightLatitude());

        // Upper lower right longitude of the retrieved grid
        map.put("raster_lr_lon", list.get(list.size() - 1).getLowerRightLongtitude());

        // Depth of the grid
        map.put("depth", list.get(0).getDepth());

        map.put("query_success", true);
        return map;
    }

    private String[][] get2D(ArrayList<QTreeNode> list) {

        //Sort nodes according to their upper left latitudes
        Collections.sort(list, new Comparator<QTreeNode>() {
            @Override
            public int compare(QTreeNode o1, QTreeNode o2) {
                if (o1.getUpperLeftLatitude() < o2.getUpperLeftLatitude()) return 1;
                else if (o1.getUpperLeftLatitude() > o2.getUpperLeftLatitude()) return -1;
                else return 0;

            }
        });

        int[] sizes = calculateSizes(list); //Get sizes
        String[][] images = new String[sizes[0]][sizes[1]];
        for (int i = 0; i < sizes[0]; i++) {
            for (int j = 0; j < sizes[1]; j++) {
                images[i][j] = imageRoot + list.get(i * (sizes[1]) + j).getName() + ".png"; //Add images to 2D array
            }
        }
        return images;
    }

    private int[] calculateSizes(ArrayList<QTreeNode> list) {
        HashSet<Double> lat = new HashSet<>();
        HashSet<Double> lon = new HashSet<Double>();

        //Find size using number of different lower right latitude and lower right longitude values
        for (QTreeNode node : list) {
            lat.add(node.getLowerRightLatitude());
            lon.add(node.getLowerRightLongtitude());
        }
        int[] size = new int[2];
        size[0] = lat.size();
        size[1] = lon.size();
        return size;
    }


    public void search(QTreeNode queryBox, QTreeNode tile, double lonDpp, ArrayList<QTreeNode> list) {
        //If there are more than one element is the list
        if (list.size() > 0) {
            //Check intersection and lonDPP conditions with the depth of first element
            if (checkIntersection(tile, queryBox) && tile.getLonDPP() <= lonDpp && list.get(0).getDepth() == tile.getDepth()) {
                list.add(tile); //Add this tile to the list
                return;
            }
        }
        //If there is no element in the list
        //Check intersection and lonDPP conditions
        else if (checkIntersection(tile, queryBox) && tile.getLonDPP() <= lonDpp) {
            list.add(tile); //Add this tile to the list
            return;
        }
        //Call child nodes
        if (tile.NW != null) {
            search(queryBox, tile.NW, lonDpp, list);
            search(queryBox, tile.NE, lonDpp, list);
            search(queryBox, tile.SW, lonDpp, list);
            search(queryBox, tile.SE, lonDpp, list);
        }
    }

    // Return true if two tiles are intersecting with each other
    public boolean checkIntersection(QTreeNode tile, QTreeNode queryBox) {

        // If one tile is on left side of other, return false
        if ((queryBox.getUpperLeftLongtitude() >= tile.getLowerRightLongtitude() ||
                tile.getUpperLeftLongtitude() >= queryBox.getLowerRightLongtitude())) return false;

        // // If one tile is above other, return false
        if (queryBox.getUpperLeftLatitude() <= tile.getLowerRightLatitude() ||
                (tile.getUpperLeftLatitude() <= queryBox.getLowerRightLatitude())) return false;

        return true;
    }
}