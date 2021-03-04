package ShellNightmare.Terminal.TerminalFX;

import ShellNightmare.Terminal.DaemonMessage;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.TerminalFX.color.SGR;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ManMode extends TermMode {
    private static final KeyCodeCombination keyG = new KeyCodeCombination(KeyCode.G);
    private static final KeyCodeCombination keyH = new KeyCodeCombination(KeyCode.H);
    private static final KeyCodeCombination ctrlQ = new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN);

    private static final String HELP = "This is the help of the man mode."; // TODO
    private static final String HELP_FOOTER = "HELP -- Press RETURN for more, or ^q when done";
    private static final String HELP_END_FOOTER = "HELP -- END -- Press g to see it again, or ^q when done";

    private static final String FOOTER_PATTERN = "Manual page %s(%d) line %d (press h for help or ^q to quit)";
    private static final String FOOTER_END_PATTERN = "Manual page %s(%d) line %d (END) (press h for help or ^q to quit)";
    private static final String FOOTER_PERCENT_PATTERN = "Manual page %s(%d) line %d/%d %d%% (press h for help or ^q to quit)";

    public static final String MAN_HELP_RESOURCE = "/Texts/man_help.txt";
    private static String DEFAULT_MAN_HELP = "No help found.";

    private final String cmd; // la commande
    private final int section; // la section du man de la commande (1 commandes utilisateurs, 2 appels système, 3 bibliothèques ...)
    private final String content; // le man de la commande

    private boolean helpShowing = false;
    private int nbLines = 0;

    public ManMode(Terminal terminal, String cmd, int section, String content) {
        super(terminal);

        this.cmd = cmd;
        this.section = section;
        this.content = content;

        DodoTextArea body = terminal.getBody();
        body.setEditable(false);
        body.getStyleClass().add("man-terminal");
        body.print(content);
        body.moveTo(0);
        body.requestFollowCaret();

        terminal.addFooter();
        terminal.getFooter().setEditable(false);

        addManFooterListener();
        Platform.runLater(() -> updateManFooter(null, 0, 1)); // runLater() au cas où eof est déjà atteint
    }

    public static void Init(URL helpSrc){
        StringBuilder bs = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(helpSrc.openStream(), StandardCharsets.UTF_8))){
            String line;
            while((line = reader.readLine()) != null){
                bs.append(line);
                bs.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        DEFAULT_MAN_HELP = bs.toString();
    }

    private void addManFooterListener(){
        terminal.getBody().firstVisibleLineNumberProperty().addListener(this::updateManFooter);
        terminal.getBody().lastVisibleLineNumberProperty().addListener(this::updateManFooter);
    }

    private void removeManFooterListener(){
        terminal.getBody().firstVisibleLineNumberProperty().removeListener(this::updateManFooter);
        terminal.getBody().lastVisibleLineNumberProperty().removeListener(this::updateManFooter);
    }

    private void updateManFooter(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue){
        int firstLine = terminal.getBody().firstVisibleLineNumberProperty().get();
        terminal.getFooter().clear();
        if(terminal.getBody().eofReachedProperty().get()){
            terminal.getFooter().print(SGR.reverse(String.format(FOOTER_END_PATTERN, cmd, section, firstLine)));
            if(nbLines == 0){
                nbLines = terminal.getBody().linesCount.getValue();
            }
        }
        else if(nbLines != 0){
            int percent = (int) (100.0 * terminal.getBody().lastVisibleLineNumberProperty().get() / nbLines);
            terminal.getFooter().print(SGR.reverse(String.format(FOOTER_PERCENT_PATTERN, cmd, section, firstLine, nbLines, percent)));
        }
        else {
            terminal.getFooter().print(SGR.reverse(String.format(FOOTER_PATTERN, cmd, section, firstLine)));
        }
    }

    private void updateHelpFooter(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue){
        terminal.getFooter().clear();
        if(newValue){
            terminal.getFooter().print(SGR.reverse(HELP_END_FOOTER));
        }
        else {
            terminal.getFooter().print(SGR.reverse(HELP_FOOTER));
        }
    }

    @Override
    public boolean filterTypedKey(KeyEvent e, TermArea a) {
        return false;
    }

    @Override
    public boolean filterPressedKey(KeyEvent e, TermArea a) {
        return false;
    }

    @Override
    public boolean onKeyPressed(KeyEvent e, TermArea a) {
        if(keyG.match(e)){ // retour au début
            terminal.getBody().moveTo(0);
        }
        else if(keyH.match(e)){ // affiche l'aide
            if(!helpShowing){
                removeManFooterListener();
                terminal.pushBody(); // crée une nouvelle zone
                DodoTextArea body = terminal.getBody();
                body.setEditable(false);
                body.getStyleClass().add("man-terminal");
                body.print(HELP);

                body.eofReachedProperty().addListener(this::updateHelpFooter);
                Platform.runLater(() -> updateHelpFooter(null, false, body.eofReachedProperty().get()));

                helpShowing = true;
                return true;
            }
        }
        else if(ctrlQ.match(e)){ // cache l'aide ou sort du man
            if(helpShowing){
                terminal.getBody().eofReachedProperty().removeListener(this::updateHelpFooter);
                terminal.popBody();
                addManFooterListener();
                helpShowing = false;
                Platform.runLater(() -> {
                    updateManFooter(null, 0, 1);
                });
            }
            else {
                removeManFooterListener();
                MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(DaemonMessage.MAN_EXIT));
                terminal.enterCmdMode();
            }

            return true;
        }

        return false;
    }
}
