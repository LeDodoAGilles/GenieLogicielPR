package Monitoring;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerSideHandler extends Handler {
    private final Server server;

    public String pseudo = "???";
    public boolean readyToPlay = false;

    public ServerSideHandler(Server server, int id, Socket socket) throws IOException {
        super(id, socket);
        this.server = server;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        super.run();
        server.clientDisconnected(id);
    }

    @Override
    protected void receive(Message<?> msg) {
        if(msg.destId == 0) { // 0 = l'id du serveur
            openMessage(msg);
        }
        else { // client
            // TODO discard or forward ? (question de sécurité : permet de communiquer entre eux sans qu'on contrôle ?)
            try {
                server.forward(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
