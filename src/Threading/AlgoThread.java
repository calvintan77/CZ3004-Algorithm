package Threading;

import Algorithms.MazeExplorer;
import Constants.MapConstants;
import GUI.GUI;
import Robot.IRobot;
import Robot.RpiRobot;
import Robot.VirtualRobot;
import connection.SyncObject;
import utils.*;

import java.util.ArrayList;
import java.util.List;

public class AlgoThread implements  Runnable {
    private Thread t;
    public void run(){
        IRobot robot = RobotController.REAL_RUN? RpiRobot.getInstance() : VirtualRobot.getInstance();
        Map explorationMap = new Map();
        MazeExplorer explorer = new MazeExplorer();
        explorer.setRobot(robot);
        try {
            SyncObject.getSyncObject().HasExplorationStarted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Initial state packet
        SyncObject.getSyncObject().AddGUIUpdate(explorationMap, robot.getPosition(), robot.getOrientation());
        // Explore the maze
        explorer.exploreMaze(explorationMap, SyncObject.getSyncObject().settings.getTimeLimit(), SyncObject.getSyncObject().settings.getCoveragePercent());
        SyncObject.getSyncObject().SetExplorationFinished();
        // Calibrate for FP
        robot.Calibrate(explorationMap);
        // Wait for button if virtual run
        if(!RobotController.REAL_RUN){
            try {
                SyncObject.getSyncObject().HasFastestPathStarted();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Get waypoint
        Coordinate waypoint = null;
        try {
            waypoint = SyncObject.getSyncObject().GetWaypoint();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(waypoint == null){
            waypoint = new Coordinate(1,1);
        }
        //Prepare algo for fastest path
        try{
            // Set up a blocked path
            if (explorationMap.getSeenPercentage() < 100) {
                explorationMap = explorationMap.CloneWithUnseenAsObstacles();
            }
            ShortestPath result;
            // Find a path to the goal
            do {
                Graph graph = new Graph(explorationMap, waypoint.getX(), waypoint.getY());
                result = graph.GetShortestPath();
                if (result == null) {
                    System.out.println("Unable to find path through waypoint");
                    graph = new Graph(explorationMap, 1, 1);
                    result = graph.GetShortestPath();
                }
                if (result == null) {
                    explorationMap.expandSearchSpace();
                }
            }while (result == null && !explorationMap.getAllUnseen().isEmpty());
            // Prepare for fastest path
            List<RobotCommand> fastestPathInstructions = new ArrayList<>();
            // Prepare Orientation
            if (result.isStartingOrientationHorizontal()) {
                fastestPathInstructions.addAll(robot.prepareOrientationCmds(Orientation.RIGHT));
            } else {
                fastestPathInstructions.addAll(robot.prepareOrientationCmds(Orientation.UP));
            }
            //TODO: Figure out how to draw path
//            List<GraphNode> path = result.getPath();
//            for(GraphNode n: path){
//                if (!( (n.getX()==0 && n.getY()==0) ||
//                        (n.getX()== MapConstants.MAP_WIDTH-1 && n.getY()==MapConstants.MAP_HEIGHT-1) ))
//                    gui.setMazeGridColor(n.getX(), n.getY(), GUI.FASTEST_PATH_COLOR);
//            }
            // Prepare Instructions
            fastestPathInstructions.addAll(result.generateInstructions());
            robot.setFastestPath(fastestPathInstructions);
            if(!RobotController.REAL_RUN) robot.doFastestPath(true);
        } catch (Exception e) {
        //					e.printStackTrace();
            System.out.println("Unable to find fastest path");
        }
    }

    public void start(){
        System.out.println("Starting AlgoThread...");
        if(t == null){
            t = new Thread(this, "AlgoThread");
            t.start();
        }
    }
}
