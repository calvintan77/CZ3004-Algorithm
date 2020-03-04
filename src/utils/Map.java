package utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;

import Constants.MapConstants;
import Constants.SensorConstants;
import Main.GUI;

public class Map {

	private static final String MAP_FILE_PATH = "src/sample arena 3.txt";

	private static Map exploredMap;
	private static Map realMap; 	//this attribute is only used during simulation. 
									//In real run, real map is not known in advanced
	
	private MapCell[][] mapCells;
	private int numSquaresExplored; // have exploredPercent and count so we don't waste time
	
	//Singleton strategy pattern
	public static Map getExploredMapInstance() {
		if (exploredMap == null)
			exploredMap = new Map();
		return exploredMap;
	}
	
	public static Map getRealMapInstance() {
		if (realMap == null) {
			/*
			 * Code to load real Map
			 */
			realMap = loadMapFromFile(MAP_FILE_PATH);
			
		}
		return realMap;
	}
	
	public Map() {
		mapCells = new MapCell[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT];
		this.numSquaresExplored = 0;
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
	
	public void markCellSeen(int x, int y) { // should we return a success code - based on whether exploredPercent >= 300 
		if (this.getCell(x,y) == null || this.getCell(x, y).getSeen()) { // defensive check
			return;
		}
		this.getCell(x, y).setSeen(true);
		this.numSquaresExplored++;
		//TODO: Don't mix responsibilities here
		if (this.getCell(x, y).isObstacle())
			GUI.getInstance().getMazeGrids()[x][y].setBackground(GUI.OBSTACLE_CELL_COLOR);
		else if (!((x <= 2 && y <= 2) || (x >= 12 && y >= 17)))
			GUI.getInstance().getMazeGrids()[x][y].setBackground(GUI.EMPTY_CELL_COLOR);
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
	
	
	// marks a given cell as a virtual wall - defensive check not given here, caller has to do 
	public void markVirtualWall(int x, int y) { 
		this.getCell(x, y).setVirtualWall(true);
	}
	
	public float getExploredPercent() {
		return (float) this.numSquaresExplored / 3f;
	}
	
	public static String convertHexToBinaryString(String hex) {
		String newHexString = "F"+hex;
		return new BigInteger(newHexString, 16).toString(2).substring(4);
	}
	
	public static String convertBinaryToHexString(String binary, int hexStringLength) {
		String hexString = new BigInteger(binary, 2).toString(16);
		int paddingSpace = hexStringLength - hexString.length();
		StringBuilder paddedHexString = new StringBuilder();
		for (int i=0; i<paddingSpace; i++) {
			paddedHexString.append("0");
		}
		paddedHexString.append(hexString);
		return paddedHexString.toString();
	}
	
	public static String convertBinaryToHexString(String binary) {
		String hexString = new BigInteger(binary, 2).toString(16);
		int paddingSpace = binary.length() / 4 - hexString.length();
		StringBuilder paddedHexString = new StringBuilder();
		for (int i=0; i<paddingSpace; i++) {
			paddedHexString.append("0");
		}
		paddedHexString.append(hexString);
		return paddedHexString.toString();
	}
	
	public static void saveMap(Map map, String savePath) {
		MapTuple tup = generateMapDescriptor(map);		
		File mapFile = new File(savePath);

		try (BufferedWriter mapFileWriter = new BufferedWriter(new FileWriter(mapFile));){
			mapFileWriter.write(tup.GetP1());
			mapFileWriter.newLine();
			mapFileWriter.write(tup.GetP2());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static MapTuple generateMapDescriptor(Map map) {
		StringBuilder b1MapDescriptor = new StringBuilder();
		StringBuilder b2MapDescriptor = new StringBuilder();
		b1MapDescriptor.append("11");
		for (int j=0; j < MapConstants.MAP_HEIGHT; j++) {
			for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
				if (map.getCell(i, j).getSeen()) {
					b1MapDescriptor.append("1");
					if (map.getCell(i, j).isObstacle())
						b2MapDescriptor.append("1");
					else b2MapDescriptor.append("0");
				} else b1MapDescriptor.append("0");
			}
		}
		b1MapDescriptor.append("11");

		if (b2MapDescriptor.length() % 4 != 0) {
			int padding = 4 - (b2MapDescriptor.length()%4);
			for (int i=0; i<padding; i++) {
				b2MapDescriptor.append("0");
			}
		}
		String h1MapDescriptor = convertBinaryToHexString(b1MapDescriptor.toString());
		String h2MapDescriptor = convertBinaryToHexString(b2MapDescriptor.toString());
		return new MapTuple(h1MapDescriptor, h2MapDescriptor);
	}
	

	public void loadMap(JButton[][] mapGrids) {
		for (int x=0; x<MapConstants.MAP_WIDTH; x++) {
			for (int y=0; y<MapConstants.MAP_HEIGHT; y++) {
				if (mapGrids[x][y].getBackground() == GUI.OBSTACLE_CELL_COLOR) {
					mapCells[x][y].setObstacleStatus(true);
				} else if (mapGrids[x][y].getBackground() == GUI.EMPTY_CELL_COLOR
						|| mapGrids[x][y].getBackground() == GUI.GOAL_START_ZONE_COLOR){
					mapCells[x][y].setObstacleStatus(false);
				}
			}
		}
		Map.saveMap(Map.getRealMapInstance(), "src/inputMap.txt");
	}

	public void clearMap() {
		for (int x=0; x<MapConstants.MAP_WIDTH; x++) {
			for (int y=0; y<MapConstants.MAP_HEIGHT; y++) {
				if (x<=2 && y<=2) {
					mapCells[x][y].setSeen(true);
				} else {
					mapCells[x][y].clear();
				}
				if (x==0 || y==0 || x==MapConstants.MAP_WIDTH-1 || y==MapConstants.MAP_HEIGHT-1)
					mapCells[x][y].setVirtualWall(true);
			}
		}
		this.numSquaresExplored = 9;
	}
	
	public static Map loadMapFromFile(String filePath) {
		Map resultMap = new Map();
		File mapFile = new File(filePath);
		try (BufferedReader mapFileReader = new BufferedReader(new FileReader(mapFile));){
			String h1MapDescriptor = mapFileReader.readLine();
			String h2MapDescriptor = mapFileReader.readLine();
			String b1MapDescriptor = convertHexToBinaryString(h1MapDescriptor);
			String b2MapDescriptor = convertHexToBinaryString(h2MapDescriptor);
			int stringIndex = 0;
			for (int j=0; j < MapConstants.MAP_HEIGHT; j++) {
				for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
					if (b1MapDescriptor.charAt(j*MapConstants.MAP_WIDTH+i+2) == '0')
						continue;
					resultMap.getCell(i, j).setSeen(true);
					if (b2MapDescriptor.charAt(stringIndex) == '0') {
						resultMap.getCell(i, j).setObstacleStatus(false);
					} else resultMap.getCell(i, j).setObstacleStatus(true);
					stringIndex++;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return resultMap;
	}
	
	public int getNumExplored() {
		return this.numSquaresExplored;
	}
	
	public void updateExploredPercentage() {
		numSquaresExplored = 0;
		for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j < MapConstants.MAP_HEIGHT; j++) {
				if (mapCells[i][j].getSeen())
					numSquaresExplored++;
			}
		}
	}
	
	// unelegant way of getting unseen 
	public List<MapCell> getAllUnseen() {
		List<MapCell> arr = new ArrayList<MapCell>(); 
		for (int i = 0; i < MapConstants.MAP_WIDTH; i++) {
			for (int j = 0; j < MapConstants.MAP_HEIGHT; j++) {
				if (!this.getCell(i, j).getSeen()) {
					arr.add(this.getCell(i, j));
				}
			}
		}
		return arr; 
	}
	
	// unelegant way of getting seen 
	public List<MapCell> getAllSeen() {
		List<MapCell> arr = new ArrayList<MapCell>(); 
		for (int i = 0; i < MapConstants.MAP_WIDTH; i++) {
			for (int j = 0; j < MapConstants.MAP_HEIGHT; j++) {
				if (this.getCell(i, j).getSeen()) {
					arr.add(this.getCell(i, j));
				}
			}
		}
		return arr; 
	}
	
	/**
	 * @param values: list of string values to update in format of l,f,f,f,r 
	 **/
	public void updateFromSensor(List<Integer> values, Coordinate curPos, Orientation o) { 
		switch (o) { 
			case UP: 
				updateSingleSensor(values.get(0), SensorConstants.LONG_RANGE, new Coordinate(curPos.getX() - 1, curPos.getY() + 1), Orientation.getCounterClockwise(o));
				updateSingleSensor(values.get(1), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() - 1, curPos.getY() + 1), o);
				updateSingleSensor(values.get(2), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX(), curPos.getY() + 1), o);
				updateSingleSensor(values.get(3), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() + 1, curPos.getY() + 1), o);
				updateSingleSensor(values.get(4), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() + 1, curPos.getY() + 1), Orientation.getClockwise(o));
				break;
			case DOWN:
				updateSingleSensor(values.get(0), SensorConstants.LONG_RANGE, new Coordinate(curPos.getX() + 1, curPos.getY() - 1), Orientation.getCounterClockwise(o));
				updateSingleSensor(values.get(1), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() + 1, curPos.getY() - 1), o);
				updateSingleSensor(values.get(2), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX(), curPos.getY() - 1), o);
				updateSingleSensor(values.get(3), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() - 1, curPos.getY() - 1), o);
				updateSingleSensor(values.get(4), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() - 1, curPos.getY() - 1), Orientation.getClockwise(o));
				break;
			case RIGHT:
				updateSingleSensor(values.get(0), SensorConstants.LONG_RANGE, new Coordinate(curPos.getX() + 1, curPos.getY() + 1), Orientation.getCounterClockwise(o));
				updateSingleSensor(values.get(1), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() + 1, curPos.getY() + 1), o);
				updateSingleSensor(values.get(2), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() + 1, curPos.getY()), o);
				updateSingleSensor(values.get(3), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() + 1, curPos.getY() - 1), o);
				updateSingleSensor(values.get(4), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() + 1, curPos.getY() - 1), Orientation.getClockwise(o));
				break;
			case LEFT: 
				updateSingleSensor(values.get(0), SensorConstants.LONG_RANGE, new Coordinate(curPos.getX() - 1, curPos.getY() - 1), Orientation.getCounterClockwise(o));
				updateSingleSensor(values.get(1), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() - 1, curPos.getY() - 1), o);
				updateSingleSensor(values.get(2), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() - 1, curPos.getY()), o);
				updateSingleSensor(values.get(3), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() - 1, curPos.getY() + 1), o);
				updateSingleSensor(values.get(4), SensorConstants.SHORT_RANGE, new Coordinate(curPos.getX() - 1, curPos.getY() + 1), Orientation.getClockwise(o));
				break;
		}
	}
	
	/** 
	 * updates value based on a single sensor 
	 * @param value: sensor's readings 
	 * @param maxValue: max sensor reading 
	 * @param sensorPos: actual sensor position
	 * @param o: sensor orientation relative to map
	 */
	public void updateSingleSensor(int value, int maxValue, Coordinate sensorPos, Orientation o) {
		if(value==-1) value = maxValue;
		switch (o) {
			case RIGHT: 
				// update all seen 
				for (int i = 1; i <= value; i++) { 
					this.markCellSeen(sensorPos.getX() + i, sensorPos.getY());
				}
				if (value != maxValue) { // obstacle in front
					this.setObstacle(new Coordinate(sensorPos.getX() + value + 1, sensorPos.getY()));	
				}
				break;
			case LEFT: 
				for (int i = 1; i <= value; i++) { 
					this.markCellSeen(sensorPos.getX() - i, sensorPos.getY());
				}
				if (value != maxValue) { // obstacle in front
					this.setObstacle(new Coordinate(sensorPos.getX() - value - 1, sensorPos.getY()));		
				}
				break;
			case UP:	
				for (int i = 1; i <= value; i++) { 
					this.markCellSeen(sensorPos.getX(), sensorPos.getY() + i);
				}
				if (value != maxValue) { // obstacle in front
					this.setObstacle(new Coordinate(sensorPos.getX(), sensorPos.getY() + value + 1));		
				}
				break;
			case DOWN: 
				for (int i = 1; i <= value; i++) { 
					this.markCellSeen(sensorPos.getX(), sensorPos.getY() - i);
				}
				if (value != maxValue) { // obstacle in front
					this.setObstacle(new Coordinate(sensorPos.getX(), sensorPos.getY() - value - 1));		
				}
				break;
		}
	}
	
	/**
	 * set c to be obstacle and surrounding to be virtual walls 
	 * @param c
	 */
	public void setObstacle(Coordinate c) {
		setObstacle(c, true);
	}
	
	/**
	 * set c to be obstacle and surrounding to be virtual walls 
	 * @param c
	 */
	public void setObstacle(Coordinate c, boolean markSeen) {
		if(this.getCell(c) == null) return;
		this.getCell(c).setObstacleStatus(true);
		if(markSeen) this.markCellSeen(c.getX(), c.getY());
		for (int i = c.getX() - 1; i <= c.getX() + 1; i++) {
			for (int j = c.getY() - 1; j <= c.getY() + 1; j++) {
				if (i == c.getX() && j == c.getY()) {
					continue;
				} else {
					if (this.getCell(i, j) != null) {
						this.getCell(i, j).setVirtualWall(true);
					}
				}
			}
		}
	}
	
	/**
	 * unset c to be obstacle and surrounding to be virtual walls 
	 * @param c
	 */
	public void unsetObstacle(Coordinate c) {
		if(this.getCell(c) == null) return;
		this.getCell(c).setObstacleStatus(false);
		for (int i = c.getX() - 1; i <= c.getX() + 1; i++) {
			for (int j = c.getY() - 1; j <= c.getY() + 1; j++) {
				if (i != 0 && i != 14 && j != 0 && j != 19 && 
						this.getCell(i, j) != null && 
						this.getCell(i, j).getSeen() &&
						!this.getAdjacent(this.getCell(i, j)).stream().anyMatch(x -> x.isObstacle()&&x.getSeen())) {
					this.getCell(i, j).setVirtualWall(false);
				}
			}
		}
	}
	
	/**
	 * returns a list of valid neighbouring cells - ie, not virtual wall, not obstacles
	 * @param m
	 * @return
	 */
	public HashMap<String, MapCell> getNeighbours(MapCell m) {
		HashMap<String, MapCell> map = new HashMap<String, MapCell>(); 
		
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
	 * returns a list of valid neighbouring cells - ie, not virtual wall, not obstacles
	 * @param m
	 * @return
	 */
	public List<MapCell> getAdjacent(MapCell m) {
		List<MapCell> cells = new ArrayList<>();
		for(int i = m.x-1; i <= m.x+1; i++) {
			for(int j = m.y-1; j <= m.y+1; j++) {
				if(i == m.x && j == m.y) continue;
				if(this.getCell(i, j) != null) {
					cells.add(this.getCell(i, j));
				}
			}
		}
		return cells;
	}
	
	public Map clone() {
		Map cloneMap = new Map();
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				if (!this.getCell(i, j).getSeen()) {
					cloneMap.setObstacle(new Coordinate(i,j), false);
				} else cloneMap.getCell(i, j).setSeen(true);
				if(this.getCell(i, j).isObstacle()) {
					cloneMap.setObstacle(new Coordinate(i, j), false);
				}
			}
		}
		return cloneMap;
	}
	
	public void expandSearchSpace() {
		List<Coordinate> seenNeighbours = getAllSeen().stream().map(this::getAdjacent).flatMap(Collection::stream).filter(x -> !x.getSeen()).map(cell -> new Coordinate(cell.x, cell.y)).collect(Collectors.toList());
		for(Coordinate c:seenNeighbours) {
			this.getCell(c).setSeen(true);
			unsetObstacle(c);
		}
	}
	
	
	
	public static void main(String[] args) {
		File mapFile = new File(MAP_FILE_PATH);
		try (BufferedReader mapFileReader = new BufferedReader(new FileReader(mapFile));){
			String h1MapDescriptor = mapFileReader.readLine();
			String h2MapDescriptor = mapFileReader.readLine();
			String b2MapDescriptor = convertHexToBinaryString(h2MapDescriptor);
//			System.out.println(b2MapDescriptor);
//			System.out.println(b2MapDescriptor.length());
			System.out.println(h2MapDescriptor);
			System.out.println(convertBinaryToHexString(b2MapDescriptor));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
}
