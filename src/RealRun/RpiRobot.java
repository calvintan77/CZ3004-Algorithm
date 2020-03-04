package RealRun;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import Constants.MapConstants;
import Constants.SensorConstants;
import Simulator.IRobot;
import connection.AlgoClient;
import connection.SyncObject;
import utils.*;

public class RpiRobot implements IRobot{
	private static RpiRobot robot = null;

	private Coordinate position;
	private Orientation o;

	public static IRobot getInstance(){
		if (robot == null) {
			robot = new RpiRobot();
		}
		return robot;
	}

	private RpiRobot(){

	}
	
	@Override
	public List<Integer> getSensorValues() {
		try {
			return SyncObject.GetSensorData();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return null;
	}

	@Override
	public void doCommandWithSensor (RobotCommand command, Map map) {
		try {
			AlgoClient.GetInstance().SendMove(command, Map.generateMapDescriptor(map), this.o, this.position);
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
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	@Override
	public Orientation getOrientation() {
		return this.o;
	}

	@Override
	public Coordinate getPosition() {
		return this.position;
	}

	@Override
	public void setPosition(int x, int y) {
		this.position = new Coordinate(x, y);
	}
	
	@Override
	public void setOrientation(Orientation o) {
		this.o = o;
	}

	@Override
	public List<RobotCommand> prepareOrientationCmds(Orientation target) {
		// Orientation update
		List<RobotCommand> avaiCommands = new ArrayList<RobotCommand>(); 
		if (this.getOrientation() != target) {
			int rightTurns = this.getOrientation().getRightTurns(target);
			if (rightTurns > 0) {
				for (int i = 0; i < rightTurns; i++) {
					avaiCommands.add(RobotCommand.TURN_RIGHT);
				}
			} else {
				for (int i = 0; i < -rightTurns; i++) {
					avaiCommands.add(RobotCommand.TURN_LEFT);
				}
			}
		}
		return avaiCommands; 
	}

	@Override
	public HashMap<MapCell, Orientation> getSensorVisibilityCandidates(Map map, MapCell cell) {
		HashMap<MapCell, Orientation> candidates = new HashMap<>();
		MapCell check;
		//4 is left sensor length
		for (int i = 1; i <= SensorConstants.LONG_RANGE; i++) {
			//POSITION IS LEFT OF ROBOT IN MAP
			MapCell cand = map.getCell(cell.x + i, cell.y);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			// LEFT SENSOR
			check = map.getCell(cell.x + i + 1, cell.y - 1);
			if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.UP);
			//FRONT AND RIGHT SENSORS
			if (i <= SensorConstants.SHORT_RANGE) {
				// RIGHT SENSOR
				check = map.getCell(cell.x + i + 1, cell.y + 1);
				if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.DOWN);
				// FRONT MIDDLE SENSOR
				check = map.getCell(cell.x + i + 1, cell.y);
				if (isViableCell(check))candidates.put(check, Orientation.LEFT);
			}
		}

		//POSITION IS RIGHT OF ROBOT IN MAP
		for (int i = 1; i <= SensorConstants.LONG_RANGE; i++) {
			MapCell cand = map.getCell(cell.x - i, cell.y);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			// LEFT SENSOR
			check = map.getCell(cell.x - i - 1, cell.y + 1);
			if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.DOWN);
			//FRONT AND RIGHT SENSORS
			if (i <= SensorConstants.SHORT_RANGE) {
				// RIGHT SENSOR
				check = map.getCell(cell.x - i - 1, cell.y - 1);
				if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.UP);
				// FRONT MIDDLE SENSOR
				check = map.getCell(cell.x - i - 1, cell.y);
				if (isViableCell(check))candidates.put(check, Orientation.RIGHT);
			}
		}

		//POSITION IS BELOW THE ROBOT IN MAP
		for (int i = 1; i <= SensorConstants.LONG_RANGE; i++) {
			MapCell cand = map.getCell(cell.x, cell.y + i);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			// LEFT SENSOR
			check = map.getCell(cell.x + 1, cell.y + i + 1);
			if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.LEFT);
			//FRONT AND RIGHT SENSORS
			if (i <= SensorConstants.SHORT_RANGE) {
				// RIGHT SENSOR
				check = map.getCell(cell.x - 1, cell.y + i + 1);
				if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.RIGHT);
				// FRONT MIDDLE SENSOR
				check = map.getCell(cell.x, cell.y + i + 1);
				if (isViableCell(check))candidates.put(check, Orientation.DOWN);
			}
		}

		//POSITION IS ABOVE THE ROBOT IN MAP
		for (int i = 1; i <= SensorConstants.LONG_RANGE; i++) {
			MapCell cand = map.getCell(cell.x, cell.y - i);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			// LEFT SENSOR
			check = map.getCell(cell.x - 1, cell.y - i - 1);
			if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.RIGHT);
			//FRONT AND RIGHT SENSORS
			if (i <= SensorConstants.SHORT_RANGE) {
				// RIGHT SENSOR
				check = map.getCell(cell.x + 1, cell.y - i - 1);
				if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.LEFT);
				// FRONT MIDDLE SENSOR
				check = map.getCell(cell.x, cell.y - i - 1);
				if (isViableCell(check))candidates.put(check, Orientation.UP);
			}
		}
		return candidates;
	}
	
	private boolean isViableCell(MapCell cell) {
		return cell != null && !cell.isObstacle() && !cell.isVirtualWall();
	}

	/**
	 * Method to calibrate the actual robot against given corners/walls
	 * @param Map: the current seen map at this timestep
	 * @return: void but arduino will do command 
	 */
	public boolean Calibrate(Map m) { 	
		// get distances to nearest 4 walls
		// check xy to see if we are at start 
		// yes; send calibrate over 
		// no: check 
		// check in front/right/down/left order 
		List<Orientation> available = getAvailableCalibrations(m);
		if (available.size() == 0) {
			return false; 
		}
		List<RobotCommand> toSend = new ArrayList<RobotCommand>(); 
		// toSend.add(6);
		Orientation temp = Orientation.getClockwise(o);
		if(available.contains(this.o)) { 
			toSend.add(RobotCommand.CALIBRATE);
		} else if (available.contains(Orientation.getClockwise(temp))) { 
			toSend.addAll(prepareOrientationCmds(Orientation.getClockwise(temp)));
			toSend.add(RobotCommand.CALIBRATE);
		}

		if(available.contains(temp)) { 
			toSend.addAll(prepareOrientationCmds(Orientation.getClockwise(temp)));
			toSend.add(RobotCommand.CALIBRATE);
		} else if (available.contains(Orientation.getCounterClockwise(o))) { 
			toSend.addAll(prepareOrientationCmds(Orientation.getCounterClockwise(o)));
			toSend.add(RobotCommand.CALIBRATE);
		}

		toSend.addAll(prepareOrientationCmds(o));
		// TODO: set docommand here later 
		return true; 
	}

	/**
	 * method to return available calibrations 
	 * @param m: current map state 
	 * @return: list of available orientations to take 
	 */
	public List<Orientation> getAvailableCalibrations(Map m) { 
		List<Orientation> available = new ArrayList<Orientation>(); 
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

	@Override
	public void doFastestPath(List<RobotCommand> cmds) {
		AlgoClient.GetInstance().sendFastestPath(cmds);
	}
}
