package utils;

import java.util.LinkedList;
import java.util.List;

/**
 * Class to represent shortest path result
 */
public class ShortestPath{
    private double weight;
    private List<GraphNode> path;

    public ShortestPath(double weight, List<GraphNode> path) {
        this.weight = weight;
        this.path = path;
    }

    public List<GraphNode> getPath(){
        return path;
    }

    public double getWeight(){
        return weight;
    }

    public List<RobotCommand> generateInstructions(){
        List<RobotCommand> result = new LinkedList<>();
        //Set starting orientation
        Orientation currOrientation;
        if(isStartingOrientationHorizontal()){
            if(path.size() < 2) currOrientation = Orientation.RIGHT;
            else currOrientation = path.get(2).getX() > path.get(1).getX()? Orientation.RIGHT : Orientation.LEFT;
        }else{
            if(path.size() < 2) currOrientation = Orientation.UP;
            else currOrientation = path.get(2).getY() > path.get(1).getY()? Orientation.UP : Orientation.DOWN;
        }
        //Loop over the path
        for(int i = 0; i < path.size(); i++){
            GraphNode curr = path.get(i);
            if(curr.isVirtual()) continue;
            if(currOrientation.isAligned(curr.isHorizontal())){
                result.add(RobotCommand.MOVE_FORWARD);
            }else{
                GraphNode next = path.get(i+1);
                switch(currOrientation){
                    case UP:
                        if (next.getX() > curr.getX()) {
                            result.add(RobotCommand.TURN_RIGHT);
                            currOrientation = Orientation.RIGHT;
                        } else {
                            result.add(RobotCommand.TURN_LEFT);
                            currOrientation = Orientation.LEFT;
                        }
                        break;
                    case DOWN:
                        if (next.getX() < curr.getX()) {
                            result.add(RobotCommand.TURN_RIGHT);
                            currOrientation = Orientation.LEFT;
                        } else {
                            result.add(RobotCommand.TURN_LEFT);
                            currOrientation = Orientation.RIGHT;
                        }
                        break;
                    case LEFT:
                        if (next.getY() > curr.getY()) {
                            result.add(RobotCommand.TURN_RIGHT);
                            currOrientation = Orientation.UP;
                        } else {
                            result.add(RobotCommand.TURN_LEFT);
                            currOrientation = Orientation.DOWN;
                        }
                        break;
                    case RIGHT:
                        if (next.getY() < curr.getY()) {
                            result.add(RobotCommand.TURN_RIGHT);
                            currOrientation = Orientation.DOWN;
                        } else {
                            result.add(RobotCommand.TURN_LEFT);
                            currOrientation = Orientation.UP;
                        }
                        break;
                }
            }
        }
        result.remove(0);
        return result;
    }

    public boolean isStartingOrientationHorizontal(){
        return path.get(1).isHorizontal();
    }

    public boolean isEndingOrientationHorizontal(){
        return path.get(path.size()-1).isHorizontal();
    }

    public Orientation getStartingOrientation(){
        int compensation = path.get(0).isVirtual()?1:0;
        // Unfortunate neutral case
        if(path.size() == 1) return isStartingOrientationHorizontal() ? Orientation.RIGHT : Orientation.UP;
        if(isStartingOrientationHorizontal()){
            return path.get(1+compensation).getX() > path.get(0+compensation).getX() ? Orientation.RIGHT : Orientation.LEFT;
        }else{
            return path.get(1+compensation).getY() > path.get(0+compensation).getY() ? Orientation.UP : Orientation.DOWN;
        }
    }

    public Orientation getEndingOrientation(){
        // Unfortunate neutral case
        if(path.size() == 1) return isEndingOrientationHorizontal() ? Orientation.RIGHT : Orientation.UP;
        int compensation = path.get(path.size()-1).isVirtual()?2:1;
        if(isEndingOrientationHorizontal()){
            return path.get(path.size()-compensation).getX() > path.get(path.size()-compensation-1).getX() ? Orientation.RIGHT : Orientation.LEFT;
        }else{
            return path.get(path.size()-compensation).getY() > path.get(path.size()-compensation-1).getY() ? Orientation.UP : Orientation.DOWN;
        }
    }

    public Coordinate getDestination() {
        // Unfortunate neutral case
        if(path.size() == 1) return new Coordinate(path.get(0).getX(), path.get(0).getY());
        if(path.get(path.size()-1).isVirtual()) return new Coordinate(path.get(path.size()-2).getX(), path.get(path.size()-2).getY());
        return new Coordinate(path.get(path.size()-1).getX(), path.get(path.size()-1).getY());
    }
}