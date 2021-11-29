package stream;

import java.io.BufferedReader;

/**
 * Cette classe est un thread qui permet d'implémenter les fonctions d'écoute pour un client
 */
public class ClientThreadEcoute extends Thread {

    /**
     * Données entrantes de la socket
     */
    private final BufferedReader socIn;

    /**
     * Constructeur
     * @param scI données entrantes de la socket
     */
    public ClientThreadEcoute(BufferedReader scI) {
        this.socIn = scI;
    }

    /**
     * Permet le lancement du thread
     */
    public void run() {
        try {
            while (true) {
                String line = this.socIn.readLine();
                System.out.println(line);
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }


}
