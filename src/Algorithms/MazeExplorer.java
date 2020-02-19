package Algorithms;

import utils.GraphNode;
import utils.Map;
import utils.MapCell;
import utils.Orientation;
import utils.RobotCommand;
import utils.ShortestPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Simulator.Robot; 

public class MazeExplorer {
	private static MazeExplorer mazeExplorer;
	private static Robot robot; 
	// private Map map; 	
	public static MazeExplorer getInstance() {
		if (mazeExplorer == null) {
			mazeExplorer = new MazeExplorer();
		}
		return mazeExplorer;
	}
	
	// a maze explorer is tied to 1 robot; inelegant way to tie them together because of the way we get instance...
	public void setRobot(Robot r) {
		MazeExplorer.robot = r; 
	}
	
	// no defensive checks - caller calls at own risk 
	public Robot getRobot() {
		return MazeExplorer.robot; 
	}
	
	// explore given Map within time limit; assumes known endpoint, constant orientation 
	public boolean exploreMaze(Map map, int timeLimit, int x, int y) {
		// we exceeded time limit or we have fully explored the map
		// probably start a timer here 
		if (timeLimit < timeLimit || map.getExploredPercent() >= 100) {
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
		robot.setOrientation(Orientation.RIGHT); // facing right 
		// TODO: change to do/while so initial check passes
		do { 
			// check sensor values; update cells 
			String toUpdate = robot.getSensorValues(robot.getPosition(), robot.getOrientation()); // toUpdate is a string
			map.updateFromSensor(toUpdate);
			// choose direction after updating values
			Orientation o = this.chooseDirection(map, new MapCell(robot.getPosition()), robot.getOrientation());
			// translate orientation to actual command
			// this does not actually work
			robot.doCommand(RobotCommand.valueOf(o.toString()));
		}
		while (System.nanoTime() - startTime < tLimit && robot.getPosition()[0] != 1 && robot.getPosition()[1] != 1); 
		
		// after exiting the loop above, we are guaranteed to be at the start zone - check if map fully explored 
		while (map.getExploredPercent() < 100 && System.nanoTime() - startTime < tLimit) { 
			// enqueue all unseen cells 
			List<MapCell> unseen = map.getAllUnseen(); 
			// shortest path to unseen 
			for (MapCell m : unseen) { 
				try {
				ShortestPath p = AStarAlgo.AStarSearch(new GraphNode(robot.getPosition()[0], robot.getPosition()[1], false), new GraphNode(m.x, m.y, false));
				} 
				
				catch (Exception e) {
					// if we cannot find a path we die 
				}
			}
		}
		
		// path back to start position
		try {
		ShortestPath p = AStarAlgo.AStarSearch(new GraphNode(robot.getPosition()[0], robot.getPosition()[1], false), new GraphNode(1, 1, false));
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * choose a next direction to traverse given your current position and orientation
	 * @param map: the current explored map 
	 * @param curPos: robot's current position
	 * @param Orientation: robot's current orientation
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
		default:
			// normal 
			if (neighbours.containsKey("right")) {
				return Orientation.RIGHT;
			} else if (neighbours.containsKey("up")) {
				return Orientation.UP;
			} else if (neighbours.containsKey("left")) {
				return Orientation.LEFT; 
			} else return Orientation.DOWN; 
		}
	}
}
