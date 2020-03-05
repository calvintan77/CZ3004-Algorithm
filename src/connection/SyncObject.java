package connection;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import GUI.GUISettings;
import GUI.GUIUpdate;
import utils.*;

public class SyncObject{
    private static SyncObject instance;
    private Semaphore isWaypointAvailable = new Semaphore(0);
    private Semaphore hasExplorationStarted = new Semaphore(0);
    private Semaphore isSensorDataAvailable = new Semaphore(0);
    private Semaphore hasGUIUpdate = new Semaphore(0);
    // Only for virtual runs
    private Semaphore startFastestPath = new Semaphore(0);
    // Only for virtual runs
    private Semaphore resetRobot = new Semaphore(0);
    // Only for virtual runs
    private Lock lockExploreStatus = new ReentrantLock();
    private boolean hasExplorationFinished = false;

    public GUISettings settings = new GUISettings();

    //Waypoint
    private Coordinate waypoint;

    private SyncObject(){
    }

    public static SyncObject getSyncObject(){
        if(instance == null){
            instance = new SyncObject();
        }
        return instance;
    }

    public void DefineWaypoint(Coordinate c){
        waypoint = c;
        isWaypointAvailable.release();
    }

    public Coordinate GetWaypoint() throws InterruptedException{
        isWaypointAvailable.acquire();
        return waypoint;
    }

    //Exploration

    public void SignalExplorationStarted(){
        hasExplorationStarted.release();
    }

    public boolean HasExplorationStarted() throws InterruptedException{
        hasExplorationStarted.acquire();
        return true;
    }

    //Sensors

    private Queue<List<Integer>> sensorData = new ConcurrentLinkedQueue<>(); 

    public void AddSensorData(List<Integer> data){
        sensorData.add(data);
        isSensorDataAvailable.release();
    }

    public List<Integer> GetSensorData() throws InterruptedException{
        isSensorDataAvailable.acquire();
        return sensorData.poll();
    }

    // GUI Update Queue
    private Queue<GUIUpdate> GUIUpdates = new ConcurrentLinkedQueue<>();

    public void AddGUIUpdate(Map mapToClone, Coordinate c, Orientation o){
        Map map = mapToClone.clone();
        GUIUpdates.add(new GUIUpdate(map, c, o));
        hasGUIUpdate.release();
    }

    public GUIUpdate GetGUIUpdate() throws InterruptedException{
        hasGUIUpdate.acquire();
        return GUIUpdates.poll();
    }

    //Fastest Path

    public void SignalFastestPath(){
        startFastestPath.release();
    }

    public boolean HasFastestPathStarted() throws InterruptedException{
        startFastestPath.acquire();
        return true;
    }

    //Reset Robot

    public void SignalResetRobot(){
        resetRobot.release();
    }

    public boolean ShouldResetRobot() throws InterruptedException{
        resetRobot.acquire();
        //instance = new SyncObject();
        return true;
    }

    //Has Exploration Finished
    public void SetExplorationFinished(){
        lockExploreStatus.lock();
        hasExplorationFinished = true;
        lockExploreStatus.unlock();
    }

    public boolean HasExplorationFinished(){
        lockExploreStatus.lock();
        boolean temp = hasExplorationFinished;
        lockExploreStatus.unlock();
        return  temp;
    }
}