package Algorithms;

import Simulator.Robot;
import utils.Map;
import utils.Orientation;

public class MazeExplorer {
	private static MazeExplorer mazeExplorer;
	private Map map; 
	private Robot robot;
	private int[] robotPosition;
	private Orientation robotOrientation;
	private boolean reachGoalZone;
	private int timeLeft; 
	
	public static MazeExplorer getInstance() {
		if (mazeExplorer == null) {
			mazeExplorer = new MazeExplorer();
		}
		return mazeExplorer;
	}
	
	public void init(Map map, int timeLimit, double targetExplorePercent, Orientation orientation, int x, int y) {
		this.map = map;
		this.timeLeft = timeLimit;
		robotPosition[0] = x;
		robotPosition[1] = y;
	}
	
	public void setTimeLeft(int time) {
		this.timeLeft = time;
	}
	
	// explore given Map within time limit; assumes known endpoint, constant orientation 
//	public boolean exploreMaze(Map map, int timeLimit, double targetExplorePercent, int x, int y) {
//		// we exceeded time limit or we have fully explored the map
//		// probably start a timer here 
//		init(map, timeLimit, targetExplorePercent, x, y);
//		if (this.timeLeft > 0 || map.getExploredPercent() >= 100) {
//			return true;
//		}
//		// mark current cell as explored 
//		map.markCellExplored(x, y);
//		// check if 1. is obstacle 2. have enough space 
//		// later need to add impl to check if actually is obstacle
//		if (map.getCell(x, y).isObstacle()) {
//			return false; 
//		}
//		if (map.getCell(x, y).isExplored()) { // already explored portion
//			return false;
//		}
//		if (x == 1 && y == 1) {
//			return true; // we want to head to end point and repeat to start point 
//		}
//		// end timer here
//		boolean found = exploreMaze(map, timeLimit, targetExplorePercent, x, y+1) || exploreMaze(map, timeLimit, targetExplorePercent, x - 1, y) || exploreMaze(map, timeLimit, targetExplorePercent, x + 1, y) || exploreMaze(map, timeLimit, targetExplorePercent, x, y - 1);
//		if (found) { 
//			// check if enough space
//			// go there 
//		} else {
//			// dead end - do something?
//		}
//		return found; 
//	}
	
	public void exploreMaze2(Map map, int timeLimit, double targetExplorePercent, Orientation orientation, int x, int y) {
		init(map, timeLimit, targetExplorePercent, orientation, x, y);
		while (timeLeft > 0 && map.getExploredPercent() < targetExplorePercent) {
			String[] sensorValues = robot.getSensorValues(robotPosition, robotOrientation).split(",");
			
			
		}
	}
}