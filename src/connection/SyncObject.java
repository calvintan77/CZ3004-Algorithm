package connection;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import utils.Coordinate;

public class SyncObject{
    private static Semaphore isWaypointAvailable = new Semaphore(0);
    private static Semaphore hasExplorationStarted = new Semaphore(0);
    private static Semaphore isSensorDataAvailable = new Semaphore(0);

    //Waypoint
    private static Coordinate waypoint;

    public static void DefineWaypoint(Coordinate c){
        waypoint = c;
        isWaypointAvailable.release();
    }

    public static Coordinate GetWaypoint() throws InterruptedException{
        isWaypointAvailable.acquire();
        return waypoint;
    }

    //Exploration

    public static void SignalExplorationStarted(){
        hasExplorationStarted.release();
    }

    public static boolean HasExplorationStarted() throws InterruptedException{
        hasExplorationStarted.acquire();
        return true;
    }

    //Sensors

    private static Queue<List<Integer>> sensorData = new ConcurrentLinkedQueue<>(); 

    public static void AddSensorData(List<Integer> data){
        sensorData.add(data);
        isSensorDataAvailable.release();
    }

    public static List<Integer> GetSensorData() throws InterruptedException{
        isSensorDataAvailable.acquire();
        return sensorData.poll();
    }
}