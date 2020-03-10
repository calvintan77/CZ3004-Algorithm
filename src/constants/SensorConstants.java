package constants;

import utils.Orientation;
import utils.Sensor;

public class SensorConstants {
	public static final int BLIND_SENSOR = 1;
	public static final int SHORT_RANGE = 3;
	public static final int LONG_RANGE = 5;

	public static final Sensor LEFT_SENSOR = new Sensor(Orientation.LEFT, -1, 0, LONG_RANGE);
	public static final Sensor FRONT_LEFT_SENSOR = new Sensor(Orientation.UP, -1, 1, SHORT_RANGE);
	public static final Sensor FRONT_MIDDLE_SENSOR = new Sensor(Orientation.UP, 0, 1, SHORT_RANGE);
	public static final Sensor FRONT_RIGHT_SENSOR = new Sensor(Orientation.UP, 1, 1, SHORT_RANGE);
	public static final Sensor RIGHT_SENSOR = new Sensor(Orientation.RIGHT, 1, 0, SHORT_RANGE);
    public static final boolean DEBUG_SENSORS = false;
}
