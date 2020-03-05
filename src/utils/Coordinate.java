package utils;

public class Coordinate {
    private final int X;
    private final int Y;
    private Facing facing = Facing.NONE;

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public Facing getFacing(){
        return facing;
    }
    
    public void setFacing(Facing f) {
    	this.facing = f;
    }

    public Coordinate(int x, int y) {
        X = x;
        Y = y;
    }

    public Coordinate(int x, int y, Facing f){
        this(x, y);
        this.facing = f;
    }

    public enum Facing{
        HORIZONTAL, VERTICAL, NONE
    }

    public boolean equals(Object o) {
        if(!(o instanceof Coordinate)) return false;
        Coordinate cor = (Coordinate) o;
        return this.X == cor.getX() && this.Y == cor.getY();
    }
}
