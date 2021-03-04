package utils.keycode;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import java.util.Arrays;
import java.util.List;

import static utils.keycode.KeyCodeWatcher.WATCHER;

public class DodoMultiKeyCodeCombination implements DodoKey {
    private final KeyCodeCombination combination;
    private final List<KeyCode> modifiers;

    public DodoMultiKeyCodeCombination(KeyCodeCombination c, KeyCode... modifiers){
        combination = c;
        this.modifiers = Arrays.asList(modifiers);
    }

    @Override
    public boolean match(KeyEvent e){
        return combination.match(e) && modifiers.stream().allMatch(WATCHER::isPressed);
    }
}
