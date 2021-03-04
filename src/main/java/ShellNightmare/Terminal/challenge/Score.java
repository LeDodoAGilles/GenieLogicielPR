package ShellNightmare.Terminal.challenge;

import java.io.Serializable;
import java.time.Duration;

public class Score implements Serializable {
    private static final long serialVersionUID = 5246981711155L;

    public String username;
    public ScoreTime time;

    public Score(String username, long elapsedNanos){
        this.username = username;
        time = new ScoreTime(Duration.ofNanos(elapsedNanos));
    }

    // getters et setter necessaires pour le property factory de tableview
    public String getUsername(){
        return username;
    }

    public ScoreTime getTime(){
        return time;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setTime(ScoreTime time){
        this.time = time;
    }

    @Override
    public String toString(){
        return String.format("USER=%s | TIME=%s", username, time);
    }
}
