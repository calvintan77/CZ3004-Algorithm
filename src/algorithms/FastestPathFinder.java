package algorithms;

import maze.Map;
import path.Graph;
import path.ShortestPath;
import robot.AbstractRobot;
import utils.*;

import java.util.ArrayList;
import java.util.List;

public class FastestPathFinder {
    private final Map map;
    public FastestPathFinder(Map map){        // Set up a blocked path
        if (map.getSeenPercentage() < 100) {
            this.map = map.CloneWithUnseenAsObstacles();
        }else {
            this.map = map.clone();
        }
    }

    public List<RobotCommand> GetFastestPath(AbstractRobot robot, Coordinate waypoint){
        try{
            ShortestPath result;
            // Find a path to the goal
            do {
                Graph graph = new Graph(map, waypoint.getX(), waypoint.getY());
                result = graph.GetShortestPath();
                if (result == null) {
                    System.out.println("Unable to find path through waypoint");
                    graph = new Graph(map, 1, 1);
                    result = graph.GetShortestPath();
                }
                if (result == null) {
                    map.expandSearchSpace();
                }
            }while (result == null && !map.getAllUnseen().isEmpty());

            //TODO: Figure out how to draw path
//            List<GraphNode> path = result.getPath();
//            for(GraphNode n: path){
//                if (!( (n.getX()==0 && n.getY()==0) ||
//                        (n.getX()== MapConstants.MAP_WIDTH-1 && n.getY()==MapConstants.MAP_HEIGHT-1) ))
//                    gui.setMazeGridColor(n.getX(), n.getY(), GUI.FASTEST_PATH_COLOR);
//            }
            if(result == null)  return null;
            // Prepare Instructions
            return GenerateCommands(result, robot);
        } catch (Exception e) {
            //					e.printStackTrace();
            System.out.println("Unable to find fastest path");
        }
        return null;
    }

    private List<RobotCommand> GenerateCommands(ShortestPath result, AbstractRobot robot){
        // Prepare for fastest path
        List<RobotCommand> fastestPathInstructions = new ArrayList<>();
        // Prepare Orientation
        if (result.isStartingOrientationHorizontal()) {
            fastestPathInstructions.addAll(robot.prepareOrientationCmds(Orientation.RIGHT));
        } else {
            fastestPathInstructions.addAll(robot.prepareOrientationCmds(Orientation.UP));
        }
        fastestPathInstructions.addAll(result.generateInstructions());
        return fastestPathInstructions;
    }
}
