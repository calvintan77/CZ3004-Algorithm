package map;

public class MapCell {

	private boolean isObstacle;
	private boolean isVirtualWall;
	private boolean isSeen;
	private boolean hasConflict;
	public final int x; // changed to public for convenience
	public final int y;
	
	public MapCell(int x, int y) {
		this.x = x;
		this.y = y; 
	}
	
	public boolean isObstacle() {
		return isObstacle;
	}
	
	public boolean isVirtualWall() {
		return isVirtualWall;
	}
	
	public void setObstacleStatus(boolean status) {
		System.out.println("setting " + x + ", " + y + "to " + (isObstacle?"Obstacle":"Empty"));
		isObstacle = status;
	}
	
	public void setVirtualWall(boolean status) {
		isVirtualWall = status;
	}
	
	public void setSeen(boolean status) {
		this.isSeen = status; 
	}
	
	public boolean isValidSeen() {
		return this.isSeen && !this.hasConflict;
	}

	public boolean isSeen() { return this.isSeen; }
	
	public String toString() {
		return x+","+y;
	}

	public boolean isHasConflict() {
		return hasConflict;
	}

	public void setConflict(boolean conflict){
		hasConflict = conflict;
	}
}
