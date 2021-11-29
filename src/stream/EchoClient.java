package stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Classe représentant un client
 */
public class EchoClient {


    public static void main(String[] args) {

        /**
         *  La socket établie entre le client et le serveur
         */
        Socket echoSocket;

        /**
         * Stream pour l'écriture des messages
         */
        PrintStream socOut = null;

        /**
         * Données entrantes du terminal (saisies par l'utilisateur)
         */
        BufferedReader stdIn = null;

        /**
         * Données entrantes de la socket
         */
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

    }

}
