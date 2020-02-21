package utils;

import Algorithms.AStarAlgo;

import java.util.*;


public class Graph {
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
        this(MapProcessor.ProcessMapForFastestPath(map, waypointX, waypointY));
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
        return pathWPHoriz.getWeight() < pathWPVert.getWeight()? pathWPHoriz: pathWPVert;
    }

    /**
     * Finds the shortest path through a waypoint by breaking it into two A* searches and combining them
     * @param waypoint - Waypoint node to path through
     * @return ShortestPath object containing the combined path and path length.
     */
    private ShortestPath GetShortestPathThroughWaypoint(GraphNode waypoint){
        try {
            ShortestPath toWaypoint = AStarAlgo.AStarSearch(start, waypoint);
            ShortestPath fromWaypoint =AStarAlgo.AStarSearch(waypoint, end);
            List<GraphNode> path1 = toWaypoint.getPath();
            List<GraphNode> path2 = fromWaypoint.getPath();
            if(path1.size() >= 2 && path2.size() >= 2 && path1.get(path1.size()-2).equals(path2.get(1))) {
                if(waypoint.isVirtual()){
                    path1.addAll(path2.subList(1, fromWaypoint.getPath().size()));
                    return new ShortestPath(toWaypoint.getWeight() + fromWaypoint.getWeight(), path1);
                }
                Optional<java.util.Map.Entry<GraphNode, Float>> turningPoint = waypoint.getNeighbours().stream().filter(node ->
                        node.getKey().getX()==waypoint.getX()
                                && node.getKey().getY() == waypoint.getY()
                                && (node.getKey().isHorizontal() != waypoint.isHorizontal())).findFirst();
                if(turningPoint.isPresent()) {
                    path1.add(turningPoint.get().getKey());
                    path1.addAll(path2);
                    return new ShortestPath(toWaypoint.getWeight() + fromWaypoint.getWeight() + 2 * turningPoint.get().getValue(), path1);
                }else{
                    throw new Exception("No turning nodes!");
                }
            }else{
                path1.addAll(path2.subList(1, fromWaypoint.getPath().size()));
                return new ShortestPath(toWaypoint.getWeight() + fromWaypoint.getWeight(), path1);
            }
        }catch(Exception e){
            //TODO Handle Gracefully
            return null;
        }
    }
}