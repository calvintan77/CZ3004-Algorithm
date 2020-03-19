package threading;

import algorithms.FastestPathFinder;
import algorithms.MazeExplorer;
import connection.AlgoClient;
import constants.RobotConstants;
import map.Map;
import map.MapTuple;
import robot.AbstractRobot;
import robot.RpiRobot;
import robot.VirtualRobot;
import connection.SyncObject;
import utils.*;

import java.util.ArrayList;
import java.util.List;

public class AlgoThread implements  Runnable {
    private Thread t;
    public void run(){
        try {
            if (RobotConstants.REAL_RUN) {
                RealRun();
            } else {
                SimulationRun();
            }
        }catch (Exception e){
            System.out.println("AlgoThread: " + e.toString());
        }
    }

    private void SimulationRun() throws InterruptedException{
        AbstractRobot robot = new VirtualRobot();
        Map explorationMap = new Map();
        MazeExplorer explorer = new MazeExplorer(robot);
        SyncObject.getSyncObject().IsExplorationStarted();
        // Initial state packet
        SyncObject.getSyncObject().SetGUIUpdate(explorationMap, robot.getPosition(), robot.getOrientation());
        // Explore the maze
        explorer.exploreMaze(explorationMap, SyncObject.getSyncObject().settings.getTimeLimit(), SyncObject.getSyncObject().settings.getCoveragePercent());
        // Notify UI that exploration completed
        SyncObject.getSyncObject().SignalExplorationFinished();
        MapLoader.saveMap(explorationMap, "mds.txt");
        Orientation o = robot.getOrientation();
        Coordinate c = robot.getPosition();
        while (true) {
            robot.setPosition(c.getX(), c.getY());
            robot.setOrientation(o);
            // Wait for button press
            SyncObject.getSyncObject().IsFastestPathStart();
            // Get waypoint
            Coordinate waypoint = getWaypoint();

            //Prepare algo for fastest path
            List<RobotCommand> fastestPathInstructions = new FastestPathFinder(explorationMap).GetFastestPath(robot, waypoint);
            robot.setFastestPath(fastestPathInstructions);
            robot.doFastestPath(true);
        }
    }

    private void RealRun(){
        RpiRobot robot = new RpiRobot();
        Map explorationMap = new Map();
        MazeExplorer explorer = new MazeExplorer(robot);
        // Real exploration, wait for signal from android
        System.out.println("Waiting for exploration to start...");
        robot.Calibrate(explorationMap);
        try {
            SyncObject.getSyncObject().IsExplorationStarted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Received Start Signal");
        // Initial state packet to GUI
        SyncObject.getSyncObject().SetGUIUpdate(explorationMap, robot.getPosition(), robot.getOrientation());
        // Explore the maze
        try {
            explorer.exploreMaze(explorationMap, RobotConstants.REAL_EXPLORE_TIME_LIMIT, RobotConstants.REAL_EXPLORE_COVERAGE);
        }catch(Exception e){
            System.out.println("RealAlgoThread: " + e.toString());
        }
        // Notify UI that exploration completed
        SyncObject.getSyncObject().SignalExplorationFinished();
        MapLoader.saveMap(explorationMap, "mds.txt");
        robot.EnableCalibrate();
        // Get waypoint
        Coordinate waypoint = getWaypoint();
        // Calibrate for Fastest Path
        //robot.Calibrate(explorationMap);
        //Prepare algo for fastest path
        List<RobotCommand> fastestPathInstructions = new FastestPathFinder(explorationMap).GetFastestPath(robot, waypoint);
        robot.setFastestPath(fastestPathInstructions);
    }

    private Coordinate getWaypoint(){
        Coordinate waypoint = null;
        try {
            waypoint = SyncObject.getSyncObject().GetWaypoint();
        }catch(Exception e){
            System.out.println("Get Waypoint: " + e.toString());
        }
        if(waypoint == null){
            waypoint = new Coordinate(1,1);
        }
        return waypoint;
    }

    public void interrupt(){
        t.interrupt();
    }

    public boolean isAlive(){
        if(t == null) return false;
        return t.isAlive();
    }

    public void start(){
        System.out.println("Starting AlgoThread...");
        if(t == null){
            t = new Thread(this, "AlgoThread");
            t.start();
        }
    }
}
