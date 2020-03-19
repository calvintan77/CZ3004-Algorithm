package algorithms;

import map.MapCell;
import path.GraphNode;
import path.ShortestPath;
import robot.AbstractRobot;
import utils.*;

import map.Map;

import javax.naming.TimeLimitExceededException;
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
			Orientation original = robot.getOrientation();
			Orientation nextOrientation = this.chooseDirection(map, map.getCell(robot.getPosition()), robot.getOrientation());
			if(robot.getOrientation().getRightTurns(nextOrientation) != 0) {
				if (robot.getOrientation().getRightTurns(nextOrientation) != 1) {
					Orientation other = Orientation.getClockwise(robot.getOrientation());
					if (ShouldSee(other, robot.getPosition(), map)) {
						robot.prepareOrientation(robot.prepareOrientationCmds(other), map);
						if (robot.canCalibrate(robot.getOrientation(), map) || robot.getPosition().equals(new Coordinate(14, 19))) {
							if (robot.getOrientation().getRightTurns(nextOrientation) == -1) {
								robot.Calibrate(map, original);
							} else {
								robot.Calibrate(map);
							}
						}
					}
				}
				robot.prepareOrientation(robot.prepareOrientationCmds(nextOrientation), map);
				if(checkObstruction(map, robot.getOrientation(), robot.getPosition())){
					robot.prepareOrientation(robot.prepareOrientationCmds(original), map);
					continue;
				}
			}
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
		while (!ExitCondition(map, weight, startTime, targetCoverage, tLimit)) {
			try {
				ShortestPath toUnexploredPoint = GetShortestPathToFrontier(map, unseen);
				if(toUnexploredPoint != null){
					DoShortestPathWithSensor(toUnexploredPoint, map, unseen, weight, startTime, targetCoverage, tLimit);
				}else{
					System.out.println("Unable to use typical route, attempting to brute force candidates :(");
					HashMap<MapCell, Orientation> candidates = new HashMap<>();
					unseen.stream().map(cell -> robot.getSensorVisibilityCandidates(map, cell)).flatMap(maps -> maps.entrySet().stream()).forEach(x -> candidates.put(x.getKey(), x.getValue()));
					toUnexploredPoint = GetShortestPathToCandidates(map, candidates);
					if(toUnexploredPoint == null || toUnexploredPoint.generateInstructions().size() == 0){
						break;
					}
					if(DoShortestPathWithSensor(toUnexploredPoint, map, unseen, weight, startTime, targetCoverage, tLimit)) {
						for(RobotCommand command: robot.prepareOrientationCmds(candidates.get(map.getCell(toUnexploredPoint.getDestination())))){
							robot.doCommandWithSensor(command, map);
							if (robot.canCalibrate(robot.getOrientation(), map) || robot.getPosition().equals(new Coordinate(14, 19))) {
								robot.Calibrate(map);
							}
							long numUnseen = unseen.stream().filter(x -> !x.isSeen()).count();
							if (numUnseen != unseen.size()) {
								break;
							}
						}
					}
				}
				weight = getPathToStart(map).getWeight();
				unseen = unseen.stream().filter(x -> !x.isSeen()).collect(Collectors.toList());
			} catch (Exception e) {
				System.out.println("Unable to access remaining cells, cutting losses");
			}
		}
		robot.doCommandWithSensor(RobotCommand.NO_OP, map);
		System.out.println("RETURNING TO START");

		// path back to start position
		try {
			ShortestPath toStartingPoint;
			if(ExitCondition(map, weight, startTime, targetCoverage, tLimit)) {
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
			robot.setPosition(toStartingPoint.getDestination().getX(), toStartingPoint.getDestination().getY());
			robot.getPosition().setFacing(Coordinate.Facing.NONE);
			robot.setOrientation(toStartingPoint.getEndingOrientation());
		} catch (Exception e) {
			System.out.println("MazeExplorer: " + e.toString());
			e.printStackTrace();
		}
		System.out.println("END OF EXPLORATION");
	}

	/**
	 * Helper method to see if robot should face orientation o to see unseen blocks.
	 * @param o - orientation to face
	 * @param pos - robot position
	 * @param map - map to reference
	 * @return true if robot can see more facing that direction
	 */
	private boolean ShouldSee(Orientation o, Coordinate pos, Map map){
		switch(o){
			case UP:
				for(int x = pos.getX() - 1; x <= pos.getX() + 1; x++){
					MapCell cell = map.getCell(x, pos.getY() + 2);
					//if(cell == null || (cell.isSeen() && !cell.isObstacle())) return false;
					if(cell != null && !cell.isSeen()) return true;
				}
				break;
			case LEFT:
				for(int y = pos.getY() - 1; y <= pos.getY() + 1; y++){
					MapCell cell = map.getCell(pos.getX() - 2, y);
					//if(cell == null || (cell.isSeen() && !cell.isObstacle())) return false;
					if(cell != null && !cell.isSeen()) return true;				}
				break;
			case DOWN:
				for(int x = pos.getX() - 1; x <= pos.getX() + 1; x++){
					MapCell cell = map.getCell(x, pos.getY() - 2);
					//if(cell == null || (cell.isSeen() && !cell.isObstacle())) return false;
					if(cell != null && !cell.isSeen()) return true;				}
				break;
			case RIGHT:
				for(int y = pos.getY() - 1; y <= pos.getY() + 1; y++){
					MapCell cell = map.getCell(pos.getX() + 2, y);
					//if(cell == null || (cell.isSeen() && !cell.isObstacle())) return false;
					if(cell != null && !cell.isSeen()) return true;				}
				break;
		}
		return false;
	}

	private ShortestPath GetShortestPathToFrontier(Map map, List<MapCell> unseen){
		List<Coordinate> seenNeighbours = unseen.stream().map(map::getNeighbours).map(HashMap::values).flatMap(Collection::stream).filter(MapCell::isSeen).map(cell -> new Coordinate(cell.x, cell.y)).collect(Collectors.toList());
		List<Coordinate> start = GetStartingCoords();
		List<GraphNode> nodes = MapProcessor.ProcessMap(map, start, seenNeighbours);
		return AStarAlgo.AStarSearch(nodes.get(0), nodes.get(1));
	}

	private ShortestPath GetShortestPathToCandidates(Map map, HashMap<MapCell, Orientation> candidates){
		List<Coordinate> destinations = candidates.keySet().stream().map(cell -> new Coordinate(cell.x, cell.y, candidates.get(cell).isAligned(true) ? Coordinate.Facing.HORIZONTAL : Coordinate.Facing.VERTICAL)).collect(Collectors.toList());
		List<Coordinate> start = GetStartingCoords();
		List<GraphNode> nodes = MapProcessor.ProcessMap(map, start, destinations);
		return AStarAlgo.AStarSearch(nodes.get(0), nodes.get(1));
	}

	private boolean DoShortestPathWithSensor(ShortestPath toUnexploredPoint, Map map, List<MapCell> unseen, double weight, long startTime, int targetCoverage, long tLimit) throws InterruptedException, TimeLimitExceededException {
		// Orientation update
		if(IsMakingWeirdTurns(toUnexploredPoint)) {
			toUnexploredPoint.getPath().remove(1);
		}
		List<RobotCommand> commands = robot.prepareOrientationCmds(toUnexploredPoint.getStartingOrientation());
		commands.addAll(toUnexploredPoint.generateInstructions());
		// Loop over commands until discover new unseen
		for (RobotCommand cmd : commands) {
			if(ExitCondition(map, weight, startTime, targetCoverage, tLimit)) throw new TimeLimitExceededException();
			if (cmd == RobotCommand.MOVE_FORWARD && checkObstruction(map, robot.getOrientation(), robot.getPosition())){
				return false;
			}
			long numUnseen = unseen.stream().filter(x -> !x.isSeen()).count();
			if (numUnseen != unseen.size()){
				return false;
			}
			robot.doCommandWithSensor(cmd, map);
			if (robot.canCalibrate(robot.getOrientation(), map) || robot.getPosition().equals(new Coordinate(14, 19))) {
				robot.Calibrate(map);
			}
		}
		long numUnseen = unseen.stream().filter(x -> !x.isSeen()).count();
		return numUnseen == unseen.size();
	}

	private boolean ExitCondition(Map map, double weight, long startTime, int targetCoverage, long tLimit){
		return !(map.getSeenPercentage() < targetCoverage && System.nanoTime()  - startTime + weight  * (1000000000) + BUFFER < tLimit);
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
	
	private ShortestPath getPathToStart(Map map) {
		List<Coordinate> start = GetStartingCoords();
		List<Coordinate> end = new LinkedList<>();
		end.add(new Coordinate(1,1));
		List<GraphNode> nodes = MapProcessor.ProcessMap(map, start, end);
		return AStarAlgo.AStarSearch(nodes.get(0), nodes.get(1));
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
		if(map.getCell(x, y) == null) return true;
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

