package path;

import utils.Coordinate;
import utils.Orientation;
import utils.RobotCommand;

import java.util.LinkedList;
import java.util.List;

/**
 * Class to represent shortest path result
 */
public class ShortestPath{
    private final double weight;
    private final List<GraphNode> path;

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
        Orientation currOrientation = this.getStartingOrientation();
        //Loop over the path
        for(int i = 0; i < path.size(); i++){
            GraphNode curr = path.get(i);
            if(curr.isVirtual()) continue;
            if(currOrientation.isAligned(curr.isHorizontal())){
                result.add(RobotCommand.MOVE_FORWARD);
            }else{
                GraphNode next = path.get(i+1);
                if(next.isVirtual()) continue;
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
        for(GraphNode n: path){
            if(n.isVirtual()) continue;
            return n.isHorizontal();
        }
        return false;
    }

    public boolean isEndingOrientationHorizontal(){
        for(int i = path.size()-1; i >= 0; i--){
            if(path.get(i).isVirtual()) continue;
            return path.get(i).isHorizontal();
        }
        return false;
    }

    public Orientation getStartingOrientation(){
        for(int i = 0; i < path.size(); i++){
            if (path.get(i).isVirtual()) continue;
            // Unfortunate neutral case
            if(i+1>= path.size()){
                return path.get(i).isHorizontal() ? Orientation.RIGHT : Orientation.UP;
            }
            if(path.get(i).isHorizontal()){
                return path.get(i+1).getX() > path.get(i).getX() ? Orientation.RIGHT : Orientation.LEFT;
            }else{
                return path.get(i+1).getY() > path.get(i).getY() ? Orientation.UP : Orientation.DOWN;
            }
        }
        return Orientation.UP;
    }

    public Orientation getEndingOrientation(){
        for(int i = path.size() - 1; i >= 0; i--){
            if (path.get(i).isVirtual()) continue;
            // Unfortunate neutral case
            if(i-1 < 0){
                return path.get(i).isHorizontal() ? Orientation.RIGHT : Orientation.UP;
            }
            if(path.get(i).isHorizontal()){
                return path.get(i).getX() > path.get(i-1).getX() ? Orientation.RIGHT : Orientation.LEFT;
            }else{
                return path.get(i).getY() > path.get(i-1).getY() ? Orientation.UP : Orientation.DOWN;
            }
        }
        return Orientation.UP;
    }

    public Coordinate getDestination() {
        // Unfortunate neutral case
        if(path.size() == 1) return new Coordinate(path.get(0).getX(), path.get(0).getY());
        if(path.get(path.size()-1).isVirtual()) return new Coordinate(path.get(path.size()-2).getX(), path.get(path.size()-2).getY());
        return new Coordinate(path.get(path.size()-1).getX(), path.get(path.size()-1).getY());
    }
}