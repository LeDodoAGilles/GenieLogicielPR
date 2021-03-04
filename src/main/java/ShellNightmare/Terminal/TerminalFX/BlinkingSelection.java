package ShellNightmare.Terminal.TerminalFX;

import javafx.scene.paint.Color;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.Selection;
import org.fxmisc.richtext.SelectionImpl;

import java.util.ArrayList;

public class BlinkingSelection extends SelectionImpl<DodoStyle, String, DodoStyle> {
    private static int nextId = 0;

    public final DodoStyle originalStyle;
    public final DodoStyle hiddenStyle;

    public BlinkingSelection(GenericStyledArea<DodoStyle, String, DodoStyle> area, DodoStyle style) {
        super("blinking selection " + (nextId++), area,
                path -> {
                    path.setHighlightFill(Color.rgb(0,0,0,0)); // rend la sélection invisible
                });

        this.originalStyle = style;



        if(style.reverse.isPresent() && style.reverse.get()){
            if(style.dim.isPresent()) // car sinon le texte ne prends pas la couleur de fond mais une version assombrie de celle-ci
                style = style.setDim(false);
            this.hiddenStyle = style.setBackground(style.getForeground().get()); // /!\ reverse donc foreground <-> background
        }
        else {
            this.hiddenStyle = style.setForeground(style.getBackground().get());
        }
    }
}
