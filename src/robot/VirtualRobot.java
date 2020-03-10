package robot;

import java.util.ArrayList;
import java.util.List;

import constants.SensorConstants;
import connection.SyncObject;
import map.Map;
import utils.*;

public class VirtualRobot extends AbstractRobot {
	private List<RobotCommand> fastestPathInstructions;

	public VirtualRobot() {

	}

	//return string in the structure: "left,front,front,front,right"
	private List<Integer> getSensorValues() {
		Map realMap = Map.getRealMapInstance();
		List<Integer> sensorValues = new ArrayList<>();
		sensorValues.add(getSingleSensor(realMap, SensorConstants.LEFT_SENSOR));
		sensorValues.add(getSingleSensor(realMap, SensorConstants.FRONT_LEFT_SENSOR));
		sensorValues.add(getSingleSensor(realMap, SensorConstants.FRONT_MIDDLE_SENSOR));
		sensorValues.add(getSingleSensor(realMap, SensorConstants.FRONT_RIGHT_SENSOR)==SensorConstants.FRONT_RIGHT_SENSOR.GetRange()?SensorConstants.FRONT_RIGHT_SENSOR.GetRange():getSingleSensor(realMap, SensorConstants.FRONT_RIGHT_SENSOR)-1);
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
		}
		if(map != null){
			map.updateFromSensor(this.getSensorValues(), this.position, this.o);
		}
		SyncObject.getSyncObject().SetGUIUpdate(map, this.position, this.o);
		Thread.sleep(cmd == RobotCommand.MOVE_FORWARD? (int)(1000*SyncObject.getSyncObject().settings.getForwardWeight()) :
				(int)(1000*SyncObject.getSyncObject().settings.getTurningWeight()));    //int timePerStep = 1000/speed (ms)
	}

	@Override
	public void Calibrate(Map m, Orientation finalOrientation) {
		try {
			prepareOrientation(prepareOrientationCmds(finalOrientation), m);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void Calibrate(Map m) {
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

	@Override
	public boolean canCalibrate(Orientation o, Map m) {
		return true;
	}
}
