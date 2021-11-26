package stream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

/**
 * Cette classe est un thread représentant un client.
 */
public class ClientThread extends Thread {

    private final Socket clientSocket;
    private final EchoServerMultiThreaded server;
    private String groupeEnvoie;
    private String pseudo;

    /**
     * Constructeur
     * @param s socket
     * @param se serveur
     */
    ClientThread(Socket s, EchoServerMultiThreaded se) {
        this.clientSocket = s;
        this.server = se;
        this.groupeEnvoie = "";
        this.pseudo = "";
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * Reçoit et gére les requêtes du client.
     **/
    public void run() {
        try {
            BufferedReader socIn;
            socIn = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
            while (true) {
                String line = socIn.readLine();
                String teteMessage = line.split("_")[0];
                String commandeAEffectuer = "";
                if(line.split("_").length > 1){
                    commandeAEffectuer = line.split("_")[line.split("_").length-1];
                }
                if(!teteMessage.equals("")){
                    switch (commandeAEffectuer){

                        case ("sendPseudo") :
                            server.initialiserConversation(this);
                            this.pseudo = teteMessage;
                            server.envoyerMessageConnexionAccepte(this);

                            break;

                        case("getMenu") :
                            server.afficherMenu(this);
                            break;

                        case ("createGroup") :
                            server.rejoindreGroupe(this, teteMessage);
                            break;
                        case ("deleteGroup"):
                            server.supprimerGroupe(this, teteMessage);
                            break;
                        case ("joinGroup"):
                            if(server.getListeGroupes().contains(teteMessage)) {
                                socOut.println("Vous communiquez désormais avec le groupe: " + teteMessage);
                                groupeEnvoie = teteMessage;
                                server.initialiserConversation(this);
                            }else{
                                server.envoyerMessageErreurGroupeInexistant(this);
                            }
                            break;
                        case ("leaveGroup"):
                            socOut.println("Vous avez quitté le groupe : " + this.groupeEnvoie);
                            socOut.println("Attention, vos messages sont désormais publiques.");
                            this.groupeEnvoie ="";
                            server.initialiserConversation(this);
                            break;

                        case ("showUsers"):
                            server.envoyerListeDesPersonnesConnectees(this);
                            break;

                        case ("showGroup"):
                            server.afficherListeGroupes(this);
                            break;

                        default:
                            this.server.transmettreMessage(new Date(), pseudo + " : " + line, groupeEnvoie);
                            break;
                    }
                }

            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }


    public String getPseudo() {
        return pseudo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientThread that = (ClientThread) o;
        return Objects.equals(pseudo, that.pseudo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pseudo);
    }

    public String getGroupeEnvoie() {
        return groupeEnvoie;
    }
}

