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
import java.io.IOException;
import java.nio.file.FileSystems;

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
import Simulator.Robot;
import utils.Map;
import utils.Orientation;
import utils.RobotCommand;


public class GUI extends JFrame implements ActionListener{
	private static GUI gui;

	private static final String EXPLORATION = "Explore maze";
	private static final String FASTEST_PATH = "Find fastest path";
	
	private JPanel displayedPane, mapPane, settingPane, exploredMapPane;
	private JLabel status, timer, coverageRateUpdate;
	private JButton[][] mapGrids, mazeGrids;
	private JTextField[] exploreTextFields, ffpTextFields;
	private JButton exploreButton, ffpButton;
	private int[] robotPosition;
	private Orientation currentOrientation;
	private int targetExplorePercentage;
	public static int exploreTimeLimit;
	
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
					mapGrids[realX][realY].setBackground(Color.GRAY);
				} else {
					mapGrids[realX][realY].setActionCommand("ToggleObstacleAt " + realX + "," + realY);
					mapGrids[realX][realY].addActionListener(this);
					mapGrids[realX][realY].setBorder(BorderFactory.createLineBorder(Color.GRAY));
					mapGrids[realX][realY].setBackground(Color.GREEN);
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
			loadMap();
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
		JLabel[] ffpCtrlLabels = new JLabel[2];
		ffpTextFields = new JTextField[2];
		ffpButton = new JButton("Find fatest path");
		
		if (RobotController.REAL_RUN) {
			ffpButton.setEnabled(false);
		} else {
			ffpButton.setActionCommand("FindFastestPath");
			ffpButton.addActionListener(this);
			ffpButton.setEnabled(false);
		}
		
		ffpCtrlLabels[0] = new JLabel("Speed (steps/sec): ");
		ffpCtrlLabels[1] = new JLabel("Time limit (sec): ");
		for (int i = 0; i < 2; i++) {
			ffpTextFields[i] = new JTextField(10);
			if (RobotController.REAL_RUN) {
				ffpTextFields[i].setEditable(false);
			}
		}

		JPanel ffpInputPane = new JPanel(new GridLayout(2, 2));
		ffpInputPane.add(ffpCtrlLabels[0]);
		ffpInputPane.add(ffpTextFields[0]);
		ffpInputPane.add(ffpCtrlLabels[1]);
		ffpInputPane.add(ffpTextFields[1]);
		
		if (!RobotController.REAL_RUN) {
			ffpCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[0].setEditable(false);
	
			ffpTextFields[1].setText("120");
			ffpCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
			ffpTextFields[1].getDocument().addDocumentListener(new InitialRobotAttibuteListener());
			ffpTextFields[1].getDocument().putProperty("name", "Robot FFP Time Limit");
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
				mazeGrids[realX][realY].setBorder(BorderFactory.createLineBorder(Color.GRAY));
				if (realY == 9) {
					mazeGrids[realX][realY]
							.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, Color.BLUE),
									BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY)));
				}
				
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
			if (mapGrids[x][y].getBackground() == Color.GREEN) {
				mapGrids[x][y].setBackground(Color.RED);
			} else {
				mapGrids[x][y].setBackground(Color.GREEN);
			}
		} else if (cmd.equals("SwitchCtrl")) {
			JComboBox cb = (JComboBox) e.getSource();
			JPanel cardPanel = (JPanel) settingPane.getComponent(1);
			CardLayout cardLayout = (CardLayout) (cardPanel.getLayout());
			cardLayout.show(cardPanel, (String) cb.getSelectedItem());
		} else if (cmd.equals("LoadMap")) {
			Map.getRealMapInstance().loadMap(mapGrids);
		} else if (cmd.equals("ClearMap")) {
			for (int x=0; x < MapConstants.MAP_WIDTH; x++) {
				for (int y=0; y < MapConstants.MAP_HEIGHT; y++) {
					if (! (x <= 2 && y <= 2) || (x >= 13 && y >= 13))
						mapGrids[x][y].setBackground(Color.GREEN);
				}
			}
			Map.getRealMapInstance().loadMap(mapGrids);
		} else if (cmd.equals("ExploreMaze")) {
			exploreButton.setEnabled(false);
//        	_controller.exploreMaze();
		} else if (cmd.equals("FindFastestPath")) {
			ffpButton.setEnabled(false);
//			_controller.findFastestPath();
		}
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
			if (name.equals("Robot Initial Position")) {
				try {
					String position = doc.getText(0, doc.getLength());
					if (position.matches("[0-9]+,[0-9]+")) {
						int index = position.indexOf(",");
						int x = Integer.parseInt(position.substring(0, index));
						int y = Integer.parseInt(position.substring(index + 1));
						if (robotPosition == null) {
							robotPosition = new int[] {x,y};
							for (int i=x-1; i<=x+1; i++) {
								for (int j=y-1; j<=y+1; j++) {
									mazeGrids[i][j].setBackground(Color.CYAN);
								}
							}
							currentOrientation = Orientation.UP;
							mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(Color.GRAY);
						} else {
							for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
								for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
									mazeGrids[i][j].setBackground(mapGrids[i][j].getBackground());
								}
							}
							for (int i=x-1; i<=x+1; i++) {
								for (int j=y-1; j<=y+1; j++) {
									mazeGrids[i][j].setBackground(Color.CYAN);
								}
							}
							robotPosition[0] = x;
							robotPosition[1] = y;
						}
					} else {
//						_controller.resetMaze(_mazeGrids);
						status.setText("robot initial position not set");
					}
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			} else if (name.equals("Robot Explore Speed")) {
				try {
					String speed = doc.getText(0, doc.getLength());
					if (speed.matches("[0-9]+")) {
//						_controller.setRobotSpeed(Integer.parseInt(speed));
						Robot.getInstance().setSpeed(Integer.parseInt(speed));
						ffpTextFields[0].setText(speed);
					} else {
						status.setText("robot speed not set");
					}
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			} else if (name.equals("Target Coverage")) {
				try {
					String coverage = doc.getText(0, doc.getLength());
					if (coverage.matches("[0-9]+")) {
//						_controller.setCoverage(Integer.parseInt(coverage));
						coverageRateUpdate.setText("Coverage (%): 0");
					} else {
						status.setText("target coverage not set");
					}
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			} else if (name.equals("Exploration time limit")) {
				try {
					String timeLimit = doc.getText(0, doc.getLength());
					if (timeLimit.matches("[0-9]+")) {
						GUI.exploreTimeLimit = Integer.parseInt(timeLimit);
						timer.setText("Time left (sec): " + timeLimit);
					} else {
						status.setText("time limit for exploring not set");
					}
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			} else if (name.equals("Robot FFP Time Limit")) {
				try {
					String timeLimit = doc.getText(0, doc.getLength());
					if (timeLimit.matches("[0-9]+")) {
//						_controller.setFFPTimeLimit(Integer.parseInt(timeLimit));
						coverageRateUpdate.setText("");
						timer.setText("Time left (sec): " + timeLimit);
					} else {
						status.setText("time limit for fastest path not set");
					}
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			}
		}

	}
	
	private void loadMap() {
		Map map = Map.getRealMapInstance();
		for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
			for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
				if (map.getCell(i, j).isObstacle()) {
					mapGrids[i][j].setBackground(Color.RED);
				}
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
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(Color.CYAN);
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(Color.GRAY);
				break;
			case LEFT:
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(Color.CYAN);
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(Color.GRAY);
				break;
			case DOWN:
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(Color.CYAN);
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(Color.GRAY);
				break;
			case RIGHT:
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(Color.CYAN);
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(Color.GRAY);
				break;
		}
	}
	
	private void turnRobotRight(Orientation orientation) {
		currentOrientation = Orientation.getClockwise(orientation);
		switch(orientation) {
			case UP:
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(Color.CYAN);
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(Color.GRAY);
				break;
			case LEFT:
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(Color.CYAN);
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(Color.GRAY);
				break;
			case DOWN:
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(Color.CYAN);
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(Color.GRAY);
				break;
			case RIGHT:
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(Color.CYAN);
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(Color.GRAY);
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
						mazeGrids[i][j].setBackground(mapGrids[i][j].getBackground());
					}
				}
				robotPosition[1] += 1;
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(Color.CYAN);
					}
				}
				mazeGrids[robotPosition[0]][robotPosition[1]+1].setBackground(Color.GRAY);
				break;
			case LEFT:
				if (robotPosition[0] - 1 < 1) {
					System.out.println("Invalid move");
					return;
				}
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(mapGrids[i][j].getBackground());
					}
				}
				robotPosition[0] -= 1;
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(Color.CYAN);
					}
				}
				mazeGrids[robotPosition[0]-1][robotPosition[1]].setBackground(Color.GRAY);
				break;
			case DOWN:
				if (robotPosition[1] - 1 < 1) {
					System.out.println("Invalid move");
					return;
				}
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(mapGrids[i][j].getBackground());
					}
				}
				robotPosition[1] -= 1;
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(Color.CYAN);
					}
				}
				mazeGrids[robotPosition[0]][robotPosition[1]-1].setBackground(Color.GRAY);
				break;
			case RIGHT:
				if (robotPosition[0] + 2 >= MapConstants.MAP_WIDTH) {
					System.out.println("Invalid move");
					return;
				}
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(mapGrids[i][j].getBackground());
					}
				}
				robotPosition[0] += 1;
				for (int i=robotPosition[0]-1; i<=robotPosition[0]+1; i++) {
					for (int j=robotPosition[1]-1; j<=robotPosition[1]+1; j++) {
						mazeGrids[i][j].setBackground(Color.CYAN);
					}
				}
				mazeGrids[robotPosition[0]+1][robotPosition[1]].setBackground(Color.GRAY);
				break;
		}
	}
	

	public void refreshExploreInput() {
		for (int i = 0; i < 4; i++) {
			exploreTextFields[i].setText(exploreTextFields[i].getText());
		}
	}
	
	public void refreshFfpInput() {
		for (int i = 0; i < 2; i++) {
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
		String[] ffpInput = new String[2];
		for (int i = 0; i < 2; i++) {
			ffpInput[i] = ffpTextFields[i].getText();
		}
		
		if (! ffpInput[0].matches("[0-9]+")) {
			return false;
		}
		
		if (! ffpInput[1].matches("[0-9]+")) {
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
