package Main;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import Algorithms.MazeExplorer;
import Simulator.Robot;



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
	}
}
