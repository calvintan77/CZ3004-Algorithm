package utils;

/**
 * Class representing a single sensor and its relative position and orientation
 */
public class Sensor {
    private OrientationMutationFunction orientationMutation;
    private final int xOffset;
    private final int yOffset;
    private final int range;
    public Coordinate GetSensorPosition(Orientation robotOrientation, Coordinate robotPosition){
        switch(robotOrientation){
            case UP:
                return new Coordinate(robotPosition.getX() + xOffset, robotPosition.getY() + yOffset);
            case RIGHT:
                return new Coordinate(robotPosition.getX() + yOffset, robotPosition.getY() - xOffset);
            case DOWN:
                return new Coordinate(robotPosition.getX() - xOffset, robotPosition.getY() - yOffset);
            case LEFT:
                return new Coordinate(robotPosition.getX() - yOffset, robotPosition.getY() + xOffset);
        }
        return null;
    }

    public Coordinate GetRobotPositionFromSensorPos(Orientation robotOrientation, Coordinate sensorPosition){
        switch(robotOrientation){
            case UP:
                return new Coordinate(sensorPosition.getX() - xOffset, sensorPosition.getY() - yOffset);
            case RIGHT:
                return new Coordinate(sensorPosition.getX() - yOffset, sensorPosition.getY() + xOffset);
            case DOWN:
                return new Coordinate(sensorPosition.getX() + xOffset, sensorPosition.getY() + yOffset);
            case LEFT:
                return new Coordinate(sensorPosition.getX() + yOffset, sensorPosition.getY() - xOffset);
        }
        return null;
    }

    public Orientation getSensorFacing(Orientation robotOrientation){
        return orientationMutation.mutateOrientation(robotOrientation);
    }

    public Sensor(Orientation sensorOrientationOffset, int xOffset, int yOffset, int range) {
        switch(sensorOrientationOffset){
            case LEFT:
                this.orientationMutation = Orientation::getCounterClockwise;
                break;
            case UP:
                this.orientationMutation = (x -> x);
                break;
            case RIGHT:
                this.orientationMutation = Orientation::getClockwise;
                break;
            case DOWN:
                this.orientationMutation = (x -> Orientation.getClockwise(Orientation.getClockwise(x)));
                break;
        }
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.range = range;
    }

    public int GetRange(){
        return this.range;
    }

    private interface OrientationMutationFunction {
         Orientation mutateOrientation(Orientation o);
    }
}
