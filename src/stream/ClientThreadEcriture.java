package stream;

import java.io.BufferedReader;
import java.io.PrintStream;

public class ClientThreadEcriture extends Thread {

    private PrintStream socOut;
    private BufferedReader stdIn;
    private String pseudo;
    private boolean aEnvoyerPseudo;

    public ClientThreadEcriture(PrintStream scO, BufferedReader stdI) {
        this.socOut = scO;
        this.stdIn = stdI;
        this.aEnvoyerPseudo = false;
    }

    public void run() {


        try {
            String line;


            while (true) {

                if (!aEnvoyerPseudo) {
                    System.out.println("Veuillez saisir votre pseudo : ");
                    line = this.stdIn.readLine();
                    pseudo = line;
                    System.out.println("Connection initialisée, vous pouvez dialoguer.");
                    aEnvoyerPseudo = true;

                } else {

                    line = pseudo + ": " + stdIn.readLine();
                    if (line.equals(pseudo + ": .")) break;

                    if (line.equals(pseudo + ": Menu")) {
                        System.out.println("Voici la liste des groupes auxquels vous êtes inscrits : ");

                        System.out.println("Tapez :" +
                                "-1 Pour créer un groupe" +
                                "-2 Pour quitter un groupe");

                    } else {
                        socOut.println(line);
                    }
                }
            }



        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }
}
