package stream;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;

public class EchoClient {


    public static void main(String[] args) throws IOException {

        Socket echoSocket = null;
        PrintStream socOut = null;
        BufferedReader stdIn = null;
        BufferedReader socIn = null;


        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        try {

            // creation socket ==> connexion
            echoSocket = new Socket(args[0], new Integer(args[1]));
            socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            socOut= new PrintStream(echoSocket.getOutputStream());
            stdIn = new BufferedReader(new InputStreamReader(System.in));


        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:"+ args[0]);
            System.exit(1);
        }

        ClientThreadEcoute ctEcoute = new ClientThreadEcoute(socIn);
        ctEcoute.start();

        ClientThreadEcriture ctEcriture = new ClientThreadEcriture(socOut, stdIn);
        ctEcriture.start();

        /**
        socIn.close();
        socOut.close();
        stdIn.close();
        echoSocket.close();
         **/

    }

}
