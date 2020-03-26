package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import constants.MapConstants;
import constants.RobotConstants;
import connection.SyncObject;
import map.Map;
import utils.*;


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
	public static final Color FASTEST_PATH_COLOR = Color.YELLOW;
	public static final Color WAYPOINT_COLOR = Color.BLUE;
	public static final Color CONFLICT_COLOR = Color.MAGENTA;
	
	private final JPanel displayedPane;
	private JPanel settingPane;
	private JLabel status, timer, coverageRateUpdate;
	private JButton[][] mapGrids, mazeGrids;
	private JTextField[] exploreTextFields, ffpTextFields;
	private JButton exploreButton, ffpButton;
	private GUIUpdate update;
	private SwingWorker<Void, Void> pollingWorker;
	private Timer timerWorker;
	private int wayPointX = -1;
	private int wayPointY = -1;
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
	
	public void setMazeGridColor(int x, int y, Color color) {
		if(x < 0 || x >= MapConstants.MAP_WIDTH-1||y < 0 || y >= MapConstants.MAP_HEIGHT -1) return;
		mazeGrids[x][y].setBackground(color);
	}
	
	public void setMapGridColor(int x, int y, Color color) {
		if(x < 0 || x >= MapConstants.MAP_WIDTH-1||y < 0 || y >= MapConstants.MAP_HEIGHT -1) return;
		mapGrids[x][y].setBackground(color);
	}

	private void initializeDisplayedPane(JPanel contentPane) {

		/*
		 * Add right panel: the reference map and two control buttons
		 * (load/clear).
		 */
		JPanel mapPane = new JPanel(new FlowLayout());
		mapPane.setPreferredSize(new Dimension(450, 650));
		JPanel map = new JPanel();
		map.setLayout(new GridLayout(MapConstants.MAP_HEIGHT, MapConstants.MAP_WIDTH));
		map.setPreferredSize(new Dimension(450, 600));
		mapGrids = new JButton[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT];
		for (int y = MapConstants.MAP_HEIGHT - 1; y >= 0; y--) {
			for (int x = 0; x < MapConstants.MAP_WIDTH; x++) {
				mapGrids[x][y] = new JButton();
				
				if (RobotConstants.REAL_RUN) {
					mapGrids[x][y].setEnabled(false);
					mapGrids[x][y].setBackground(ROBOT_HEAD_COLOR);
				} else {
					mapGrids[x][y].setActionCommand("ToggleObstacleAt " + x + "," + y);
					mapGrids[x][y].addActionListener(this);
					mapGrids[x][y].setBorder(BorderFactory.createLineBorder(ROBOT_HEAD_COLOR));
					mapGrids[x][y].setBackground(EMPTY_CELL_COLOR);
					if ((x <= 2 & y <= 2) || (x >= 12 & y >= 17)) {
						mapGrids[x][y].setEnabled(false);
						mapGrids[x][y].setBackground(Color.ORANGE);
						if (x == 1 && y == 1) {
							mapGrids[x][y].setText("S");
						} else if (x == 13 && y == 18) {
							mapGrids[x][y].setText("G");
						}
					}
				}
				
				map.add(mapGrids[x][y]);
			}
		}
		
		if (!RobotConstants.REAL_RUN) {
			loadMapGrids();
		}
		
		mapPane.add(map);
		JButton loadMap = new JButton("Load");
		
		if (RobotConstants.REAL_RUN) {
			loadMap.setEnabled(false);
		} else {
			loadMap.setActionCommand("LoadMap");
			loadMap.addActionListener(this);
		}
		
		JButton clearMap = new JButton("Clear");
		
		if (RobotConstants.REAL_RUN) {
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
		String[] comboBoxItems = { EXPLORATION, FASTEST_PATH };
		JComboBox<String> cbCtrlSwitch = new JComboBox<>(comboBoxItems);
		cbCtrlSwitch.setFont(new Font("Tahoma", Font.BOLD, 16));
		cbCtrlSwitch.setEditable(false);
		cbCtrlSwitch.addActionListener(this);
		cbCtrlSwitch.setActionCommand("SwitchCtrl");
		settingPane.add(cbCtrlSwitch, BorderLayout.NORTH);

		// Add control panel for exploring.
		JLabel[] exploreCtrlLabels = new JLabel[4];
		exploreTextFields = new JTextField[4];
		exploreButton = new JButton("Explore");
		
		if (RobotConstants.REAL_RUN) {
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
			if (RobotConstants.REAL_RUN) {
				exploreTextFields[i].setEditable(false);
			}
		}

		JPanel exploreInputPane = InitInputPanel(exploreCtrlLabels, exploreTextFields);

		if (!RobotConstants.REAL_RUN) {
			exploreTextFields[0].setEditable(false);
			exploreCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[0].setText("1,1");
			exploreTextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[0].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			exploreTextFields[0].getDocument().putProperty("name", "Robot Initial Position");
			
			exploreCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[1].setText(Float.toString(SyncObject.getSyncObject().settings.getRobotSpeed()));
			exploreTextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[1].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			exploreTextFields[1].getDocument().putProperty("name", "Robot Explore Speed");
			
			exploreCtrlLabels[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[2].setText(Integer.toString(RobotConstants.REAL_EXPLORE_COVERAGE));
			exploreTextFields[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[2].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			exploreTextFields[2].getDocument().putProperty("name", "Target Coverage");
			
			exploreCtrlLabels[3].setFont(new Font("Tahoma", Font.PLAIN, 14));
			exploreTextFields[3].setText(Integer.toString(RobotConstants.REAL_EXPLORE_TIME_LIMIT));
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
		ffpButton = new JButton("Find fastest path");
		
		if (RobotConstants.REAL_RUN) {
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
			if (RobotConstants.REAL_RUN) {
				ffpTextFields[i].setEditable(false);
			}
		}

		JPanel ffpInputPane = InitInputPanel(ffpCtrlLabels, ffpTextFields);

		if (!RobotConstants.REAL_RUN) {
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
		JPanel exploredMapPane = new JPanel(new FlowLayout());
		exploredMapPane.setPreferredSize(new Dimension(500, 650));
		JPanel maze = new JPanel();
		maze.setLayout(new GridLayout(MapConstants.MAP_HEIGHT, MapConstants.MAP_WIDTH));
		maze.setPreferredSize(new Dimension(450, 600));
		mazeGrids = new JButton[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT];
		for (int y = MapConstants.MAP_HEIGHT - 1; y >= 0; y--) {
			for (int x = 0; x < MapConstants.MAP_WIDTH; x++) {
				mazeGrids[x][y] = new JButton();
				mazeGrids[x][y].setEnabled(false);
				mazeGrids[x][y].setBorder(BorderFactory.createLineBorder(ROBOT_HEAD_COLOR));
				
				maze.add(mazeGrids[x][y]);
				// Indicate Start and Goal
				if (x == 1 & y == 1) {
					mazeGrids[x][y].setText("S");
				}
				if (x == 13 && y == 18) {
					mazeGrids[x][y].setText("G");
				}
				//Draw start zone
				if ((x <= 2 & y <= 2)) {
					mazeGrids[x][y].setEnabled(false);
					mazeGrids[x][y].setBackground(GOAL_START_ZONE_COLOR);
				} else {
					// Set unexplored colour
					mazeGrids[x][y].setBackground(UNEXPLORED_CELL_COLOR);
				}
			}
		}
		DrawRobotOnUI(new Coordinate(1,1), Orientation.UP);
		exploredMapPane.add(maze);
		contentPane.add(exploredMapPane, BorderLayout.WEST);
	}

	private JPanel InitInputPanel(JLabel[] ffpCtrlLabels, JTextField[] ffpTextFields) {
		JPanel ffpInputPane = new JPanel(new GridLayout(4, 2));
		ffpInputPane.add(ffpCtrlLabels[0]);
		ffpInputPane.add(ffpTextFields[0]);
		ffpInputPane.add(ffpCtrlLabels[1]);
		ffpInputPane.add(ffpTextFields[1]);
		ffpInputPane.add(ffpCtrlLabels[2]);
		ffpInputPane.add(ffpTextFields[2]);
		ffpInputPane.add(ffpCtrlLabels[3]);
		ffpInputPane.add(ffpTextFields[3]);
		return ffpInputPane;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		try {
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
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				JPanel cardPanel = (JPanel) settingPane.getComponent(1);
				CardLayout cardLayout = (CardLayout) (cardPanel.getLayout());
				cardLayout.show(cardPanel, (String) cb.getSelectedItem());
			} else if (cmd.equals("LoadMap")) {
				MapLoader.loadRealMapFromGUI(mapGrids);
			} else if (cmd.equals("ClearMap")) {
				clearMapGrids();
			} else if (cmd.equals("ExploreMaze")) {
				if(!RobotConstants.REAL_RUN) {
					if (!this.isIntExploreInput()) {
						this.setStatus("invalid input for exploration");
						this.setExploreBtnEnabled(true);
						return;
					}
					gui.refreshExploreInput();
					if(update != null){
						SyncObject.getSyncObject().SignalResetRobot();
						update = null;
					}
					if(timerWorker != null){
						timerWorker.stop();
						timerWorker = null;
					}

					if (!RobotConstants.REAL_RUN) {
						DrawMap(Map.getRealMapInstance(), mapGrids);
						if(wayPointX < 0 || wayPointX > MapConstants.MAP_WIDTH-1 || wayPointY < 0 || wayPointY > MapConstants.MAP_HEIGHT - 1){
							colourGridCell(Map.getRealMapInstance(), mapGrids, 1, 1);
						}else {
							colourGridCell(Map.getRealMapInstance(), mapGrids, wayPointX, wayPointY);
						}
					}
					wayPointX = -1;
					wayPointY = -1;
					SyncObject.getSyncObject().SignalExplorationStart();
				}
			} else if (cmd.equals("FindFastestPath")) {
				if(!RobotConstants.REAL_RUN) {
					if (!gui.isIntFFPInput()) {
						gui.setStatus("invalid input for finding fastest path");
						gui.setFfpBtnEnabled(true);
						return;
					}
					gui.refreshFfpInput();
					ffpButton.setEnabled(false);
					SyncObject.getSyncObject().SignalFastestPathStart();
					ffpButton.setEnabled(true);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void pollInBackground() {
		if(pollingWorker == null) {
			pollingWorker = new SwingWorker<>() {
				@Override
				protected Void doInBackground() {
					while (true) {
						try {
							if (update != null && timerWorker == null) {
								startTimer();
							}
							update = SyncObject.getSyncObject().GetGUIUpdate();
							//Draw Map
							DrawMap(update.getMap(), mazeGrids);
							List<Coordinate> path = SyncObject.getSyncObject().GetFastestPathSquares();
							//Draw Path
							if (path != null) {
								DrawMap(Map.getRealMapInstance(), mapGrids);
								for (Coordinate c : path) {
									setMapGridColor(c.getX(), c.getY(), FASTEST_PATH_COLOR);
									setMazeGridColor(c.getX(), c.getY(), FASTEST_PATH_COLOR);
								}
							}
							setMapGridColor(wayPointX, wayPointY, WAYPOINT_COLOR);
							setMazeGridColor(wayPointX, wayPointY, WAYPOINT_COLOR);
							//Draw Robot
							DrawRobotOnUI(update.getRobotPos(), update.getOrientation());
						} catch (Exception e) {
							System.out.println("Polling Worker: " + e.toString());
						}
					}
				}
			};
			pollingWorker.execute();
		}
	}

	private void startTimer(){
		TimerEvent timeActionListener;
		if (RobotConstants.REAL_RUN) {
			timeActionListener = new TimerEvent(RobotConstants.REAL_EXPLORE_TIME_LIMIT, () -> SyncObject.getSyncObject().IsExplorationFinished());
		} else timeActionListener = new TimerEvent(SyncObject.getSyncObject().settings.getTimeLimit(), () -> SyncObject.getSyncObject().IsExplorationFinished());
		timerWorker = new Timer(1000, timeActionListener);
		timeActionListener.setTimer(timerWorker);
		timerWorker.start();
	}

	private void colourGridCell(Map map, JButton[][] grid, int i, int j){
		if(i < 0 || j < 0 || i >= MapConstants.MAP_WIDTH || j >= MapConstants.MAP_HEIGHT) return;
		if (i<=2 && j<= 2) {
			grid[i][j].setBackground(GOAL_START_ZONE_COLOR);
		}else if (map == null || !map.getCell(i, j).isSeen()) {
			grid[i][j].setBackground(UNEXPLORED_CELL_COLOR);
		} else if(i >= 12 && j >=17){
			grid[i][j].setBackground(GOAL_START_ZONE_COLOR);
		} else if (map.getCell(i, j).isHasConflict()){
			grid[i][j].setBackground(CONFLICT_COLOR);
		}else if (map.getCell(i, j).isObstacle()) {
			grid[i][j].setBackground(OBSTACLE_CELL_COLOR);
		} else {
			grid[i][j].setBackground(EMPTY_CELL_COLOR);
		}
	}

	@Override
	public void setVisible(boolean visible){
		if(visible) this.pollInBackground();
		super.setVisible(visible);
	}

	/**
	 * Erases simulation map and saves it via loadRealMapFromGUI
	 */
	public void clearMapGrids() {
		for (int x=0; x < MapConstants.MAP_WIDTH; x++) {
			for (int y=0; y < MapConstants.MAP_HEIGHT; y++) {
				if (! ((x <= 2 && y <= 2)))
					mapGrids[x][y].setBackground(EMPTY_CELL_COLOR);
			}
		}
		MapLoader.loadRealMapFromGUI(mapGrids);
	}

	public void setStatus(String message) {
		status.setText(message);
	}

	public void setTimer (int timeLeft) {
		timer.setText("Time left (sec): " + timeLeft);
	}
	
	public void setTimerMessage (String message) {
		timer.setText(message);
	}
	
	public void setCoverageUpdate (Float coverage) {
		coverageRateUpdate.setText("Coverage (%): " + 	String.format("%.1f", coverage));
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
			Document doc = e.getDocument();
			String name = (String) doc.getProperty("name");
			String input = null;
			try {
				input = doc.getText(0, doc.getLength());
			} catch (BadLocationException ex) {
				System.out.println(ex.toString());
			}
			assert input != null;
			switch (name) {
				case "Robot Explore Speed":
					if (input.matches("[0-9.]+")) {
						float speed = Float.parseFloat(input);
						SyncObject.getSyncObject().settings.setRobotSpeed(speed);
						ffpTextFields[0].setText(input);
					}

					break;
				case "Target Coverage":
					if (input.matches("[0-9]+")) {
						coverageRateUpdate.setText("Coverage (%): 0");
						int targetExplorePercentage = Integer.parseInt(input);
						SyncObject.getSyncObject().settings.setCoveragePercent(targetExplorePercentage);
					}

					break;
				case "Exploration time limit":
					if (input.matches("[0-9]+")) {
						int exploreTimeLimit = Integer.parseInt(input);
						SyncObject.getSyncObject().settings.setTimeLimit(exploreTimeLimit);
						timer.setText("Time left (sec): " + input);
					}
					break;
				case "Robot FFP Time Limit":
					if (input.matches("[0-9]+")) {
						coverageRateUpdate.setText("");
						timer.setText("Time left (sec): " + input);
						fastestPathTimeLimit = Integer.parseInt(input);
					}
					break;
				case "WaypointX":
					if (input.matches("[0-9]+")) {
						int value = Integer.parseInt(input);
						if (value > MapConstants.MAP_WIDTH-1)
							return;
						setWaypoint(value, wayPointY);
					}
					break;
				case "WaypointY":
					if (input.matches("[0-9]+")) {
						int value = Integer.parseInt(input);
						if (value > MapConstants.MAP_HEIGHT - 1)
							return;
						setWaypoint(wayPointX, value);
					}
					break;
			}
		}
	}

	private void setWaypoint(int x, int y){
		Map map;
		Coordinate pos;
		Orientation o;
		if(update == null){
			map = null;
			pos = new Coordinate(1,1);
			o = Orientation.UP;
		}else{
			map = update.getMap();
			pos = update.getRobotPos();
			o = update.getOrientation();
		}
		GUI.getInstance().colourGridCell(map, mazeGrids, wayPointX, wayPointY);
		if(!RobotConstants.REAL_RUN) GUI.getInstance().colourGridCell(map, mapGrids, wayPointX, wayPointY);
		wayPointX = x;
		wayPointY = y;
		GUI.getInstance().setMazeGridColor(wayPointX, wayPointY, WAYPOINT_COLOR);
		if(!RobotConstants.REAL_RUN) GUI.getInstance().setMapGridColor(wayPointX, wayPointY, WAYPOINT_COLOR);
		if(wayPointX > 0 && wayPointX < MapConstants.MAP_WIDTH-1 && wayPointY > 0 && wayPointY < MapConstants.MAP_HEIGHT -1){
			SyncObject.getSyncObject().SetWaypoint(new Coordinate(wayPointX, wayPointY));
		}
		DrawRobotOnUI(pos, o);
	}
	
	private void loadMapGrids() {
		Map map = Map.getRealMapInstance();
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				if ((i<=2 && j<=2) || (i>=12 && j>=17))
					mapGrids[i][j].setBackground(GOAL_START_ZONE_COLOR);
				else if (map.getCell(i, j).isSeen()) {
					if (map.getCell(i, j).isObstacle()) 
						mapGrids[i][j].setBackground(OBSTACLE_CELL_COLOR);
					else mapGrids[i][j].setBackground(EMPTY_CELL_COLOR);
				} else mapGrids[i][j].setBackground(UNEXPLORED_CELL_COLOR);
			}
		}
	}

	/**
	 * Draws the full grid using the map
	 * @param map - map to draw (This map is immutable, should be gotten from Sync Object)
	 * @param grids - grid to draw on
	 */
	public void DrawMap(Map map, JButton[][] grids){
		for(int i = 0; i < MapConstants.MAP_WIDTH; i++){
			for(int j = 0; j < MapConstants.MAP_HEIGHT; j++){
				if(i != wayPointX || j != wayPointY) colourGridCell(map, grids, i, j);
			}
		}
		UpdateCoverage(map);
	}

	public void UpdateCoverage(Map map){
		Float coverageRate = map.getNumSeen() *100f / 300f ;
		gui.setCoverageUpdate(coverageRate);
	}

	/**
	 * Draws robot on the UI based on its position and orientation
	 * @param botPosition - Coordinate of robot
	 * @param botOrientation - Orientation of robot
	 */
	public void DrawRobotOnUI(Coordinate botPosition, Orientation botOrientation){
		if(botPosition.getX() <= 0 || botPosition.getX() >= MapConstants.MAP_WIDTH-1 || botPosition.getY() <= 0 || botPosition.getY() >= MapConstants.MAP_HEIGHT-1){
			System.out.println("Invalid Position to draw!");
			return;
		}
		for (int i=botPosition.getX()-1; i<=botPosition.getX()+1; i++) {
			for (int j=botPosition.getY()-1; j<=botPosition.getY()+1; j++) {
				mazeGrids[i][j].setBackground(ROBOT_COLOR);
			}
		}
		switch(botOrientation){
			case UP:
				mazeGrids[botPosition.getX()][botPosition.getY()+1].setBackground(ROBOT_HEAD_COLOR);
				break;
			case DOWN:
				mazeGrids[botPosition.getX()][botPosition.getY()-1].setBackground(ROBOT_HEAD_COLOR);
				break;
			case RIGHT:
				mazeGrids[botPosition.getX()+1][botPosition.getY()].setBackground(ROBOT_HEAD_COLOR);
				break;
			case LEFT:
				mazeGrids[botPosition.getX()-1][botPosition.getY()].setBackground(ROBOT_HEAD_COLOR);
				break;
		}
	}

	public void refreshExploreInput() {
		for (JTextField exploreTextField : exploreTextFields) {
			exploreTextField.setText(exploreTextField.getText());
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
		
		if (!exploreInput[1].matches("[0-9.]+")) {
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

		return exploreInput[3].matches("[0-9]+");
	}

	public boolean isIntFFPInput() {
		String[] ffpInput = new String[4];
		for (int i = 0; i < 2; i++) {
			ffpInput[i] = ffpTextFields[i].getText();
			if (!ffpInput[i].matches("[0-9.]+"))
				return false;
		}
		
		
		return true;
	}

	static class TimerEvent implements ActionListener {
		final int timeLimit;
		int timeLeft;
		Timer timer;
		final IExplorationStatus explorationStatus;

		public TimerEvent(int timeLimit, IExplorationStatus explorationStatus) {
			this.timeLimit = timeLimit;
			timeLeft = timeLimit;
			this.explorationStatus = explorationStatus;
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
				} else if (explorationStatus.HasExplorationEnded()) {
					gui.setTimerMessage(String.format("finish within time limit (%ds)", timeLimit - timeLeft));
					gui.setExploreBtnEnabled(true);
					gui.setFfpBtnEnabled(true);
					if (timer != null)
						timer.stop();
				}

			}

		}
	}
}
