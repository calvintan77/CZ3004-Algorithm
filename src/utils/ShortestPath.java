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
        Orientation currOrientation = isStartingOrientationHorizontal()?
                Orientation.RIGHT : Orientation.UP;
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

    public Orientation getStartingOrientation(){
        // Unfortunate neutral case
        if(path.size() == 1) return isStartingOrientationHorizontal() ? Orientation.RIGHT : Orientation.UP;
        if(isStartingOrientationHorizontal()){
            return path.get(1).getX() > path.get(0).getX() ? Orientation.RIGHT : Orientation.LEFT;
        }else{
            return path.get(1).getY() > path.get(0).getY() ? Orientation.UP : Orientation.DOWN;
        }
    }
}