package connection;

import java.net.*;
import java.io.*;

public class TCPSocket { 
    private Socket conn; 
    private DataOutputStream dos;
    private BufferedReader br;
    public TCPSocket(String ip, int port) {
        while (true) {
            try {
                this.conn = new Socket(ip, port);
                this.dos = new DataOutputStream(conn.getOutputStream());
                this.br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                break;
            } catch (Exception e) {
                System.out.println("TCPSocket: " + e.toString());
            }
        } 
    }

    public void Send(String message){
        System.out.println("Attempt to send: " + message);
        try {
            this.dos.writeBytes(message + "\n");
        }catch(Exception e){
            System.out.println("Socket Write Exception: " + e.toString());
        }
    }

    public String Receive() throws IOException {
        return this.br.readLine();
    }

    public void Close() { 
        try { 
        this.dos.close();
        this.br.close();
        this.conn.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}