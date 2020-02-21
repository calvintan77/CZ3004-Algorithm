package Simulator;

import utils.MapCell;
import utils.Orientation;
import utils.RobotCommand;


// TODO: refactor all methods that have primitive parameters to take wrapper objects...
// TODO: fix relationship between robot and map	
public interface RobotInterface {
	public String getSensorValues(int[] robotPosition, Orientation robotOrientation); // this
	public void doCommand(RobotCommand cmd);
	public Orientation getOrientation(); 
	public MapCell getPositionCell(); 
	public void setPosition(int x, int y); // this
	public void setOrientation(Orientation o);
}
