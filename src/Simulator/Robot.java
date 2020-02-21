package Simulator;

import java.util.ArrayList;
import java.util.List;

import Constants.MapConstants;
import Main.GUI;
import Main.RobotController;
import utils.*;

public class Robot implements IRobot {
	private static Robot robot;
	private int speed;
	private Orientation o; // need to initialize 
	private Coordinate position;
	
	//Singleton strategy pattern
	public static IRobot getInstance() {
		if (robot == null)
			robot = new Robot();
		return robot;
	}
	
	private Robot() {
		
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	//return string in the structure: "left,front,front,front,right"
	public List<Integer> getSensorValues() {
		Map realMap = Map.getRealMapInstance();
		List<Integer> sensorValues = new ArrayList<>();
		int x = this.position.getX();
		int y = this.position.getY();
		int cnt = 0;
		switch(this.o) {
			case UP:
				for (int j=y+1; j <= y+1; j++) {
					for (int i=x-2; i>=Math.max(x-5, 0); i--) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt);
					cnt = 0;
				}
				

				for (int i=x-1; i<=x+1; i++) {
					for (int j=y+2; j <= Math.min(y+3, MapConstants.MAP_HEIGHT-1); j++) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt);
					cnt = 0;
				}
				
				
				for (int i=x+2; i <= Math.min(x+3, MapConstants.MAP_WIDTH-1); i++) {
					if (realMap.getCell(i,y+1).isObstacle()) {
						break;
					}
					cnt++;
				}
				sensorValues.add(cnt);
				
				break;
			case LEFT:
				for (int i=x-1; i>=x-1; i--) {
					for (int j=y-2; j >= Math.max(y-5,0); j--) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt);
					cnt = 0;
				}
				
				for (int j=y-1; j <= y+1; j++) {
					for (int i=x-2; i >= Math.max(x-3, 0); i--) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt);
					cnt = 0;
				}
				
				for (int j=y+2; j<= Math.min(y+3, MapConstants.MAP_HEIGHT-1); j++) {
					if (realMap.getCell(x-1, j).isObstacle()) {
						break;
					}
					cnt++;
				}
				sensorValues.add(cnt);
				
				break;
			case RIGHT: 
				for (int i=x+1; i<=x+1; i++) {
					for (int j=y+2; j <= Math.min(y+5,MapConstants.MAP_HEIGHT-1); j++) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt);
					cnt = 0;
				}

				for (int j=y+1; j >= y-1; j--) {
					for (int i=x+2; i <= Math.min(x+3, MapConstants.MAP_WIDTH-1); i++) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt);
					cnt = 0;
				}
							
				for (int j=y-2; j >= Math.max(y-3, 0); j--) {
					if (realMap.getCell(x+1, j).isObstacle()) {
						break;
					}
					cnt++;
				}
				sensorValues.add(cnt);
				
				break;
			case DOWN:
				for (int j=y-1; j >= y-1; j--) {
					for (int i=x+2; i <= Math.min(x+5, MapConstants.MAP_WIDTH-1); i++) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt);
					cnt = 0;
				}
				
				for (int i=x+1; i >= x-1; i--) {
					for (int j=y-2; j >= Math.max(y-3, 0); j--) {
						if (realMap.getCell(i, j).isObstacle()) {
							break;
						}
						cnt++;
					}
					sensorValues.add(cnt);
					cnt = 0;
				}
								
				for (int i=x+2; i <= Math.min(x+3, MapConstants.MAP_WIDTH-1); i++) {
					if (realMap.getCell(i,y-1).isObstacle()) {
						break;
					}
					cnt++;
				}
				sensorValues.add(cnt);
				
				break;
				
		}
		
		return sensorValues;
	}
	
	public void doCommand(RobotCommand cmd){
		switch (cmd){
			case TURN_LEFT:
				this.setOrientation(Orientation.getCounterClockwise(this.o));
				break;
			case TURN_RIGHT:
				this.setOrientation(Orientation.getClockwise(this.o));
				break;
			case MOVE_FORWARD:
				switch(this.o){
					case UP:
						this.setPosition(this.getPosition().getX(), this.getPosition().getY() + 1);
						break;
					case LEFT:
						this.setPosition(this.getPosition().getX() - 1, this.getPosition().getY());
						break;
					case DOWN:
						this.setPosition(this.getPosition().getX(), this.getPosition().getY() - 1);
						break;
					case RIGHT:
						this.setPosition(this.getPosition().getX() + 1, this.getPosition().getY());
						break;
				}
		}
		try {
			Thread.sleep(1000/speed); 	//int timePerStep = 1000/speed (ms)
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		GUI.getInstance().updateRobotUI(cmd);
	}

	@Override
	public Orientation getOrientation() { 
		return o; 
	}

	@Override
	public Coordinate getPosition() {
		return position; 
	}

	@Override
	public void setPosition(int x, int y) { 
		this.position = new Coordinate(x, y);
	}

	@Override
	public void setOrientation(Orientation o) {
		this.o = o; 
	}
}
