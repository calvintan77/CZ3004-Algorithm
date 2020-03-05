package Threading;

import GUI.GUI;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        GUI.getInstance().setVisible(true);
        GUI.getInstance().refreshExploreInput();
        TCPThread tcpThread = new TCPThread();
        tcpThread.start();
        AlgoThread algoThread = new AlgoThread();
        algoThread.start();
        algoThread.wait();
    }
}
