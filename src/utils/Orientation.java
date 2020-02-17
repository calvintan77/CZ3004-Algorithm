package utils;


public enum Orientation {
	UP, LEFT, DOWN, RIGHT;
	
	public static Orientation getClockwise(Orientation curDirection) {
        return values()[(curDirection.ordinal() + values().length - 1) % values().length];
    }
	
	public static Orientation getCounterClockwise(Orientation curDirection) {
        return values()[(curDirection.ordinal() + 1) % values().length];
    }
}
