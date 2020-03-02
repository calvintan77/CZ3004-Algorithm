package Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Constants.SensorConstants;
import Main.GUI;
import utils.*;

public class VirtualRobot implements IRobot {
	private static VirtualRobot virtualRobot;
	private int speed;
	private Orientation o; // need to initialize 
	private Coordinate position;

	//Singleton strategy pattern
	public static IRobot getInstance() {
		if (virtualRobot == null)
			virtualRobot = new VirtualRobot();
		return virtualRobot;
	}

	private VirtualRobot() {

	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	//return string in the structure: "left,front,front,front,right"
	public List<Integer> getSensorValues() {
		Map realMap = Map.getRealMapInstance();
		List<Integer> sensorValues = new ArrayList<>();
		int x = this.position.getX();
		int y = this.position.getY();
		int cnt = 0;
		switch (this.o) {
			case UP:
				sensorValues.add(getSingleSensor(realMap, SensorConstants.LONG_RANGE, getPosition().getX() - 1, getPosition().getY() + 1, Orientation.getCounterClockwise(getOrientation())));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() - 1, getPosition().getY() + 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX(), getPosition().getY() + 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() + 1, getPosition().getY() + 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() + 1, getPosition().getY() + 1, Orientation.getClockwise(getOrientation())));
				break;
			case LEFT:
				sensorValues.add(getSingleSensor(realMap, SensorConstants.LONG_RANGE, getPosition().getX() - 1, getPosition().getY() - 1, Orientation.getCounterClockwise(getOrientation())));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() - 1, getPosition().getY() - 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() - 1, getPosition().getY(), getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() - 1, getPosition().getY() + 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() - 1, getPosition().getY() + 1, Orientation.getClockwise(getOrientation())));
				break;
			case RIGHT:
				sensorValues.add(getSingleSensor(realMap, SensorConstants.LONG_RANGE, getPosition().getX() + 1, getPosition().getY() + 1, Orientation.getCounterClockwise(getOrientation())));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() + 1, getPosition().getY() + 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() + 1, getPosition().getY(), getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() + 1, getPosition().getY() - 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() + 1, getPosition().getY() - 1, Orientation.getClockwise(getOrientation())));
				break;
			case DOWN:
				sensorValues.add(getSingleSensor(realMap, SensorConstants.LONG_RANGE, getPosition().getX() + 1, getPosition().getY() - 1, Orientation.getCounterClockwise(getOrientation())));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() + 1, getPosition().getY() - 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX(), getPosition().getY() - 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() - 1, getPosition().getY() - 1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, SensorConstants.SHORT_RANGE, getPosition().getX() - 1, getPosition().getY() - 1, Orientation.getClockwise(getOrientation())));
				break;

		}

		return sensorValues;
	}

	/**
	 * updates value based on a single sensor
	 *
	 * @param map:      actual map
	 * @param maxValue: max sensor reading
	 * @param x:        actual sensor x
	 * @param y:        actual sensor y
	 * @param o:        sensor orientation relative to map
	 */
	public int getSingleSensor(Map map, int maxValue, int x, int y, Orientation o) {
		switch (o) {
			case RIGHT:
				// update all seen
				for (int i = 1; i <= maxValue; i++) {
					if (map.getCell(x + i, y) == null || map.getCell(x + i, y).isObstacle())
						return i - 1;

				}
				return maxValue;
			case LEFT:
				for (int i = 1; i <= maxValue; i++) {
					if (map.getCell(x - i, y) == null || map.getCell(x - i, y).isObstacle())
						return i - 1;
				}
				return maxValue;
			case UP:
				for (int i = 1; i <= maxValue; i++) {
					if (map.getCell(x, y + i) == null || map.getCell(x, y + i).isObstacle())
						return i - 1;
				}
				return maxValue;
			case DOWN:
				for (int i = 1; i <= maxValue; i++) {
					if (map.getCell(x, y - i) == null || map.getCell(x, y - i).isObstacle())
						return i - 1;
				}
				return maxValue;
		}
		return -1;
	}

	public void doCommand(RobotCommand cmd) {
		switch (cmd) {
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
		}
		try {
			Thread.sleep((cmd == RobotCommand.MOVE_FORWARD? 1000 : 2000) / speed);    //int timePerStep = 1000/speed (ms)
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		GUI.getInstance().updateRobotUI(cmd);
	}

	@Override
	public Orientation getOrientation() {
		return o;
	}

	@Override
	public Coordinate getPosition() {
		return position;
	}

	@Override
	public void setPosition(int x, int y) {
		this.position = new Coordinate(x, y);
	}

	@Override
	public void prepareOrientation(Orientation target) {
		prepareOrientation(target, false, null);
	}

	@Override
	public void prepareOrientation(Orientation target, boolean checkSensors, Map map) {
		// Orientation update
		if (this.getOrientation() != target) {
			int rightTurns = this.getOrientation().getRightTurns(target);
			if (rightTurns > 0) {
				for (int i = 0; i < rightTurns; i++) {
					this.doCommand(RobotCommand.TURN_RIGHT);
					if (checkSensors) map.updateFromSensor(getSensorValues(), this.position, this.o);
				}
			} else {
				for (int i = 0; i < -rightTurns; i++) {
					this.doCommand(RobotCommand.TURN_LEFT);
					if (checkSensors) map.updateFromSensor(getSensorValues(), this.position, this.o);
				}
			}
		}

	}

	@Override
	public void setOrientation(Orientation o) {
		this.o = o;
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
}
