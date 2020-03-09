package utils;

import constants.MapConstants;
import gui.GUI;
import map.Map;
import map.MapTuple;

import javax.swing.*;
import java.io.*;
import java.math.BigInteger;

public class MapLoader {

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

    public static MapTuple generateMapDescriptor(Map map){
        // Initialise explored hex counter
        int counter1 = 3;
        int digit1 = 2;
        // Initialise map content hex counter
        int counter2 = 0;
        int digit2 = 0;
        StringBuilder b1MapDescriptor = new StringBuilder();
        StringBuilder b2MapDescriptor = new StringBuilder();
        for (int j = 0; j < MapConstants.MAP_HEIGHT; j++) {
            for (int i=0; i < MapConstants.MAP_WIDTH; i++) {
                // Increment hex counter for exploration
                digit1++;
                if (map.getCell(i, j).isSeen()) {
                    // Increment hex counter for seen cell
                    digit2++;
                    counter1 = (counter1 << 1) + 1;
                    if (map.getCell(i, j).isObstacle())
                        counter2 = (counter2 << 1) + 1;
                    else{
                        counter2 = (counter2 << 1);
                    }
                } else{
                    counter1 = (counter1 << 1);
                }
                if(digit1 == 4){
                    digit1 = 0;
                    b1MapDescriptor.append(Integer.toHexString(counter1));
                    counter1 = 0;
                }
                if(digit2 == 4){
                    digit2 = 0;
                    b2MapDescriptor.append(Integer.toHexString(counter2));
                    counter2 = 0;
                }
            }
        }
        b1MapDescriptor.append(Integer.toHexString((counter1 << 2) + 3));
        if(digit2 != 0) b2MapDescriptor.append(Integer.toHexString((counter2 << (4-digit2))));
        return new MapTuple(b1MapDescriptor.toString(), b2MapDescriptor.toString());
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
        //Saves map to file
        MapLoader.saveMap(map, "src/inputMap.txt");
    }

    public static Map loadMapFromFile(String filePath){
        Map resultMap = new Map();
        File mapFile = new File(filePath);
        try (BufferedReader mapFileReader = new BufferedReader(new FileReader(mapFile))) {
            BigInteger h1MapDescriptor = new BigInteger(mapFileReader.readLine(), 16);
            BigInteger h2MapDescriptor = new BigInteger(mapFileReader.readLine(), 16);
            h1MapDescriptor = h1MapDescriptor.shiftRight(2);
            int squares = MapConstants.MAP_HEIGHT * MapConstants.MAP_WIDTH;
            for(int i = squares-1; i >= 0; i--){
                if(h1MapDescriptor.testBit(0)){
                    resultMap.markCellSeen(getX(i), getY(i));
                    if(h2MapDescriptor.testBit(0)){
                        resultMap.setObstacle(new Coordinate(getX(i), getY(i)));
                    }
                    h2MapDescriptor = h2MapDescriptor.shiftRight(1);
                }
                h1MapDescriptor = h1MapDescriptor.shiftRight(1);
            }
        }catch(Exception e){
            System.out.println("Exception on map loading: " + e.toString());
        }
        return resultMap;
    }

    private static int getX(int i){
        return i % MapConstants.MAP_WIDTH;
    }

    private static int getY(int i){
        return i / MapConstants.MAP_WIDTH;
    }
}
