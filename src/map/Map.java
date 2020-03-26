package map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import constants.MapConstants;
import constants.SensorConstants;
import utils.Coordinate;
import utils.MapLoader;
import utils.Orientation;
import utils.Sensor;

public class Map {

	private static final String MAP_FILE_PATH = "src/sample arena 3.txt";

	private static Map realMap; 	//this attribute is only used during simulation.
									//In real run, real map is not known in advanced

	private final MapCell[][] mapCells;
	private int numSquaresSeen = 0; // num of squares seen by robot

	public static Map getRealMapInstance() {
		if (realMap == null) {
			// load real map
			realMap = MapLoader.loadMapFromFile(MAP_FILE_PATH);

		}
		return realMap;
	}

	public Map() {
		mapCells = new MapCell[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT];
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				mapCells[i][j] = new MapCell(i, j);
				if(i == 0 || j == 0 || i == MapConstants.MAP_WIDTH-1 || j == MapConstants.MAP_HEIGHT-1){
					mapCells[i][j].setVirtualWall(true);
				}
				if (i<=2 && j<=2)
					this.markCellSeen(i, j);
			}
		}
	}

	public void initSeenSquares(){
		this.numSquaresSeen = 0;
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				if(this.getCell(i, j).isSeen()){
					this.numSquaresSeen += 1;
				}
			}
		}
	}

	public void markCellSeen(int x, int y) { // should we return a success code - based on whether exploredPercent >= 300
		if (this.getCell(x,y) == null || this.getCell(x, y).isSeen()) { // defensive check
			return;
		}
		this.getCell(x, y).setSeen(true);
		this.numSquaresSeen++;
	}

	public MapCell getCell(int x, int y) {
		// caller has to check
		if (x < 0 || y < 0 || x >= MapConstants.MAP_WIDTH || y >= MapConstants.MAP_HEIGHT) {
			return null;
		}
		return mapCells[x][y];
	}

	public MapCell getCell(Coordinate c) {
		return getCell(c.getX(), c.getY());
	}

	public float getSeenPercentage() {
		return (float) this.numSquaresSeen / 3f;
	}


	public int getNumSeen() {
		return this.numSquaresSeen;
	}

	/**
	 * Searches all cells to find unseen
	 * @return unseen cells
	 */
	public List<MapCell> getAllUnseen() {
		List<MapCell> arr = new ArrayList<>();
		for (int i = 0; i < MapConstants.MAP_WIDTH; i++) {
			for (int j = 0; j < MapConstants.MAP_HEIGHT; j++) {
				if (!this.getCell(i, j).isSeen()) {
					arr.add(this.getCell(i, j));
				}
			}
		}
		return arr;
	}

	/**
	 * Searches all cells to get seen cells
	 * @return seen cells
	 */
	public List<MapCell> getAllSeen() {
		List<MapCell> arr = new ArrayList<>();
		for (int i = 0; i < MapConstants.MAP_WIDTH; i++) {
			for (int j = 0; j < MapConstants.MAP_HEIGHT; j++) {
				if (this.getCell(i, j).isSeen()) {
					arr.add(this.getCell(i, j));
				}
			}
		}
		return arr;
	}

	/**
	 * Takes a list of sensor readings and updates the map
	 * @param values: list of string values to update in format of Left(long),Front Left,Front Middle,Front Right,Right
	 **/
	public void updateFromSensor(List<Integer> values, Coordinate curPos, Orientation o) {
		if(SensorConstants.DEBUG_SENSORS) {
			System.out.println("Sensors: ");
			for (int i : values) {
				System.out.print(i + ",");
			}
			System.out.println("Coord: " + curPos.getX() + ", " + curPos.getY() + " Orientation: " + o.name());
		}
		updateSingleSensor(values.get(0), SensorConstants.LEFT_SENSOR, curPos, o);
		updateSingleSensor(values.get(1), SensorConstants.FRONT_LEFT_SENSOR, curPos, o);
		updateSingleSensor(values.get(2), SensorConstants.FRONT_MIDDLE_SENSOR, curPos, o);
		updateSingleSensor(values.get(3), SensorConstants.FRONT_RIGHT_SENSOR, curPos, o);
		updateSingleSensor(values.get(4), SensorConstants.RIGHT_SENSOR, curPos, o);
	}

	public void updateSingleSensor(int value, Sensor sensor, Coordinate robotPos, Orientation robotOrientation){
		updateSingleSensor(value, sensor.GetRange(), sensor.GetSensorPosition(robotOrientation, robotPos), sensor.getSensorFacing(robotOrientation));
	}

	/**
	 * Updates map based on a single sensor
	 * @param value: sensor's readings
	 * @param maxValue: max sensor reading
	 * @param sensorPos: actual sensor position
	 * @param o: sensor orientation relative to map
	 */
	public void updateSingleSensor(int value, int maxValue, Coordinate sensorPos, Orientation o) {
		if(value == SensorConstants.ERROR_VALUE) return;
		if(value == SensorConstants.NULL_VALUE || value > maxValue) value = maxValue;
		switch (o) {
			case RIGHT:
				// update all seen
				for (int i = 1; i <= value; i++) {
					if(this.isCellObstacle(sensorPos.getX() + i, sensorPos.getY())) return;
					this.markCellSeen(sensorPos.getX() + i, sensorPos.getY());
				}
				if (value < maxValue) { // obstacle in front
					this.setObstacle(new Coordinate(sensorPos.getX() + value + 1, sensorPos.getY()));
				}
				break;
			case LEFT:
				for (int i = 1; i <= value; i++) {
					if(this.isCellObstacle(sensorPos.getX() - i, sensorPos.getY())) return;
					this.markCellSeen(sensorPos.getX() - i, sensorPos.getY());
				}
				if (value < maxValue) { // obstacle in front
					this.setObstacle(new Coordinate(sensorPos.getX() - value - 1, sensorPos.getY()));
				}
				break;
			case UP:
				for (int j = 1; j <= value; j++) {
					if(this.isCellObstacle(sensorPos.getX(), sensorPos.getY() + j)) return;
					this.markCellSeen(sensorPos.getX(), sensorPos.getY() + j);
				}
				if (value < maxValue) { // obstacle in front
					this.setObstacle(new Coordinate(sensorPos.getX(), sensorPos.getY() + value + 1));
				}
				break;
			case DOWN:
				for (int j = 1; j <= value; j++) {
					if(this.isCellObstacle(sensorPos.getX(), sensorPos.getY() - j)) return;
					this.markCellSeen(sensorPos.getX(), sensorPos.getY() - j);
				}
				if (value < maxValue) { // obstacle in front
					this.setObstacle(new Coordinate(sensorPos.getX(), sensorPos.getY() - value - 1));
				}
				break;
		}
	}

	public boolean isCellObstacle(int x, int y){
		MapCell cell = this.getCell(x, y);
		return cell == null || cell.isObstacle();
	}

	/**
	 * set c to be an obstacle and seen, and surrounding 3x3 is set as virtual walls.
	 * @param c - coordinate of obstacle
	 */
	public void setObstacle(Coordinate c) {
		setObstacle(c, true);
	}

	/**
	 * set c to be an obstacle and set seen if markSeen is true, and surrounding 3x3 is set as virtual walls.
	 * @param c - coordinate of obstacle
	 * @param markSeen - whether to mark cell as seen
	 */
	public void setObstacle(Coordinate c, boolean markSeen) {
		if(this.getCell(c) == null || (this.getCell(c).isSeen() && !this.getCell(c).isObstacle())) return;
		this.getCell(c).setObstacleStatus(true);
		if(markSeen) this.markCellSeen(c.getX(), c.getY());
		for (int i = c.getX() - 1; i <= c.getX() + 1; i++) {
			for (int j = c.getY() - 1; j <= c.getY() + 1; j++) {
				if (i != c.getX() || j != c.getY()) {
					if (this.getCell(i, j) != null) {
						this.getCell(i, j).setVirtualWall(true);
					}
				}
			}
		}
	}

	/**
	 * unset c from being an obstacle, and remove surrounding virtual walls if no other obstacles exist
	 * @param c - coordinate to unset
	 */
	public void unsetObstacle(Coordinate c) {
		if(this.getCell(c) == null) return;
		this.getCell(c).setObstacleStatus(false);
		for (int i = c.getX() - 1; i <= c.getX() + 1; i++) {
			for (int j = c.getY() - 1; j <= c.getY() + 1; j++) {
				if (i != 0 && i != 14 && j != 0 && j != 19 &&
						this.getCell(i, j) != null &&
						this.getCell(i, j).isSeen() &&
						this.getAdjacent(this.getCell(i, j)).stream().noneMatch(x -> x.isObstacle()&&x.isSeen())) {
					this.getCell(i, j).setVirtualWall(false);
				}
			}
		}
	}

	/**
	 * returns a list of valid neighbouring cells - ie, not virtual wall, not obstacles (Only UP/DOWN/LEFT/RIGHT)
	 * @param m - cell to find neighbours from
	 * @return List of neighbours
	 */
	public HashMap<String, MapCell> getNeighbours(MapCell m) {
		HashMap<String, MapCell> map = new HashMap<>();

		switch (m.x) {
			case 0: {// left most row
				MapCell right = this.getCell(m.x+1, m.y);
				if (!right.isObstacle() && !right.isVirtualWall()) {
					map.put("right", right);
					}
				break;
				}
			case MapConstants.MAP_WIDTH-1: { // right most row
				MapCell left = this.getCell(m.x-1, m.y);
				if (!left.isObstacle() && !left.isVirtualWall()) {
					map.put("left", left);
					}
				break;
				}
			default: {
				MapCell right = this.getCell(m.x+1, m.y);
				if (!right.isObstacle() && !right.isVirtualWall()) {
					map.put("right", right);
					}
				MapCell left = this.getCell(m.x-1, m.y);
				if (!left.isObstacle() && !left.isVirtualWall()) {
					map.put("left", left);
					}
				break;
			}
		}

		switch (m.y) {
			case 0: {// bottom most row
				MapCell up = this.getCell(m.x, m.y+1);
				if (!up.isObstacle() && !up.isVirtualWall()) {
					map.put("up", up);
					}
				break;
				}
			case MapConstants.MAP_HEIGHT-1: { // top most row
				MapCell down = this.getCell(m.x, m.y-1);
				if (!down.isObstacle() && !down.isVirtualWall()) {
					map.put("down", down);
					}
				break;
				}
			default: {
				MapCell up = this.getCell(m.x, m.y+1);
				if (!up.isObstacle() && !up.isVirtualWall()) {
					map.put("up", up);
					}
				MapCell down = this.getCell(m.x, m.y-1);
				if (!down.isObstacle() && !down.isVirtualWall()) {
					map.put("down", down);
					}
				break;
			}
		}

		return map;
	}

	/**
	 * returns a list of all adjacent cells (in a 3x3 grid)
	 * @param m - cell to get adjacent of
	 * @return list of adjacent cells
	 */
	public List<MapCell> getAdjacent(MapCell m) {
		List<MapCell> cells = new ArrayList<>();
		for (int i = m.x - 1; i <= m.x + 1; i++) {
			for (int j = m.y - 1; j <= m.y + 1; j++) {
				if (i == m.x && j == m.y) continue;
				if (this.getCell(i, j) != null) {
					cells.add(this.getCell(i, j));
				}
			}
		}
		return cells;
	}

	/**
	 * Creates a new map with unseen cells as obstacles
	 * @return new map with unseen cells as obstacles
	 */
	public Map CloneWithUnseenAsObstacles() {
		Map cloneMap = new Map();
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				if(this.getCell(i, j).isObstacle()) {
					cloneMap.setObstacle(new Coordinate(i, j), false);
				}
				if (!this.getCell(i, j).isSeen()) {
					cloneMap.setObstacle(new Coordinate(i,j), false);
				} else cloneMap.getCell(i, j).setSeen(true);
			}
		}
		return cloneMap;
	}

	/**
	 * Literal clone of map
	 * @return cloned map
	 */
	@Override
	public Map clone() {
		Map cloneMap = new Map();
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				if(this.getCell(i, j).isObstacle()) {
					cloneMap.setObstacle(new Coordinate(i, j));
				}
				if (this.getCell(i, j).isSeen()) {
					cloneMap.getCell(i, j).setSeen(true);
				}
				if(i == 0 || i == MapConstants.MAP_WIDTH-1 || j == 0 || j == MapConstants.MAP_HEIGHT-1){
					cloneMap.getCell(i,j).setVirtualWall(true);
				}
			}
		}
		cloneMap.initSeenSquares();
		return cloneMap;
	}
	/**
	 * Get the frontier of unseen nodes and set them to seen (assumes that there are no obstacles)
	 */
	public void expandSearchSpace() {
		List<Coordinate> seenNeighbours = getAllSeen().stream().map(this::getAdjacent).flatMap(Collection::stream).filter(x -> !x.isSeen()).map(cell -> new Coordinate(cell.x, cell.y)).collect(Collectors.toList());
		for(Coordinate c:seenNeighbours) {
			this.getCell(c).setSeen(true);
			unsetObstacle(c);
		}
	}
}
