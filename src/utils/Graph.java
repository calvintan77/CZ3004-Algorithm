package utils;

import Constants.MapConstants;

import java.util.*;
import java.util.Map.Entry;


public class Graph {
    public static float FORWARD_WEIGHT = 1;
    public static float TURNING_WEIGHT = 0;
    private GraphNode start;
    private GraphNode waypointHoriz;
    private GraphNode waypointVert;
    private GraphNode end;

    /**
     * This is the constructor from Map
     * @param map - A map of the arena
     * @param waypointX - X coord of waypoint
     * @param waypointY - Y coord of waypoint
     */
    public Graph(Map map, int waypointX, int waypointY){
        this(ProcessMap(map, waypointX, waypointY));
    }

    /**
     * Hacky solution to process list entries
     * @param ls - List of nodes to pass into other constructor
     */
    private Graph(List<GraphNode> ls){
        this(ls.get(0), ls.get(1), ls.get(2), ls.get(3));
    }

    /**
     * Proper constructor without any work done (can be used for testing)
     * @param start - Starting node
     * @param end - Ending node
     * @param waypointHoriz - Horizontal node of waypoint
     * @param waypointVert - Vertical node of waypoint
     */
    public Graph(GraphNode start, GraphNode end, GraphNode waypointHoriz, GraphNode waypointVert){
        this.start = start;
        this.end = end;
        this.waypointHoriz = waypointHoriz;
        this.waypointVert = waypointVert;
    }

    /**
     * Finds the shortest path from the start to the end in the graph going through the waypoint.
     * Tries entering the waypoint Vertically and Horizontally to find the minimum path between the two.
     * @return ShortestPath object containing path length and a List of nodes as path.
     */
    public ShortestPath GetShortestPath(){
        ShortestPath pathWPHoriz = GetShortestPathThroughWaypoint(waypointHoriz);
        ShortestPath pathWPVert = GetShortestPathThroughWaypoint(waypointVert);
        return pathWPHoriz.weight < pathWPVert.weight? pathWPHoriz: pathWPVert;
    }

    /**
     * Finds teh shortest path through a waypoint by breaking it into two A* searches and combining them
     * @param waypoint - Waypoint node to path through
     * @return ShortestPath object containing the combined path and path length.
     */
    public ShortestPath GetShortestPathThroughWaypoint(GraphNode waypoint){
        try {
            ShortestPath toWaypoint = AStarSearch(start, waypoint);
            ShortestPath fromWaypoint = AStarSearch(waypoint, end);
            List<GraphNode> path = toWaypoint.path;
            path.addAll(fromWaypoint.path.subList(1, fromWaypoint.path.size()));
            return new ShortestPath(toWaypoint.weight + fromWaypoint.weight, path);
        }catch(Exception e){
            //TODO Handle Gracefully
            return null;
        }
    }

    /**
     * Heuristic search to find the shortest path
     * @param source - Source node
     * @param destination - Destination node
     * @return ShortestPath object containing path length and List of nodes as path
     * @throws Exception - unable to find path from source to destination
     */
    private ShortestPath AStarSearch(GraphNode source, GraphNode destination) throws Exception {
        PriorityQueue<AStarNode> toExplore = new PriorityQueue<>(10,
                Comparator.comparingDouble(AStarNode::getWeight));
        HashMap<GraphNode, AStarNode> explored = new HashMap<>();
        AStarNode first = new AStarNode(0, source);
        AStarNode curr;
        toExplore.add(first);
        while(!toExplore.isEmpty()){
            curr = toExplore.poll();
            if(curr.getGraphNode() == destination){
                return BackTrack(explored, destination);
            }
            for (Entry<GraphNode, Float> pair: start.getNeighbours()) {
                double estimatedWeight = curr.getWeight() + pair.getValue() + pair.getKey().getEuclideanDistanceTo(destination);
                if(!explored.containsKey(pair.getKey())) {
                    AStarNode node = new AStarNode(estimatedWeight, pair.getKey());
                    node.setParent(curr);
                    explored.put(pair.getKey(), node);
                    toExplore.add(node);
                }else {
                    AStarNode existing = explored.get(pair.getKey());
                    if (existing.getWeight() > estimatedWeight){
                        toExplore.remove(existing);
                        existing.setWeight(estimatedWeight);
                        existing.setParent(curr);
                        toExplore.add(existing);
                    }
                }
            }
        }
        throw new Exception();
    }

    /**
     * Backtracks a Hashmap of explored nodes to find the shortest path
     * @param explored - Hashmap of explored nodes
     * @param destination - Destination node
     * @return ShortestPath object containing path length as well as a List of nodes representing the path
     */
    private ShortestPath BackTrack(HashMap<GraphNode, AStarNode> explored, GraphNode destination) {
        AStarNode curr = explored.get(destination);
        List<GraphNode> result = new LinkedList<>();
        while(curr.getParent() != null){
            result.add(0, curr.graphNode);
            curr = curr.parent;
        }
        return new ShortestPath(explored.get(destination).weight, result);
    }

    /**
     * Wrapper around a graph node to add extra properties such as parent
     */
    private static class AStarNode {
        private double weight;
        private GraphNode graphNode;
        private AStarNode parent = null;
        public AStarNode(double weight, GraphNode graphNode){
            this.graphNode = graphNode;
            this.weight = weight;
        }
        public double getWeight(){
            return weight;
        }
        public void setWeight(double weight){
            this.weight = weight;
        }
        public AStarNode getParent(){
            return parent;
        }
        public void setParent(AStarNode parent){
            this.parent = parent;
        }
        public GraphNode getGraphNode(){
            return graphNode;
        }
    }

    /**
     * Class to represent shortest path result
     */
    private static class ShortestPath{
        private double weight;
        private List<GraphNode> path;

        public ShortestPath(double weight, List<GraphNode> path) {
            this.weight = weight;
            this.path = path;
        }
    }

    /**
     * Processes a map of the arena into a node representation
     * @param map - Map of arena
     * @param waypointX - X coord of waypoint
     * @param waypointY - Y coord of waypoint
     * @return A list of the relevant nodes
     */
    private static List<GraphNode> ProcessMap(Map map, int waypointX, int waypointY){
        GraphNode[][][] graph = new GraphNode[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT][2];
        for(int i = 0; i < MapConstants.MAP_WIDTH; i++){
            for(int j = 0; j < MapConstants.MAP_HEIGHT; j++){
                MapCell cell = map.getCell(i,j);
                if(cell.isObstacle() || cell.isVirtualWall()){
                    continue;
                }

                // Create nodes
                GraphNode horizGraphNode = new GraphNode(i, j, true);
                GraphNode vertGraphNode = new GraphNode(i,j, false);
                horizGraphNode.addNeighbour(vertGraphNode, TURNING_WEIGHT);
                vertGraphNode.addNeighbour(horizGraphNode, TURNING_WEIGHT);

                // Horizontal Neighbour
                if(i > 0 && !horizGraphNode.isNeighbour(graph[i-1][j][0])){
                    horizGraphNode.addNeighbour(graph[i-1][j][0], FORWARD_WEIGHT);
                    graph[i-1][j][0].addNeighbour(horizGraphNode, FORWARD_WEIGHT);
                }
                graph[i][j][0] = horizGraphNode;

                // Vertical Neighbour
                if(j > 0 && !vertGraphNode.isNeighbour(graph[i][j-1][1])){
                    vertGraphNode.addNeighbour(graph[i][j-1][1], FORWARD_WEIGHT);
                    graph[i][j-1][1].addNeighbour(vertGraphNode, FORWARD_WEIGHT);
                }
                graph[i][j][0] = vertGraphNode;
            }
        }
        //Format Start Zone
        GraphNode start = new GraphNode(0,0, true);
        graph[1][1][0].addNeighbour(start, 0f);
        graph[1][1][1].addNeighbour(start, 0f);

        //Format End Zone
        GraphNode end = new GraphNode(19,19, true);
        graph[12][17][0].addNeighbour(end, 0f);
        graph[12][17][1].addNeighbour(end, 0f);
        graph[13][17][0].addNeighbour(end, 0f);
        graph[13][17][1].addNeighbour(end, 0f);
        graph[12][18][0].addNeighbour(end, 0f);
        graph[12][18][1].addNeighbour(end, 0f);
        graph[13][18][0].addNeighbour(end, 0f);
        graph[13][18][1].addNeighbour(end, 0f);

        return Arrays.asList(start, end, graph[waypointX][waypointY][0], graph[waypointX][waypointY][1]);
    }
}
