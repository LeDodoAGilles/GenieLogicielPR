package Monitoring.message;

import Monitoring.Message;
import Monitoring.MessageHeader;

/** Envoyé par le client au serveur après avoir reçu son id, afin de l'informer de son pseudo.
 * Renvoyé par le serveur à tous les clients pour qu'ils sachent qui est dans le lobby. */
public class PresentezVousMessage extends Message<String> {
    private static final MessageHeader HEADER = MessageHeader.PRESENTEZ_VOUS;

    public PresentezVousMessage(int srcId, int destId, String data) {
        super(srcId, destId, HEADER, data); // destId = 0 le serveur
    }
    public PresentezVousMessage(int srcId, String data) {
        super(srcId, 0, HEADER, data); // destId = 0 le serveur
    }

    public PresentezVousMessage(String data){
        super(HEADER, data);
        destId = 0;
    }
}
