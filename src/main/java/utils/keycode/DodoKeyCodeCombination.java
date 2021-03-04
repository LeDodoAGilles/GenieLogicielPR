package utils.keycode;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.Arrays;
import java.util.List;

import static utils.keycode.KeyCodeWatcher.WATCHER;

/** Permet d'ajouter d'autres modifiers à un @KeyCodeCombination */
public class DodoKeyCodeCombination implements DodoKey {
    private final KeyCodeCombination combination;

    public DodoKeyCodeCombination(KeyCodeCombination c){
        combination = c;
    }

    @Override
    public boolean match(KeyEvent e){
        return combination.match(e);
    }
}
