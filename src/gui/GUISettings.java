package gui;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GUISettings {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private float RobotSpeed = 2.5f;
    private int coveragePercent = 100;
    private int timeLimit = 360;

    public GUISettings() {
    }


    public float getRobotSpeed() {
        lock.readLock().lock();
        float temp = RobotSpeed;
        lock.readLock().unlock();
        return temp;
    }

    //TODO: Calibrate the weights accordingly
    public void setRobotSpeed(float robotSpeed) {
        lock.writeLock().lock();
        try {
            this.RobotSpeed = robotSpeed;
        }finally {
            lock.writeLock().unlock();
        }
    }

    public float getForwardWeight(){
        lock.readLock().lock();
        float temp = 1f / RobotSpeed;
        lock.readLock().unlock();
        return temp;
    }

    public float getTurningWeight(){
        lock.readLock().lock();
        float temp = 2f / RobotSpeed;
        lock.readLock().unlock();
        return temp;
    }

    public int getCoveragePercent() {
        lock.readLock().lock();
        int temp = coveragePercent;
        lock.readLock().unlock();
        return temp;
    }

    public void setCoveragePercent(int coveragePercent) {
        lock.writeLock().lock();
        try {
            this.coveragePercent = coveragePercent;
        }finally {
            lock.writeLock().unlock();
        }
    }

    public int getTimeLimit() {
        lock.readLock().lock();
        int temp = timeLimit;
        lock.readLock().unlock();
        return temp;
    }

    public void setTimeLimit(int timeLimit) {
        lock.writeLock().lock();
        try {
            this.timeLimit = timeLimit;
        }finally {
            lock.writeLock().unlock();
        }
    }
}
