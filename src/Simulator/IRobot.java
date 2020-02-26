package Simulator;

import utils.*;

import java.util.HashMap;
import java.util.List;


// TODO: refactor all methods that have primitive parameters to take wrapper objects...
// TODO: fix relationship between robot and map	
public interface IRobot {
	public List<Integer> getSensorValues(); // this
	public void doCommand(RobotCommand cmd);
	public Orientation getOrientation(); 
	public Coordinate getPosition();
	public void setPosition(int x, int y); // this
	public void prepareOrientation(Orientation o);
	public List<Object[]> prepareOrientation(Orientation o, boolean checkSensors);
	public void setOrientation(Orientation o);
	public HashMap<MapCell, Orientation> getSensorVisibilityCandidates(Map map, MapCell cell);
}
