package utils;

public enum RobotCommand {
	TURN_LEFT("J"), TURN_RIGHT("L"), MOVE_FORWARD("I");

	private String letter;

	RobotCommand(String letter){
		this.letter = letter;
	}

	private String getLetter(){
		return letter;
	}
}
