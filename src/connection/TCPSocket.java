package connection;

import java.net.*;
import java.io.*; 

public class TCPSocket { 
    private Socket conn; 
    private PrintWriter pw; 
    private BufferedReader bw; 

    public TCPSocket(String ip, int port) { 
        while (true) {
        try { 
            this.conn = new Socket(ip, port);
            this.pw = new PrintWriter(conn.getOutputStream()); 
            this.bw = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            break; 
        } catch (Exception e) { 
            continue; 
            }
        } 
    }

    public void Send(String message) { 
        this.pw.println(message);
    }

    public String Receive() throws IOException { 
        return this.bw.readLine();
    }

    public void Close() { 
        try { 
        this.pw.close();
        this.bw.close();
        this.conn.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}