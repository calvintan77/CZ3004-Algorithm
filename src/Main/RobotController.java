package Main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import Algorithms.MazeExplorer;
import Constants.MapConstants;
import RealRun.RpiRobot;
import Simulator.IRobot;
import Simulator.VirtualRobot;
import utils.*;


public class RobotController {
	public static RobotController robotController;
	public static final boolean REAL_RUN = false;
	private static final int EXPLORE_TIME_LIMIT = 360;
	private static final int FASTEST_PATH_TIME_LIMIT = 120;
	private static final int REAL_ROBOT_SPEED = 1;
	private IRobot robot;
	
	private GUI gui;
	
	public static RobotController getInstance() {
		if (robotController == null) {
			robotController = new RobotController();
		}
		return robotController;
	}
	
	public RobotController() {
		gui = GUI.getInstance();
	}
	
	public void exploreMaze() {	
		if (!REAL_RUN) {
			robot = VirtualRobot.getInstance();
			if (!gui.isIntExploreInput()) {
				gui.setStatus("invalid input for exploration");
				gui.setExploreBtnEnabled(true);
				return;
			}
			gui.refreshExploreInput();
		}
		
		MazeExplorer explorer = MazeExplorer.getInstance();
		explorer.setRobot(robot);

		ExploreTimeClass timeActionListener;
		SwingWorker<Void, Void> exploreMaze = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				gui.setStatus("Exploring");
				try {
					if (!REAL_RUN) {
						robot.prepareOrientation(robot.prepareOrientationCmds(Orientation.UP), null);
						explorer.exploreMaze(Map.getExplorationMap(), GUI.exploreTimeLimit, gui.getTargetExplorePercent());
					}else explorer.exploreMaze(Map.getExplorationMap(), EXPLORE_TIME_LIMIT, gui.getTargetExplorePercent());
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
				MapLoader.saveMap(Map.getExplorationMap(), "src/exploredMap.txt");
				return null;
			}
			@Override
			public void done() {
				gui.setStatus("Done exploring");
			}

		};
		if (REAL_RUN) {
			timeActionListener = new ExploreTimeClass(EXPLORE_TIME_LIMIT, exploreMaze);
			
		} else timeActionListener = new ExploreTimeClass(GUI.exploreTimeLimit, exploreMaze);
		SwingWorker<Void, Void> updateCoverage = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				while (!(exploreMaze.isDone() || exploreMaze.isCancelled())) {
					Float coverageRate = Map.getExplorationMap().getNumSeen() *100f / 300f ;
					gui.setCoverageUpdate(coverageRate);
				}
				return null;
			}
		};

		Timer exploringTimer = new Timer(1000, timeActionListener);
		timeActionListener.setTimer(exploringTimer);
		exploringTimer.start();
		updateCoverage.execute();
		exploreMaze.execute();
		
	}
	
	public void fastestPath() {
		if (!RobotController.REAL_RUN) {
			robot = VirtualRobot.getInstance();
			if (!gui.isIntFFPInput()) {
				gui.setStatus("invalid input for finding fastest path");
				gui.setFfpBtnEnabled(true);
				return;
			}
			gui.refreshFfpInput();
		} else robot = RpiRobot.getInstance();
		SwingWorker<Void, Void> fastestPath = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				gui.setStatus("finding fastest path");
				try {
					int wayPointX = gui.getWayPointX();
					int wayPointY = gui.getWayPointY();
					Map exploredMap = null;
					if (Map.getExplorationMap().getSeenPercentage() < 100) {
						exploredMap = Map.getExplorationMap().CloneWithUnseenAsObstacles();
					} else exploredMap = Map.getExplorationMap();
					ShortestPath result;
					do {
						Graph graph = new Graph(exploredMap, wayPointX, wayPointY);
						if (wayPointX != 1 || wayPointY != 1) {
							gui.setMazeGridColor(wayPointX, wayPointY, GUI.WAYPOINT_COLOR);
							gui.setMapGridColor(wayPointX, wayPointY, GUI.WAYPOINT_COLOR);
						}
						result = graph.GetShortestPath();
						if (result == null) {
							System.out.println("Unable to find path through waypoint");
							graph = new Graph(exploredMap, 1, 1);
							result = graph.GetShortestPath();
						}
						if (result == null) {
							exploredMap.expandSearchSpace();
						}
					}while (result == null && !exploredMap.getAllUnseen().isEmpty());
					if (result.isStartingOrientationHorizontal()) {
						robot.prepareOrientation(robot.prepareOrientationCmds(Orientation.RIGHT), null);
					} else robot.prepareOrientation(robot.prepareOrientationCmds(Orientation.UP), null);
					List<GraphNode> path = result.getPath();
					for(GraphNode n: path){
						if (!( (n.getX()==0 && n.getY()==0) || 
							(n.getX()==MapConstants.MAP_WIDTH-1 && n.getY()==MapConstants.MAP_HEIGHT-1) ))
					    	gui.setMazeGridColor(n.getX(), n.getY(), GUI.FASTEST_PATH_COLOR);
					}
					
					robot.setFastestPath(result.generateInstructions());
					robot.doFastestPath(true);
					for(GraphNode n: path){
						if (!(gui.getMazeGridColor(n.getX(), n.getY()) == GUI.ROBOT_COLOR
							|| gui.getMazeGridColor(n.getX(), n.getY()) == GUI.ROBOT_HEAD_COLOR)
							&& !( (n.getX()==0 && n.getY()==0) || 
								(n.getX()==MapConstants.MAP_WIDTH-1 && n.getY()==MapConstants.MAP_HEIGHT-1) ))
							gui.setMazeGridColor(n.getX(), n.getY(), GUI.FASTEST_PATH_COLOR);
					}
					if (wayPointX != 1 || wayPointY != 1) {
						gui.setMazeGridColor(wayPointX, wayPointY, GUI.WAYPOINT_COLOR);
						gui.setMapGridColor(wayPointX, wayPointY, GUI.WAYPOINT_COLOR);
					}
				} catch (Exception e) {
//					e.printStackTrace();
					System.out.println("Unable to find fastest path");					
				}
				return null;
			}
			@Override
			public void done() {
				gui.setStatus("Done finding fastest path");
			}

		};
		
		ExploreTimeClass timeActionListener;
		if (REAL_RUN) {
			timeActionListener = new ExploreTimeClass(FASTEST_PATH_TIME_LIMIT, fastestPath);
			
		} else timeActionListener = new ExploreTimeClass(GUI.fastestPathTimeLimit, fastestPath);
		Timer fastestPathTimer = new Timer(1000, timeActionListener);
		timeActionListener.setTimer(fastestPathTimer);
		fastestPathTimer.start();
		fastestPath.execute();
		
	}
	
	class ExploreTimeClass implements ActionListener {
		int timeLimit;
		int timeLeft;
		Timer timer;
		SwingWorker<Void, Void> task;
		public ExploreTimeClass(int timeLimit, SwingWorker<Void,Void> task) {
			this.timeLimit = timeLimit;
			timeLeft = timeLimit;
			this.task = task;
		}
		
		public void setTimer(Timer timer) {
			this.timer = timer;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			timeLeft--;
			GUI.getInstance().setTimer(timeLeft);
			if (timeLeft >= 0) {

				
				if (timeLeft == 0) {
					gui.setTimerMessage("Time out");
					gui.setExploreBtnEnabled(true);
					gui.setFfpBtnEnabled(true);
					if (timer != null)
						timer.stop();
				} else if (task.isDone()) {
					gui.setTimerMessage(String.format("finish within time limit (%ds)", timeLimit-timeLeft));
					gui.setExploreBtnEnabled(true);
					gui.setFfpBtnEnabled(true);
					if (timer != null)
						timer.stop();
				}
				
			} 
			
		}
		
	}
	
	public static void main(String[] args) {
		GUI myGui = GUI.getInstance();
		myGui.setVisible(true);
		myGui.refreshExploreInput();
	}
}
