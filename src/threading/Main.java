package threading;

import constants.RobotConstants;
import gui.GUI;

public class Main {
    public static void main(String[] args) {
        // Start UI Thread
        GUI.getInstance().setVisible(true);
        GUI.getInstance().refreshExploreInput();
        // Start Comm Thread
        if(RobotConstants.REAL_RUN) {
            TCPThread tcpThread = new TCPThread();
            tcpThread.start();
        }
        //Start Algo Thread
        AlgoThread algoThread = new AlgoThread();
        algoThread.start();
        //Reset Simulation
        if(!RobotConstants.REAL_RUN){
            ResetThread resetThread = new ResetThread(algoThread);
            resetThread.start();
        }
        while(true);
    }
}
