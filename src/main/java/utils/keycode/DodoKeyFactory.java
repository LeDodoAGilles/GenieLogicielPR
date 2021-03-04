package utils.keycode;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

/* Events travel from the scene to the focused node (event capturing) and then back to the scene (event bubbling).
Event filter are triggered during event capturing, while onKeyPressed and event handler are triggered during event bubbling. */

public class DodoKeyFactory {
    /** Pour éliminer les combinaisons Ctrl+ , Shift+, Alt+ , ... */
    public static DodoKeyCodeCombination keycode(KeyCode code){
        return new DodoKeyCodeCombination(new KeyCodeCombination(code));
    }

    public static DodoKeyCodeCombination ctrl(KeyCode code){
        return new DodoKeyCodeCombination(new KeyCodeCombination(code, KeyCodeCombination.CONTROL_DOWN));
    }

    public static DodoKeyCodeCombination shift(KeyCode code){
        return new DodoKeyCodeCombination(new KeyCodeCombination(code, KeyCodeCombination.SHIFT_DOWN));
    }

    public static DodoKeyCodeCombination ctrl_shift(KeyCode code){
        return new DodoKeyCodeCombination(new KeyCodeCombination(code, KeyCodeCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
    }

    public static DodoKeyCodeCombination alt(KeyCode code){
        return new DodoKeyCodeCombination(new KeyCodeCombination(code, KeyCodeCombination.ALT_DOWN));
    }

    public static DodoKeyCodeCombination meta(KeyCode code){
        return new DodoKeyCodeCombination(new KeyCodeCombination(code, KeyCodeCombination.META_DOWN));
    }

    public static DodoMultiKeyCodeCombination escape(KeyCodeCombination combi){
        return new DodoMultiKeyCodeCombination(combi, KeyCode.ESCAPE);
    }

    public static DodoMultiKeyCodeCombination escape(KeyCode code){
        return escape(new KeyCodeCombination(code));
    }

    public static DodoKeyCodeCombinationAlternatives either(DodoKey... combinations){
        return new DodoKeyCodeCombinationAlternatives(combinations);
    }
}
