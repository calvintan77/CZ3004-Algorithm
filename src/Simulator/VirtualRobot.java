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
					if(map.getCell(x+i, y) == null || map.getCell(x+i, y).isObstacle())
						return i-1;

				}
				return maxValue;
			case LEFT:
				for (int i = 1; i <= maxValue; i++) {
					if(map.getCell(x-i, y) == null || map.getCell(x-i, y).isObstacle())
						return i-1;
				}
				return maxValue;
			case UP:
				for (int i = 1; i <= maxValue; i++) {
					if(map.getCell(x, y+i) == null || map.getCell(x, y+i).isObstacle())
						return i-1;
				}
				return maxValue;
			case DOWN:
				for (int i = 1; i <= maxValue; i++) {
					if(map.getCell(x, y-i) == null || map.getCell(x, y-i).isObstacle())
						return i-1;
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
		try {
			Thread.sleep(1000/speed); 	//int timePerStep = 1000/speed (ms)
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
		prepareOrientation(target, false);
	}

	@Override
	public List<Object[]> prepareOrientation(Orientation target, boolean checkSensors) {
		// Orientation update
		List<Object[]> updateList = new ArrayList<>();
		if(this.getOrientation() != target){
			int rightTurns = this.getOrientation().getRightTurns(target);
			if(rightTurns > 0) {
				for (int i = 0; i < rightTurns; i++) {
					this.doCommand(RobotCommand.TURN_RIGHT);
					if(checkSensors) updateList.add(new Object[] {getSensorValues(), o});
				}
			}else{
				for(int i = 0; i < -rightTurns; i++){
					this.doCommand(RobotCommand.TURN_LEFT);
					if(checkSensors) updateList.add(new Object[] {getSensorValues(), o});
				}
			}
		}
		return updateList;
	}

	@Override
	public void setOrientation(Orientation o) {
		this.o = o; 
	}

	@Override
	public HashMap<MapCell, Orientation> getSensorVisibilityCandidates(Map map, MapCell cell) {
		HashMap<MapCell, Orientation> candidates = new HashMap<>();
		//UP orientation
		//UP orientation for left sensor
		up_leftSensor: for(int i = cell.x+2; i<=cell.x+5; i++){
			//Check obstruction
			if (map.getCell(i-1, cell.y) == null || !map.getCell(i-1, cell.y).getSeen() 
					|| map.getCell(i-1, cell.y).isObstacle()) break;
			MapCell curr = map.getCell(i, cell.y - 1);
			if (curr == null) break;
			if (!curr.getSeen() ||curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.UP);
		}
		//UP orientation for front sensor
		MapCell cellOneRowUnder = map.getCell(cell.x, cell.y-1);
		MapCell cellTwoRowUnder = map.getCell(cell.x, cell.y-2);
		if (!( cellOneRowUnder == null || !cellOneRowUnder.getSeen() 
				|| cellOneRowUnder.isObstacle()) 
			&& !(cellTwoRowUnder == null || !cellTwoRowUnder.getSeen() 
					|| cellTwoRowUnder.isObstacle())) {
			up_frontSensor: for (int i = cell.x-1; i<=cell.x+1; i++) {
				for (int j = cell.y-2; j>=cell.y-3; j--) {
					MapCell curr = map.getCell(i,j);
					if (curr == null) break;
					if (!curr.getSeen() ||curr.isObstacle() || curr.isVirtualWall()) continue;
					candidates.put(curr, Orientation.UP);
				}
			}
		}
		//UP orientation for right sensor
		up_rightSensor: for(int i = cell.x-2; i>=cell.x-3; i--){
			//Check obstruction
			if (map.getCell(i+1, cell.y) == null || !map.getCell(i+1, cell.y).getSeen() 
					|| map.getCell(i+1, cell.y).isObstacle()) break;
			MapCell curr = map.getCell(i, cell.y - 1);
			if(curr == null || (!curr.getSeen()) ||curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.UP);
		}
		
		//DOWN orientation
		//DOWN orientation for left sensor
		down_leftSensor: for(int i = cell.x-2; i>=cell.x-5; i--){
			//Check obstruction
			if (map.getCell(i+1, cell.y) == null || !map.getCell(i+1, cell.y).getSeen() 
					|| map.getCell(i+1, cell.y).isObstacle()) break;
			MapCell curr = map.getCell(i, cell.y + 1);
			if (curr == null) break;
			if (!curr.getSeen() ||curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.DOWN);
		}
		//DOWN orientation for front sensor
		MapCell cellOneRowAbove = map.getCell(cell.x, cell.y+1);
		MapCell cellTwoRowAbove = map.getCell(cell.x, cell.y+2);
		if (!( cellOneRowAbove == null || !cellOneRowAbove.getSeen() 
				|| cellOneRowAbove.isObstacle()) 
			&& !(cellTwoRowAbove == null || !cellTwoRowAbove.getSeen() 
					|| cellTwoRowAbove.isObstacle())) {
			down_frontSensor: for (int i = cell.x-1; i<=cell.x+1; i++) {
				for (int j = cell.y+2; j<=cell.y+3; j++) {
					MapCell curr = map.getCell(i,j);
					if (curr == null) break;
					if (!curr.getSeen() ||curr.isObstacle() || curr.isVirtualWall()) continue;
					candidates.put(curr, Orientation.DOWN);
				}
			}
		}
		//DOWN orientation for right sensor
		down_rightSensor: for(int i = cell.x+2; i<=cell.x+3; i++){
			//Check obstruction
			if (map.getCell(i-1, cell.y) == null || !map.getCell(i-1, cell.y).getSeen() 
					|| map.getCell(i-1, cell.y).isObstacle()) break;
			MapCell curr = map.getCell(i, cell.y + 1);
			if(curr == null || (!curr.getSeen()) ||curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.DOWN);
		}
//		outer_down: for(int i = cell.x-2; i>=cell.x-5; i--){
//			MapCell curr = map.getCell(i, cell.y + 1);
//			if(curr == null || curr.isObstacle() || curr.isVirtualWall()) continue;
//			for(int i2 = i; i2 <= cell.x; i2++){
//				if(map.getCell(i2, cell.y) == null || map.getCell(i2, cell.y).isObstacle()){
//					continue outer_down;
//				}
//			}
//			candidates.put(curr, Orientation.DOWN);
//		}
		//RIGHT orientation
		//RIGHT orientation for left sensor
		right_leftSensor: for(int j = cell.y-2; j>=cell.y-5; j--){
			//Check obstruction
			if (map.getCell(cell.x, j+1) == null || !map.getCell(cell.x, j+1).getSeen() 
					|| map.getCell(cell.x, j+1).isObstacle()) break;
			MapCell curr = map.getCell(cell.x-1, j);
			if (curr == null) break;
			if (!curr.getSeen() ||curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.RIGHT);
		}
		//RIGHT orientation for front sensor
		MapCell cellOneRowToLeft = map.getCell(cell.x-1, cell.y);
		MapCell cellTwoRowToLeft = map.getCell(cell.x-2, cell.y);
		if (!( cellOneRowToLeft == null || !cellOneRowToLeft.getSeen() 
				|| cellOneRowToLeft.isObstacle()) 
			&& !(cellTwoRowToLeft == null || !cellTwoRowToLeft.getSeen() 
					|| cellTwoRowToLeft.isObstacle())) {
			right_frontSensor: for (int j = cell.y-1; j<=cell.y+1; j++) {
				for (int i = cell.x-2; i>=cell.x-3; i--) {
					MapCell curr = map.getCell(i,j);
					if (curr == null) break;
					if (!curr.getSeen() ||curr.isObstacle() || curr.isVirtualWall()) continue;
					candidates.put(curr, Orientation.RIGHT);
				}
			}
		}
		//RIGHT orientation for right sensor
		right_rightSensor: for(int j = cell.y+2; j<=cell.y+3; j++){
			//Check obstruction
			if (map.getCell(cell.x, j-1) == null || !map.getCell(cell.x, j-1).getSeen() 
					|| map.getCell(cell.x, j-1).isObstacle()) break;
			MapCell curr = map.getCell(cell.x-1, j);
			if(curr == null || (!curr.getSeen()) ||curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.RIGHT);
		}
//		outer_right: for(int j = cell.y+2; j>=cell.y+5; j--){
//			MapCell curr = map.getCell(cell.x - 1, j);
//			if(curr == null || curr.isObstacle() || curr.isVirtualWall()) continue;
//			for(int j2 = j; j2 >= cell.y; j2--){
//				if(map.getCell(cell.x, j2) == null || map.getCell(cell.x, j2).isObstacle()){
//					continue outer_right;
//				}
//			}
//			candidates.put(curr, Orientation.RIGHT);
//		}
		//LEFT orientation
		//LEFT orientation for leftSensor
		left_leftSensor: for(int j = cell.y+2; j<=cell.y+5; j++){
			//Check obstruction
			if (map.getCell(cell.x, j-1) == null || !map.getCell(cell.x, j-1).getSeen() 
					|| map.getCell(cell.x, j-1).isObstacle()) break;
			MapCell curr = map.getCell(cell.x+1, j);
			if (curr == null) break;
			if (!curr.getSeen() ||curr.isObstacle() || curr.isVirtualWall()) continue;
			candidates.put(curr, Orientation.RIGHT);
		}
		//LEFT orientation for front sensor
		MapCell cellOneRowToRight = map.getCell(cell.x+1, cell.y);
		MapCell cellTwoRowToRight = map.getCell(cell.x+2, cell.y);
		if (!( cellOneRowToRight == null || !cellOneRowToRight.getSeen() 
				|| cellOneRowToRight.isObstacle()) 
			&& !(cellTwoRowToRight == null || !cellTwoRowToRight.getSeen() 
					|| cellTwoRowToRight.isObstacle())) {
			left_frontSensor: for (int j = cell.y-1; j<=cell.y+1; j++) {
				for (int i = cell.x+2; i<=cell.x+3; i++) {
					MapCell curr = map.getCell(i,j);
					if (curr == null) break;
					if (!curr.getSeen() ||curr.isObstacle() || curr.isVirtualWall()) continue;
					candidates.put(curr, Orientation.RIGHT);
				}
			}
		}
		return candidates;
	}
}
