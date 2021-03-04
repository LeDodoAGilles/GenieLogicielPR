package ShellNightmare.Terminal.TerminalFX.audio;

import javafx.scene.media.MediaPlayer;

public class AudioElement {
    public MediaPlayer player;
    public java.io.File tempFile;

    public AudioElement(MediaPlayer player, java.io.File tempFile){
        this.player = player;
        this.tempFile = tempFile;
    }

    public void dispose(){
        player.stop();
        player.dispose();
        tempFile.delete();
    }
}
