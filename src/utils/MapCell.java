package utils;

import java.util.ArrayList;
import java.util.List;

public class MapCell {

	private boolean isObstacle;
	private boolean isVirtualWall;
	private boolean isSeen; 
	public int x; // changed to public for convenience
	public int y; 
	
	public MapCell(int x, int y) {
		this.x = x;
		this.y = y; 
	}
	
	public MapCell(Coordinate arr) {
		this.x = arr.getX();
		this.y = arr.getY();
	}
	
	public boolean isObstacle() {
		return isObstacle;
	}
	
	public boolean isVirtualWall() {
		return isVirtualWall;
	}
	
	public void setObstacleStatus(boolean status) {
		isObstacle = status;
	}
	
	public void setVirtualWall(boolean status) {
		isVirtualWall = status;
	}
	
	public void setSeen(boolean status) {
		this.isSeen = status; 
	}
	
	public boolean getSeen() {
		return this.isSeen;
	}
	
	public void clear() {
		this.isSeen = false;
		this.isVirtualWall = false;
		this.isObstacle = false;
	}
	
	public String toString() {
		return x+","+y;
	}
	
}
