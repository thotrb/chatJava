package stream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Classe représentant un serveur multithreaded
 */
public class EchoServerMultiThreaded {

    /**
     * Non du fichier de sauvegarde
     */
    public final static String NOM_FICHIER_SAUVEGARDE = "sauvegarde.txt";

    /**
     * Liste des clients connectés
     */
    private final ArrayList<ClientThread> clients;

    /**
     * Map contenant les messages envoyés. La clé est la date d'envoie (en millisecondes écoulées après 1970),
     * la valeur est le corps du message avec la date entre parenthèses
     */
    private final HashMap<String, HashMap<Long, String>> messagesParGroupe;

    /**
     * Constructeur : initialise la liste des clients connectés et la map des messages
     */
    public EchoServerMultiThreaded() {
        clients = new ArrayList<>();
        messagesParGroupe = new HashMap<>();
    }

    /**
     * Permet d'initialiser la map contenant l'historique grâce au fichier texte
     * @throws IOException liée à readLine
     */
    public void initialiserHashMap() throws IOException {
        BufferedReader buff = new BufferedReader(new FileReader(NOM_FICHIER_SAUVEGARDE));
        String line;
        while ((line = buff.readLine()) != null) {
            String message = line;
            line = buff.readLine();
            SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            try{
                Date dateMessage = formater.parse(line);
                SimpleDateFormat formater2 = new SimpleDateFormat("'le' dd/MM/yyyy 'à' hh:mm:ss");
                //messagesEchanges.put(dateMessage.getTime(), message + " (" +  formater2.format(dateMessage)+ ")");

                String groupeMessage = message.split(" ")[0];
                if (messagesParGroupe.containsKey(groupeMessage)) {
                    messagesParGroupe.get(groupeMessage).put(dateMessage.getTime(), message + " (" + formater2.format(dateMessage) + ")");
                } else {
                    HashMap<Long, String> messages = new HashMap<>();
                    messages.put(dateMessage.getTime(), message + " (" + formater2.format(dateMessage) + ")");
                    messagesParGroupe.put(groupeMessage, messages);
                }
            }
            catch(Exception ex){
                System.out.println(ex.getMessage());
            }
        }

    }

    /**
     * Permet l'ajout d'un client
     * @param client est le client à ajouter
     */
    public void addClient(ClientThread client) {
        this.clients.add(client);
    }

    /**
     * Méthode main
     * @param args : numéro de port. Un seul paramètre doit être passé
     * @throws IOException gestion des exceptions input/output
     */
    public static void main(String[] args) throws IOException {
        ServerSocket listenSocket;
        EchoServerMultiThreaded serv = new EchoServerMultiThreaded();
        serv.initialiserHashMap();

        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }
        try {
            listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
            System.out.println("Server ready...");
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                ClientThread ct = new ClientThread(clientSocket, serv);
                serv.addClient(ct);
                ct.start();
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }

    /**
     * Permet de récupérer la liste des groupes
     * @return la liste des noms des groupes
     */
    public Set<String> getListeGroupes() {
        return messagesParGroupe.keySet();
    }

    /**
     * Permet de transmettre un message
     * @param dateEnvoie date à laquelle le message est envoyé
     * @param message corps du message
     * @param groupeMessage nom du groupe dans lequel le message est envoyé. "" Pour les messages publics.
     * @throws IOException gestion des exceptions input/output
     */
    public void transmettreMessage(Date dateEnvoie, String message, String groupeMessage) throws IOException {

        SimpleDateFormat formater = new SimpleDateFormat("'le' dd/MM/yyyy 'à' hh:mm:ss");
        SimpleDateFormat formater2 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        FileWriter writer = new FileWriter(NOM_FICHIER_SAUVEGARDE, true);
        writer.write(groupeMessage + " " + message + "\n");
        writer.write(formater2.format(dateEnvoie) + '\n');
        writer.close();

        if (messagesParGroupe.containsKey(groupeMessage)) {
            messagesParGroupe.get(groupeMessage).put(dateEnvoie.getTime(), message + " (" + formater.format(dateEnvoie) + ")");
        } else {
            HashMap<Long, String> messages = new HashMap<>();
            messages.put(dateEnvoie.getTime(), message + " (" + formater.format(dateEnvoie) + ")");
            messagesParGroupe.put(groupeMessage, messages);
        }

        for (ClientThread client : this.clients) {
            if (client.getGroupeEnvoie().equals(groupeMessage)) {
                PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
                socOut.println("A : " + groupeMessage + " - " +message + " (" + formater.format(dateEnvoie) + ")");
            }
        }
    }

    /**
     * Permet de voir les personnes connectées (affiche les pseudos)
     * @param client client connecté souhaitant connaitre les autres personnes connectées
     * @throws IOException gestion des exceptions input/output
     */
    public void envoyerListeDesPersonnesConnectees(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        if (clients.size() > 0) {
            socOut.println("Voici la liste des utilisateurs connectés :");
            for (ClientThread c : clients) {
                socOut.println(c.getPseudo());
            }
        } else {
            socOut.println("Aucun utilisateur connecté");

        }
    }

    /**
     * Permet de prévenir l'utilisateur, lorsqu'il essaie de rejoindre un groupe inexistant.
     * @param client est l'utilisateur qui souhaite rejoindre un groupe.
     * @throws IOException gestion des exceptions input/output
     */
    public void envoyerMessageErreurGroupeInexistant(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        socOut.println("Le groupe que vous souhaitez rejoindre n'existe pas dans la liste: ");
        for (String groupe : this.getListeGroupes()) {
            socOut.println(groupe);
        }
        socOut.println("Attention, vos messages sont publiques.");
    }

    /**
     * Permet d'afficher la liste des groupes.
     * @param client est l'utilisateur qui demande l'affichage des groupes.
     * @throws IOException gestion des exceptions input/output
     */
    public void afficherListeGroupes(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        Set<String> groupes = this.getListeGroupes();

        if (groupes.size() > 1) {
            socOut.println("Voici la liste des groupes auxquels vous pouvez accéder : ");
            for (String groupe : groupes) {
                if(!groupe.equals("")){
                    socOut.println(groupe);
                }
            }
        } else {
            socOut.println("Aucun groupe existant");
        }

    }

    /**
     * Permet l'affichage du menu lorsque l'utilisateur saisi "Menu"
     * @param client est l'utilisateur qui souhaite accéder au Menu.
     * @throws IOException gestion des exceptions input/output
     */
    public void afficherMenu(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        this.afficherListeGroupes(client);
        socOut.println("Tapez : \n" +
                "-1 Pour créer un groupe \n" +
                "-2 Pour supprimer un groupe \n" +
                "-3 Rejoindre un groupe \n" +
                "-4 Afficher la liste des utilisateurs connectés \n" +
                "-5 Afficher la liste des groupes");
        if (!client.getGroupeEnvoie().equals("")) {
            socOut.println("-6 Quitter le groupe " + client.getGroupeEnvoie());
        }

    }

    /**
     * Permet d'accéder à un groupe pour envoyer des messages seulement aux personnes concernées.
     * @param client est l'utilisateur qui souhaite rejoindre un groupe.
     * @param nomGroupe est le nom du groupe que l'utilisateur souhaite rejoindre.
     * @throws IOException gestion des exceptions input/output
     */
    public void rejoindreGroupe(ClientThread client, String nomGroupe) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        this.ajouterGroupe(nomGroupe);
        socOut.println("Groupe ajouté avec succès.");
        this.afficherListeGroupes(client);

    }

    /**
     * Permet de signaler la validation de la connexion lorsque l'utilisateur se connecte
     * @param client est le client qui se connecte
     * @throws IOException gestion des exceptions input/output
     */
    public void envoyerMessageConnexionAccepte(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        socOut.println("Connexion initialisée, vous pouvez dialoguer.");
        socOut.println("Tapez \"Menu\" pour accéder au menu de l'application.");

    }

    /**
     * Permet d'afficher les messages antérieurs à la date de connexion de l'utilisateur
     * @param client est le client connecté à qui on doit afficher les messages échangés avant sa connexion
     * @throws IOException gestion des exceptions input/output
     */
    public void initialiserConversation(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        String message;

        if (!messagesParGroupe.containsKey(client.getGroupeEnvoie())) {
            HashMap<Long, String> messages = new HashMap<>();
            messagesParGroupe.put(client.getGroupeEnvoie(), messages);
        }

        Map sortedMap = new TreeMap(messagesParGroupe.get(client.getGroupeEnvoie()));
        Set set = sortedMap.entrySet();
        for (Object obj : set) {
            Map.Entry me = (Map.Entry) obj;
            message = (String) me.getValue();
            socOut.println(message);
        }
    }

    /**
     * Permet la suppression d'un groupe
     * @param client client qui supprime le groupe
     * @param nomGroupe nom du groupe à supprimer
     * @return true si le fichier tampon qui copie les données de l'ancien fichier sans les messages du groupe est bien
     * renommé selon le nom de fichier de sauvegarde. False si le renommage ne se passe pas correctement.
     * @throws IOException gestion des exceptions input/output
     */
    public boolean supprimerGroupe(ClientThread client, String nomGroupe) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        if (this.getListeGroupes().contains(nomGroupe) && !nomGroupe.equals("")) {
            messagesParGroupe.remove(nomGroupe);

            socOut.println("Groupe supprimé avec succès.");
            this.afficherListeGroupes(client);
        } else {
            socOut.println("Le groupe que vous souhaitez supprimer n'existe pas dans la liste : \n");
            for (String groupe : this.getListeGroupes()) {
                socOut.println(groupe);
            }
        }

        //supprimer dans le fichier text
        File inputFile = new File(NOM_FICHIER_SAUVEGARDE);
        File tempFile = new File("myTempFile.txt");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String currentLine;

        boolean trouveLigne = false;

        while ((currentLine = reader.readLine()) != null) {
            if (trouveLigne) {
                trouveLigne = false;
                continue;
            }
            // trim newline when comparing with lineToRemove
            String trimmedLine = currentLine.trim();
            //System.err.println(trimmedLine);
            if (trimmedLine.split(" ")[0].equals(nomGroupe)) {
                trouveLigne = true;
                continue;
            }
            writer.write(currentLine + System.getProperty("line.separator"));
        }
        writer.close();
        reader.close();
        return tempFile.renameTo(inputFile);
    }

    /**
     * Permet l'ajout d'un groupe
     * @param nomGroupe est le nom du groupe à ajouter
     */
    public void ajouterGroupe(String nomGroupe) {
        if (!messagesParGroupe.containsKey(nomGroupe)) {
            HashMap<Long, String> messages = new HashMap<>();
            messagesParGroupe.put(nomGroupe, messages);
        }
    }
}