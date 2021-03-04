package Monitoring.message;

import Monitoring.Message;
import Monitoring.MessageHeader;
import ShellNightmare.Terminal.challenge.Challenge;

public class DownloadMessage extends Message<Challenge> {
    private static final MessageHeader HEADER = MessageHeader.DOWNLOAD;

    public DownloadMessage(Challenge data) {
        super(0, -1, HEADER, data); // destiné a être broadcast
    }
}
