package threading;

import connection.AlgoClient;

public class TCPThread implements Runnable{
    private Thread t;

    public void run(){
        while(true) {
            try {
                AlgoClient.GetInstance().HandleIncoming();
            }catch (Exception e){
                System.out.println("TCPListener: " + e.toString());
            }
        }
    }

    public void start(){
        System.out.println("Starting thread TCPListener...");
        if(t == null){
            t = new Thread(this, "TCPListener");
            t.start();
        }
    }
}
