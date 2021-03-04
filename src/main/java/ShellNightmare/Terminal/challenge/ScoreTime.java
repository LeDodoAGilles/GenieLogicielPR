package ShellNightmare.Terminal.challenge;

import java.io.Serializable;
import java.time.Duration;

// afin d'avoir un affichage correct dans tableview
public class ScoreTime implements Serializable {
    private static final long serialVersionUID = 5222231711155L;

    public Duration time;

    public ScoreTime(Duration time){
        this.time = time;
    }

    @Override
    public String toString(){
        return String.format("%d:%02d:%02d.%03d", time.toHours(), time.toMinutes()%60L, time.toSeconds()%60L, time.toMillis()%1000L);
    }
}
