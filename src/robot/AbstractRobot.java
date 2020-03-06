package robot;

import constants.SensorConstants;
import maze.Map;
import maze.MapCell;
import utils.*;

import java.util.*;

public abstract class AbstractRobot {
	protected Coordinate position = new Coordinate(1,1);
	protected Orientation o = Orientation.UP;
	protected abstract List<Integer> getSensorValues();
	public abstract void doCommandWithSensor(RobotCommand cmd, Map map) throws InterruptedException;
	public abstract void prepareOrientation(List<RobotCommand> cmds, Map map) throws InterruptedException;
	public Orientation getOrientation(){
		return this.o;
	}
	public Coordinate getPosition(){
		return this.position;
	}
	public void setPosition(int x, int y){
		this.position = new Coordinate(x, y);
	}
	public void setOrientation(Orientation o){
		this.o = o;
	}
	public List<RobotCommand> prepareOrientationCmds(Orientation target){
		return prepareAnyOrientation(this.o, target);
	}

	protected List<RobotCommand> prepareAnyOrientation(Orientation from, Orientation target){
		List<RobotCommand> avaiCommands = new ArrayList<>();
		if (from != target) {
			int rightTurns = from.getRightTurns(target);
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
	public HashMap<MapCell, Orientation> getSensorVisibilityCandidates(Map map, MapCell cell){
		HashMap<MapCell, Orientation> candidates = new HashMap<>();
		List<Sensor> sensors = new ArrayList<>();
		sensors.add(SensorConstants.LEFT_SENSOR);
		sensors.add(SensorConstants.FRONT_LEFT_SENSOR);
		sensors.add(SensorConstants.FRONT_MIDDLE_SENSOR);
		sensors.add(SensorConstants.FRONT_RIGHT_SENSOR);
		sensors.add(SensorConstants.RIGHT_SENSOR);
		int maxRange = sensors.stream().map(Sensor::GetRange).max(Integer::compare).orElse(0);
		//POSITION IS LEFT OF ROBOT IN MAP
		for (int i = 1; i <= maxRange; i++) {
			Coordinate candCoord = new Coordinate(cell.x + i, cell.y);
			MapCell cand = map.getCell(candCoord);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_LEFT_SENSOR, candCoord, Orientation.LEFT);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.LEFT_SENSOR, candCoord, Orientation.UP);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_RIGHT_SENSOR, candCoord, Orientation.LEFT);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.RIGHT_SENSOR, candCoord, Orientation.DOWN);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_MIDDLE_SENSOR, candCoord, Orientation.LEFT);
		}

		//POSITION IS RIGHT OF ROBOT IN MAP
		for (int i = 1; i <= maxRange; i++) {
			Coordinate candCoord = new Coordinate(cell.x - i, cell.y);
			MapCell cand = map.getCell(candCoord);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_LEFT_SENSOR, candCoord, Orientation.RIGHT);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.LEFT_SENSOR, candCoord, Orientation.DOWN);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_RIGHT_SENSOR, candCoord, Orientation.RIGHT);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.RIGHT_SENSOR, candCoord, Orientation.UP);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_MIDDLE_SENSOR, candCoord, Orientation.RIGHT);
		}

		//POSITION IS BELOW THE ROBOT IN MAP
		for (int i = 1; i <= maxRange; i++) {
			Coordinate candCoord = new Coordinate(cell.x, cell.y + i);
			MapCell cand = map.getCell(candCoord);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_LEFT_SENSOR, candCoord, Orientation.DOWN);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.LEFT_SENSOR, candCoord, Orientation.LEFT);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_RIGHT_SENSOR, candCoord, Orientation.DOWN);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.RIGHT_SENSOR, candCoord, Orientation.RIGHT);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_MIDDLE_SENSOR, candCoord, Orientation.DOWN);
		}

		//POSITION IS ABOVE THE ROBOT IN MAP
		for (int i = 1; i <= maxRange; i++) {
			Coordinate candCoord = new Coordinate(cell.x, cell.y - i);
			MapCell cand = map.getCell(candCoord);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_LEFT_SENSOR, candCoord, Orientation.UP);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.LEFT_SENSOR, candCoord, Orientation.RIGHT);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_RIGHT_SENSOR, candCoord, Orientation.UP);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.RIGHT_SENSOR, candCoord, Orientation.LEFT);
			AddToHashmapAfterCheck(map, candidates, i, SensorConstants.FRONT_MIDDLE_SENSOR, candCoord, Orientation.UP);
		}
		return candidates;
	}

	private void AddToHashmapAfterCheck(Map map, HashMap<MapCell, Orientation> candidates, int distance, Sensor sensor, Coordinate candCoord, Orientation robotOrientation){
		if(distance <= sensor.GetRange()) {
			MapCell check = map.getCell(sensor.GetRobotPositionFromSensorPos(robotOrientation, candCoord));
			if (isViableCell(check) && !candidates.containsKey(check)) candidates.put(check, robotOrientation);
		}
	}

	private boolean isViableCell(MapCell cell) {
		return cell != null && !cell.isObstacle() && !cell.isVirtualWall();
	}

	public abstract boolean Calibrate(Map m);
	public abstract void setFastestPath(List<RobotCommand> cmds);
	public abstract void doFastestPath(boolean toGoalZone) throws InterruptedException;
	public abstract boolean canCalibrate(Orientation o, Map m);
}
