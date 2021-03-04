package Monitoring;

import java.io.IOException;
import java.net.*;

/** Gère tout ce qui a trait au monitoring, serveur-client, ...
 *
 * @author Louri */
public class Monitor {
    public static int PORT = 42000; // mis à jour par le fichier de configuration

    public Server server;
    public ClientSideHandler clientSideHandler;

    public void dispose(){
        if(server != null){
            try {
                server.discardEveryConnection();
                server = null;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(clientSideHandler != null){
            try {
                clientSideHandler.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientSideHandler = null;
        }
    }

    public void setOpenedClientSocket(Socket clientSocket) throws IOException {
        clientSideHandler = new ClientSideHandler(clientSocket);

        clientSideHandler.waitForId();
    }

    public void setupServer(){
        if(server != null)
            return;

        server = new Server(PORT);
    }

    public void listenEnteringConnections() throws IOException {
        if(server == null)
            return;

        server.acceptConnections();
    }

    public void closeEnteringConnections() throws IOException, InterruptedException {
        if(server == null)
            return;

        server.discardEnteringConnections();
    }

    /** Détermine l'adresse ip automatiquement.
     *
     * Based on: https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java#answer-38342964
     * */
    public String acquireAutoIp(){
        String result = "";
        try {
            // Essaie de se connecter à un serveur DNS de Google par protocol UDP.
            // L'OS regarde la table de routage pour déterminer notre ip.
            DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 80);
            result = socket.getLocalAddress().getHostAddress();

        } catch (SocketException e) {
            System.err.println("Impossible de créer une socket.");
            e.printStackTrace();
            return null;
        } catch (UnknownHostException e) {
            System.err.println("Aucune connexion internet.");
            e.printStackTrace();
            return null;
        }

        // Ou encore

        /*Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("google.com", 80));
            result = socket.getLocalAddress().toString();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        return result;
    }
}
