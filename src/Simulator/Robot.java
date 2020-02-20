package Simulator;

import java.util.ArrayList;
import java.util.List;

import Constants.MapConstants;
import Main.GUI;
import utils.Map;
import utils.MapCell;
import utils.Orientation;
import utils.RobotCommand;

public class Robot implements RobotInterface {
	private static Robot robot;
	private int speed;
	private Orientation o; // need to initialize 
	private int[] position; 
	
	//Singleton strategy pattern
	public static Robot getInstance() {
		if (robot == null)
			robot = new Robot();
		return robot;
	}
	
	public Robot() {
		
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	//return string in the structure: "left,left,front,front,front,right"
	public String getSensorValues(int[] robotPosition, Orientation robotOrientation) {
		Map realMap = Map.getRealMapInstance();
		List<String> sensorValues = new ArrayList<>();
		int x = robotPosition[0];
		int y = robotPosition[1];
		int cnt = 0;
		switch(robotOrientation) {
			case UP:
				for (int j=y; j <= y+1; j++) {
					for (int i=x-2; i>=Math.max(x-5, 0); i--) {
						if (realMap.getCell(i, j).isObstacle()) {
							sensorValues.add(cnt+"");
							cnt = 0;
							break;
						}
						cnt++;
					}
				}
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}

				for (int i=x-1; i<=x+1; i++) {
					for (int j=y+2; j <= Math.min(y+3, MapConstants.MAP_HEIGHT-1); j++) {
						if (realMap.getCell(i, j).isObstacle()) {
							sensorValues.add(cnt+"");
							cnt = 0;
							break;
						}
						cnt++;
					}
				}
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				
				for (int i=x+2; i <= Math.min(x+3, MapConstants.MAP_WIDTH-1); i++) {
					if (realMap.getCell(i,y+1).isObstacle()) {
						sensorValues.add(cnt+"");
						cnt = 0;
						break;
					}
					cnt++;
				}
				
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				break;
			case LEFT:
				for (int i=x; i>=x-1; i--) {
					for (int j=y-2; j >= Math.max(y-5,0); j--) {
						if (realMap.getCell(i, j).isObstacle()) {
							sensorValues.add(cnt+"");
							cnt = 0;
							break;
						}
						cnt++;
					}
				}
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				for (int j=y-1; j <= y+1; j++) {
					for (int i=x-2; i >= Math.max(x-3, 0); i--) {
						if (realMap.getCell(i, j).isObstacle()) {
							sensorValues.add(cnt+"");
							cnt = 0;
							break;
						}
						cnt++;
					}
				}
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				
				for (int j=y+2; j<= Math.min(y+3, MapConstants.MAP_HEIGHT-1); j++) {
					if (realMap.getCell(x-1, j).isObstacle()) {
						sensorValues.add(cnt+"");
						cnt = 0;
						break;
					}
					cnt++;
				}
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				break;
			case RIGHT: 
				for (int i=x; i<=x+1; i++) {
					for (int j=y+2; j <= Math.min(y+5,MapConstants.MAP_HEIGHT-1); j++) {
						if (realMap.getCell(i, j).isObstacle()) {
							sensorValues.add(cnt+"");
							cnt = 0;
							break;
						}
						cnt++;
					}
				}
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				for (int j=y+1; j >= y-1; j--) {
					for (int i=x+2; i <= Math.min(x+3, MapConstants.MAP_WIDTH-1); i++) {
						if (realMap.getCell(i, j).isObstacle()) {
							sensorValues.add(cnt+"");
							cnt = 0;
							break;
						}
						cnt++;
					}
				}
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				
				for (int j=y-2; j >= Math.max(y-3, 0); j--) {
					if (realMap.getCell(x+1, j).isObstacle()) {
						sensorValues.add(cnt+"");
						cnt = 0;
						break;
					}
					cnt++;
				}
				
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				break;
			case DOWN:
				for (int j=y; j >= y-1; j--) {
					for (int i=x+2; i <= Math.min(x+5, MapConstants.MAP_WIDTH-1); i++) {
						if (realMap.getCell(i, j).isObstacle()) {
							sensorValues.add(cnt+"");
							cnt = 0;
							break;
						}
						cnt++;
					}
				}
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}

				for (int i=x+1; i >= x-1; i--) {
					for (int j=y-2; j >= Math.max(y-3, 0); j++) {
						if (realMap.getCell(i, j).isObstacle()) {
							sensorValues.add(cnt+"");
							cnt = 0;
							break;
						}
						cnt++;
					}
				}
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				
				for (int i=x+2; i <= Math.min(x+3, MapConstants.MAP_WIDTH-1); i++) {
					if (realMap.getCell(i,y-1).isObstacle()) {
						sensorValues.add(cnt+"");
						cnt = 0;
						break;
					}
					cnt++;
				}
				
				if (cnt != 0) {
					sensorValues.add(cnt+"");
				}
				
				break;
				
		}
		
		return String.join(",", sensorValues);
	}
	
	public void doCommand(RobotCommand cmd){
		GUI.getInstance().updateRobotUI(cmd);
	}
	
	public Orientation getOrientation() { 
		return o; 
	}
	
	public int[] getPosition() { 
		return position; 
	}
	
	public MapCell getPositionCell() { 
		return new MapCell(this.getPosition()[0], this.getPosition()[1]);
	}
	
	public void setPosition(int x, int y) { 
		position[0] = x;
		position[1] = y; 
	}
	
	// no defensive check for x 
	public void setOrientation(int x) { 
		o = Orientation.values()[x]; 
	}
	
	public void setOrientation(Orientation o) {
		this.o = o; 
	}
}
