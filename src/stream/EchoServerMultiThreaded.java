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
    private HashMap<Long, String> messagesEchanges;

    public EchoServerMultiThreaded() throws FileNotFoundException {
        clients = new ArrayList<ClientThread>();
        messagesEchanges = new HashMap<>();



    }

    public void initialiserHashMap () throws IOException, ParseException {
        BufferedReader buff = new BufferedReader(new FileReader(NOM_FICHIER_SAUVEGARDE));
        String line;
        while((line = buff.readLine()) != null){
            String message = line;
            line = buff.readLine();
            SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            Date dateMessage = formater.parse(line);
            SimpleDateFormat formater2 = new SimpleDateFormat("'le' dd/MM/yyyy 'à' hh:mm:ss");
            messagesEchanges.put(dateMessage.getTime(), message + " (" +  formater2.format(dateMessage)+ ")");
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
                serv.initialiserConversation(ct);


            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }

    }

    public void transmettreMessage(Date dateEnvoie, String message) throws IOException {

        SimpleDateFormat formater = new SimpleDateFormat("'le' dd/MM/yyyy 'à' hh:mm:ss");
        SimpleDateFormat formater2 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        FileWriter writer = new FileWriter(NOM_FICHIER_SAUVEGARDE, true);
        writer.write(message + "\n");
        writer.write(formater2.format(dateEnvoie) + '\n');
        writer.close();

        messagesEchanges.put(dateEnvoie.getTime(), message + " (" +formater.format(dateEnvoie) + ")");

        System.out.println("TAILLE LISTE : " + this.clients.size());
        for (ClientThread client : this.clients) {

            PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
            socOut.println(message  + " (" +  formater.format(dateEnvoie)+ ")");
        }
    }

    public void initialiserConversation(ClientThread client) throws IOException {
        PrintStream socOut = new PrintStream(client.getClientSocket().getOutputStream());
        String message = "";

        Map sortedMap = new TreeMap(messagesEchanges);
        Set set = sortedMap.entrySet();
        Iterator ite = set.iterator();
        while(ite.hasNext()){
            Map.Entry me = (Map.Entry)ite.next();
            message = (String) me.getValue();
            socOut.println(message);

        }

    }
}