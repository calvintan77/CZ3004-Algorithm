package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;

import Constants.MapConstants;

public class Map {
	private static final String MAP_FILE_PATH = "C:\\Users\\USER\\Desktop\\newMap.txt";
	private static Map exploredMap;
	private static Map realMap; 	//this attribute is only used during simulation. 
									//In real run, real map is not known in advanced
	
	private MapCell[][] mapCells;
	
	//Singleton strategy pattern
	public static Map getExploredMapInstance() {
		if (exploredMap == null)
			exploredMap = new Map();
		return exploredMap;
	}
	
	public static Map getRealMapInstance() {
		if (realMap == null) {
			realMap = new Map();
			/*
			 * Code to load real Map
			 */
			File mapFile = new File(MAP_FILE_PATH);
			try (BufferedReader mapFileReader = new BufferedReader(new FileReader(mapFile));){
				String hexStringMapDescriptor1 = mapFileReader.readLine();
				String hexStringMapDescriptor2 = mapFileReader.readLine();
				String binaryStringMapDescriptor2 = convertHexToBinaryString(hexStringMapDescriptor2);
				int stringIndex = 0;
				for (int j=0; j < MapConstants.MAP_HEIGHT; j++) {
					for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
						if (binaryStringMapDescriptor2.charAt(stringIndex) == '0') {
							realMap.getCell(i, j).setObstacleStatus(false);
						} else realMap.getCell(i, j).setObstacleStatus(true);
						stringIndex++;
					}
				}
			} catch (Exception e){
				e.printStackTrace();
			}
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
		for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j < MapConstants.MAP_HEIGHT; j++) {
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
		if (binaryStringMapDescriptor2.length() % 8 != 0) {
			for (int i=0; i < 8-(binaryStringMapDescriptor2.length() % 8); i++) {
				binaryStringMapDescriptor2.append("1");
			}
		}
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
