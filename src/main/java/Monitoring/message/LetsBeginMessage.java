package Monitoring.message;

import Monitoring.Message;
import Monitoring.MessageHeader;

public class LetsBeginMessage extends Message<Boolean> {
    private static final MessageHeader HEADER = MessageHeader.LETS_BEGIN;
    public LetsBeginMessage() {
        super(0, -1, HEADER, true);
    }
}
