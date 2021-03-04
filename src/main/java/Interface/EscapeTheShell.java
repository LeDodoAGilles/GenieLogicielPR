package Interface;

import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.TerminalFX.ManMode;
import ShellNightmare.Terminal.TerminalFX.Terminal;
import ShellNightmare.Terminal.TerminalFX.audio.AudioRegister;
import ShellNightmare.Terminal.TerminalFX.color.FontRegister;
import ShellNightmare.Terminal.TerminalFX.nano.NanoMode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import utils.keycode.KeyCodeWatcher;

/** L'Application qui a été développée.
 *
 * L'application utilise les toutes dernières nouveautés apportées par JavaFX et Java 15 .
 *
 * L'application est composée de plusieurs fenêtres : une fenêtre principale dans laquelle se déroule la majorité des
 * interactions, et des fenêtres secondaires (appellées dialogs) réservées à des traitements spécifiques.
 *
 * La fenêtre principale est composée de plusieurs {@link GUI}s (Graphical User Interface)
 * et la navigation entre ceux-cis est assurée par le {@link Framework}.
 *
 * Équipe de développement :
 * @author Arthur : Game Design
 * @author Gaëtan : Système de fichiers, Interpréteur / Commandes / Bash, tests Unitaire
 * @author Louri  : Interface, Terminal, Nano, Monitoring
 * @author Marc   : Interface, Visuels
 * @author Samuel : Game Design
 * */
public class EscapeTheShell extends Application {
    /** Chemin de l'icône de l'application dans le jar. */
    private static final String ICON = "/Images/icon.png";

    /** Titre de la fenêtre */
    public static final String TITLE = "Escape the Shell";

    /** Si la fenêtre principale peut être redimensionnée. */
    private static final boolean RESIZEABLE = true;

    /** Couleur de fond des fenêtres de l'application. */
    private static final Paint BACKGROUND = Color.BEIGE;

    /** Police de caractères par défaut du {@link Terminal}.
     * Chargée dans le stylesheet d'initialisation main.css */
    private static final String DEFAULT_TERMINAL_FONT = "Consolas";

    /** Crée la Scene et ses composants, puis montre le Stage principal (ie. affiche la fenêtre principale).
     * Initialise aussi le reste de l'application.
     *
     * @param primaryStage le Stage correspondant à la fenêtre */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chargement de la configuration de l'application
        EscapeTheShellConfig config = new EscapeTheShellConfig();
        config.load(EscapeTheShellConfig.CONFIG);

        boolean fullscreen = (boolean) config.getValue(EscapeTheShellConfig.FULLSCREEN).get();
        int width = (int) config.getValue(EscapeTheShellConfig.WIDTH).get();
        int height = (int) config.getValue(EscapeTheShellConfig.HEIGHT).get();

        // Initialisation des ressources
        NanoMode.Init(getClass().getResource(NanoMode.NANO_HELP_RESOURCE));
        ManMode.Init(getClass().getResource(ManMode.MAN_HELP_RESOURCE));
        FontRegister.INSTANCE.set(0, DEFAULT_TERMINAL_FONT, null);

        // Données communes aux Terminaux
        MetaContext.Init();

        // Gestionnaire de navigation des GUIs
        Framework framework = new Framework(primaryStage, ICON, TITLE, fullscreen, width, height, RESIZEABLE, BACKGROUND, config);

        // Contrôle plus poussé des raccourcis clavier
        KeyCodeWatcher.WATCHER.init(primaryStage.getScene()); // après framework car la Scene doit être définie

        // Charge le menu principal
        framework.next(MainGUI.FXML);

        // Traitements à faire avant de quitter l'application (croix rouge, alt+F4, ...)
        // Libération des ressources, des fichiers temporaires créés ...
        Platform.setImplicitExit(false); // enlève le comportement par défaut de fermeture
        primaryStage.setOnCloseRequest(e -> {
            AudioRegister.INSTANCE.removeAll();
            FontRegister.INSTANCE.removeAll();
            System.exit(0); // quitte l'application toute entière
        });

        // Affiche la fenêtre (et ainsi le menu principal de l'application)
        primaryStage.show();
    }

    /** Lance l'application. Ce n'est pas le vrai point d'entrée du programme,
     * à cause de Maven & JavaFX qui demandent à ce qu'il soit dans une autre classe.
     *
     * La méthode <code>start</code> est appelée automatiquement après l'appel à <code>launch</code>. */
    public static void main(String[] args) {
        launch(args);
    }
}
