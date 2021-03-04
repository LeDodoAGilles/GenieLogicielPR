package Monitoring.message;

import Monitoring.Message;
import Monitoring.MessageHeader;
import ShellNightmare.Terminal.challenge.Score;

public class ScoreMessage extends Message<Score> {
    private static final MessageHeader HEADER = MessageHeader.SCORE;

    public ScoreMessage(Score data) {
        super(-1, 0, HEADER, data);
    }
}
