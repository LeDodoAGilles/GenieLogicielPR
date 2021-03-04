package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.*;
import ShellNightmare.Terminal.FileSystem.BinaryData;
import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.PseudoPath;
import ShellNightmare.Terminal.TerminalFX.audio.AudioRegister;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Manipulation de la musique stockée dans le filesystem du context
 *
 * Il doit nécessairement y avoir au moins une option
 * music --load path name : charge (sans jouer) la musique du fichier à path, l'enregistre avec name
 * music --load path --play [--loop] name : charge et joue la musique (en bouclant ou non)
 * music --load path [--loop] name : charge (sans jouer) la musique (en bouclant ou non)
 * music --play name : reprend la lecture là où on l'avait stoppée
 * music --loop name : jouer en boucle
 * music --pause name : arrêter de jouer, le prochain reprendra à cet endroit
 * music --stop name : arrêter de jouer, le prochain play reprendra au début
 * */

/**
 * commande voir man pour le détail
 * @author Louri Noël
 */
@Permission
@NeededParameters(value = "1")
public class Music extends Command {
    boolean load = false;
    String filename = null;
    String key = null;
    boolean play = false;
    boolean loop = false;
    boolean pause = false;
    boolean stop = false;

    @Override
    public void processCommand() {
        if(pause && stop){
            stderr.add("pause et stop sont mutuellement exclusifs.");
            return;
        }

        if(pause && play){
            stderr.add("pause et play sont mutuellement exclusifs.");
            return;
        }

        if(load && pause){
            stderr.add("load et pause sont mutuellement exclusifs.");
            return;
        }

        if(load && stop){
            stderr.add("load et stop sont mutuellement exclusifs.");
            return;
        }

        if(!(load || play || loop || pause || stop)){
            stderr.add("Au moins une option doit être utilisée.");
            return;
        }

        key = neededParameters.get(0);

        if(filename != null){
            PseudoPath p = new PseudoPath(context.currentPath);
            if (!addErrorMessages(p.setPseudoPath(filename)) && p.isFileExist()){
                Platform.runLater(() -> processInRightThread(p));
            }
        }
        else {
            Platform.runLater(() -> processInRightThread(null));
        }
    }

    private void processInRightThread(PseudoPath p){
        // stop et pause à traiter en premier
        if(stop)
            AudioRegister.INSTANCE.get(key).ifPresent(MediaPlayer::stop);
        if(pause)
            AudioRegister.INSTANCE.get(key).ifPresent(MediaPlayer::pause);

        // puis le load
        if(load){
            if(p == null){
                System.err.println("fichier binaire introuvable");
                return;
            }

            File<BinaryData> binaryFile = File.ConvertFile(p.getChildFile(), BinaryData.class);

            java.io.File temp;
            try {
                temp = Files.createTempFile("EscapeTheShell", "").toFile();

                try(OutputStream outputStream = new FileOutputStream(temp)){
                    binaryFile.getInodeData().writeToStream(outputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Media media = new Media(temp.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);
            AudioRegister.INSTANCE.add(key, player, temp);
        }

        if(AudioRegister.INSTANCE.get(key).isPresent()){
            MediaPlayer player = AudioRegister.INSTANCE.get(key).get();

            // et fin loop et play
            if(loop){
                player.setStartTime(Duration.ZERO);
                player.setStopTime(player.getTotalDuration());
                player.setCycleCount(MediaPlayer.INDEFINITE);
            }

            if(play){
                player.play();
            }
        }
    }

    public void LONGOPT_l_load(String filename){
        load = true;
        this.filename = filename;
    }

    public void LONGOPT_r_play(){play = true;}

    public void LONGOPT_o_loop(){loop = true;}

    public void LONGOPT_p_pause(){pause = true;}

    public void LONGOPT_x_stop(){stop = true;}
}
