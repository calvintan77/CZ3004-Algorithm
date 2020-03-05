package Threading;

import Constants.RobotConstants;
import GUI.GUI;

public class Main {
    public static void main(String[] args) throws InterruptedException {
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
            resetThread.run();
        }
        while(true);
    }
}
