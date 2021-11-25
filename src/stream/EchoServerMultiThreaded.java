package stream;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

import java.lang.*;
import java.util.*;
import java.util.stream.*;
import java.util.stream.Collectors;

public class EchoServerMultiThreaded {

    public final static String NOM_FICHIER_SAUVEGARDE = "sauvegarde.txt";
    private ArrayList<ClientThread> clients;

    private HashMap<String, HashMap<Long, String>> messagesParGroupe;

    public EchoServerMultiThreaded() throws FileNotFoundException {
        clients = new ArrayList<ClientThread>();
        //messagesEchanges = new HashMap<>();

        messagesParGroupe = new HashMap<>();
        HashMap<Long, String> conversationPublique = new HashMap<>();


    }

    public void initialiserHashMap() throws IOException, ParseException {
        BufferedReader buff = new BufferedReader(new FileReader(NOM_FICHIER_SAUVEGARDE));
        String line;
        while ((line = buff.readLine()) != null) {
            String message = line;
            line = buff.readLine();
            SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
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

    }

    public void addClient(ClientThread client) {
        this.clients.add(client);
    }

    public ArrayList<ClientThread> getClients() {
        return this.clients;
    }

    /**
     * main method
     *
     * @param args port
     **/
    public static void main(String args[]) throws IOException, ParseException {
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
                //serv.setWriter(new PrintWriter(EchoServerMultiThreaded.NOM_FICHIER_SAUVEGARDE));
                Socket clientSocket = listenSocket.accept();
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                ClientThread ct = new ClientThread(clientSocket, serv);
                serv.addClient(ct);
                ct.start();
                //serv.initialiserConversation(ct);
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }

    }

    public Set<String> getListeGroupes() {
        return messagesParGroupe.keySet();
    }

    public void transmettreMessage(Date dateEnvoie, String message, String groupeMessage) throws IOException {

        SimpleDateFormat formater = new SimpleDateFormat("'le' dd/MM/yyyy 'à' hh:mm:ss");
        SimpleDateFormat formater2 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        FileWriter writer = new FileWriter(NOM_FICHIER_SAUVEGARDE, true);
        writer.write(groupeMessage + " " + message + "\n");
        writer.write(formater2.format(dateEnvoie) + '\n');
        writer.close();

        //messagesEchanges.put(dateEnvoie.getTime(), groupeMessage + " " + message + " (" +formater.format(dateEnvoie) + ")");

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

    public void envoyerMessageErreurGroupeInexistant(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        socOut.println("Le groupe que vous souhaitez rejoindre n'existe pas dans la liste: ");
        for (String groupe : this.getListeGroupes()) {
            socOut.println(groupe);
        }
        socOut.println("Attention, vos messages sont publiques.");
    }

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

    public void afficherMenu(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        this.afficherListeGroupes(client);
        socOut.println("Tapez : \n" +
                "-1 Pour créer un groupe \n" +
                "-2 Pour supprimer un groupe \n" +
                "-3 Rejoindre un groupe \n" +
                "-4 Afficher la liste des utilisateurs connectés");
        if (!client.getGroupeEnvoie().equals("")) {
            socOut.println("-5 Quitter le groupe " + client.getGroupeEnvoie());
        }

    }

    public void rejoindreGroupe(ClientThread client, String nomGroupe) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        this.ajouterGroupe(nomGroupe);
        socOut.println("Groupe ajouté avec succès.");
        this.afficherListeGroupes(client);

    }

    public void envoyerMessageConnexionAccepte(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        socOut.println("Connexion initialisée, vous pouvez dialoguer.");
        socOut.println("Tapez \"Menu\" pour accéder au menu de l'application.");

    }

    public void initialiserConversation(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        String message = "";

        if (!messagesParGroupe.containsKey(client.getGroupeEnvoie())) {
            HashMap<Long, String> messages = new HashMap<>();
            messagesParGroupe.put(client.getGroupeEnvoie(), messages);
        }

        Map sortedMap = new TreeMap(messagesParGroupe.get(client.getGroupeEnvoie()));
        Set set = sortedMap.entrySet();
        Iterator ite = set.iterator();
        while (ite.hasNext()) {
            Map.Entry me = (Map.Entry) ite.next();
            message = (String) me.getValue();
            socOut.println(message);
        }
    }

    public void supprimerGroupe(ClientThread client, String nomGroupe) throws IOException {
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
        boolean successful = tempFile.renameTo(inputFile);


    }

    public void ajouterGroupe(String nomGroupe) {
        if (!messagesParGroupe.containsKey(nomGroupe)) {
            HashMap<Long, String> messages = new HashMap<>();
            messagesParGroupe.put(nomGroupe, messages);
        }
    }
}