package threading;

import connection.SyncObject;

public class ResetThread implements Runnable{
    private Thread t;
    private AlgoThread algoThread;
    @Override
    public void run() {
        while(true) {
            try {
                SyncObject.getSyncObject().CheckResetRobot(algoThread);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            algoThread = new AlgoThread();
            algoThread.start();
            SyncObject.getSyncObject().SignalExplorationStart();
        }
    }

    public void start(){
        System.out.println("Starting resetThread...");
        if(t == null){
            t = new Thread(this, "resetThread");
            t.start();
        }
    }

    public ResetThread(AlgoThread algoThread) {
        this.algoThread = algoThread;
    }
}
