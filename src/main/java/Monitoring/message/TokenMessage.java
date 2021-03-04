package Monitoring.message;

import Monitoring.Message;
import Monitoring.MessageHeader;

public class TokenMessage extends Message<Integer> {
    private static final MessageHeader HEADER = MessageHeader.TOKEN;

    public TokenMessage(int data) {
        super(0, data, HEADER, data);
    }
}
