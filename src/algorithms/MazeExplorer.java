package algorithms;

import maze.MapCell;
import path.GraphNode;
import path.ShortestPath;
import robot.AbstractRobot;
import utils.*;

import maze.Map;

import java.util.*;
import java.util.stream.Collectors;

public class MazeExplorer {
	private final AbstractRobot robot;
	private static final double BUFFER = 1.5  * (1000000000);

	public MazeExplorer(AbstractRobot r){
		this.robot = r;
	}

	// explore given map within given time limit - iter 
	// at every step we need to update sensor values
	// after update, determine is there are any virtual walls in front
	// while NO VIRTUAL WALL we can move  
	/**
	 * right wall hug to explore  
	 * @param map: the given map which we are doing exploration on - start point is assumed to be (1,1) 
	 * @param timeLimit: the time (in seconds) which we have to explore the maze 
	 */
	public void exploreMaze(Map map, long timeLimit, int targetCoverage) throws InterruptedException{
		long startTime = System.nanoTime();
		long tLimit = timeLimit * (1000000000);

		// initial calibration
		if (robot.getPosition() == null)
			robot.setPosition(1, 1);
		if (robot.getOrientation() == null)
			robot.setOrientation(Orientation.UP); // facing right
		robot.doCommandWithSensor(RobotCommand.TURN_RIGHT, map);
		double weight = 0;
		// Initial Right Wall Hug
		do { 
			// choose direction after updating values
			Orientation nextOrientation = this.chooseDirection(map, map.getCell(robot.getPosition()), robot.getOrientation());
			// translate orientation to actual command
			// update robot's internal state
			robot.prepareOrientation(robot.prepareOrientationCmds(nextOrientation),map);
			// Position update
			robot.doCommandWithSensor(RobotCommand.MOVE_FORWARD, map);
			if (robot.canCalibrate(robot.getOrientation(), map) || robot.getPosition().equals(new Coordinate(14, 19))) {
				robot.Calibrate(map); 
			}
			try {
				weight = getPathToStart(map).getWeight();
			}catch (Exception e) {
				System.out.println("MazeExplorer: " + e.toString());
			}
		}
		while (System.nanoTime() - startTime + weight  * (1000000000) + BUFFER < tLimit && map.getSeenPercentage() < targetCoverage && (robot.getPosition().getX() != 1 || robot.getPosition().getY() != 1));
		
		// after exiting the loop above, we are guaranteed to be at the start zone - check if map fully explored
		// enqueue all unseen cells
		List<MapCell> unseen = map.getAllUnseen();
		while (map.getSeenPercentage() < targetCoverage && System.nanoTime() - startTime + weight  * (1000000000) + BUFFER < tLimit) {
			// shortest path to unseen
			// fuck doing fp in java
			List<Coordinate> seenNeighbours = unseen.stream().map(map::getNeighbours).map(HashMap::values).flatMap(Collection::stream).filter(MapCell::isSeen).map(cell -> new Coordinate(cell.x, cell.y)).collect(Collectors.toList());
			try {
				List<Coordinate> start = GetStartingCoords();
				List<GraphNode> nodes = MapProcessor.ProcessMap(map, start, seenNeighbours);
				ShortestPath toUnexploredPoint = AStarAlgo.AStarSearch(nodes.get(0), nodes.get(1));
				// Orientation update
				if(IsMakingWeirdTurns(toUnexploredPoint)){
					toUnexploredPoint.getPath().remove(1);
				}
				robot.prepareOrientation(robot.prepareOrientationCmds(toUnexploredPoint.getStartingOrientation()),map);
				for(RobotCommand cmd: toUnexploredPoint.generateInstructions()){
					if(!(map.getSeenPercentage() < targetCoverage && System.nanoTime() - startTime + weight  * (1000000000) + BUFFER < tLimit)) break;
					if(cmd == RobotCommand.MOVE_FORWARD && checkObstruction(map, robot.getOrientation(), robot.getPosition())) break;
					long numUnseen = unseen.stream().filter(x -> !x.isSeen()).count();
					if (numUnseen != unseen.size()) break;
					robot.doCommandWithSensor(cmd, map);
				}
				weight = getPathToStart(map).getWeight();
				unseen = unseen.stream().filter(x -> !x.isSeen()).collect(Collectors.toList());
			} catch (Exception e) {
				System.out.println("Unable to use typical route, attempting to brute force candidates :(");
				HashMap<MapCell, Orientation> candidates = new HashMap<>();
				unseen.stream().map(cell -> robot.getSensorVisibilityCandidates(map, cell)).flatMap(maps -> maps.entrySet().stream()).forEach(x -> candidates.put(x.getKey(), x.getValue()));
				List<Coordinate> destinations = candidates.keySet().stream().map(cell -> new Coordinate(cell.x, cell.y, candidates.get(cell).isAligned(true) ? Coordinate.Facing.HORIZONTAL : Coordinate.Facing.VERTICAL)).collect(Collectors.toList());
				try {
					List<Coordinate> start = GetStartingCoords();
					List<GraphNode> nodes = MapProcessor.ProcessMap(map, start, destinations);
					ShortestPath toUnexploredPoint = AStarAlgo.AStarSearch(nodes.get(0), nodes.get(1));
					// Orientation update
					if(IsMakingWeirdTurns(toUnexploredPoint)) {
						toUnexploredPoint.getPath().remove(1);
					}
					robot.prepareOrientation(robot.prepareOrientationCmds(toUnexploredPoint.getStartingOrientation()),map);
					// Loop over commands until discover new unseen
					for (RobotCommand cmd : toUnexploredPoint.generateInstructions()) {
						if(!(map.getSeenPercentage() < targetCoverage && System.nanoTime()  - startTime + weight  * (1000000000) + BUFFER < tLimit)) break;
						if (cmd == RobotCommand.MOVE_FORWARD && checkObstruction(map, robot.getOrientation(), robot.getPosition()))
							break;
						long numUnseen = unseen.stream().filter(x -> !x.isSeen()).count();
						if (numUnseen != unseen.size()) break;
						robot.doCommandWithSensor(cmd, map);
					}
					// See edge case cell
					robot.prepareOrientation(robot.prepareOrientationCmds(candidates.get(map.getCell(toUnexploredPoint.getDestination()))), map);
					weight = getPathToStart(map).getWeight();
					unseen = unseen.stream().filter(x -> !x.isSeen()).collect(Collectors.toList());
				} catch (Exception e2) {
					System.out.println("Unable to access cell, cutting losses");
					break;
				}
			}
		}
		
		// path back to start position
		try {
			ShortestPath toStartingPoint;
			if(!(map.getSeenPercentage() < targetCoverage && System.nanoTime()  - startTime + weight  * (1000000000) + BUFFER < tLimit)) {
				toStartingPoint = getPathToStart(map.CloneWithUnseenAsObstacles());
			}else {
				toStartingPoint = getPathToStart(map);
			}
			// Set orientation to face the right way to go to start
			List<RobotCommand> prepOrientation = robot.prepareOrientationCmds(toStartingPoint.getStartingOrientation());
			prepOrientation.addAll(toStartingPoint.generateInstructions());
			robot.setFastestPath(prepOrientation);
			robot.doFastestPath(false);
			// Prepare for FP to goalzone
			robot.getPosition().setFacing(Coordinate.Facing.NONE);
		} catch (Exception e) {
			System.out.println("MazeExplorer: " + e.toString());
		}
	}

	private List<Coordinate> GetStartingCoords(){
		List<Coordinate> start = new LinkedList<>();
		start.add(robot.getPosition());
		switch (robot.getOrientation()) {
			case UP:case DOWN: start.get(0).setFacing(Coordinate.Facing.VERTICAL); break;
			case LEFT: case RIGHT: start.get(0).setFacing(Coordinate.Facing.HORIZONTAL); break;
		}
		return start;
	}
	
	private ShortestPath getPathToStart(Map map) throws Exception {
		List<Coordinate> start = GetStartingCoords();
		List<Coordinate> end = new LinkedList<>();
		end.add(new Coordinate(1,1));
		List<GraphNode> nodes = MapProcessor.ProcessMap(map, start, end);
		ShortestPath toStartingPoint = AStarAlgo.AStarSearch(nodes.get(0), nodes.get(1));
		return toStartingPoint;
	}

	private boolean IsMakingWeirdTurns(ShortestPath path){
		return (path.getStartingOrientation().getRightTurns(robot.getOrientation()) == 2 ||
				path.getStartingOrientation().getRightTurns(robot.getOrientation()) == -2) &&
				path.getPath().size() > 2 &&
				path.getPath().get(2).getX() == robot.getPosition().getX() &&
				path.getPath().get(2).getY() == robot.getPosition().getY();
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

