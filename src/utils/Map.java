package utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;

import javax.swing.JButton;

import Constants.MapConstants;

public class Map {
	private static final String MAP_FILE_PATH = "D:\\CZ3004-MULTIDISCIPLINARY PROJECT\\MDP 2020 Sem2\\src\\Sample arena 5.txt";
	private static Map exploredMap;
	private static Map realMap; 	//this attribute is only used during simulation. 
									//In real run, real map is not known in advanced
	
	private MapCell[][] mapCells;
	private int exploredPercent; // have exploredPercent and count so we don't waste time 
	
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
				mapCells[i][j] = new MapCell();
			}
		}
		this.exploredPercent = 0; 
	}
	
	public void markCellExplored(int x, int y) { // should we return a success code - based on whether exploredPercent >= 100 
		this.getCell(x, y).setExploredStatus(true);
		this.exploredPercent += 1; 
	}
	
	public MapCell getCell(int x, int y) {
		return mapCells[x][y];
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
				if (map.getCell(i, j).isExplored()) {
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
		Map.saveMap(Map.getRealMapInstance(), "D:/CZ3004-MULTIDISCIPLINARY PROJECT/MDP 2020 Sem2/src/inputMap.txt");
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
					resultMap.getCell(i, j).setExploredStatus(true);
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
	
	public double getExploredPercent() {
		return (double) this.exploredPercent / 300.0 * 100.0; 
	}
	
	public void updateExploredPercentage() {
		exploredPercent = 0;
		for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j < MapConstants.MAP_HEIGHT; j++) {
				if (mapCells[i][j].isExplored())
					exploredPercent++;
			}
		}
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
