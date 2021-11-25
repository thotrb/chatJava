package stream;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ClientThread
        extends Thread {

    private Socket clientSocket;
    private EchoServerMultiThreaded server;
    private Date derniereDateDeReception;
    private String groupeEnvoie;
    private String pseudo;


    ClientThread(Socket s, EchoServerMultiThreaded se) {
        this.clientSocket = s;
        this.server = se;
        this.derniereDateDeReception = new Date();
        this.groupeEnvoie = "";
        this.pseudo = "";
    }


    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * receives a request from client then sends an echo to the client
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



    public Date getDerniereDateDeReception() {
        return derniereDateDeReception;
    }

    public void setDerniereDateDeReception(Date dateDeCreation) {
        this.derniereDateDeReception = dateDeCreation;
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

