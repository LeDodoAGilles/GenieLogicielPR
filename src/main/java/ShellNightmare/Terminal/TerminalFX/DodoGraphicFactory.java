package ShellNightmare.Terminal.TerminalFX;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.reactfx.value.Val;

import java.util.function.IntFunction;

// Reprend l'essentiel de org.fxmisc.richtext.LineNumberFactory

public class DodoGraphicFactory implements IntFunction<Node> {
    private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);
    private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
    private static final Font DEFAULT_FONT = Font.font("monospace", FontPosture.ITALIC, 13);
    private static final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.web("#ddd"), null, null));

    private final DodoTextArea area;

    private DodoGraphicFactory(DodoTextArea area){
        this.area = area;
    }

    public static DodoGraphicFactory get(DodoTextArea area){
        return new DodoGraphicFactory(area);
    }

    /** Formate l'affichage du numéro de ligne 'x' en ayant le nombre de lignes total 'max'. */
    private String format(int x, int max) {
        // nombre de chiffres du numéro de ligne le plus grand
        int digits = 1 + (int) Math.floor(Math.log10(max));
        // %% pour échapper le 2e %. n° de ligne avec padding à gauche encadré par un espace des deux côtés
        String pattern = String.format(" %%%dd ", digits);
        return String.format(pattern, x);
    }

    @Override
    public Node apply(int line) {
        // Formatage mis à jour à chaque changement du nombre de lignes
        // la numérotation des lignes commence à 1
        Val<String> formatted = area.linesCount.map(n -> format(line+1, n));

        Label lineNo = new Label();
        lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));

        lineNo.setFont(DEFAULT_FONT);
        lineNo.setBackground(DEFAULT_BACKGROUND);
        lineNo.setTextFill(DEFAULT_TEXT_FILL);
        lineNo.setPadding(DEFAULT_INSETS);
        lineNo.setAlignment(Pos.TOP_RIGHT);
        lineNo.getStyleClass().add("lineno");

        return lineNo;
    }
}
