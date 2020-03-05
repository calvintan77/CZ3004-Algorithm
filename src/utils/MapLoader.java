package utils;

import constants.MapConstants;
import gui.GUI;
import maze.Map;
import maze.MapTuple;

import javax.swing.*;
import java.io.*;
import java.math.BigInteger;

public class MapLoader {
    public static String convertHexToBinaryString(String hex) {
        String newHexString = "F"+hex;
        return new BigInteger(newHexString, 16).toString(2).substring(4);
    }

    public static String convertBinaryToHexString(String binary, int hexStringLength) {
        String hexString = new BigInteger(binary, 2).toString(16);
        int paddingSpace = hexStringLength - hexString.length();
        return "0".repeat(Math.max(0, paddingSpace)) + hexString;
    }

    public static String convertBinaryToHexString(String binary) {
        return convertBinaryToHexString(binary, binary.length());
    }

    public static void saveMap(Map map, String savePath) {
        MapTuple tup = generateMapDescriptor(map);
        File mapFile = new File(savePath);

        try (BufferedWriter mapFileWriter = new BufferedWriter(new FileWriter(mapFile))){
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
        for (int j = 0; j < MapConstants.MAP_HEIGHT; j++) {
            for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
                if (map.getCell(i, j).isSeen()) {
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
            b2MapDescriptor.append("0".repeat(padding));
        }
        String h1MapDescriptor = convertBinaryToHexString(b1MapDescriptor.toString());
        String h2MapDescriptor = convertBinaryToHexString(b2MapDescriptor.toString());
        return new MapTuple(h1MapDescriptor, h2MapDescriptor);
    }


    public static void loadRealMapFromGUI(JButton[][] mapGrids) {
        Map map = Map.getRealMapInstance();
        for (int x=0; x<MapConstants.MAP_WIDTH; x++) {
            for (int y=0; y<MapConstants.MAP_HEIGHT; y++) {
                if (mapGrids[x][y].getBackground() == GUI.OBSTACLE_CELL_COLOR) {
                    map.getCell(x, y).setObstacleStatus(true);
                } else if (mapGrids[x][y].getBackground() == GUI.EMPTY_CELL_COLOR
                        || mapGrids[x][y].getBackground() == GUI.GOAL_START_ZONE_COLOR){
                    map.getCell(x, y).setObstacleStatus(false);
                }
            }
        }
        MapLoader.saveMap(map, "src/inputMap.txt");
    }

    public static Map loadMapFromFile(String filePath) {
        Map resultMap = new Map();
        File mapFile = new File(filePath);
        try (BufferedReader mapFileReader = new BufferedReader(new FileReader(mapFile))){
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
            resultMap.initSeenSquares();
        } catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }
}
