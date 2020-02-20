package RealRun;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import Simulator.Robot;
import utils.Orientation;
import utils.RobotCommand;

public class RobotRPI extends Robot{
	public static final String RPI_IP_ADDRESS = "192.168.9.9";
	public static final int RPI_PORT = 3053;
	
	private Socket rpiSocket;
	private BufferedWriter toRPI;
	private Scanner fromRPI;
	
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
	

	@Override
	public String getSensorValues(int[] robotPosition, Orientation robotOrientation) {
		try {
			sendMessage("GET_SENSOR_VALUES");
			return readMessage();
		} catch (Exception e) {
			
		}
		return null;
	}
	
	@Override
	public void doCommand(RobotCommand command) {
		try {
			sendMessage(command.name());
		} catch (Exception e) {
			
		}
	}
}
