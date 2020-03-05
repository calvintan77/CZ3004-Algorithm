package GUI;

import utils.MapProcessor;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GUISettings {
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private int RobotSpeed = 10;
    private int coveragePercent = 100;
    private int timeLimit = 360;

    public GUISettings() {
    }

    public int getRobotSpeed() {
        lock.readLock().lock();
        int temp = RobotSpeed;
        lock.readLock().unlock();
        return temp;
    }

    public void setRobotSpeed(int robotSpeed) {
        lock.writeLock().lock();
        try {
            this.RobotSpeed = robotSpeed;
            MapProcessor.FORWARD_WEIGHT = 1f / RobotSpeed;
            MapProcessor.TURNING_WEIGHT = 2f / RobotSpeed;
        }finally {
            lock.writeLock().unlock();
        }
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
