package utils.keycode;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;

public class KeyCodeWatcher {
    public static final KeyCodeWatcher WATCHER = new KeyCodeWatcher();

    private final ArrayList<KeyCode> pressed = new ArrayList<>(); // not thread-safe

    private KeyCodeWatcher(){}

    public void init(Scene scene){
        scene.setOnKeyPressed(e -> pressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressed.remove(e.getCode()));
    }

    // not thread-safe, mais puisque seul le thread JavaFX l'utilise, ça va
    public boolean isPressed(KeyCode code){
        return pressed.contains(code);
    }
}
