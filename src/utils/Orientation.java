package utils;


public enum Orientation {
	UP, LEFT, DOWN, RIGHT;
	
	public static Orientation getClockwise(Orientation curDirection) {
        return values()[(curDirection.ordinal() + values().length - 1) % values().length];
    }
	
	public static Orientation getCounterClockwise(Orientation curDirection) {
        return values()[(curDirection.ordinal() + 1) % values().length];
    }

    public boolean isAligned(boolean isHorizontal){
        return (isHorizontal == (this == RIGHT || this == LEFT));
    }

    public int getRightTurns(Orientation dest){
	    int diff = this.ordinal() - dest.ordinal();
	    if(diff == -3){
	        return 1;
        }
	    if(diff == 3){
	        return -1;
        }
	    return diff;
    }

    public Coordinate behindCurrent(Coordinate curr){
	    switch(this){
            case UP:
                return new Coordinate(curr.getX(), curr.getY() - 1);
            case LEFT:
                return new Coordinate(curr.getX() + 1, curr.getY());
            case DOWN:
                return new Coordinate(curr.getX(), curr.getY() + 1);
            case RIGHT:
                return new Coordinate(curr.getX() - 1, curr.getY());
        }
        return curr;
    }
}
