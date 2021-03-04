package Monitoring;

import Interface.Framework;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Consumer;

// Un seul endroit où on lit, plusieurs où on écrit mais pas en même temps.

public abstract class Handler implements Runnable {
    protected int id; // Client id, pas final
    public final Socket socket;
    private final Object outputLock;
    protected ObjectInputStream in;
    protected ObjectOutputStream out;
    private final HashMap<MessageHeader, Consumer<Message<?>>> behaviours;

    /** id du client et socket associée */
    public Handler(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
        behaviours = new HashMap<>();
        outputLock = new Object();

        // in et out initialisés dans un ordre différent chez l'Handler du client et du serveur, car sinon interbloquage
        // https://stackoverflow.com/questions/14110986/new-objectinputstream-blocks
    }

    /** Écoute les messages entrants et réagit automatiquement grâce aux behaviours. */
    public void run(){
        while(true){
            try {
                Message<?> msg = (Message<?>) in.readObject();
                System.out.println("Received : " + msg); // TODO debug
                receive(msg);
            } catch (IOException | ClassNotFoundException e){
                break;
            }
        }

        try {
            if(socket != null)
                socket.close();
        } catch (IOException e) {
        }
    }

    /** Décide quoi faire avec le message, généralement l'accepter ou non.
     * Si le message est accepté, la méthode openMessage() doit être appelée. */
    protected abstract void receive(Message<?> msg);

    /** "Ouvre" un message, vérifie son header et déclenche le comportement approprié s'il existe. */
    protected void openMessage(Message<?> msg){
        if(behaviours.containsKey(msg.header)){
            behaviours.get(msg.header).accept(msg);
        }
        else {
            System.err.println("Le message reçu ne peut pas être interprété : header non reconnu. " + msg);
        }
    }

    /** Envoie un message à travers la socket.
     * Thread-safe. */
    public void send(Message<?> msg) throws IOException {
        // Ne pas modifier srcId ou destId ici.
        System.out.println("Send : " + msg); // TODO debug
        synchronized(outputLock){
            out.writeObject(msg); // can throw SocketException when socket is closed
        }
    }

    /** Ajoute un comportement qui se déclenchera si un message avec le bon header est reçu.
     * Thread-safe. */
    public void addBehaviour(MessageHeader header, Consumer<Message<?>> behaviour){
        if(behaviours.containsKey(header)){
            System.err.println("Warning : le comportement " + header + " a été remplacé.");
        }
        behaviours.put(header, behaviour);
    }

    /** Enlève le comportement associé à cet header.
     * Thread-safe. */
    public void removeBehaviour(MessageHeader header){
        if(!behaviours.containsKey(header)){
            System.err.println("Warning : le comportement " + header + " n'est pas enregistré.");
            return;
        }
        behaviours.remove(header);
    }
}
