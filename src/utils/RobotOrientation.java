package utils;

public enum RobotOrientation {
    UP, DOWN, LEFT, RIGHT;

    public boolean isAligned(boolean isHorizontal){
        return (isHorizontal == (this == RIGHT || this == LEFT));
    }
}
