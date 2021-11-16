package stream;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Objects;

public class ClientThread
        extends Thread {

    private Socket clientSocket;
    private EchoServerMultiThreaded server;
    private Date derniereDateDeReception;


    ClientThread(Socket s, EchoServerMultiThreaded se) {
        this.clientSocket = s;
        this.server = se;
        this.derniereDateDeReception = new Date();
    }


    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * receives a request from client then sends an echo to the client
     **/
    public void run() {
        try {
            BufferedReader socIn = null;
            socIn = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
            while (true) {
                String line = socIn.readLine();
                System.out.println(line);
                this.server.transmettreMessage(new Date(), line);
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }



    public Date getDerniereDateDeReception() {
        return derniereDateDeReception;
    }

    public void setDerniereDateDeReception(Date dateDeCreation) {
        this.derniereDateDeReception = dateDeCreation;
    }
}

