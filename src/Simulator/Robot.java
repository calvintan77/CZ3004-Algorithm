package Simulator;

import java.util.ArrayList;
import java.util.List;

import Constants.MapConstants;
import Main.GUI;
import Main.RobotController;
import RealRun.RobotRPI;
import utils.Map;
import utils.Orientation;
import utils.RobotCommand;

public class Robot {
	private static Robot robot;
	private int speed;
	
	//Singleton strategy pattern
	public static Robot getInstance() {
		if (!RobotController.REAL_RUN) {
			if (robot == null)
				robot = new Robot();
		} else {
			if (robot == null) {
				RobotRPI rpi = new RobotRPI();
				try {
					rpi.setUpConnection();
				} catch (Exception e) {
					
				}
				robot = rpi;
			}
		}
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
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt+"");
					cnt = 0;
				}
				

				for (int i=x-1; i<=x+1; i++) {
					for (int j=y+2; j <= Math.min(y+3, MapConstants.MAP_HEIGHT-1); j++) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt+"");
					cnt = 0;
				}
				
				
				for (int i=x+2; i <= Math.min(x+3, MapConstants.MAP_WIDTH-1); i++) {
					if (realMap.getCell(i,y+1).isObstacle()) {
						break;
					}
					cnt++;
				}
				sensorValues.add(cnt+"");
				
				break;
			case LEFT:
				for (int i=x; i>=x-1; i--) {
					for (int j=y-2; j >= Math.max(y-5,0); j--) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt+"");
					cnt = 0;
				}
				
				for (int j=y-1; j <= y+1; j++) {
					for (int i=x-2; i >= Math.max(x-3, 0); i--) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt+"");
					cnt = 0;
				}
				
				for (int j=y+2; j<= Math.min(y+3, MapConstants.MAP_HEIGHT-1); j++) {
					if (realMap.getCell(x-1, j).isObstacle()) {
						break;
					}
					cnt++;
				}
				sensorValues.add(cnt+"");
				
				break;
			case RIGHT: 
				for (int i=x; i<=x+1; i++) {
					for (int j=y+2; j <= Math.min(y+5,MapConstants.MAP_HEIGHT-1); j++) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt+"");
					cnt = 0;
				}

				for (int j=y+1; j >= y-1; j--) {
					for (int i=x+2; i <= Math.min(x+3, MapConstants.MAP_WIDTH-1); i++) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt+"");
					cnt = 0;
				}
							
				for (int j=y-2; j >= Math.max(y-3, 0); j--) {
					if (realMap.getCell(x+1, j).isObstacle()) {
						break;
					}
					cnt++;
				}
				sensorValues.add(cnt+"");
				
				break;
			case DOWN:
				for (int j=y; j >= y-1; j--) {
					for (int i=x+2; i <= Math.min(x+5, MapConstants.MAP_WIDTH-1); i++) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt+"");
					cnt = 0;
				}
				
				for (int i=x+1; i >= x-1; i--) {
					for (int j=y-2; j >= Math.max(y-3, 0); j--) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt+"");
					cnt = 0;
				}
								
				for (int i=x+2; i <= Math.min(x+3, MapConstants.MAP_WIDTH-1); i++) {
					if (realMap.getCell(i,y-1).isObstacle()) {
						break;
					}
					cnt++;
				}
				sensorValues.add(cnt+"");
				
				break;
				
		}
		
		return String.join(",", sensorValues);
	}
	
	public void doCommand(RobotCommand cmd){
		try {
			Thread.sleep(1000/speed); 	//int timePerStep = 1000/speed (ms)
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		GUI.getInstance().updateRobotUI(cmd);
	}
	
}