package threading;

import connection.AlgoClient;
import connection.SyncObject;
import constants.RobotConstants;
import gui.GUI;

public class Main {
    public static void main(String[] args) {
        // Start Sync Manager
        SyncObject.getSyncObject();
        // Start UI Thread
        GUI.getInstance().setVisible(true);
        GUI.getInstance().refreshExploreInput();
        // Start Comm Thread
        if(RobotConstants.REAL_RUN) {
            AlgoClient.GetInstance();
            TCPThread tcpThread = new TCPThread();
            tcpThread.start();
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
