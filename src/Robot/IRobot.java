package Robot;

import utils.*;

import java.util.HashMap;
import java.util.List;

public interface IRobot {
	public List<Integer> getSensorValues(); 
	public void doCommandWithSensor(RobotCommand cmd, Map map);
	public void prepareOrientation(List<RobotCommand> cmds, Map map);
	public Orientation getOrientation(); 
	public Coordinate getPosition();
	public void setPosition(int x, int y); 
	public List<RobotCommand> prepareOrientationCmds(Orientation o);
	public void setOrientation(Orientation o);
	public HashMap<MapCell, Orientation> getSensorVisibilityCandidates(Map map, MapCell cell);
	public boolean Calibrate(Map m); 
	public void setFastestPath(List<RobotCommand> cmds);
	public void doFastestPath(boolean toGoalZone);
}
