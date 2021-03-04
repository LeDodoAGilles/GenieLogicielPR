package ShellNightmare.Terminal.TerminalFX.color;

import java.util.ArrayList;
import java.util.Arrays;


/** Couleurs couramment supportées par les terminaux.
 * Elles sont déclarées dans l'ordre de leur numéro (de 0 à 15).
 * De plus, chaque couleur est associée à un nom de variable css qui devra être définie et chargée dans JavaFX.
 * Ce nom de variable peut être obtenu avec toString.*/
public enum Aixterm_Color {
    BLACK( "terminal-Black"),
    RED( "terminal-Red"),
    GREEN( "terminal-Green"),
    YELLOW("terminal-Yellow"),
    BLUE("terminal-Blue"),
    MAGENTA("terminal-Magenta"),
    CYAN("terminal-Cyan"),
    WHITE( "terminal-White"),
    BRIGHT_BLACK( "terminal-Bright-Black"),
    BRIGHT_RED("terminal-Bright-Red"),
    BRIGHT_GREEN( "terminal-Bright-Green"),
    BRIGHT_YELLOW("terminal-Bright-Yellow"),
    BRIGHT_BLUE( "terminal-Bright-Blue"),
    BRIGHT_MAGENTA( "terminal-Bright-Magenta"),
    BRIGHT_CYAN("terminal-Bright-Cyan"),
    BRIGHT_WHITE("terminal-Bright-White");

    // Copie statique afin de ne pas recopier le tableau à chaque fois que l'on veut faire SGR_Color.values()[index]
    public static final ArrayList<Aixterm_Color> ALL = new ArrayList<>(){{
        this.addAll(Arrays.asList(Aixterm_Color.values()));
    }};

    private final String cssName;

    Aixterm_Color(final String cssName){
        this.cssName = cssName;
    }

    @Override
    public String toString(){
        return cssName;
    }
}
