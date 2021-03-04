package Monitoring;

import Interface.Framework;
import Interface.LobbyGUI;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSideHandler extends Handler {
    public LobbyGUI lobby; // TODO pour l'instant

    public ClientSideHandler(Socket socket) throws IOException {
        super(-1, socket); // id=-1 pour l'instant, sera changé plus tard
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void setupBehaviours(){
        this.addBehaviour(MessageHeader.PRESENTEZ_VOUS, lobby::receivePresentezVous);
        this.addBehaviour(MessageHeader.DOWNLOAD, lobby::receiveDownload);
        this.addBehaviour(MessageHeader.LETS_BEGIN, lobby::receiveLetsBegin);
        this.addBehaviour(MessageHeader.MALUS, this::receiveMalus);
    }

    private void receiveMalus(Message<?> msg){
        // TODO
    }

    @Override
    public void run() {
        super.run();
        Platform.runLater(() -> Framework.showAlert("Déconnexion", "Vous avez été déconnecté du serveur.", "", Alert.AlertType.INFORMATION, true));
    }

    public void waitForId(){
        // On récupère le token d'authentification avant de passer au traitement général.
        Message<?> msg;
        try {
            msg = (Message<?>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return;
        }

        // Obligé de le traiter ici, car on ne sait pas encore quel id on a : on ne peut pas trier les msg dans receive
        if(msg.header == MessageHeader.TOKEN){
            this.id = msg.destId; // ou cast msg.data en un int, qu'importe
            assert msg.destId == (int) msg.data;
        }
        else { // Le premier message ne donne pas le token d'authentification, c'est inattendu.
            try {
                socket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return;
        }
    }

    @Override
    protected void receive(Message<?> msg) {
        // Le message n'est accepté que si ce Client en est bien le destinaire.
        if(msg.destId == id){
            openMessage(msg);
        }
    }

    @Override
    public void send(Message<?> msg) throws IOException {
        super.send(msg.from(id)); // Envoie le message avec le bon srcId
    }
}
