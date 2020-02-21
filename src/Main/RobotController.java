package Main;

import java.util.Scanner;

import javax.swing.Timer;

import Algorithms.MazeExplorer;
import Constants.MapConstants;
import Simulator.IRobot;
import Simulator.VirtualRobot;
import utils.Map;
import utils.MapCell;


public class RobotController {
	public static final boolean REAL_RUN = false;
	private static final int EXPLORE_TIME_LIMIT = 360;
	private static final int FASTEST_PATH_TIME_LIMIT = 120;
	private static final int REAL_ROBOT_SPEED = 1;
	private Timer exploringTimer;
	
	private GUI gui;
	
	public RobotController() {
		gui = GUI.getInstance();
	}
	public void exploreMaze() {
			/*
			if (!REAL_RUN) {
				if (!gui.isIntExploreInput()) {
					gui.setStatus("invalid input for exploration");
					gui.setExploreBtnEnabled(true);
					return;
				}
				gui.refreshExploreInput();
				
			}
			
			MazeExplorer explorer = MazeExplorer.getInstance();
			Robot robot = Robot.getInstance();
			
			ExploreTimeClass timeActionListener;
			if (REAL_RUN) {
				timeActionListener = new ExploreTimeClass(EXPLORE_TIME_LIMIT);
				
			} else timeActionListener = new ExploreTimeClass(GUI.exploreTimeLimit);
			SwingWorker<Void, Void> exploreMaze = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					if (!REAL_RUN) {
						robot.setSpeed(REAL_ROBOT_SPEED);
					}
//					_hasReachedStart = false;
//					
//					explorer.explore(_robotPosition, _robotOrientation);
//					
//					//Compute the fastest path right after exploration for real run
//					if (REAL_RUN) {
//						AStarPathFinder pathFinder = AStarPathFinder.getInstance();
//						_fastestPath = pathFinder.findFastestPath(MazeExplorer.START[0], MazeExplorer.START[1], 
//								MazeExplorer.GOAL[0], MazeExplorer.GOAL[1], explorer.getMazeRef());
//						Orientation bestOri = pathFinder.getBestInitialOrientation(_fastestPath);
//						explorer.adjustOrientationTo(bestOri);
//					}
					
					return null;
				}
				@Override
				public void done() {
					
//					_hasReachedStart = true;
//					
//					//Generate P1 & P2 map descriptors
//					String P1Descriptor, P2Descriptor;
//					P1Descriptor = explorer.getP1Descriptor();
//					P2Descriptor = explorer.getP2Descriptor();
//					System.out.println("P1 descriptor: " + P1Descriptor);
//					System.out.println("P2 descriptor: " + P2Descriptor);
//				
//					//Set simulator console status
//					gui.setCoverageUpdate("actual coverage (%): " + String.format("%.1f", _actualCoverage));			
//					if (!gui.getTimerMessage().equals("exploration: time out")) {
//						gui.setTimerMessage("explored within time limit");
//					}
//					if (exploringTimer.isRunning()) {
//						exploringTimer.stop();
//					}
//					if (explorer.hasExploredTillGoal()) {
//						gui.setStatus("exploration reaches goal zone");
//						if (!REAL_RUN) {
//							gui.setFfpBtnEnabled(true);
//						}
//					} else {
//						gui.setStatus("exploration not reaches goal zone");
//					}
					
					
					//Waiting for fastest path command for real run
//					if (!REAL_RUN) {
//						gui.setExploreBtnEnabled(true);
//					} else {
//						try {
//							String msgFFP = _pcClient.readMessage();
//							while (!msgFFP.equals(Message.START_FASTEST)) {
//								msgFFP = _pcClient.readMessage();
//							}
//							gui.setStatus("start finding fastest path");
//							findFastestPath();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
				}
			};
			
			SwingWorker<Void, Void> updateCoverage = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
//					int numExplored;
//					JButton[][] mazeGrids = gui.getMazeGrids();
//					while (!_hasReachedStart) { 
//						numExplored = 0;
//						for (int x = 0; x < Arena.MAP_WIDTH; x++) {
//							for (int y = 0; y < Arena.MAP_LENGTH; y++) {
//								if (mazeGrids[x][y].getBackground() != Color.BLACK) {
//									numExplored ++;
//								}
//							}
//						}
//						_actualCoverage = (float)(100 * numExplored) / (float)(Arena.MAP_LENGTH * Arena.MAP_WIDTH);
//						gui.setCoverageUpdate( _actualCoverage);
//					}
					return null;
				}
			};
			
		
			exploringTimer = new Timer(1000, timeActionListener);
			exploringTimer.start();
			gui.setStatus("robot exploring");
//			_hasReachedTimeThreshold = false;
			exploreMaze.execute();
			updateCoverage.execute();
			
		}
	
	class ExploreTimeClass implements ActionListener {
		int timeLeft; 
		
		public ExploreTimeClass(int timeLimit) {
			timeLeft = timeLimit;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			timeLeft--;
			GUI.getInstance().setTimer(timeLeft);
//			MazeExplorer.getInstance().setTimeLeft(timeLeft);
//			if (timeLeft >= 0) {
//				SwingWorker<Void, Float> getThreshold = new SwingWorker<Void, Float>() {
////					Path _backPath;
//					@Override
//					protected Void doInBackground() throws Exception {
////						float threshold;
////						MazeExplorer explorer = MazeExplorer.getInstance();
////						AStarPathFinder pathFinder = AStarPathFinder.getInstance();
////						_backPath = pathFinder.findFastestPath(_robotPosition[0], _robotPosition[1], MazeExplorer.START[0], MazeExplorer.START[1], explorer.getMazeRef());
////						threshold = _backPath.getNumOfSteps() * (1 / (float)_speed) + THRESHOLD_BUFFER_TIME;
////						publish(threshold);
//						return null;
//					}
//					@Override
//					protected void process(List<Float> chunks) {
////						Float curThreshold = chunks.get(chunks.size() - 1);
////						if (timeLeft <= curThreshold) {
////							_hasReachedTimeThreshold = true;
////						}
//					}
//					
//				};
//				
//				if (timeLeft == 0) {
//					exploringTimer.stop();
//					gui.setTimerMessage("exploration: time out");
//					Toolkit.getDefaultToolkit().beep();
//				}
//				
//				getThreshold.execute();
				
//			} 
			
			if (timeLeft == 0) {
				exploringTimer.stop();
			}
		}
		*/
	}
	
	public static void main(String[] args) {
		GUI myGui = GUI.getInstance();
		myGui.setVisible(true);
		myGui.refreshExploreInput();
		Scanner sc = new Scanner(System.in);
		String key = null;
		IRobot myRobot = VirtualRobot.getInstance();
        Map realMap = Map.getRealMapInstance();
        Map map = Map.getExploredMapInstance();
        for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
        	for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
        		MapCell cell = realMap.getCell(i, j);
        		//cell.setExploredStatus(true);
        		if (!cell.isObstacle()) {
        			//TODO: Refactor to MAP, this sets the boundary of arena to virtual walls
        			if (i==0 || i==MapConstants.MAP_WIDTH-1 || j==0 || j==MapConstants.MAP_HEIGHT-1) {
        				cell.setVirtualWall(true);
        			} else {
        				for (int p=i-1; p<=i+1; p++) {
        					for (int q=j-1; q<=j+1; q++)
        						if (realMap.getCell(p, q).isObstacle())
        							cell.setVirtualWall(true);
        				}
        			}
        		}
        	}
        }
//        Graph graph = new Graph(realMap, 11, 3);
//        ShortestPath result = graph.GetShortestPath();
//        System.out.println(result.getWeight());
//        for(GraphNode n: result.getPath()){
//            System.out.println("Coordinate: (" + n.getX() + ", " + n.getY() + "), Orientation: " + (n.isHorizontal()? "horizontal":"vertical"));
//        }
//        System.out.println("Starting Orientation");
//        System.out.println(result.isStartingOrientationHorizontal()?"Facing Left":"Facing Up");
//        System.out.println("Instructions:");
//
//        for(RobotCommand command: result.generateInstructions()){
//            myRobot.doCommand(command);
//        }
		MazeExplorer e = MazeExplorer.getInstance();
        e.setRobot(myRobot);
        e.exploreMaze(map, 100000000);
	}
}
