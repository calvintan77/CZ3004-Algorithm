package connection;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import constants.SensorConstants;
import utils.Coordinate;
import map.MapTuple;
import utils.Orientation;
import utils.RobotCommand;

public class AlgoClient{ 
    private static AlgoClient instance;
    private static final String MOVE = "0"; 
    private static final String FASTEST_PATH = "1";
    private static final String START_FASTEST_PATH = "2";
    private static final String EXPLORATION_START = "3";
    private static final String SET_WAYPOINT = "4";
    private static final String SENSOR = "5";
    private static final String CALIBRATION = "6";
    private static final String RPI_IP = "192.168.9.9";
    private static final int PORT = 9999;
    final TCPSocket sock;
    private AlgoClient(TCPSocket sock) { 
        this.sock = sock; 
    }

    public static AlgoClient GetInstance(){
        if(instance == null){
            instance = new AlgoClient(new TCPSocket(RPI_IP, PORT));
        }
        return instance;
    }

    public void sendFastestPath(List<RobotCommand> ls) { 
        StringBuilder builder = new StringBuilder();
        builder.append(FASTEST_PATH);
        for(int i = 0; i < ls.size()-1; i++){
            if((ls.get(i) == RobotCommand.TURN_RIGHT && ls.get(i+1) == RobotCommand.TURN_LEFT) ||
                (ls.get(i) == RobotCommand.TURN_LEFT && ls.get(i+1) == RobotCommand.TURN_RIGHT))
            {
                ls.remove(i+1);
                ls.remove(i);
                i--;
            }
        }

        int i = 0;
        for (RobotCommand command : ls) {
            if (command == RobotCommand.MOVE_FORWARD) { 
                i++;
            } else {
                if (i != 0) { 
                    if (i > 16) { 
                        builder.append('F');
                        i -= 16;
                    }
                    builder.append(Integer.toHexString(i).toUpperCase());
                    i = 0; 
                } 
                builder.append(command.getLetter());
            }
        }
        if (i != 0) { 
            if (i > 16) { 
                builder.append('F');
                i -= 16;
            }
            builder.append(Integer.toHexString(i).toUpperCase()); 
        }
        sock.Send(builder.toString());
    }

    public void StartFastestPath(){
        sock.Send(START_FASTEST_PATH);
    }

    public void SendMove(RobotCommand command, MapTuple map, Orientation o, Coordinate c) { 
        StringBuilder builder = new StringBuilder();
        builder.append(MOVE);
        builder.append(command.getLetter());
        builder.append(Integer.toHexString(c.getX()));
        String y = Integer.toHexString(c.getY());
        if (y.length() == 1) { 
            builder.append("0");
        }
        builder.append(Integer.toHexString(c.getY()));
        builder.append(o.ordinal());
        builder.append("|");
        builder.append(map.GetP1());
        builder.append("|");
        builder.append(map.GetP2());
        sock.Send(builder.toString());
    }

    public void SendCalibrate(List<RobotCommand> ls) { 
        StringBuilder builder = new StringBuilder();
        builder.append(CALIBRATION);
        for (RobotCommand command : ls) { 
            builder.append(command.getLetter()); 
        }
        sock.Send(builder.toString());
    }
    
    public void HandleIncoming() throws IOException { 
        String message = sock.Receive();
        System.out.println("Handler: " + message);
        if(message.length() == 0) return;
        switch (Character.toString(message.charAt(0))) {
            case EXPLORATION_START:
                System.out.println("Detect exploration started");
                SyncObject.getSyncObject().SignalExplorationStart();
                break;
            case SET_WAYPOINT:
                String x = Character.toString(message.charAt(1));
                String y = message.substring(2);
                SyncObject.getSyncObject().SetWaypoint(new Coordinate(Integer.parseInt(x, 16), Integer.parseInt(y, 16)));
                break;
            case SENSOR:
                List<Integer> sensorData = message.substring(1).chars()
                .mapToObj(dat -> (dat == SensorConstants.SENSOR_NULL) ? SensorConstants.NULL_VALUE : (dat == SensorConstants.SENSOR_ERROR) ? SensorConstants.ERROR_VALUE : Integer.parseInt(Character.toString((char) dat)))
                .collect(Collectors.toList());
                SyncObject.getSyncObject().SetSensorData(sensorData);
                break;
        }
    }
}