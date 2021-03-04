package utils.keycode;

import javafx.scene.input.KeyEvent;

import java.util.Arrays;
import java.util.List;

public class DodoKeyCodeCombinationAlternatives implements DodoKey {
    private final List<DodoKey> alternatives;

    public DodoKeyCodeCombinationAlternatives(DodoKey... combinations){
        alternatives = Arrays.asList(combinations);
    }

    @Override
    public boolean match(KeyEvent e){
        return alternatives.stream().anyMatch(c -> c.match(e));
    }
}
