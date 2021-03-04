package Interface;

import utils.config.BooleanConfigItem;
import utils.config.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Fichier de configuration de l'application EscapeTheShell. */
public class EscapeTheShellConfig extends Config {
    /** Chemin du fichier de configuration de l'application. */
    public static final String CONFIG = "settings.config";

    /* Nom des clés du fichier de configuration */
    public static final String FULLSCREEN = "fullscreen";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";

    public static final String PORT = "port";

    public static final String TERMINAL = "terminal";
    public static final String CARET = "caret";

    public static final String TERMINAL_FOREGROUND = "terminal-foreground";
    public static final String TERMINAL_BACKGROUND = "terminal-background";

    public static final String TERMINAL_BLACK = "terminal-Black";
    public static final String TERMINAL_RED = "terminal-Red";
    public static final String TERMINAL_GREEN = "terminal-Green";
    public static final String TERMINAL_YELLOW = "terminal-Yellow";
    public static final String TERMINAL_BLUE = "terminal-Blue";
    public static final String TERMINAL_MAGENTA = "terminal-Magenta";
    public static final String TERMINAL_CYAN = "terminal-Cyan";
    public static final String TERMINAL_WHITE = "terminal-White";
    public static final String TERMINAL_BRIGHT_BLACK = "terminal-Bright-Black";
    public static final String TERMINAL_BRIGHT_RED = "terminal-Bright-Red";
    public static final String TERMINAL_BRIGHT_GREEN = "terminal-Bright-Green";
    public static final String TERMINAL_BRIGHT_YELLOW = "terminal-Bright-Yellow";
    public static final String TERMINAL_BRIGHT_BLUE = "terminal-Bright-Blue";
    public static final String TERMINAL_BRIGHT_MAGENTA = "terminal-Bright-Magenta";
    public static final String TERMINAL_BRIGHT_CYAN = "terminal-Bright-Cyan";
    public static final String TERMINAL_BRIGHT_WHITE = "terminal-Bright-White";



    /* Valeurs par défaut */

    /** Si l'application doit être lancée en plein écran. */
    private static final boolean DEFAULT_FULLSCREEN = false;
    /** Largeur par défaut de la fenêtre principale. */
    private static final int DEFAULT_WIDTH = 1280;
    /** Hauteur par défaut de la fenêtre principale. */
    private static final int DEFAULT_HEIGHT = 600;
    /** Port par défaut à utiliser entre le serveur et le client. */
    private static final int DEFAULT_PORT = 42000;

    /** Liste des stylesheets prédéfinis pour le terminal. Le premier est celui utilisé par défaut. */
    public static final List<String> TERMINAL_STYLESHEETS = new ArrayList<>(Arrays.asList("/arch.css", "/ubuntu.css"));

    public static final String DEFAULT_CARET = "#fff";

    public static final String DEFAULT_TERMINAL_FOREGROUND = "#17a88b";
    public static final String DEFAULT_TERMINAL_BACKGROUND = "#000";

    public static final String DEFAULT_TERMINAL_BLACK = "#000";
    public static final String DEFAULT_TERMINAL_RED = "rgb(205,0,0)";
    public static final String DEFAULT_TERMINAL_GREEN = "rgb(0,205,0)";
    public static final String DEFAULT_TERMINAL_YELLOW = "rgb(205,205,0)";
    public static final String DEFAULT_TERMINAL_BLUE = "rgb(0,0,238)";
    public static final String DEFAULT_TERMINAL_MAGENTA = "rgb(205,0,205)";
    public static final String DEFAULT_TERMINAL_CYAN = "rgb(0,205,205)";
    public static final String DEFAULT_TERMINAL_WHITE = "rgb(229,229,229)";
    public static final String DEFAULT_TERMINAL_BRIGHT_BLACK = "rgb(127,127,127)";
    public static final String DEFAULT_TERMINAL_BRIGHT_RED = "#f00";
    public static final String DEFAULT_TERMINAL_BRIGHT_GREEN = "#0f0";
    public static final String DEFAULT_TERMINAL_BRIGHT_YELLOW = "#ff0";
    public static final String DEFAULT_TERMINAL_BRIGHT_BLUE = "rgb(92,92,255)";
    public static final String DEFAULT_TERMINAL_BRIGHT_MAGENTA = "#f0f";
    public static final String DEFAULT_TERMINAL_BRIGHT_CYAN = "#0ff";
    public static final String DEFAULT_TERMINAL_BRIGHT_WHITE = "#fff";

    /** Configure les paramètres par défaut de l'application EscapeTheShell. */
    public EscapeTheShellConfig(){
        addDefaultItem(new BooleanConfigItem(FULLSCREEN, DEFAULT_FULLSCREEN, "Si l'application doit être lancée en plein écran."));
        addDefaultItem(new IntegerConfigItem(WIDTH, DEFAULT_WIDTH, "Largeur de la fenêtre principale si n'est pas en plein écran."));
        addDefaultItem(new IntegerConfigItem(HEIGHT, DEFAULT_HEIGHT, "Hauteur de la fenêtre principale si n'est pas en plein écran."));
        addDefaultItem(new IntegerConfigItem(PORT, DEFAULT_PORT, "Port par défaut à utiliser entre le serveur et le client."));
        addDefaultItem(new EnumConfigItem(TERMINAL, TERMINAL_STYLESHEETS, TERMINAL_STYLESHEETS.get(0), "\nStyle de terminal sur lequel se baser.\nValeurs possibles : " + String.join(" ", TERMINAL_STYLESHEETS)));

        addDefaultItem(new ColorConfigItem(CARET, DEFAULT_CARET, "\nCouleur du curseur dans le terminal."));
        addDefaultItem(new ColorConfigItem(TERMINAL_FOREGROUND, DEFAULT_TERMINAL_FOREGROUND, "\n\nToutes les couleurs suivantes écrasent celles définies dans le style du terminal\nsi la valeur indiquée n'est pas none. Les couleurs possibles sont de la forme\nr,b,g\nfff (3 caractères en hexa)\naabbcc (6 caractères en hexa)\n\nCouleur du texte dans le terminal."));
        addDefaultItem(new ColorConfigItem(TERMINAL_BACKGROUND, DEFAULT_TERMINAL_BACKGROUND, "Couleur de fond du terminal."));
        addDefaultItem(new ColorConfigItem(TERMINAL_BLACK, DEFAULT_TERMINAL_BLACK, "\nLes couleurs suivantes sont utilisées pour afficher différentes choses."));
        addDefaultItem(new ColorConfigItem(TERMINAL_RED, DEFAULT_TERMINAL_RED, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_GREEN, DEFAULT_TERMINAL_GREEN, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_YELLOW, DEFAULT_TERMINAL_YELLOW, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_BLUE, DEFAULT_TERMINAL_BLUE, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_MAGENTA, DEFAULT_TERMINAL_MAGENTA, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_CYAN, DEFAULT_TERMINAL_CYAN, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_WHITE, DEFAULT_TERMINAL_WHITE, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_BRIGHT_BLACK, DEFAULT_TERMINAL_BRIGHT_BLACK, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_BRIGHT_RED, DEFAULT_TERMINAL_BRIGHT_RED, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_BRIGHT_GREEN, DEFAULT_TERMINAL_BRIGHT_GREEN, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_BRIGHT_YELLOW, DEFAULT_TERMINAL_BRIGHT_YELLOW, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_BRIGHT_BLUE, DEFAULT_TERMINAL_BRIGHT_BLUE, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_BRIGHT_MAGENTA, DEFAULT_TERMINAL_BRIGHT_MAGENTA, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_BRIGHT_CYAN, DEFAULT_TERMINAL_BRIGHT_CYAN, ""));
        addDefaultItem(new ColorConfigItem(TERMINAL_BRIGHT_WHITE, DEFAULT_TERMINAL_BRIGHT_WHITE, ""));
    }


}
