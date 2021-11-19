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
            BufferedReader socIn = null;
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
                if(teteMessage != ""){
                    switch (commandeAEffectuer){

                        case ("sendPseudo") :
                            this.pseudo = teteMessage;
                            socOut.println("Connection initialisée, vous pouvez dialoguer.");
                            socOut.println("Tapez \"Menu\" pour accéder au menu de l'application.");

                            break;

                        case("getGroups") :
                            Set<String> groupes  = this.server.getListeGroupes();
                            socOut.println("Voici la liste des groupes auxquels vous êtes inscrits : ");
                            for(String groupe : groupes){
                                socOut.println(groupe);
                            }
                            socOut.println("Tapez : \n" +
                                    "-1 Pour créer un groupe \n" +
                                    "-2 Pour supprimer un groupe \n" +
                                    "-3 Rejoindre un groupe");
                            if(this.groupeEnvoie != ""){
                                socOut.println("-4 Quitter le groupe " + this.groupeEnvoie);
                            }
                            break;

                        case ("createGroup") :
                            groupes = server.getListeGroupes();
                            server.ajouterGroupe(teteMessage);
                            socOut.println("Groupe ajouté avec succès.");
                            socOut.println("Liste des groupes : ");
                            for(String groupe : groupes){
                                socOut.println(groupe);
                            }
                            break;
                        case ("deleteGroup"):
                            groupes = server.getListeGroupes();
                            if(server.getListeGroupes().contains(teteMessage) && !teteMessage.equals("")){
                                server.supprimerGroupe(teteMessage);
                                socOut.println("Groupe supprimé avec succès.");
                                socOut.println("Liste des groupes : ");
                                for(String groupe : groupes){
                                    socOut.println(groupe);
                                }
                            }else{
                                socOut.println("Le groupe que vous souhaitez supprimer n'existe pas dans la liste : \n");
                                for(String groupe : groupes){
                                    socOut.println(groupe);
                                }
                            }
                            break;
                        case ("joinGroup"):
                            groupes = server.getListeGroupes();
                            if(server.getListeGroupes().contains(teteMessage)) {
                                socOut.println("Vous communquez désormais avec le groupe: " + teteMessage);
                                groupeEnvoie = teteMessage;
                                server.initialiserConversation(this);
                            }else{
                                socOut.println("Le groupe que vous souhaitez rejoindre n'existe pas dans la liste: ");
                                for(String groupe : groupes){
                                    socOut.println(groupe);
                                }
                                socOut.println("Attention, vos messages sont publiques.");
                            }
                            break;
                        case ("leaveGroup"):
                            socOut.println("Vous avez quitté le groupe : " + this.groupeEnvoie);
                            socOut.println("Attention, vos messages sont désormais publiques.");
                            this.groupeEnvoie ="";
                            server.initialiserConversation(this);
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

    public String getGroupeEnvoie() {
        return groupeEnvoie;
    }
}

