package Interface;

import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.Daemon;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.FileSystem.User;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.TerminalFX.Terminal;
import ShellNightmare.Terminal.TerminalFX.audio.AudioRegister;
import ShellNightmare.Terminal.challenge.Challenge;
import ShellNightmare.Terminal.interpreter.ExplicitCommandInterpreter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.commons.codec.digest.DigestUtils;
import utils.Clonage;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static ShellNightmare.Terminal.DaemonMessage.ESCAPED;
import static ShellNightmare.Terminal.MetaContext.mainDaemon;
import static ShellNightmare.Terminal.challenge.ChallengeHeader.SHAPER_USERNAME;

/** GUI de l'interface de jeu, permettant de suivre un {@link Challenge}.
 *
 * @author Marc
 * @author Louri
 * @author Gaëtan Lounes */
public class GameGUI extends GUI {
    public static final String FXML = "/FXML/Game.fxml";
    private static final String HTML_HELP = "help/index.html";

    public static final String ERROR_INFO_STYLE = "-fx-fill: red;";

    @FXML
    private BorderPane terminalPane;
    @FXML
    private TextArea notepad;
    @FXML
    private ScrollPane infosScrollPane;
    @FXML
    private TextFlow infos;
    @FXML
    private TextField passwordTF;
    @FXML
    private Label timeLabel;

    private Challenge challenge;    // challenge joué

    private Terminal terminal;

    private ArrayList<Timeline> infosTimelines = new ArrayList<>(); // timelines gérant le temps d'affichage des infos
    private Text frozenInfo = null; // text qui affiche que le terminal est gelé (null s'il ne l'est pas)

    private Timeline timerTimeline; // incrémente automatiquement le compteur elapsedSeconds
    private int elapsedSeconds = 0; // utilisé pour l'affichage

    private Daemon previousDaemon = null;

    @Override
    public void reset(Object[] args) {}

    @Override
    public void init(Object[] args) {
        challenge = (Challenge) args[0];
        MetaContext.win=false;

        if(challenge.context.currentUser.name.isEmpty())
            challenge.context.currentUser.name = MetaContext.username; // avant le clone car c'est pas un "deep" clone

        System.out.println(challenge.context.currentUser.name);

        Context c = challenge.context;


        String password = challenge.header.generatePassword().getKey();
        challenge.header.hashedRootPassword = DigestUtils.sha512Hex(password);
        c.rootUser.hash = challenge.header.hashedRootPassword;


        Optional<User> ou = c.getUser(SHAPER_USERNAME);
        if(ou.isEmpty()){
            Framework.showError("Challenge corrompu", "L'Architecte / le Shaper n'existe pas.");
            framework.previous();
            return;
        }

        User Shaper = ou.get();
        User playerUser = c.currentUser;
        c.currentUser=Shaper;
        ExplicitCommandInterpreter eci = new ExplicitCommandInterpreter(c);

        c.setEnvar("__password", password);
        var sa =Utils.stringSplitter(password,(challenge.header.numberCutKey==0)?1:challenge.header.numberCutKey);
        int k=0;
        for (String s: sa){
            c.setEnvar(String.format("__passwordPart%d",k++),s);
        }

        eci.processCommand("cd","/");
        eci.processCommand("bash","scripts/*");
        eci.processCommand("bash","initScript.sh");
        eci.processCommand("cd","home");
        c.currentPath.u = playerUser;
        c.currentUser = playerUser;

        previousDaemon = mainDaemon; // sauvegarde pour la validation de l'éditeur
        terminal = new Terminal(false);
        mainDaemon.setData(terminal, c);
        terminal.init();


        terminalPane.setCenter(terminal);

        // affichera automatiquement une info si le terminal est gelé
        terminal.isFrozen.addListener((e, o, becameFrozen) -> {
            if(becameFrozen){
                if(frozenInfo == null)
                    frozenInfo = showInfo("Le terminal est gelé ! Faites Ctrl+Q pour le dégeler.\n", "-fx-fill: red;", Duration.INDEFINITE);
            }
            else if(frozenInfo != null){
                infos.getChildren().remove(frozenInfo);
                frozenInfo = null;
            }
        });

        // Repère automatiquement que le joueur a gagné (se base sur l'ajout du score)
        // /!\ suppose que le score ne sera changé qu'une seule fois : si le joueur gagne
        MetaContext.scoreProp.addListener((e, o, newScore) -> {
            win();
            challenge.scores.add(newScore);

            if(challenge.filename != null)
                challenge.updateScoresOnDisk();
        });

        terminal.getBody().requestFocus();

        // Initialise le timer (ne sert qu'à l'affichage, c'est le terminal qui détermine le temps précis)
        timerTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), // incrément toutes les secondes
                        actionEvent -> {
                            elapsedSeconds += 1;
                            int seconds = elapsedSeconds % 60;
                            int minutes = (elapsedSeconds / 60) % 60;
                            int hours = elapsedSeconds / 3600;
                            timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                        }
                )
        );
        timerTimeline.setCycleCount(Animation.INDEFINITE); // boucle à l'infini
        timerTimeline.play(); // lance le timer


    }

    @Override
    public void dispose(){
        timerTimeline.stop();
        infosTimelines.forEach(Timeline::stop);
        infosTimelines.clear();

        mainDaemon = previousDaemon;

        // TODO dispose terminal
    }

    /** Affiche un message dans l'encadré des informations.
     * À ce message est attribué un style css (principalement la couleur du texte)
     * et une durée d'apparition au bout de laquelle il sera effacé.
     * Pour afficher un message permanent, utiliser <code>Duration.INDEFINITE</code> .
     *
     * @param msg      l'information à afficher
     * @param style    le style css à appliquer à l'information
     * @param lifetime durée d'apparition de l'information
     * @return         l'élément qui affiche l'information, à des fins de traitements ultérieurs */
    public Text showInfo(String msg, String style, Duration lifetime){
        Text text = new Text();
        text.setStyle(style);
        text.setText(msg);
        infos.getChildren().add(text);

        // scroll automatiquement pour s'assurer de voir l'ajout
        infos.layout();
        infosScrollPane.layout();
        infosScrollPane.setVvalue(1.0);

        // TODO dispose ?
        Timeline timeline = new Timeline(new KeyFrame(lifetime, actionEvent -> infos.getChildren().remove(text)));
        timeline.play();

        infosTimelines.add(timeline);
        timeline.setOnFinished(event -> infosTimelines.remove(timeline));

        return text;
    }

    /** Vérifie si la touche Entrée a été appuyée dans le textfield du mot de passe.
     * Si oui, alors a le même effet que de cliquer sur le bouton Unlock.
     *
     * @param event l'événement à vérifier */
    @FXML
    private void onPasswordTFKeyPressed(KeyEvent event){ // touche entrée pour le password
        if(event.getCode().equals(KeyCode.ENTER)){
            event.consume();
            unlock();
        }
    }

    /** Teste le mot de passe rentré dans le textfield.
     * Appelé lors de l'appui sur le bouton Unlock.
     * Contrairement à l'exécution d'une commande dans le terminal, ne passe pas le joueur en mode Root. */
    @FXML
    private void unlock(){
        // vérifie le mot de passe rentré, et fait que le joueur "s'échappe" s'il s'agit du bon
        if (challenge.context.rootUser.hash.equals(DigestUtils.sha512Hex(passwordTF.getText())))
            MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(ESCAPED));
        else
            showInfo("Le mot de passe est incorrect.\n", ERROR_INFO_STYLE, Duration.seconds(5));
    }

    /** Appelé lorsque le joueur a gagné.
     * Arrête le timer d'affichage.
     * Ajoute l'information que le joueur a gagné dans l'encadré des informations.
     * Valide le challenge : le joueur a réussi à le terminer, donc c'est possible. */
    public void win(){
        timerTimeline.stop();
        showInfo("Vous avez trouvé le bon mot de passe !\n", "-fx-fill: green;", Duration.INDEFINITE);

        if(challenge.filename != null && !challenge.header.validated){
            challenge.header.validated = true;
            challenge.updateHeaderOnDisk(); // sauvegarde aussi le mot de passe Root généré pour cette session
        }
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

    /** Revient au menu précédent.
     * Appelé lors de l'appui sur le bouton Quitter . */
    @FXML
    private void selectReturn(){
        AudioRegister.INSTANCE.removeAll();
        framework.previous();
    }
}
