package Interface;

import ShellNightmare.Terminal.FileSystem.User;
import ShellNightmare.Terminal.MetaContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import utils.CssToColorHelper;

import java.io.IOException;
import java.util.Arrays;

import static ShellNightmare.Terminal.challenge.ChallengeHeader.SHAPER_USERNAME;

/** Menu principal.
 * Propose de naviguer vers le mode Solo, Multiplayer ou Editor.
 *
 * @author Marc
 * @author Louri */
public class MainGUI extends GUI {
    public static final String FXML = "/FXML/Main.fxml";
    private static final String HTML_HELP = "help/index.html";

    @Override
    public void reset(Object[] args) {
        // aucun traitement particulier lors d'un retour
    }

    @Override
    public void init(Object[] args) {
        framework.loadSettings();

        // Après avoir affiché la fenêtre, affiche par dessus le dialog de login.
        Platform.runLater(this::login);
    }

    @Override
    public void dispose(){}

    /** Affiche le dialog de login.
     * Demande à l'utilisateur de rentrer un pseudo.
     *
     * @author Louri
     * */
    private void login(){
        // pseudo aléatoire par défaut.
        String defaultUsername = String.format("Invité_%d", (int) (65536 * Math.random()));
        LoginPane dialog = new LoginPane(defaultUsername, Arrays.asList(User.ROOT_USERNAME, SHAPER_USERNAME));
        Stage stage = framework.makeDialog(dialog, "login");

        stage.showAndWait(); // Empêche les interactions avec les autres fenêtres de l'application.

        // Sauvegarde le pseudo retenu.
        MetaContext.username = dialog.getUsername();
    }

    /** Navigue vers le choix du challenge à jouer en solo.
     * Appelé lors de l'appui sur le bouton Soloplayer . */
    @FXML
    private void selectSoloplayer() throws IOException {
        framework.next(ChooserGUI.FXML, new Object[]{ChooserGUI.ChooserState.PLAY});
    }

    /** Navigue vers le mode multijoueur.
     * Appelé lors de l'appui sur le bouton Multiplayer . */
    @FXML
    private void selectMultiplayer() throws IOException {
        framework.next(MultiModeGUI.FXML, null);
    }

    /** Navigue vers le choix du challenge à éditer.
     * Appelé lors de l'appui sur le bouton Editor . */
    @FXML
    private void selectEditor() throws IOException {
        framework.next(ChooserGUI.FXML, new Object[]{ChooserGUI.ChooserState.EDIT});
    }

    /** Affiche l'aide de l'application.
     * Appelé lors de l'appui sur le bouton Information . */
    @FXML
    private void selectInfo(){
        framework.showInfo(HTML_HELP);
    }

    /** Affiche les options.
     * Appelé lors de l'appui sur le bouton Option . */
    @FXML
    private void selectOption() throws IOException {
        framework.showOption();
    }

    /** Quitte l'application.
     * Appelé lors de l'appui sur le bouton Quitter . */
    @FXML
    private void selectExit(){
        Platform.exit();
    }
}
