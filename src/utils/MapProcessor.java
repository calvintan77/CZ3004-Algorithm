package utils;

import Constants.MapConstants;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MapProcessor {
    public static float FORWARD_WEIGHT = 1;
    public static float TURNING_WEIGHT = 0;

    /**
     * Processes a map of the arena into a node representation. Multiple ending points and both starting orientations
     * are represented via a starting node and final node that are arbitrary and connect to the viable locations via 0 weight edges.
     * @param map - Map of arena
     * @param waypointX - X coord of waypoint
     * @param waypointY - Y coord of waypoint
     * @return A list of the relevant nodes
     */
    public static List<GraphNode> ProcessMapForFastestPath(Map map, int waypointX, int waypointY){
        List<Coordinate> Starting = new LinkedList<>();
        Starting.add(new Coordinate(1, 1));
        List<Coordinate> Ending = new LinkedList<>();
        Ending.add(new Coordinate(12, 17));
        Ending.add(new Coordinate(13, 17));
        Ending.add(new Coordinate(12, 18));
        Ending.add(new Coordinate(13, 18));
        return ProcessMap(map, Starting, Ending, new Coordinate(waypointX, waypointY));

    }

    public static List<GraphNode> ProcessMap(Map map, List<Coordinate> StartingPoints, List<Coordinate> EndingPoints){
        List<GraphNode> mapResult = ProcessMap(map, StartingPoints, EndingPoints, StartingPoints.get(0));
        mapResult.remove(3);
        mapResult.remove(2);
        return mapResult;
    }

    // hit at least 1 ending point 
    public static List<GraphNode> ProcessMap(Map map, List<Coordinate> StartingPoints, List<Coordinate> EndingPoints, Coordinate waypoint){
        GraphNode[][][] graph = new GraphNode[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT][2];
        for(int i = 0; i < MapConstants.MAP_WIDTH; i++){
            for(int j = 0; j < MapConstants.MAP_HEIGHT; j++){
                MapCell cell = map.getCell(i,j);
                if(cell.isObstacle() || cell.isVirtualWall() || !cell.isExplored()){
                    continue;
                }

                // Create nodes
                GraphNode horizGraphNode = new GraphNode(i, j, true);
                GraphNode vertGraphNode = new GraphNode(i, j, false);
                horizGraphNode.addNeighbour(vertGraphNode, TURNING_WEIGHT);
                vertGraphNode.addNeighbour(horizGraphNode, TURNING_WEIGHT);

                // Horizontal Neighbour
                if(i > 0 && graph[i-1][j][0] != null && !horizGraphNode.isNeighbour(graph[i-1][j][0])){
                    horizGraphNode.addNeighbour(graph[i-1][j][0], FORWARD_WEIGHT);
                    graph[i-1][j][0].addNeighbour(horizGraphNode, FORWARD_WEIGHT);
                }
                graph[i][j][0] = horizGraphNode;

                // Vertical Neighbour
                if(j > 0 && graph[i][j-1][1] != null && !vertGraphNode.isNeighbour(graph[i][j-1][1])){
                    vertGraphNode.addNeighbour(graph[i][j-1][1], FORWARD_WEIGHT);
                    graph[i][j-1][1].addNeighbour(vertGraphNode, FORWARD_WEIGHT);
                }
                graph[i][j][1] = vertGraphNode;
            }
        }
        //Format Start Zone
        GraphNode start = new GraphNode(0,0, true, true);
        for(Coordinate st: StartingPoints){
            if(graph[st.getX()][st.getY()][0] != null){
                ConnectNodesWithZero(graph, start, st);
            }
        }

        //Format End Zone
        GraphNode end = new GraphNode(14,19, true, true);
        for(Coordinate ed: EndingPoints){
            if(graph[ed.getX()][ed.getY()][0] != null) {
                ConnectNodesWithZero(graph, end, ed);
            }
        }
        return Arrays.asList(start, end, graph[waypoint.getX()][waypoint.getY()][0], graph[waypoint.getX()][waypoint.getY()][1]);
    }

    private static void ConnectNodesWithZero(GraphNode[][][] graph, GraphNode node1, Coordinate node2) {
        graph[node2.getX()][node2.getY()][0].addNeighbour(node1, 0f);
        graph[node2.getX()][node2.getY()][1].addNeighbour(node1, 0f);
        node1.addNeighbour( graph[node2.getX()][node2.getY()][0], 0f);
        node1.addNeighbour( graph[node2.getX()][node2.getY()][1], 0f);
    }
}
