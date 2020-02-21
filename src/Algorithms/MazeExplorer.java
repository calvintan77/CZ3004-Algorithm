package Algorithms;

import Simulator.IRobot;
import utils.*;

import utils.Map;
import utils.Orientation;

import java.util.*;
import java.util.stream.Collectors;

public class MazeExplorer {
	private static MazeExplorer mazeExplorer;
	private static IRobot robot;
	// private Map map; 	
	public static MazeExplorer getInstance() {
		if (mazeExplorer == null) {
			mazeExplorer = new MazeExplorer();
		}
		return mazeExplorer;
	}
	
	// a maze explorer is tied to 1 robot; inelegant way to tie them together because of the way we get instance...
	public void setRobot(IRobot r) {
		MazeExplorer.robot = r; 
	}
	
	// no defensive checks - caller calls at own risk 
	public IRobot getRobot() {
		return MazeExplorer.robot; 
	}
	
	// explore given Map within time limit; assumes known endpoint, constant orientation 
	public boolean exploreMaze(Map map, int timeLimit, int x, int y) {
		// we exceeded time limit or we have fully explored the map
		// probably start a timer here 
		if (timeLimit < timeLimit || map.getNumExplored() >= 100) {
			return true;
		}
		// mark current cell as explored 
		map.markCellExplored(x, y);
		// check if 1. is obstacle 2. have enough space 
		// later need to add impl to check if actually is obstacle
		if (map.getCell(x, y).isObstacle()) {
			return false; 
		}
		if (map.getCell(x, y).isExplored()) { // already explored portion
			return false;
		}
		if (x == 1 && y == 1) {
			return true; // we want to head to end point and repeat to start point 
		}
		// end timer here
		boolean found = exploreMaze(map, timeLimit, x, y+1) || exploreMaze(map, timeLimit, x - 1, y) || exploreMaze(map, timeLimit, x + 1, y) || exploreMaze(map, timeLimit, x, y - 1);
		if (found) { 
			// check if enough space
			// go there 
		} else {
			// dead end - do something?
		}
		return found; 
	}

	// explore given map within given time limit - iter 
	// at every step we need to update sensor values
	// after update, determine is there are any virtual walls in front
	// while NO VIRTUAL WALL we can move 
	// TODO: translate orientation to actual move 
	/**
	 * right wall hug to explore  
	 * @param map: the given map which we are doing exploration on - start point is assumed to be (1,1) 
	 * @param timeLimit: the time (in seconds) which we have to explore the maze 
	 */
	public void exploreMaze(Map map, long timeLimit) { 
		long startTime = System.nanoTime();
		long tLimit = timeLimit * (1000000000);

		// initial calibration
		robot.setPosition(1, 1);
		// TODO: change method signature of setOrientation to accept Orientation as param instead of int...
		robot.setOrientation(Orientation.UP); // facing right
		robot.doCommand(RobotCommand.TURN_RIGHT);
		do { 
			// check sensor values; update cells 
			map.updateFromSensor(robot.getSensorValues(), robot.getPosition(), robot.getOrientation());
			// choose direction after updating values
			Orientation nextOrientation = this.chooseDirection(map, map.getCell(robot.getPosition()), robot.getOrientation());
			// translate orientation to actual command
			// this does not actually work
			// update robot's internal state
			robot.prepareOrientation(nextOrientation);
			// Position update
			robot.doCommand(RobotCommand.MOVE_FORWARD);
		}
//		while (System.nanoTime() - startTime < tLimit && (robot.getPosition().getX() != 1 || robot.getPosition().getY() != 1));
		while ((robot.getPosition().getX() != 1 || robot.getPosition().getY() != 1));
		
		// after exiting the loop above, we are guaranteed to be at the start zone - check if map fully explored 
		while (map.getNumExplored() < 300 && System.nanoTime() - startTime < tLimit) {
			// enqueue all unseen cells 
			List<MapCell> unseen = map.getAllUnseen(); 
			// shortest path to unseen 
			// fuck doing fp in java
			List<Coordinate> seenNeighbours = unseen.stream().map(map::getNeighbours).map(HashMap::values).flatMap(Collection::stream).filter(MapCell::getSeen).map(cell -> new Coordinate(cell.x, cell.y)).collect(Collectors.toList());
			// change to List<GraphNode> ProcessMap(Map map, List<Coordinate> StartingPoints, List<Coordinate> EndingPoints)
			try {
				List<Coordinate> start = new LinkedList<>();
				start.add(robot.getPosition());
				List<GraphNode> nodes = MapProcessor.ProcessMap(map, start, seenNeighbours);
				ShortestPath toUnexploredPoint = AStarAlgo.AStarSearch(nodes.get(0), nodes.get(1));
				// Orientation update
				robot.prepareOrientation(toUnexploredPoint.getStartingOrientation());
				for(RobotCommand cmd: toUnexploredPoint.generateInstructions()){
					if(cmd == RobotCommand.MOVE_FORWARD && checkObstruction(map, robot.getOrientation(), robot.getPosition())) break;
					robot.doCommand(cmd);
					map.updateFromSensor(robot.getSensorValues(), robot.getPosition(), robot.getOrientation());
				}
			} catch (Exception e) {
				System.out.println(":(");
				break;
				// if we cannot find a path we die
			}
		}
		
		// path back to start position
		try {
			List<Coordinate> start = new LinkedList<>();
			start.add(robot.getPosition());
			List<Coordinate> end = new LinkedList<>();
			end.add(new Coordinate(1,1));
			List<GraphNode> nodes = MapProcessor.ProcessMap(map, start, end);
			ShortestPath toStartingPoint = AStarAlgo.AStarSearch(nodes.get(0), nodes.get(1));
			robot.prepareOrientation(toStartingPoint.getStartingOrientation());
			for(RobotCommand cmd: toStartingPoint.generateInstructions()){
				robot.doCommand(cmd);
			}
		} catch (Exception e) {
			
		}
	}

	private boolean checkObstruction(Map map, Orientation o, Coordinate c) {
		int x = c.getX();
		int y = c.getY();
		switch (o){
			case UP:
				return (map.getCell(x, y+1).isVirtualWall()||map.getCell(x, y+1).isObstacle());
			case DOWN:
				return (map.getCell(x, y-1).isVirtualWall()||map.getCell(x, y-1).isObstacle());
			case RIGHT:
				return (map.getCell(x+1, y).isVirtualWall()||map.getCell(x+1, y).isObstacle());
			case LEFT:
				return (map.getCell(x-1, y).isVirtualWall()||map.getCell(x-1, y).isObstacle());
		}
		return true;
	}

	/**
	 * choose a next direction to traverse given your current position and orientation
	 * @param map: the current explored map 
	 * @param curPos: robot's current position
	 * @param o: robot's current orientation
	 * @return Orientation: the cardinal direction of your next move - right wall hug if possible else smallest dist 
	 */
	public Orientation chooseDirection(Map map, MapCell curPos, Orientation o) {
		// wall hug right - right, up, left, down
		// switch based on orientation and return
		// neighbours is based on absolute orientation (north, south, east, west) 
		HashMap<String, MapCell> neighbours = map.getNeighbours(curPos);
		switch (o) {
			case DOWN:
				// if we are facing down, in order to right wall hug, we try to keep "right" (this case left)
				// then go "up" (down) etc
				if (neighbours.containsKey("left")) {
					return Orientation.LEFT;
				} else if (neighbours.containsKey("down")) {
					return Orientation.DOWN;
				} else if (neighbours.containsKey("right")) {
					return Orientation.RIGHT;
				} else return Orientation.UP;
			case LEFT:
				// up, left, down, right
				if (neighbours.containsKey("up")) {
					return Orientation.UP;
				} else if (neighbours.containsKey("left")) {
					return Orientation.LEFT;
				} else if (neighbours.containsKey("down")) {
					return Orientation.DOWN;
				} else return Orientation.RIGHT;
			case UP:
				// normal
				if (neighbours.containsKey("right")) {
					return Orientation.RIGHT;
				} else if (neighbours.containsKey("up")) {
					return Orientation.UP;
				} else if (neighbours.containsKey("left")) {
					return Orientation.LEFT;
				} else return Orientation.DOWN;
			case RIGHT:
				// normal
				if (neighbours.containsKey("down")) {
					return Orientation.DOWN;
				} else if (neighbours.containsKey("right")) {
					return Orientation.RIGHT;
				} else if (neighbours.containsKey("up")) {
					return Orientation.UP;
				} else return Orientation.LEFT;
		}
		return null;
	}
}

