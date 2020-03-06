package path;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Nodes of the graph, with neighbours
 */
public class GraphNode {
    /**
     * Coordinates of the node
     */
    private final int x;
    private final int y;
    /**
     * Orientation of the node (True = Horizontal, False = Vertical)
     */
    private final boolean isHorizontal;

    private boolean isVirtual = false;
    /**
     * List of neighbours and the weights to get to them.
     */
    private final HashMap<GraphNode, Float> neighbours;

    /**
     * Constructor for node
     * @param x - x coordinate
     * @param y - y coordinate
     * @param isHorizontal - orientation
     */
    public GraphNode(int x, int y, boolean isHorizontal){
        this.x = x;
        this.y = y;
        this.isHorizontal = isHorizontal;
        neighbours = new HashMap<>();
    }

    /**
     * Constructor for node
     * @param x - x coordinate
     * @param y - y coordinate
     * @param isHorizontal - orientation
     * @param isVirtual - if it is start or end node
     */
    public GraphNode(int x, int y, boolean isHorizontal, boolean isVirtual){
        this.x = x;
        this.y = y;
        this.isHorizontal = isHorizontal;
        neighbours = new HashMap<>();
        this.isVirtual = isVirtual;
    }

    /**
     * Gets the x coordinate
     * @return x coordinate as int
     */
    public int getX(){
        return x;
    }

    /**
     * Gets the y coordinate
     * @return y coordinate as int
     */
    public int getY(){
        return y;
    }

    /**
     * Gets the orientation
     * @return True if Horizontal, False if Vertical
     */
    public boolean isHorizontal(){
        return isHorizontal;
    }

    /**
     * Gets if node is virtual
     * @return True if Virtual, False if Real
     */
    public boolean isVirtual(){
        return isVirtual;
    }

    /**
     * Adds a neighbour to the list with associated weight, with input validation
     * @param graphNode - Node to add
     * @param weight - Weight of edge
     * @return True if successful
     */
    public boolean addNeighbour(GraphNode graphNode, Float weight){
        if(!this.isVirtual && !graphNode.isVirtual) {
            if (graphNode.x == this.x && graphNode.y == this.y) {
                if (graphNode.isHorizontal == this.isHorizontal) {
                    return false;
                }
            }
            else if (getEuclideanDistanceTo(graphNode) != 1) {
                return false;
            }
        }
        neighbours.put(graphNode, weight);
        return true;
    }

    /**
     * Gets the list of neighbours
     * @return Pairs of Nodes and the weights to traverse to them
     */
    public Set<Map.Entry<GraphNode, Float>> getNeighbours(){
        return neighbours.entrySet();
    }

    public boolean isNeighbour(GraphNode graphNode){
        return neighbours.containsKey(graphNode);
    }

    /**
     * Calculates the euclidean distance to a node
     * @param graphNode - Node to compute distance to
     * @return Straight line distance to node as a double
     */
    public double getEuclideanDistanceTo(GraphNode graphNode){
        return Math.sqrt((this.x - graphNode.x)*(this.x - graphNode.x) + (this.y - graphNode.y) * (this.y - graphNode.y));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode graphNode = (GraphNode) o;
        return x == graphNode.x &&
                y == graphNode.y &&
                isHorizontal == graphNode.isHorizontal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, isHorizontal);
    }

    @Override
    public String toString(){
        return "X: " + this.getX() + ", Y: " + this.getY() + (this.isHorizontal?" Horizontal":" Vertical");
    }
}