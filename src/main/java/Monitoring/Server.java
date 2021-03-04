package Monitoring;

import Interface.LobbyGUI;
import Monitoring.message.PresentezVousMessage;
import Monitoring.message.TokenMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Écoute les connexions
 *
 * @author Louri */
public class Server implements Runnable {
    private static final int MAX_NB_THREADS = 8;

    private final int port;
    private Thread acceptEnteringConnectionsThread = null;
    private ServerSocket serverSocket = null;
    private final ExecutorService executor;
    private final ConcurrentHashMap<Integer, ServerSideHandler> handlers;

    public LobbyGUI lobby; // Pour l'instant car c'est pas correct

    public Server(int port) {
        this.port = port;
        executor = Executors.newFixedThreadPool(MAX_NB_THREADS);
        handlers = new ConcurrentHashMap<>();
    }

    public void acceptConnections() throws IOException {
        assert acceptEnteringConnectionsThread == null;
        serverSocket = new ServerSocket(port);
        acceptEnteringConnectionsThread = new Thread(this);
        acceptEnteringConnectionsThread.start();
    }

    public void discardEnteringConnections() throws IOException, InterruptedException {
        if(serverSocket != null) serverSocket.close();
        serverSocket = null;
        if(acceptEnteringConnectionsThread != null) acceptEnteringConnectionsThread.join();
        acceptEnteringConnectionsThread = null;
    }

    public void discardEveryConnection() throws IOException, InterruptedException {
        discardEnteringConnections();
        handlers.values().stream().forEach(h -> {
            try {
                h.socket.close();
            } catch (IOException e) {
            }
        });
    }

    /** acceptThread.start() appelle cette méthode.
     * Écoute et crée de nouvelles connexions qui seront gérées par des ServerSideHandler.
     * Les handlers sont stockés et peuvent être utilisés parallèlement à l'écoute des nouvelles connexions. */
    public void run(){
        Random rand = new Random();
        Socket socket;

        try {
            while(true){
                try {
                    socket = serverSocket.accept(); // Bloquant. throw SocketException si serverSocket devient fermé.
                } catch(SocketException e){
                    break; // Connection entrantes fermées.
                }

                // Génère un id unique aléatoire pour l'authentification du Client.
                // id > 0 car 0 est utilisé comme l'identifiant du serveur.
                // L'id sera utilisé pour définir le destinataire d'un message.
                int id;
                do {
                    id = 1+rand.nextInt(2^16); // 0 < id < 2^16+1
                }while(handlers.containsKey(id));

                // Crée un nouvel handler qui envoie tout de suite au Client son id.
                ServerSideHandler handler = new ServerSideHandler(this, id, socket);
                handlers.put(id, handler);
                setupBehaviours(handler);

                // Gaffe à l'ordre : send -> put car sinon 
                handler.send(new TokenMessage(id));
                System.out.println("Nouvelle connexion de " + id);
                executor.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(serverSocket != null)
                serverSocket.close(); // Car on peut être sorti de la boucle alors que serverSocket n'est pas fermé.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Envoie un message de la part du serveur. (change la source du message pour y mettre 0) */
    public void sendTo(int id, Message<?> msg) throws IOException {
        try{
            handlers.get(id).send(msg.from(0).to(id));
        } catch(NullPointerException e) {
            throw new IndexOutOfBoundsException(msg.destId + " is not a valid Client id.");
        }
    }

    /** Envoie un message à tous les Clients. */
    public void broadcast(Message<?> msg) {
        for(var pair : handlers.entrySet()){
            try{
                sendTo(pair.getKey(), msg);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /** Redirige un message vers son destinataire (sans changer la source). */
    public void forward(Message<?> msg) throws IOException {
        try{
            handlers.get(msg.destId).send(msg);
        } catch(NullPointerException e) {
            throw new IndexOutOfBoundsException(msg.destId + " is not a valid Client id.");
        }
    }

    private void setupBehaviours(ServerSideHandler handler){
        handler.addBehaviour(MessageHeader.PRESENTEZ_VOUS, this::receivePresentezVous);
        handler.addBehaviour(MessageHeader.READY, this::receiveReady);
        handler.addBehaviour(MessageHeader.MALUS, this::receiveMalus);
        handler.addBehaviour(MessageHeader.SCORE, this::receiveScore);
    }

    public void clientDisconnected(int id){
        lobby.anotherDisconnected(handlers.get(id).pseudo);
        handlers.remove(id);
        System.out.println(id + " disconnected.");
    }

    public boolean isEveryoneReadyToPlay(){
        boolean result = true;

        for(var pair : handlers.entrySet()){
            if(!pair.getValue().readyToPlay){
                result = false;
                break;
            }
        }

        return result;
    }

    public void receivePresentezVous(Message<?> msg){
        handlers.get(msg.srcId).pseudo = (String) msg.data;
        lobby.receivePresentezVous(msg);
        for(var pair : handlers.entrySet()){
            try{
                pair.getValue().send(msg.to(pair.getKey()));
                if(pair.getKey() != msg.srcId)
                    handlers.get(msg.srcId).send(new PresentezVousMessage(pair.getValue().pseudo).from(pair.getKey()).to(msg.srcId));
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void receiveReady(Message<?> msg){
        handlers.get(msg.srcId).readyToPlay = true;
    }

    public void receiveMalus(Message<?> msg){
        msg.destId = msg.srcId;
        while(msg.destId == msg.srcId)
            msg.destId = (int) (handlers.size() * Math.random());

        try {
            forward(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveScore(Message<?> msg){
        lobby.receiveScore(msg);
    }
}
