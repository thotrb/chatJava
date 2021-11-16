package stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class ClientThreadEcoute extends Thread {

    private BufferedReader socIn;

    public ClientThreadEcoute(BufferedReader scI) {
        this.socIn = scI;
    }

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
