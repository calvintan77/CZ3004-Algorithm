package robot;

import maze.Map;
import maze.MapCell;
import utils.*;

import java.util.HashMap;
import java.util.List;

public interface AbstractRobot {
	List<Integer> getSensorValues();
	void doCommandWithSensor(RobotCommand cmd, Map map) throws InterruptedException;
	void prepareOrientation(List<RobotCommand> cmds, Map map) throws InterruptedException;
	Orientation getOrientation();
	Coordinate getPosition();
	void setPosition(int x, int y);
	List<RobotCommand> prepareOrientationCmds(Orientation o);
	void setOrientation(Orientation o);
	HashMap<MapCell, Orientation> getSensorVisibilityCandidates(Map map, MapCell cell);
	boolean Calibrate(Map m);
	void setFastestPath(List<RobotCommand> cmds);
	void doFastestPath(boolean toGoalZone) throws InterruptedException;
	boolean canCalibrate(Orientation o, Map m);
}
