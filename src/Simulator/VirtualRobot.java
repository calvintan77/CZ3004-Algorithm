package Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		switch(this.o) {
			case UP:
				sensorValues.add(getSingleSensor(realMap, 4, getPosition().getX()-1, getPosition().getY()+1, Orientation.getCounterClockwise(getOrientation())));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()-1, getPosition().getY()+1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX(), getPosition().getY()+1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()+1, getPosition().getY()+1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()+1, getPosition().getY()+1, Orientation.getClockwise(getOrientation())));
				break;
			case LEFT:
				sensorValues.add(getSingleSensor(realMap, 4, getPosition().getX()-1, getPosition().getY()-1, Orientation.getCounterClockwise(getOrientation())));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()-1, getPosition().getY()-1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()-1, getPosition().getY(), getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()-1, getPosition().getY()+1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()-1, getPosition().getY()+1, Orientation.getClockwise(getOrientation())));
				break;
			case RIGHT:
				sensorValues.add(getSingleSensor(realMap, 4, getPosition().getX()+1, getPosition().getY()+1, Orientation.getCounterClockwise(getOrientation())));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()+1, getPosition().getY()+1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()+1, getPosition().getY(), getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()+1, getPosition().getY()-1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()+1, getPosition().getY()-1, Orientation.getClockwise(getOrientation())));
				break;
			case DOWN:
				sensorValues.add(getSingleSensor(realMap, 4, getPosition().getX()+1, getPosition().getY()-1, Orientation.getCounterClockwise(getOrientation())));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()+1, getPosition().getY()-1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX(), getPosition().getY()-1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()-1, getPosition().getY()-1, getOrientation()));
				sensorValues.add(getSingleSensor(realMap, 2, getPosition().getX()-1, getPosition().getY()-1, Orientation.getClockwise(getOrientation())));
				break;
				
		}
		
		return sensorValues;
	}

	/**
	 * updates value based on a single sensor
	 * @param map: actual map
	 * @param maxValue: max sensor reading
	 * @param x: actual sensor x
	 * @param y: actual sensor y
	 * @param o: sensor orientation relative to map
	 */
	public int getSingleSensor(Map map, int maxValue, int x, int y, Orientation o) {
		switch (o) {
			case RIGHT:
				// update all seen
				for (int i = 1; i <= maxValue; i++) {
					if(map.getCell(x+i, y) == null)return i-1;
					if(map.getCell(x+i, y).isObstacle()) return i-1;
				}
				return maxValue;
			case LEFT:
				for (int i = 1; i <= maxValue; i++) {
					if(map.getCell(x-i, y) == null)return i-1;
					if(map.getCell(x-i, y).isObstacle()) return i-1;
				}
				return maxValue;
			case UP:
				for (int i = 1; i <= maxValue; i++) {
					if(map.getCell(x, y+i) == null)return i-1;
					if(map.getCell(x, y+i).isObstacle()) return i-1;
				}
				return maxValue;
			case DOWN:
				for (int i = 1; i <= maxValue; i++) {
					if(map.getCell(x, y-i) == null)return i-1;
					if(map.getCell(x, y-i).isObstacle()) return i-1;
				}
				return maxValue;
		}
		return -1;
	}
	
	public void doCommand(RobotCommand cmd){
		switch (cmd){
			case TURN_LEFT:
				this.setOrientation(Orientation.getCounterClockwise(this.o));
				break;
			case TURN_RIGHT:
				this.setOrientation(Orientation.getClockwise(this.o));
				break;
			case MOVE_FORWARD:
				switch(this.o){
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
		GUI.getInstance().updateRobotUI(cmd);

		try {
			Thread.sleep(1000/speed); 	//int timePerStep = 1000/speed (ms)
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
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
		// Orientation update
		if(this.getOrientation() != target){
			int rightTurns = this.getOrientation().getRightTurns(target);
			if(rightTurns > 0) {
				for (int i = 0; i < rightTurns; i++) {
					this.doCommand(RobotCommand.TURN_RIGHT);
				}
			}else{
				for(int i = 0; i < -rightTurns; i++){
					this.doCommand(RobotCommand.TURN_LEFT);
				}
			}
		}

	}

	@Override
	public void setOrientation(Orientation o) {
		this.o = o; 
	}

	@Override
	public HashMap<MapCell, Orientation> getLeftSensorVisibilityCandidates(Map map, MapCell cell) {
		HashMap<MapCell, Orientation> candidates = new HashMap<>();
		//UP orientation
		for(int i = cell.x+2; i<=cell.x+5; i++){
			MapCell curr = map.getCell(i, cell.y - 1);
			if(curr == null || curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.UP);
		}
		//DOWN orientation
		for(int i = cell.x-2; i>=cell.x-5; i--){
			MapCell curr = map.getCell(i, cell.y + 1);
			if(curr == null || curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.DOWN);
		}
		//RIGHT orientation
		for(int j = cell.y+2; j>=cell.y+5; j--){
			MapCell curr = map.getCell(cell.x - 1, j);
			if(curr == null || curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.RIGHT);
		}
		//LEFT orientation
		for(int j = cell.y-2; j>=cell.y-5; j--){
			MapCell curr = map.getCell(cell.x + 1, j);
			if(curr == null || curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.LEFT);
		}
		return candidates;
	}
}
