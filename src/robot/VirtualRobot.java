package robot;

import java.util.ArrayList;
import java.util.List;

import constants.MapConstants;
import constants.SensorConstants;
import connection.SyncObject;
import map.Map;
import utils.*;

public class VirtualRobot extends AbstractRobot {
	private List<RobotCommand> fastestPathInstructions;
	private final int THRESHOLD = 6;
	private final int HARD_THRESHOLD = 9;
	private int calibrationCount = 1;


	public VirtualRobot() {

	}

	//return string in the structure: "left,front,front,front,right"
	private List<Integer> getSensorValues() {
		Map realMap = Map.getRealMapInstance();
		List<Integer> sensorValues = new ArrayList<>();
		sensorValues.add(getSingleSensor(realMap, SensorConstants.LEFT_SENSOR));
		sensorValues.add(getSingleSensor(realMap, SensorConstants.FRONT_LEFT_SENSOR));
		sensorValues.add(getSingleSensor(realMap, SensorConstants.FRONT_MIDDLE_SENSOR));
		//sensorValues.add(getSingleSensor(realMap, SensorConstants.FRONT_RIGHT_SENSOR)==SensorConstants.FRONT_RIGHT_SENSOR.GetRange()?SensorConstants.FRONT_RIGHT_SENSOR.GetRange():getSingleSensor(realMap, SensorConstants.FRONT_RIGHT_SENSOR)-1);
		sensorValues.add(getSingleSensor(realMap, SensorConstants.FRONT_RIGHT_SENSOR));
		sensorValues.add(getSingleSensor(realMap, SensorConstants.RIGHT_SENSOR));
		return sensorValues;
	}

	public int getSingleSensor(Map map, Sensor sensor){
		Coordinate c = sensor.GetSensorPosition(this.o, this.position);
		return getSingleSensor(map, sensor.GetRange(), c.getX(), c.getY(), sensor.getSensorFacing(this.o));
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

	@Override
	public void doCommandWithSensor(RobotCommand cmd, Map map) throws InterruptedException {
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
						this.setPosition(this.position.getX(), this.position.getY() + 1);
						break;
					case LEFT:
						this.setPosition(this.position.getX() - 1, this.position.getY());
						break;
					case DOWN:
						this.setPosition(this.position.getX(), this.position.getY() - 1);
						break;
					case RIGHT:
						this.setPosition(this.position.getX() + 1, this.position.getY());
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
		if(map != null){
			map.updateFromSensor(this.getSensorValues(), this.position, this.o);
		}
		SyncObject.getSyncObject().SetGUIUpdate(map, this.position, this.o);
		Thread.sleep(cmd == RobotCommand.MOVE_FORWARD? (int)(1000*SyncObject.getSyncObject().settings.getForwardWeight()) :
				(int)(1000*SyncObject.getSyncObject().settings.getTurningWeight()));    //int timePerStep = 1000/speed (ms)
		calibrationCount++;
		if(calibrationCount >= THRESHOLD && (getAvailableCalibrations(map).size()) > 0){
			Calibrate(map);
		}else {
			Coordinate behind = this.o.behindCurrent(this.position);
			if(map == null) return;
			if(calibrationCount >= HARD_THRESHOLD && map.getCell(behind) != null && !map.getCell(behind).isObstacle() && !map.getCell(behind).isVirtualWall() && getAvailableCalibrations(map, behind).size()>0){
				doCommandWithSensor(RobotCommand.REVERSE, map);
				doCommandWithSensor(RobotCommand.MOVE_FORWARD, map);
				calibrationCount = 0;
			}
		}
	}

	@Override
	public void Calibrate(Map m, Orientation finalOrientation) {
		if(calibrationCount == 0) return;
		Orientation orient = this.o;
		List<Orientation> available = getAvailableCalibrations(m);
		if (available.size() == 0) {
			return;
		}
		if (available.size() == 1 && available.get(0) == Orientation.getClockwise(Orientation.getClockwise(this.o))){
			return;
		}
		try {
			prepareOrientation(prepareOrientationCmds(finalOrientation), m);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.calibrationCount = 0;
	}

	public void Calibrate(Map m) {
		System.out.println("Calibrating : " + this.position.getX() + ", " + this.position.getY() + " " + this.o.name());
		Calibrate(m, this.o);
	}

	@Override
	public void setFastestPath(List<RobotCommand> cmds) {
		this.fastestPathInstructions = cmds;
	}

	@Override
	public void doFastestPath(boolean toGoalZone) throws InterruptedException {
		if(this.fastestPathInstructions == null) return;
		if(toGoalZone) SyncObject.getSyncObject().SetFastestPath(fastestPathInstructions, this.position, this.o);
		for(RobotCommand cmd: this.fastestPathInstructions){
			doCommandWithSensor(cmd, null);
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
		if(m == null) return false;
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
}
