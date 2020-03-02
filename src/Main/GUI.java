package Main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import Constants.MapConstants;
import RealRun.RpiRobot;
import Simulator.IRobot;
import Simulator.VirtualRobot;
import utils.Map;
import utils.MapProcessor;
import utils.Orientation;
import utils.RobotCommand;


public class GUI extends JFrame implements ActionListener{
	private static GUI gui;

	private static final String EXPLORATION = "Explore maze";
	private static final String FASTEST_PATH = "Find fastest path";
	
	public static final Color EMPTY_CELL_COLOR = Color.GREEN;
	public static final Color OBSTACLE_CELL_COLOR = Color.RED;
	public static final Color GOAL_START_ZONE_COLOR = Color.ORANGE;
	public static final Color ROBOT_COLOR = Color.CYAN;
	public static final Color ROBOT_HEAD_COLOR = Color.GRAY;
	public static final Color UNEXPLORED_CELL_COLOR = Color.BLACK;
	public static final Color FASTEST_PATH_CORLOR = Color.YELLOW;
	public static final Color WAYPOINT_COLOR = Color.BLUE;
	
	private JPanel displayedPane, mapPane, settingPane, exploredMapPane;
	private JLabel status, timer, coverageRateUpdate;
	private JButton[][] mapGrids, mazeGrids;
	private JTextField[] exploreTextFields, ffpTextFields;
	private JButton exploreButton, ffpButton;
	private int[] robotPosition;
	private Orientation currentOrientation;
	private int targetExplorePercentage, wayPointX, wayPointY, prevwayPointX, prevwayPointY;
	public static int exploreTimeLimit;
	public static int fastestPathTimeLimit;
	
	public static GUI getInstance() {
		if (gui == null) {
			gui = new GUI();
		}
		return gui;
	}

	/**
	 * Create the simulator.
	 */
	public GUI() {
		super("MDP Simulator - Arena Exploration & Fastest Path Computation");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		displayedPane = new JPanel();
		displayedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		displayedPane.setLayout(new BorderLayout(0, 0));
		setContentPane(displayedPane);
		initializeDisplayedPane(displayedPane);
		pack();
		
	}

	public void setExploreBtnEnabled(boolean value) {
		exploreButton.setEnabled(value);
	}
		
	public void setFfpBtnEnabled(boolean value) {
		ffpButton.setEnabled(value);
	}
	
	public JPanel getContentPane() {
		return displayedPane;
	}
	
	public JButton[][] getMazeGrids() {
		return mazeGrids;
	}
	
	public void setMazeGrids(JButton[][] mazeGrids) {
		this.mazeGrids = mazeGrids;
	}
	
	public int getTargetExplorePercent() {
		return targetExplorePercentage;
	}
	
	public int[] getRobotPosition() {
		return robotPosition;
	}
	
	public Orientation getRobotOrientation() {
		return currentOrientation;
	}
	
	public int getWayPointX() {
		return wayPointX;
	}
	
	public int getWayPointY() {
		return wayPointY;
	}
	
	public Color getMazeGridColor(int x, int y) {
		return mazeGrids[x][y].getBackground();
	}
	
	public void setMazeGridColor(int x, int y, Color color) {
		mazeGrids[x][y].setBackground(color);
	}
	
	public void setMapGridColor(int x, int y, Color color) {
		mapGrids[x][y].setBackground(color);
	}
	
	private void initializeDisplayedPane(JPanel contentPane) {

		/*
		 * Add right panel: the reference map and two control buttons
		 * (load/clear).
		 */
		mapPane = new JPanel(new FlowLayout());
		mapPane.setPreferredSize(new Dimension(450, 650));
		JPanel map = new JPanel();
		map.setLayout(new GridLayout(MapConstants.MAP_HEIGHT, MapConstants.MAP_WIDTH));
		map.setPreferredSize(new Dimension(450, 600));
		mapGrids = new JButton[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT];
		for (int x = 0; x < MapConstants.MAP_HEIGHT; x++) {
			for (int y = 0; y < MapConstants.MAP_WIDTH; y++) {
				int realX = y;
				int realY = 19-x;
				mapGrids[realX][realY] = new JButton();
				
				if (RobotController.REAL_RUN) {
					mapGrids[realX][realY].setEnabled(false);
					mapGrids[realX][realY].setBackground(ROBOT_HEAD_COLOR);
				} else {
					mapGrids[realX][realY].setActionCommand("ToggleObstacleAt " + realX + "," + realY);
					mapGrids[realX][realY].addActionListener(this);
					mapGrids[realX][realY].setBorder(BorderFactory.createLineBorder(ROBOT_HEAD_COLOR));
					mapGrids[realX][realY].setBackground(EMPTY_CELL_COLOR);
					if ((realX <= 2 & realY <= 2) || (realX >= 12 & realY >= 17)) {
						mapGrids[realX][realY].setEnabled(false);
						mapGrids[realX][realY].setBackground(Color.ORANGE);
						if (realX == 1 && realY == 1) {
							mapGrids[realX][realY].setText("S");
						} else if (realX == 13 && realY == 18) {
							mapGrids[realX][realY].setText("G");
						}
					}
				}
				
				map.add(mapGrids[realX][realY]);
			}
		}
		
		if (!RobotController.REAL_RUN) {
			loadMapGrids();
		}
		
		mapPane.add(map);
		JButton loadMap = new JButton("Load");
		
		if (RobotController.REAL_RUN) {
			loadMap.setEnabled(false);
		} else {
			loadMap.setActionCommand("LoadMap");
			loadMap.addActionListener(this);
		}
		
		JButton clearMap = new JButton("Clear");
		
		if (RobotController.REAL_RUN) {
			clearMap.setEnabled(false);
		} else {
			clearMap.setActionCommand("ClearMap");
			clearMap.addActionListener(this);
		}
		
		mapPane.add(loadMap);
		mapPane.add(clearMap);
		contentPane.add(mapPane, BorderLayout.CENTER);

		/*
		 * Add middle panel: the explore/fastest path control panel.
		 */

		// Add control switch (combo box).
		settingPane = new JPanel(new BorderLayout());
		settingPane.setBorder(new EmptyBorder(50, 20, 50, 20));
		String comboBoxItems[] = { EXPLORATION, FASTEST_PATH };
		JComboBox cbCtrlSwitch = new JComboBox(comboBoxItems);
		cbCtrlSwitch.setFont(new Font("Tahoma", Font.BOLD, 16));
		cbCtrlSwitch.setEditable(false);
		cbCtrlSwitch.addActionListener(this);
		cbCtrlSwitch.setActionCommand("SwitchCtrl");
		settingPane.add(cbCtrlSwitch, BorderLayout.NORTH);

		// Add control panel for exploring.
		JLabel[] exploreCtrlLabels = new JLabel[4];
		exploreTextFields = new JTextField[4];
		exploreButton = new JButton("Explore");
		
		if (RobotController.REAL_RUN) {
			exploreButton.setEnabled(false);
		} else {
			exploreButton.setActionCommand("ExploreMaze");
			exploreButton.addActionListener(this);
		}
		
		exploreCtrlLabels[0] = new JLabel("Robot initial position: ");
		exploreCtrlLabels[1] = new JLabel("Speed (steps/sec): ");
		exploreCtrlLabels[2] = new JLabel("Target coverage (%): ");
		exploreCtrlLabels[3] = new JLabel("Time limit (sec): ");
		for (int i = 0; i < 4; i++) {
			exploreTextFields[i] = new JTextField(10);
			if (RobotController.REAL_RUN) {
				exploreTextFields[i].setEditable(false);
			}
		}
		
		JPanel exploreInputPane = new JPanel(new GridLayout(4, 2));
		
		exploreInputPane.add(exploreCtrlLabels[0]);
		exploreInputPane.add(exploreTextFields[0]);
		exploreInputPane.add(exploreCtrlLabels[1]);
		exploreInputPane.add(exploreTextFields[1]);
		exploreInputPane.add(exploreCtrlLabels[2]);
		exploreInputPane.add(exploreTextFields[2]);
		exploreInputPane.add(exploreCtrlLabels[3]);
		exploreInputPane.add(exploreTextFields[3]);
		
		if (!RobotController.REAL_RUN) {
			exploreTextFields[0].setEditable(false);
			exploreCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[0].setText("1,1");
			exploreTextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[0].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			exploreTextFields[0].getDocument().putProperty("name", "Robot Initial Position");
			
			exploreCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[1].setText("10");
			exploreTextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[1].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			exploreTextFields[1].getDocument().putProperty("name", "Robot Explore Speed");
			
			exploreCtrlLabels[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[2].setText("100");
			exploreTextFields[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[2].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			exploreTextFields[2].getDocument().putProperty("name", "Target Coverage");
			
			exploreCtrlLabels[3].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[3].setText("360");
			exploreTextFields[3].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[3].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			exploreTextFields[3].getDocument().putProperty("name", "Exploration time limit");
		}
		
		JPanel exploreBtnPane = new JPanel();
		exploreBtnPane.add(exploreButton);

		JPanel exploreCtrlPane = new JPanel();
		exploreCtrlPane.add(exploreInputPane);
		exploreCtrlPane.add(exploreBtnPane);
		exploreCtrlPane.setBorder(new EmptyBorder(20, 20, 20, 20));

		// Add control panel for finding fastest path.
		JLabel[] ffpCtrlLabels = new JLabel[4];
		ffpTextFields = new JTextField[4];
		ffpButton = new JButton("Find fatest path");
		
		if (RobotController.REAL_RUN) {
			ffpButton.setEnabled(false);
		} else {
			ffpButton.setActionCommand("FindFastestPath");
			ffpButton.addActionListener(this);
			ffpButton.setEnabled(true);
		}
		
		ffpCtrlLabels[0] = new JLabel("Speed (steps/sec): ");
		ffpCtrlLabels[1] = new JLabel("Time limit (sec): ");
		ffpCtrlLabels[2] = new JLabel("WaypointX: ");
		ffpCtrlLabels[3] = new JLabel("WaypointY: ");
		
		for (int i = 0; i < ffpTextFields.length; i++) {
			ffpTextFields[i] = new JTextField(10);
			if (RobotController.REAL_RUN) {
				ffpTextFields[i].setEditable(false);
			}
		}

		JPanel ffpInputPane = new JPanel(new GridLayout(4, 2));
		ffpInputPane.add(ffpCtrlLabels[0]);
		ffpInputPane.add(ffpTextFields[0]);
		ffpInputPane.add(ffpCtrlLabels[1]);
		ffpInputPane.add(ffpTextFields[1]);
		ffpInputPane.add(ffpCtrlLabels[2]);
		ffpInputPane.add(ffpTextFields[2]);
		ffpInputPane.add(ffpCtrlLabels[3]);
		ffpInputPane.add(ffpTextFields[3]);
		
		if (!RobotController.REAL_RUN) {
			ffpCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[0].setEditable(false);
	
			ffpTextFields[1].setText("120");
			ffpCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[1].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			ffpTextFields[1].getDocument().putProperty("name", "Robot FFP Time Limit");
			
			ffpTextFields[2].setText("1");
			ffpCtrlLabels[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[2].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			ffpTextFields[2].getDocument().putProperty("name", "WaypointX");
			
			ffpTextFields[3].setText("1");
			ffpCtrlLabels[3].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[3].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[3].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			ffpTextFields[3].getDocument().putProperty("name", "WaypointY");
		}

		JPanel ffpBtnPane = new JPanel();
		ffpBtnPane.add(ffpButton);

		JPanel ffpCtrlPane = new JPanel();
		ffpCtrlPane.add(ffpInputPane);
		ffpCtrlPane.add(ffpBtnPane);
		ffpCtrlPane.setBorder(new EmptyBorder(20, 20, 20, 20));

		// Add card panel to switch between explore and shortest path panels.
		JPanel cardPane = new JPanel(new CardLayout());
		cardPane.add(exploreCtrlPane, EXPLORATION);
		cardPane.add(ffpCtrlPane, FASTEST_PATH);
		cardPane.setPreferredSize(new Dimension(280, 300));
		settingPane.add(cardPane, BorderLayout.CENTER);

		// Add status panel.
		JPanel statusPane = new JPanel(new BorderLayout());
		JLabel statusLabel = new JLabel("Status Console:");
		statusPane.add(statusLabel, BorderLayout.NORTH);
		JPanel statusConsole = new JPanel(new GridLayout(3, 1));
		statusConsole.setBackground(Color.LIGHT_GRAY);
		statusConsole.setPreferredSize(new Dimension(280, 100));
		status = new JLabel("waiting for commands...");
		status.setHorizontalAlignment(JLabel.CENTER);
		timer = new JLabel();
		timer.setHorizontalAlignment(JLabel.CENTER);
		coverageRateUpdate = new JLabel();
		coverageRateUpdate.setHorizontalAlignment(JLabel.CENTER);
		statusConsole.add(status);
		statusConsole.add(coverageRateUpdate);
		statusConsole.add(timer);
		statusPane.add(statusConsole, BorderLayout.CENTER);
		settingPane.add(statusPane, BorderLayout.SOUTH);

		contentPane.add(settingPane, BorderLayout.EAST);

		/*
		 * Add left panel: the maze panel.
		 */
		exploredMapPane = new JPanel(new FlowLayout());
		exploredMapPane.setPreferredSize(new Dimension(500, 650));
		JPanel maze = new JPanel();
		maze.setLayout(new GridLayout(MapConstants.MAP_HEIGHT, MapConstants.MAP_WIDTH));
		maze.setPreferredSize(new Dimension(450, 600));
		mazeGrids = new JButton[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT];
		for (int x = 0; x < MapConstants.MAP_HEIGHT; x++) {
			for (int y = 0; y < MapConstants.MAP_WIDTH; y++) {
				int realX = y;
				int realY = 19-x;
				mazeGrids[realX][realY] = new JButton();
				mazeGrids[realX][realY].setEnabled(false);
				mazeGrids[realX][realY].setBorder(BorderFactory.createLineBorder(ROBOT_HEAD_COLOR));
				
				maze.add(mazeGrids[realX][realY]);
				if ((realX <= 2 & realY <= 2) || (realX >= 12 & realY >= 17)) {
					mazeGrids[realX][realY].setEnabled(false);
					mazeGrids[realX][realY].setBackground(Color.ORANGE);
					if (realX == 1 & realY == 1) {
						mazeGrids[realX][realY].setText("S");
					} else if (realX == 13 && realY == 18) {
						mazeGrids[realX][realY].setText("G");
					}
				} else {
					mazeGrids[realX][realY].setBackground(Color.BLACK);
				}
			}
		}
		exploredMapPane.add(maze);
		contentPane.add(exploredMapPane, BorderLayout.WEST);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.matches("ToggleObstacleAt [0-9]+,[0-9]+")) {
			int index = cmd.indexOf(",");
			int x = Integer.parseInt(cmd.substring(17, index));
			int y = Integer.parseInt(cmd.substring(index + 1));
			if (mapGrids[x][y].getBackground() == EMPTY_CELL_COLOR) {
				mapGrids[x][y].setBackground(OBSTACLE_CELL_COLOR);
			} else {
				mapGrids[x][y].setBackground(EMPTY_CELL_COLOR);
			}
		} else if (cmd.equals("SwitchCtrl")) {
			JComboBox cb = (JComboBox) e.getSource();
			JPanel cardPanel = (JPanel) settingPane.getComponent(1);
			CardLayout cardLayout = (CardLayout) (cardPanel.getLayout());
			cardLayout.show(cardPanel, (String) cb.getSelectedItem());
		} else if (cmd.equals("LoadMap")) {
			Map.getRealMapInstance().loadMap(mapGrids);
		} else if (cmd.equals("ClearMap")) {
			clearMapGrids();
		} else if (cmd.equals("ExploreMaze")) {
			clearMazeGrids();
			eraseWayPoint();
			refreshExploreInput();
			exploreButton.setEnabled(false);
        	RobotController.getInstance().exploreMaze();
		} else if (cmd.equals("FindFastestPath")) {
//			refreshFfpInput();
			ffpButton.setEnabled(false);
			eraseWayPoint();
			resetRobotLocation(1,1,Orientation.UP);
			RobotController.getInstance().fastestPath();
		}
	}
	public void eraseWayPoint() {
		if (mapGrids[prevwayPointX][prevwayPointY].getBackground() == WAYPOINT_COLOR) {
			if ((prevwayPointX <= 2 && prevwayPointY <= 2) || (prevwayPointX >= 12 && prevwayPointY >= 17))
				mapGrids[prevwayPointX][prevwayPointY].setBackground(GOAL_START_ZONE_COLOR);
			else
				mapGrids[prevwayPointX][prevwayPointY].setBackground(EMPTY_CELL_COLOR);
		}
	}
	
	public void resetRobotLocation(int x, int y, Orientation o) {
		if (x<=0 || y<=0 || x>=MapConstants.MAP_WIDTH-1 || y>=MapConstants.MAP_HEIGHT-1) {
			System.out.println("Invalid robot position");
			return;
		}
		Map map = Map.getExploredMapInstance();
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				if ((i<=2 && j<= 2) || (i>=12 && j>=17))
					mazeGrids[i][j].setBackground(GOAL_START_ZONE_COLOR);
				else if (!map.getCell(i, j).getSeen()) {
					mazeGrids[i][j].setBackground(UNEXPLORED_CELL_COLOR);
				} else if (map.getCell(i, j).isObstacle()) {
					mazeGrids[i][j].setBackground(OBSTACLE_CELL_COLOR);
				} else mazeGrids[i][j].setBackground(EMPTY_CELL_COLOR);
			}
		}
		for (int i=x-1; i<=x+1; i++) {
			for (int j=y-1; j<=y+1; j++) {
				mazeGrids[i][j].setBackground(ROBOT_COLOR);
			}
		}
		int robotHeadX = x;
		int robotHeadY = y;
		switch(o) {
			case UP:
				robotHeadY++;
				break;
			case RIGHT:
				robotHeadX++;
				break;
			case DOWN:
				robotHeadY--;
				break;
			case LEFT:
				robotHeadX--;
				break;
		}
		IRobot robot = null;
		if (!RobotController.REAL_RUN) {
			robot = VirtualRobot.getInstance();
		} else robot = RpiRobot.getInstance();
		robot.setPosition(x, y);
		robot.setOrientation(o);
		robotPosition = new int[] {x,y};
		currentOrientation = o;
		
	}
	
	public void clearMapGrids() {
		for (int x=0; x < MapConstants.MAP_WIDTH; x++) {
			for (int y=0; y < MapConstants.MAP_HEIGHT; y++) {
				if (! ((x <= 2 && y <= 2) || (x >= 13 && y >= 13)))
					mapGrids[x][y].setBackground(EMPTY_CELL_COLOR);
			}
		}
		Map.getRealMapInstance().loadMap(mapGrids);
	}
	
	public void clearMazeGrids() {
		for (int x=0; x < MapConstants.MAP_WIDTH; x++) {
			for (int y=0; y < MapConstants.MAP_HEIGHT; y++) {
				if (! ((x <= 2 && y <= 2) || (x >= 12 && y >= 17)))
					mazeGrids[x][y].setBackground(UNEXPLORED_CELL_COLOR);
				else if (x <= 2 && y <= 2) {
					mazeGrids[x][y].setBackground(ROBOT_COLOR);
				} else mazeGrids[x][y].setBackground(GOAL_START_ZONE_COLOR);
			}
		}
		mazeGrids[1][2].setBackground(ROBOT_HEAD_COLOR);
		Map.getExploredMapInstance().clearMap();
		robotPosition = new int[] {1,1};
		currentOrientation = Orientation.UP;
		IRobot robot;
		if (!RobotController.REAL_RUN) {
			robot = VirtualRobot.getInstance();
		} else robot = RpiRobot.getInstance();
		robot.setPosition(robotPosition[0], robotPosition[1]);
		robot.setOrientation(currentOrientation);
	}

	public void setStatus(String message) {
		status.setText(message);
	}

	public void setTimer (int timeLeft) {
		timer.setText("Time left (sec): " + timeLeft);
	}
	
	public String getTimerMessage () {
		return timer.getText();
	}
	
	public void setTimerMessage (String message) {
		timer.setText(message);
	}
	
	public void setCoverageUpdate (Float coverage) {
		coverageRateUpdate.setText("Coverage (%): " + 	String.format("%.1f", coverage));
	}
	
	public void setCoverageUpdate (String message) {
		coverageRateUpdate.setText(message);
	}
	
	/*
	 * Add a document listener class to dynamically show robot position.
	 */
	class InitialRobotAttibuteListener implements DocumentListener {
		public void insertUpdate(DocumentEvent e) {
			update(e);
		}

		public void removeUpdate(DocumentEvent e) {
			update(e);
		}

		public void changedUpdate(DocumentEvent e) {

		}

		private void update(DocumentEvent e) {
			Document doc = (Document) e.getDocument();
			String name = (String) doc.getProperty("name");
			String input = null;
			try {
				input = doc.getText(0, doc.getLength());
			} catch (BadLocationException ex) {}
			if (name.equals("Robot Initial Position")) {
				if (input.matches("[0-9]+,[0-9]+")) {
					int index = input.indexOf(",");
					int x = Integer.parseInt(input.substring(0, index));
					int y = Integer.parseInt(input.substring(index + 1));
					if (robotPosition == null) {
						robotPosition = new int[] {x,y};
						for (int i=x-1; i<=x+1; i++) {
							for (int j=y-1; j<=y+1; j++) {
								mazeGrids[i][j].setBackground(ROBOT_COLOR);
							}
						}
						currentOrientation = Orientation.UP;
						if (!RobotController.REAL_RUN) {
							VirtualRobot.getInstance().setPosition(x,y);
							VirtualRobot.getInstance().setOrientation(currentOrientation);
						} else {
							RpiRobot.getInstance().setPosition(x, y);
							RpiRobot.getInstance().setOrientation(currentOrientation);
						}
						mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(ROBOT_HEAD_COLOR);
					} else {
						for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
							for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
								if ((i<=2 && j<=2) || (i>=12 && j>=17))
									mazeGrids[i][j].setBackground(GOAL_START_ZONE_COLOR);
								else
									mazeGrids[i][j].setBackground(EMPTY_CELL_COLOR);
							}
						}
						for (int i=x-1; i<=x+1; i++) {
							for (int j=y-1; j<=y+1; j++) {
								mazeGrids[i][j].setBackground(ROBOT_COLOR);
							}
						}
						robotPosition[0] = x;
						robotPosition[1] = y;
					}
				}
				
			} else if (name.equals("Robot Explore Speed")) {
				if (input.matches("[0-9]+")) {
					int speed = Integer.parseInt(input);
					((VirtualRobot) VirtualRobot.getInstance()).setSpeed(speed);
					ffpTextFields[0].setText(input);
					MapProcessor.FORWARD_WEIGHT = 1f/speed;
					MapProcessor.TURNING_WEIGHT = 2f/speed;
				}
				
			} else if (name.equals("Target Coverage")) {
				if (input.matches("[0-9]+")) {
					coverageRateUpdate.setText("Coverage (%): 0");
					targetExplorePercentage = Integer.parseInt(input);
				}
				
			} else if (name.equals("Exploration time limit")) {
				if (input.matches("[0-9]+")) {
					GUI.exploreTimeLimit = Integer.parseInt(input);
					timer.setText("Time left (sec): " + input);
				}
			} else if (name.equals("Robot FFP Time Limit")) {
				if (input.matches("[0-9]+")) {
					coverageRateUpdate.setText("");
					timer.setText("Time left (sec): " + input);
					fastestPathTimeLimit = Integer.parseInt(input);
				} 
			} else if (name.equals("WaypointX")) {
				if (input.matches("[0-9]+")) {
					prevwayPointX = wayPointX;
					wayPointX = Integer.parseInt(input);
				}
			} else if (name.equals("WaypointY")) {				
				if (input.matches("[0-9]+")) {
					prevwayPointY = wayPointY;
					wayPointY = Integer.parseInt(input);
				}
			}
		}

	}
	
	private void loadMapGrids() {
		Map map = Map.getRealMapInstance();
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				if ((i<=2 && j<=2) || (i>=12 && j>=17))
					mapGrids[i][j].setBackground(GOAL_START_ZONE_COLOR);
				else if (map.getCell(i, j).getSeen()) {
					if (map.getCell(i, j).isObstacle()) 
						mapGrids[i][j].setBackground(OBSTACLE_CELL_COLOR);
					else mapGrids[i][j].setBackground(EMPTY_CELL_COLOR);
				} else mapGrids[i][j].setBackground(UNEXPLORED_CELL_COLOR);
			}
		}
	}
	
	public void updateRobotUI(RobotCommand robotAction) {
		switch(robotAction) {
			case TURN_LEFT:
				turnRobotLeft(currentOrientation);
				break;
			case TURN_RIGHT:
				turnRobotRight(currentOrientation);
				break;
			case MOVE_FORWARD:
				moveRobotForward(currentOrientation);
				break;
		}
	}
	
	private void turnRobotLeft(Orientation orientation) {
		currentOrientation = Orientation.getCounterClockwise(orientation);
		switch(orientation) {
			case UP:
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(ROBOT_COLOR);
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(ROBOT_HEAD_COLOR);
				break;
			case LEFT:
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(ROBOT_COLOR);
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(ROBOT_HEAD_COLOR);
				break;
			case DOWN:
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(ROBOT_COLOR);
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(ROBOT_HEAD_COLOR);
				break;
			case RIGHT:
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(ROBOT_COLOR);
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(ROBOT_HEAD_COLOR);
				break;
		}
	}
	
	private void turnRobotRight(Orientation orientation) {
		currentOrientation = Orientation.getClockwise(orientation);
		switch(orientation) {
			case UP:
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(ROBOT_COLOR);
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(ROBOT_HEAD_COLOR);
				break;
			case LEFT:
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(ROBOT_COLOR);
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(ROBOT_HEAD_COLOR);
				break;
			case DOWN:
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(ROBOT_COLOR);
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(ROBOT_HEAD_COLOR);
				break;
			case RIGHT:
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(ROBOT_COLOR);
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(ROBOT_HEAD_COLOR);
				break;
		}
	}
	
	private void moveRobotForward(Orientation orientation) {
		switch(orientation) {
			case UP:
				if (robotPosition[1] + 2 >= MapConstants.MAP_HEIGHT) {
					System.out.println("Invalid move");
					return;
				}
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						if (!((i <= 2 && j <= 2) || (i >= 12 && j >= 17)))
							mazeGrids[i][j].setBackground(EMPTY_CELL_COLOR);
						else mazeGrids[i][j].setBackground(GOAL_START_ZONE_COLOR);
					}
				}
				robotPosition[1] += 1;
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(ROBOT_COLOR);
					}
				}
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(ROBOT_HEAD_COLOR);
				break;
			case LEFT:
				if (robotPosition[0] - 1 < 1) {
					System.out.println("Invalid move");
					return;
				}
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						if (!((i <= 2 && j <= 2) || (i >= 12 && j >= 17)))
							mazeGrids[i][j].setBackground(EMPTY_CELL_COLOR);
						else mazeGrids[i][j].setBackground(GOAL_START_ZONE_COLOR);
					}
				}
				robotPosition[0] -= 1;
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(ROBOT_COLOR);
					}
				}
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(ROBOT_HEAD_COLOR);
				break;
			case DOWN:
				if (robotPosition[1] - 1 < 1) {
					System.out.println("Invalid move");
					return;
				}
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						if (!((i <= 2 && j <= 2) || (i >= 12 && j >= 17)))
							mazeGrids[i][j].setBackground(EMPTY_CELL_COLOR);
						else mazeGrids[i][j].setBackground(GOAL_START_ZONE_COLOR);
					}
				}
				robotPosition[1] -= 1;
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(ROBOT_COLOR);
					}
				}
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(ROBOT_HEAD_COLOR);
				break;
			case RIGHT:
				if (robotPosition[0] + 2 >= MapConstants.MAP_WIDTH) {
					System.out.println("Invalid move");
					return;
				}
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						if (!((i <= 2 && j <= 2) || (i >= 12 && j >= 17)))
							mazeGrids[i][j].setBackground(EMPTY_CELL_COLOR);
						else mazeGrids[i][j].setBackground(GOAL_START_ZONE_COLOR);
					}
				}
				robotPosition[0] += 1;
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(ROBOT_COLOR);
					}
				}
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(ROBOT_HEAD_COLOR);
				break;
		}
	}
	

	public void refreshExploreInput() {
		for (int i = 0; i < exploreTextFields.length; i++) {
			exploreTextFields[i].setText(exploreTextFields[i].getText());
		}
	}
	
	public void refreshFfpInput() {
		for (int i = 0; i < 4; i++) {
			ffpTextFields[i].setText(ffpTextFields[i].getText());
			
		}
	}

	public boolean isIntExploreInput() {

		String[] exploreInput = new String[4];
		for (int i = 0; i < 4; i++) {
			exploreInput[i] = exploreTextFields[i].getText();
		}
		
		if (! exploreInput[0].matches("[0-9]+,[0-9]+")) {
			return false;
		} else {
			int posX, posY, index;
			index = exploreInput[0].indexOf(",");
			posX = Integer.parseInt(exploreInput[0].substring(0, index));
			posY = Integer.parseInt(exploreInput[0].substring(index+1));
			if (posX > MapConstants.MAP_HEIGHT || posY > MapConstants.MAP_HEIGHT) {
				return false;
			}
		}
		
		if (!exploreInput[1].matches("[0-9]+")) {
			return false;
		}
		
		if (!exploreInput[2].matches("[0-9]+")) {
			return false;
		} else {
			int coverage;
			coverage = Integer.parseInt(exploreInput[2]);
			if (coverage > 100) {
				return false;
			}
		}
		
		if (!exploreInput[3].matches("[0-9]+")) {
			return false;
		}
		
		
		return true;
	}

	public boolean isIntFFPInput() {
		String[] ffpInput = new String[4];
		for (int i = 0; i < 2; i++) {
			ffpInput[i] = ffpTextFields[i].getText();
			if (!ffpInput[i].matches("[0-9]+"))
				return false;
		}
		
		
		return true;
	}
	
	public static void main(String[] args) {
		GUI myGUI = GUI.getInstance();
		myGUI.setVisible(true);
		myGUI.refreshExploreInput();
	}
}
