package gui;

import utils.Coordinate;
import maze.Map;
import utils.Orientation;

public class GUIUpdate {
    private final Map map;
    private final Coordinate robotPos;
    private final Orientation orientation;

    public GUIUpdate(Map map, Coordinate robotPos, Orientation orientation) {
        this.map = map;
        this.robotPos = robotPos;
        this.orientation = orientation;
    }

    public Map getMap() {
        return map;
    }

    public Coordinate getRobotPos() {
        return robotPos;
    }

    public Orientation getOrientation() {
        return orientation;
    }
}
