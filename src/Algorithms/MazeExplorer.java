package Algorithms;

import utils.Map;

public class MazeExplorer {
	private static MazeExplorer mazeExplorer;
	private Map map; 
	private int timeLimit; 
	
	public static MazeExplorer getInstance() {
		if (mazeExplorer == null) {
			mazeExplorer = new MazeExplorer();
		}
		return mazeExplorer;
	}
	
	// explore given Map within time limit; assumes known endpoint, constant orientation 
	public boolean exploreMaze(Map map, int timeLimit, int x, int y) {
		// we exceeded time limit or we have fully explored the map
		// probably start a timer here 
		if (timeLimit < this.timeLimit || map.getExploredPercent() >= 100) {
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
}