package Monitoring.message;

import Monitoring.Message;
import Monitoring.MessageHeader;

public class ReadyMessage extends Message<Boolean> {
    private static final MessageHeader HEADER = MessageHeader.READY;
    public ReadyMessage() {
        super(-1, 0, HEADER, true);
    }
}
