package utils;

public class MapCell {
	private boolean isExplored;
	private boolean isObstacle;
	private boolean isVirtualWall;
	
	public MapCell() {
		
	}
	
	public boolean isExplored() {
		return isExplored;
	}
	
	public boolean isObstacle() {
		return isObstacle;
	}
	
	public boolean isVirtualWall() {
		return isVirtualWall;
	}
	
	public void setExploredStatus(boolean status) {
		isExplored = status;
	}
	
	public void setObstacleStatus(boolean status) {
		isObstacle = status;
	}
	
	public void setVirtualWall(boolean status) {
		isVirtualWall = status;
	}
}
