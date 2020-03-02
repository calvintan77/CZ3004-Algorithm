package RealRun;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import Constants.SensorConstants;
import Simulator.IRobot;
import utils.*;

public class RpiRobot implements IRobot{
	public static final String RPI_IP_ADDRESS = "192.168.9.9";
	public static final int RPI_PORT = 3053;
	private static RpiRobot robot = null;
	private boolean isConnected = false;
	
	private Socket rpiSocket;
	private BufferedWriter toRPI;
	private Scanner fromRPI;

	private Coordinate position;
	private Orientation o;

	public static IRobot getInstance(){
		if (robot == null) {
			robot = new RpiRobot();
			while(!robot.isConnected()) {
				try {
					robot.setUpConnection();
				}catch(Exception e){
					System.out.println(e);
				}
			}
		}
		return robot;
	}

	private RpiRobot(){

	}

	public boolean isConnected(){
		return this.isConnected;
	}
	
	public void setUpConnection () throws UnknownHostException, IOException{
		rpiSocket = new Socket(RPI_IP_ADDRESS, RPI_PORT);
		if (toRPI != null) {
			toRPI.close();
		}
		toRPI = new BufferedWriter(new PrintWriter(rpiSocket.getOutputStream()));
		if (fromRPI != null) {
			fromRPI.close();
		}
		fromRPI = new Scanner(rpiSocket.getInputStream());
		this.isConnected = true;
	}
	
	public void closeConnection() throws IOException {
		if (!rpiSocket.isClosed()) {
			rpiSocket.close();
		}
	}
	
	public void sendMessage(String msg) throws IOException {
		
		toRPI.write(msg);
		toRPI.flush();
		
		System.out.println("Message sent: " + msg);
	}
	
	public String readMessage() throws IOException {

		String messageReceived = fromRPI.nextLine();
		System.out.println("Message received: " + messageReceived);
		
		return messageReceived;
	}
	
	//TODO: Settle with RPI
	@Override
	public List<Integer> getSensorValues() {
		try {
			sendMessage("GET_SENSOR_VALUES");
			//return readMessage();
			//TODO
			return null;
		} catch (Exception e) {
			
		}
		return null;
	}

	//TODO: Settle with RPI
	@Override
	public void doCommand(RobotCommand command) {
		try {
			sendMessage(command.name());
		} catch (Exception e) {
			
		}
	}

	@Override
	public Orientation getOrientation() {
		return this.o;
	}

	@Override
	public Coordinate getPosition() {
		return this.position;
	}

	@Override
	public void setPosition(int x, int y) {
		this.position = new Coordinate(x, y);
	}
	
	@Override
	public void setOrientation(Orientation o) {
		this.o = o;
	}

	@Override
	public void prepareOrientation(Orientation target) {
		prepareOrientation(target, false, null);
	}

	@Override
	public void prepareOrientation(Orientation target, boolean checkSensors, Map map) {
		// Orientation update
		if (this.getOrientation() != target) {
			int rightTurns = this.getOrientation().getRightTurns(target);
			if (rightTurns > 0) {
				for (int i = 0; i < rightTurns; i++) {
					this.doCommand(RobotCommand.TURN_RIGHT);
					if (checkSensors) map.updateFromSensor(getSensorValues(), this.position, this.o);
				}
			} else {
				for (int i = 0; i < -rightTurns; i++) {
					this.doCommand(RobotCommand.TURN_LEFT);
					if (checkSensors) map.updateFromSensor(getSensorValues(), this.position, this.o);
				}
			}
		}
	}

	@Override
	public HashMap<MapCell, Orientation> getSensorVisibilityCandidates(Map map, MapCell cell) {
		HashMap<MapCell, Orientation> candidates = new HashMap<>();
		MapCell check;
		//4 is left sensor length
		for (int i = 1; i <= SensorConstants.LONG_RANGE; i++) {
			//POSITION IS LEFT OF ROBOT IN MAP
			MapCell cand = map.getCell(cell.x + i, cell.y);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			// LEFT SENSOR
			check = map.getCell(cell.x + i + 1, cell.y - 1);
			if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.UP);
			//FRONT AND RIGHT SENSORS
			if (i <= SensorConstants.SHORT_RANGE) {
				// RIGHT SENSOR
				check = map.getCell(cell.x + i + 1, cell.y + 1);
				if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.DOWN);
				// FRONT MIDDLE SENSOR
				check = map.getCell(cell.x + i + 1, cell.y);
				if (isViableCell(check))candidates.put(check, Orientation.LEFT);
			}
		}

		//POSITION IS RIGHT OF ROBOT IN MAP
		for (int i = 1; i <= SensorConstants.LONG_RANGE; i++) {
			MapCell cand = map.getCell(cell.x - i, cell.y);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			// LEFT SENSOR
			check = map.getCell(cell.x - i - 1, cell.y + 1);
			if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.DOWN);
			//FRONT AND RIGHT SENSORS
			if (i <= SensorConstants.SHORT_RANGE) {
				// RIGHT SENSOR
				check = map.getCell(cell.x - i - 1, cell.y - 1);
				if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.UP);
				// FRONT MIDDLE SENSOR
				check = map.getCell(cell.x - i - 1, cell.y);
				if (isViableCell(check))candidates.put(check, Orientation.RIGHT);
			}
		}

		//POSITION IS BELOW THE ROBOT IN MAP
		for (int i = 1; i <= SensorConstants.LONG_RANGE; i++) {
			MapCell cand = map.getCell(cell.x, cell.y + i);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			// LEFT SENSOR
			check = map.getCell(cell.x + 1, cell.y + i + 1);
			if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.LEFT);
			//FRONT AND RIGHT SENSORS
			if (i <= SensorConstants.SHORT_RANGE) {
				// RIGHT SENSOR
				check = map.getCell(cell.x - 1, cell.y + i + 1);
				if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.RIGHT);
				// FRONT MIDDLE SENSOR
				check = map.getCell(cell.x, cell.y + i + 1);
				if (isViableCell(check))candidates.put(check, Orientation.DOWN);
			}
		}

		//POSITION IS ABOVE THE ROBOT IN MAP
		for (int i = 1; i <= SensorConstants.LONG_RANGE; i++) {
			MapCell cand = map.getCell(cell.x, cell.y - i);
			if (cand == null || cand.isObstacle()) {
				break;
			}
			// LEFT SENSOR
			check = map.getCell(cell.x - 1, cell.y - i - 1);
			if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.RIGHT);
			//FRONT AND RIGHT SENSORS
			if (i <= SensorConstants.SHORT_RANGE) {
				// RIGHT SENSOR
				check = map.getCell(cell.x + 1, cell.y - i - 1);
				if (isViableCell(check) && !candidates.containsKey(check))candidates.put(check, Orientation.LEFT);
				// FRONT MIDDLE SENSOR
				check = map.getCell(cell.x, cell.y - i - 1);
				if (isViableCell(check))candidates.put(check, Orientation.UP);
			}
		}
		return candidates;
	}
	
	private boolean isViableCell(MapCell cell) {
		return cell != null && !cell.isObstacle() && !cell.isVirtualWall();
	}
}
