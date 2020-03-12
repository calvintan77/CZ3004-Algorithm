package utils;

import java.util.HashMap;
import java.util.Map;

public enum RobotCommand {
	TURN_LEFT("L"), TURN_RIGHT("R"), MOVE_FORWARD("M"), CALIBRATE("C"), NO_OP("Z");

	private String letter;
	private static final Map<String, RobotCommand> map = new HashMap<>();

	RobotCommand(String letter){
		this.letter = letter;
	}

	public String getLetter(){
		return letter;
	}
	
	static {
        for (RobotCommand robotCommand : RobotCommand.values()) {
            map.put(robotCommand.getLetter(), robotCommand);
        }
    }
	
	public static RobotCommand getCommand(String key) {
		return map.get(key);
	}
}
