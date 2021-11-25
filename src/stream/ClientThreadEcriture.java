package stream;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Objects;

public class ClientThreadEcriture extends Thread {

    private PrintStream socOut;
    private BufferedReader stdIn;
    private boolean aEnvoyerPseudo;
    private String derniereCommande, line, nomGroupe;

    public ClientThreadEcriture(PrintStream scO, BufferedReader stdI) {
        this.socOut = scO;
        this.stdIn = stdI;
        this.aEnvoyerPseudo = false;
        this.derniereCommande = "";
    }

    private void envoyerMessage(String commande) {
        line = commande;
        socOut.println(line);
        derniereCommande = "";
    }

    public void run() {


        try {
            String commande;


            while (true) {

                if (!aEnvoyerPseudo) {

                    System.out.println("Veuillez saisir votre pseudo : ");
                    String pseudo = this.stdIn.readLine();
                    while(pseudo.equals("\n") || pseudo.equals("")  ){
                        System.out.println("Veuillez saisir un pseudo valide ");
                        pseudo = this.stdIn.readLine();
                    }
                    socOut.println(pseudo + "_sendPseudo");
                    aEnvoyerPseudo = true;

                } else {

                    commande = stdIn.readLine();

                    while(commande.equals("\n") || commande.equals("")  ){
                        commande = this.stdIn.readLine();
                    }


                    if (commande.equals(".")) break;

                    if (commande.equals("Menu") || commande.equals("menu")) {
                        socOut.println("0_getMenu");
                        derniereCommande = "menuSelector";
                    } else {
                        switch (commande) {
                            case "1":
                                if (derniereCommande.equals("menuSelector")) {
                                    System.out.println("Nom du groupe à ajouter : ");
                                    String nomGroupe = this.stdIn.readLine();
                                    socOut.println(nomGroupe + "_createGroup");
                                    derniereCommande = "";

                                } else {
                                    envoyerMessage(commande);
                                }
                                break;
                            case "2":
                                if (derniereCommande.equals("menuSelector")) {
                                    System.out.println("Nom du groupe à supprimer : ");
                                    String nomGroupe = this.stdIn.readLine();
                                    socOut.println(nomGroupe + "_deleteGroup");
                                    derniereCommande = "";

                                } else {
                                    envoyerMessage(commande);
                                }
                                break;
                            case "3":
                                if (derniereCommande.equals("menuSelector")) {
                                    System.out.println("Nom du groupe à rejoindre : ");
                                    String nomGroupe = this.stdIn.readLine();
                                    socOut.println(nomGroupe + "_joinGroup");
                                    derniereCommande = "";

                                } else {
                                    envoyerMessage(commande);
                                }

                                break;
                            case "4":
                                if (derniereCommande.equals("menuSelector")) {
                                    socOut.println("0_showUsers");
                                    derniereCommande = "";
                                } else {
                                    envoyerMessage(commande);
                                }

                                break;
                            case "5":
                                if (derniereCommande.equals("menuSelector")) {
                                    socOut.println("0_showGroup");
                                    derniereCommande = "";
                                } else {
                                    envoyerMessage(commande);
                                }
                                break;
                            case "6":
                                if (derniereCommande.equals("menuSelector")) {
                                    socOut.println("0_leaveGroup");
                                    derniereCommande = "";
                                } else {
                                    envoyerMessage(commande);
                                }
                                break;
                            default:
                                envoyerMessage(commande);
                                break;
                        }


                    }


                }
            }


        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }
}
