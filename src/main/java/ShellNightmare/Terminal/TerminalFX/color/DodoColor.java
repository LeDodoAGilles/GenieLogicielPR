package ShellNightmare.Terminal.TerminalFX.color;

import javafx.scene.paint.Color;
import utils.CssToColorHelper;

import java.util.Objects;

/** Encapsule une "couleur" pouvant être un nom de variable css ou une couleur JavaFX.
 * Un tel objet est immuable, un nouveau sera créé à chaque modification.
 * Des méthodes utilitaires sont aussi mises à disposition, notamment dim et toString. */
public class DodoColor {
    // name et color sont exclusifs : si l'un est initialisé, l'autre est null.
    private final String name;
    private final Color color;

    /** Encapsule un nom de variable css.
     * @param name le nom de variable css */
    public DodoColor(String name){
        this.name = name;
        this.color = null;
    }

    /** Encapsule une couleur JavaFX.
     * @param color la couleur JavaFX */
    public DodoColor(Color color){
        this.name = null;
        this.color = color;
    }

    public Color getFXColor(){
        if(name != null)
            return CssToColorHelper.getNamedColor(name);

        if(color != null)
            return color;

        throw new AssertionError("Couleur non initialisée."); // Ne devrait pas arriver.
    }

    /** Assombrit la couleur.
     * Si la couleur est un nom de variable css, sa valeur sera récupérée.
     * Dans tous les cas, la nouvelle couleur encapsulera une couleur JavaFX.
     * Note: notamment utilisé pour le code couleur SGR dim/faint.
     * @return une couleur plus sombre */
    public DodoColor dim(){
        if(name != null)
            return new DodoColor(CssToColorHelper.getNamedColor(name).darker());

        if(color != null)
            return new DodoColor(color.darker());

        throw new AssertionError("Couleur non initialisée."); // Ne devrait pas arriver.
    }

    /** Renvoie un string interprétable comme une valeur css.
     * Si la couleur est un nom de variable css, ce nom sera retourné.
     * Si c'est une couleur JavaFX, le string "rgb(R,G,B)" sera retourné, où R G B désignent ses composantes. */
    @Override
    public String toString(){
        if(name != null)
            return name;

        if(color != null){
            int r = (int) (255 * color.getRed());
            int g = (int) (255 * color.getGreen());
            int b = (int) (255 * color.getBlue());
            return String.format("rgb(%d,%d,%d)", r, g, b);
        }

        throw new AssertionError("Couleur non initialisée."); // Ne devrait pas arriver.
    }

    // Objet immuable : on override hashCode et equals

    @Override
    public int hashCode() {
        return Objects.hash(name, color);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof DodoColor) {
            DodoColor that = (DodoColor) o;
            return Objects.equals(this.name,  that.name) &&
                   Objects.equals(this.color, that.color);
        } else {
            return false;
        }
    }
}
