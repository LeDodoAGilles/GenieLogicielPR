package Monitoring;

import java.io.Serializable;

public class Message<T extends Serializable> implements Serializable {
    public final MessageHeader header; // pour lancer un comportement automatique à la réception

    public int srcId = -1; // id de la source du message
    public int destId = -1; // id du destinataire du message
    public T data; // le corps du message

    public Message(int srcId, int destId, MessageHeader header, T data){
        this.srcId = srcId;
        this.destId = destId;
        this.header = header;
        this.data = data;
    }

    public Message(MessageHeader header, T data){
        this.header = header;
        this.data = data;
    }

    /** Set la source du message. Peut être chaîné. */
    public Message<T> from(int srcId){
        this.srcId = srcId;
        return this;
    }

    /** Set le destinataire du message. Peut être chaîné. */
    public Message<T> to(int destId){
        this.destId = destId;
        return this;
    }

    /** au format JSON */
    public String toString(){
        return String.format("{\"src\": %d, \"dest\": %d, \"header\": \"%s\", \"data\": %s}", srcId, destId, header, data.toString());
    }
}
