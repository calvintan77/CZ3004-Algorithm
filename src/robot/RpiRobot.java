package robot;

import java.util.ArrayList;
import java.util.List;

import constants.MapConstants;
import connection.AlgoClient;
import connection.SyncObject;
import map.Map;
import utils.*;

public class RpiRobot extends AbstractRobot {
	private int calibrationCount = 1;
	private final int THRESHOLD = 4;
	private final int HARD_THRESHOLD = 6;
	public RpiRobot(){

	}
	
	private List<Integer> getSensorValues() {
		try {
			return SyncObject.getSyncObject().GetSensorData();
		} catch (Exception e) {
			System.out.println("RPIRobot Get Sensor: " + e.toString());
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
					break;
				case REVERSE:
					Coordinate newCoord = this.o.behindCurrent(this.position);
					this.setPosition(newCoord.getX(), newCoord.getY());
					break;
				default:
					break;
			}
			List<Integer> SensorData = this.getSensorValues();
			if(SensorData != null && map != null) {
				map.updateFromSensor(SensorData, this.position, this.o);
			}
			SyncObject.getSyncObject().SetGUIUpdate(map, this.position, this.o);
			calibrationCount++;
			if(calibrationCount >= THRESHOLD && (getAvailableCalibrations(map).size()) > 0){
				Calibrate(map);
			}else {
				Coordinate behind = this.o.behindCurrent(this.position);
				if(calibrationCount >= HARD_THRESHOLD && map.getCell(behind) != null && !map.getCell(behind).isObstacle() && !map.getCell(behind).isVirtualWall() && getAvailableCalibrations(map, behind).size()>0){
					doCommandWithSensor(RobotCommand.REVERSE, map);
					doCommandWithSensor(RobotCommand.MOVE_FORWARD, map);
				}
			}
		} catch (Exception e) {
			System.out.println("Do Command With Sensor: " + e.toString());
			e.printStackTrace();
		}
	}
	/**
	 * Method to calibrate the actual robot against given corners/walls and return to initial facing
	 * @param m - the current seen map at this timestep
	 */
	public void Calibrate(Map m) {
		Calibrate(m, this.o);
	}

	/**
	 * Method to calibrate the actual robot against given corners/walls
	 * @param m - the current seen map at this timestep
	 * @param finalOrientation  - final orientation after calibration
	 */
	public void Calibrate(Map m, Orientation finalOrientation) {
		// get distances to nearest 4 walls
		// check xy to see if we are at start
		// yes; send calibrate over
		// no: check
		// check in front/right/down/left order
		if(calibrationCount == 0) return;
		Orientation orient = this.o;
		List<Orientation> available = getAvailableCalibrations(m);
		if (available.size() == 0) {
			return;
		}
		if (calibrationCount < HARD_THRESHOLD && available.size() == 1 && available.get(0) == Orientation.getClockwise(Orientation.getClockwise(this.o))){
			return;
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
		List<RobotCommand> finalCmds = prepareAnyOrientation(orient, finalOrientation);
		toSend.addAll(finalCmds);
		AlgoClient.GetInstance().SendCalibrate(toSend);
		this.setOrientation(finalOrientation);
		this.calibrationCount = 0;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * method to return available calibrations 
	 * @param m: current map state 
	 * @return list of available orientations to take
	 */
	public List<Orientation> getAvailableCalibrations(Map m) { 
		return getAvailableCalibrations(m, this.position);
	 }

	public List<Orientation> getAvailableCalibrations(Map m, Coordinate position) {
		List<Orientation> available = new ArrayList<>();
		// check all configs from robot's own internal xy
		for (Orientation o : Orientation.values()) {
			if (canCalibrate(o, m, position)) {
				available.add(o);
			}
		}
		return available;
	}

	public boolean canCalibrate(Orientation o, Map m) {
		return canCalibrate(o, m, this.position);
	}
		/**
         * method checks if there is something to calibrate in front
         * @param o: Orientation to check at the given moment
         * @return true if obstacle/wall in front to calibrate
         */
	public boolean canCalibrate(Orientation o, Map m, Coordinate position) {
		switch (o) { 
			case UP:
				return (position.getY() + 2 > MapConstants.MAP_HEIGHT-1) ||
				(m.getCell(position.getX()+1, position.getY()+2).isObstacle() && m.getCell(position.getX()-1, position.getY()+2).isObstacle());
			case DOWN:
				return (position.getY() - 2 < 0) ||
				(m.getCell(position.getX()+1, position.getY()-2).isObstacle() && m.getCell(position.getX()-1, position.getY()-2).isObstacle());
			case RIGHT:
				return (position.getX() + 2 > MapConstants.MAP_WIDTH-1) ||
				(m.getCell(position.getX()+2, position.getY()+1).isObstacle() && m.getCell(position.getX()+2, position.getY()-1).isObstacle());
			case LEFT: 
				return (position.getX() - 2 < 0) ||
				(m.getCell(position.getX()-2, position.getY()-1).isObstacle() && m.getCell(position.getX()-2, position.getY()+1).isObstacle());
			default: 
				return false; 
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

	public void EnableCalibrate(){
		calibrationCount = 1;
	}
}
