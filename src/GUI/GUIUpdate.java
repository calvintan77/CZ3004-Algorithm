package GUI;

import utils.Coordinate;
import utils.Map;
import utils.Orientation;

public class GUIUpdate {
    private Map map;
    private Coordinate robotPos;
    private Orientation orientation;

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
