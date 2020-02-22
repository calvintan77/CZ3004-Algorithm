package RealRun;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

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
	private Orientation direction;

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
		return this.direction;
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
	public void prepareOrientation(Orientation o) {

	}

	@Override
	public void prepareOrientation(Orientation o, boolean checkSensors) {

	}

	@Override
	public void setOrientation(Orientation o) {
		this.direction = o;
	}

	@Override
	public HashMap<MapCell, Orientation> getLeftSensorVisibilityCandidates(Map map, MapCell cell) {
		return null;
	}
}
