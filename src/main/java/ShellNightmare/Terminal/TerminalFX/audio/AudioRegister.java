package ShellNightmare.Terminal.TerminalFX.audio;

import javafx.scene.media.MediaPlayer;

import java.util.HashMap;
import java.util.Optional;

// Pour les fichiers audios stockés dans les challenge
// ATTENTION : supprime automatiquement le fichier temporaire associé crée sur le disque

public class AudioRegister {
    public static final AudioRegister INSTANCE = new AudioRegister();

    private final HashMap<String, AudioElement> register = new HashMap<>();

    private AudioRegister(){}

    public void add(String name, MediaPlayer player, java.io.File tempFile){
        if(register.containsKey(name)){
            register.get(name).dispose();
        }
        register.put(name, new AudioElement(player, tempFile));
    }

    // peut retourner null
    public Optional<MediaPlayer> get(String name){
        AudioElement e = register.get(name);
        return e != null ? Optional.of(register.get(name).player) : Optional.empty();
    }

    public void remove(String name){
        if(register.containsKey(name)){
            register.get(name).dispose();
            register.remove(name);
        }
    }

    public void removeAll(){
        register.forEach((name, element) -> element.dispose());
        register.clear();
    }
}
