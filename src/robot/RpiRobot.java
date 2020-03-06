package robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import constants.MapConstants;
import constants.SensorConstants;
import connection.AlgoClient;
import connection.SyncObject;
import maze.Map;
import maze.MapCell;
import utils.*;

public class RpiRobot extends AbstractRobot {
	public RpiRobot(){

	}
	
	@Override
	public List<Integer> getSensorValues() {
		try {
			return SyncObject.getSyncObject().GetSensorData();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return null;
	}

	@Override
	public void doCommandWithSensor (RobotCommand command, Map map) {
		try {
			AlgoClient.GetInstance().SendMove(command, MapLoader.generateMapDescriptor(map), this.o, this.position);
			switch (command) {
				case TURN_LEFT:
					this.setOrientation(Orientation.getCounterClockwise(this.o));
					break;
				case TURN_RIGHT:
					this.setOrientation(Orientation.getClockwise(this.o));
					break;
				case MOVE_FORWARD:
					switch (this.o) {
						case UP:
							this.setPosition(this.getPosition().getX(), this.getPosition().getY() + 1);
							break;
						case LEFT:
							this.setPosition(this.getPosition().getX() - 1, this.getPosition().getY());
							break;
						case DOWN:
							this.setPosition(this.getPosition().getX(), this.getPosition().getY() - 1);
							break;
						case RIGHT:
							this.setPosition(this.getPosition().getX() + 1, this.getPosition().getY());
							break;
					}
				default:
					break;
			}
			map.updateFromSensor(this.getSensorValues(), this.position, this.o);
			SyncObject.getSyncObject().SetGUIUpdate(map, this.position, this.o);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	/**
	 * Method to calibrate the actual robot against given corners/walls
	 * @param m - the current seen map at this timestep
	 * @return void but arduino will do command
	 */
	public boolean Calibrate(Map m) { 	
		// get distances to nearest 4 walls
		// check xy to see if we are at start 
		// yes; send calibrate over 
		// no: check 
		// check in front/right/down/left order
		Orientation orient = this.o;
		List<Orientation> available = getAvailableCalibrations(m);
		if (available.size() == 0) {
			return false; 
		}
		List<RobotCommand> toSend = new ArrayList<>();
		// toSend.add(6);
		Orientation temp = Orientation.getClockwise(this.o);
		if(available.contains(this.o)) { 
			toSend.add(RobotCommand.CALIBRATE);
		} else if (available.contains(Orientation.getClockwise(temp))) { 
			toSend.addAll(prepareAnyOrientation(orient, Orientation.getClockwise(temp)));
			orient = Orientation.getClockwise(temp);
			toSend.add(RobotCommand.CALIBRATE);
		}

		if(available.contains(temp)) { 
			toSend.addAll(prepareAnyOrientation(orient, temp));
			orient = temp;
			toSend.add(RobotCommand.CALIBRATE);
		} else if (available.contains(Orientation.getCounterClockwise(this.o))) {
			toSend.addAll(prepareAnyOrientation(orient, Orientation.getCounterClockwise(this.o)));
			orient = Orientation.getCounterClockwise(this.o);
			toSend.add(RobotCommand.CALIBRATE);
		}

		toSend.addAll(prepareAnyOrientation(orient, this.o));
		AlgoClient.GetInstance().SendCalibrate(toSend);
		return true; 
	}

	/**
	 * method to return available calibrations 
	 * @param m: current map state 
	 * @return list of available orientations to take
	 */
	public List<Orientation> getAvailableCalibrations(Map m) { 
		List<Orientation> available = new ArrayList<>();
		// check all configs from robot's own internal xy 
		for (Orientation o : Orientation.values()) { 
			if (canCalibrate(o, m)) { 
				available.add(o);
			}
		}
		return available;
	 }
	
	 /**
	  * method checks if there is something to calibrate in front
	  * @param o: Orientation to check at the given moment 
	  * @return true if obstacle/wall in front to calibrate
	  */
	public boolean canCalibrate(Orientation o, Map m) { 
		switch (o) { 
			case UP:
				return (this.position.getY() + 2 > MapConstants.MAP_HEIGHT-1) || 
				(m.getCell(this.position.getX()+1, this.position.getY()+2).isObstacle() && m.getCell(this.position.getX()-1, this.position.getY()+2).isObstacle());
			case DOWN:
				return (this.position.getY() - 2 < 0) || 
				(m.getCell(this.position.getX()+1, this.position.getY()-2).isObstacle() && m.getCell(this.position.getX()-1, this.position.getY()-2).isObstacle());
			case RIGHT:
				return (this.position.getX() + 2 > MapConstants.MAP_WIDTH-1) || 
				(m.getCell(this.position.getX()+2, this.position.getY()+1).isObstacle() && m.getCell(this.position.getX()+2, this.position.getY()-1).isObstacle());
			case LEFT: 
				return (this.position.getX() - 2 < 0) || 
				(m.getCell(this.position.getX()-2, this.position.getY()-1).isObstacle() && m.getCell(this.position.getX()-2, this.position.getY()+1).isObstacle());
			default: 
				return false; 
		}
	}

	@Override
	public void prepareOrientation(List<RobotCommand> cmds, Map map) {
		for(RobotCommand cmd: cmds){
			doCommandWithSensor(cmd, map);
		}
	}

	//TODO: Decide on whether to display it here
	@Override
	public void setFastestPath(List<RobotCommand> cmds) {
		AlgoClient.GetInstance().sendFastestPath(cmds);
	}

	@Override
	public void doFastestPath(boolean toGoalZone) {
		if(!toGoalZone){
			AlgoClient.GetInstance().StartFastestPath();
		}
	}
}
