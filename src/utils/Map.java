package utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;

import Constants.MapConstants;
import Main.GUI;

public class Map {
	private static final String MAP_FILE_PATH = "src/inputMap.txt";
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
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				mapCells[i][j] = new MapCell(i, j);
				if(i == 0 || j == 0 || i == MapConstants.MAP_WIDTH-1 || j == MapConstants.MAP_HEIGHT-1){
					mapCells[i][j].setVirtualWall(true);
				}
			}
		}
		this.numSquaresExplored = 0;
	}
	
	public void markCellSeen(int x, int y) { // should we return a success code - based on whether exploredPercent >= 300 
		if (this.getCell(x,y) == null || this.getCell(x, y).getSeen()) { // defensive check
			return;
		}
		this.getCell(x, y).setSeen(true);
		if (this.getCell(x, y).isObstacle())
			GUI.getInstance().getMazeGrids()[x][y].setBackground(GUI.OBSTACLE_CELL_COLOR);
		else if (!((x <= 2 && y <= 2) || (x >= 12 && y >= 17)))
			GUI.getInstance().getMazeGrids()[x][y].setBackground(GUI.EMPTY_CELL_COLOR);
		this.numSquaresExplored += 1;
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
		StringBuilder binaryStringMapDescriptor1 = new StringBuilder();
		StringBuilder binaryStringMapDescriptor2 = new StringBuilder();
		binaryStringMapDescriptor1.append("11");
		for (int j=0; j < MapConstants.MAP_HEIGHT; j++) {
			for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
				if (map.getCell(i, j).getSeen()) {
					binaryStringMapDescriptor1.append("1");
					if (map.getCell(i, j).isObstacle())
						binaryStringMapDescriptor2.append("1");
					else binaryStringMapDescriptor2.append("0");
				} else binaryStringMapDescriptor1.append("0");
			}
		}
		binaryStringMapDescriptor1.append("11");
		String hexStringMapDescriptor1 = convertBinaryToHexString(binaryStringMapDescriptor1.toString());
		
		String hexStringMapDescriptor2 = convertBinaryToHexString(binaryStringMapDescriptor2.toString());
		File mapFile = new File(savePath);
		if (!mapFile.exists())
			try (BufferedWriter mapFileWriter = new BufferedWriter(new FileWriter(mapFile));){	
				mapFileWriter.write(hexStringMapDescriptor1);
				mapFileWriter.newLine();
				mapFileWriter.write(hexStringMapDescriptor2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		else System.out.println("File already existed");
	}
	

	public void loadMap(JButton[][] mapGrids) {
		for (int x=0; x<MapConstants.MAP_WIDTH; x++) {
			for (int y=0; y<MapConstants.MAP_HEIGHT; y++) {
				if (mapGrids[x][y].getBackground() == Color.RED) {
					mapCells[x][y].setObstacleStatus(true);
				} else {
					mapCells[x][y].setObstacleStatus(false);
				}
			}
		}
//		Map.saveMap(Map.getRealMapInstance(), "D:/CZ3004-MULTIDISCIPLINARY PROJECT/MDP 2020 Sem2/src/inputMap.txt");
	}
	
	public static Map loadMapFromFile(String filePath) {
		Map resultMap = new Map();
		File mapFile = new File(filePath);
		try (BufferedReader mapFileReader = new BufferedReader(new FileReader(mapFile));){
			String hexStringMapDescriptor1 = mapFileReader.readLine();
			String hexStringMapDescriptor2 = mapFileReader.readLine();
			String binaryStringMapDescriptor2 = convertHexToBinaryString(hexStringMapDescriptor2);
			int stringIndex = 0;
			for (int j=0; j < MapConstants.MAP_HEIGHT; j++) {
				for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
					resultMap.getCell(i, j).setSeen(true);
					if (binaryStringMapDescriptor2.charAt(stringIndex) == '0') {
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
	
	/**
	 * TODO: abstract getting of sensor coord
	 * @param values: list of string values to update in format of l,f,f,f,r 
	 **/
	public void updateFromSensor(List<Integer> values, Coordinate curPos, Orientation o) { 
		switch (o) { 
			case UP: 
				updateSingleSensor(values.get(0), 4, new Coordinate(curPos.getX() - 1, curPos.getY() + 1), Orientation.getCounterClockwise(o));
				updateSingleSensor(values.get(1), 2, new Coordinate(curPos.getX() - 1, curPos.getY() + 1), o);
				updateSingleSensor(values.get(2), 2, new Coordinate(curPos.getX(), curPos.getY() + 1), o);
				updateSingleSensor(values.get(3), 2, new Coordinate(curPos.getX() + 1, curPos.getY() + 1), o);
				updateSingleSensor(values.get(4), 2, new Coordinate(curPos.getX() + 1, curPos.getY() + 1), Orientation.getClockwise(o));
				break;
			case DOWN:
				updateSingleSensor(values.get(0), 4, new Coordinate(curPos.getX() + 1, curPos.getY() - 1), Orientation.getCounterClockwise(o));
				updateSingleSensor(values.get(1), 2, new Coordinate(curPos.getX() + 1, curPos.getY() - 1), o);
				updateSingleSensor(values.get(2), 2, new Coordinate(curPos.getX(), curPos.getY() - 1), o);
				updateSingleSensor(values.get(3), 2, new Coordinate(curPos.getX() - 1, curPos.getY() - 1), o);
				updateSingleSensor(values.get(4), 2, new Coordinate(curPos.getX() - 1, curPos.getY() - 1), Orientation.getClockwise(o));
				break;
			case RIGHT:
				updateSingleSensor(values.get(0), 4, new Coordinate(curPos.getX() + 1, curPos.getY() + 1), Orientation.getCounterClockwise(o));
				updateSingleSensor(values.get(1), 2, new Coordinate(curPos.getX() + 1, curPos.getY() + 1), o);
				updateSingleSensor(values.get(2), 2, new Coordinate(curPos.getX() + 1, curPos.getY()), o);
				updateSingleSensor(values.get(3), 2, new Coordinate(curPos.getX() + 1, curPos.getY() - 1), o);
				updateSingleSensor(values.get(4), 2, new Coordinate(curPos.getX() + 1, curPos.getY() - 1), Orientation.getClockwise(o));
				break;
			case LEFT: 
				updateSingleSensor(values.get(0), 4, new Coordinate(curPos.getX() - 1, curPos.getY() - 1), Orientation.getCounterClockwise(o));
				updateSingleSensor(values.get(1), 2, new Coordinate(curPos.getX() - 1, curPos.getY() - 1), o);
				updateSingleSensor(values.get(2), 2, new Coordinate(curPos.getX() - 1, curPos.getY()), o);
				updateSingleSensor(values.get(3), 2, new Coordinate(curPos.getX() - 1, curPos.getY() + 1), o);
				updateSingleSensor(values.get(4), 2, new Coordinate(curPos.getX() - 1, curPos.getY() + 1), Orientation.getClockwise(o));
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
		if(this.getCell(c) == null) return;
		this.getCell(c).setObstacleStatus(true);
		this.markCellSeen(c.getX(), c.getY());
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
	
	public static void main(String[] args) {
		File mapFile = new File(MAP_FILE_PATH);
		try (BufferedReader mapFileReader = new BufferedReader(new FileReader(mapFile));){
			String hexStringMapDescriptor1 = mapFileReader.readLine();
			String hexStringMapDescriptor2 = mapFileReader.readLine();
			String binaryStringMapDescriptor2 = convertHexToBinaryString(hexStringMapDescriptor2);
//			System.out.println(binaryStringMapDescriptor2);
//			System.out.println(binaryStringMapDescriptor2.length());
			System.out.println(hexStringMapDescriptor2);
			System.out.println(convertBinaryToHexString(binaryStringMapDescriptor2));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
}
