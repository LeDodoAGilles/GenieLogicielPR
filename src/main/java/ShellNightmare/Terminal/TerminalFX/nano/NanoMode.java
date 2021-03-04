package ShellNightmare.Terminal.TerminalFX.nano;

import ShellNightmare.Terminal.DaemonMessage;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.TerminalFX.*;
import ShellNightmare.Terminal.TerminalFX.color.SGR;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CaretNode;
import utils.keycode.DodoKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.keycode.DodoKeyFactory.*;

// https://www.nano-editor.org/dist/latest/cheatsheet.html
// https://phoenixnap.com/kb/use-nano-text-editor-commands-linux

// TODO unset mark quand caractère tapé
// TODO mark et selection souris
// TODO unset mark quand on passe à un autre mode (ctrl+o par exemple)

// TODO state : normal, help, ask filename ...

// TODO tab / shift tab

public class NanoMode extends TermMode {
    private static final DodoKey home = either(keycode(KeyCode.HOME)); // moveTo début de ligne
    private static final DodoKey enter = keycode(KeyCode.ENTER);
    private static final DodoKey delete = keycode(KeyCode.DELETE);
    private static final DodoKey backspace = keycode(KeyCode.BACK_SPACE);
    private static final DodoKey end = either(keycode(KeyCode.END), ctrl(KeyCode.E));
    private static final DodoKey tab = keycode(KeyCode.TAB); // indente, possiblement à remplacer par des espaces
    private static final DodoKey shifttab = shift(KeyCode.TAB); // dés-indente
    private static final DodoKey pageup = either(keycode(KeyCode.PAGE_UP), ctrl(KeyCode.Y)); // previous page
    private static final DodoKey pagedown = either(keycode(KeyCode.PAGE_DOWN), ctrl(KeyCode.V)); // next page
    private static final DodoKey escapeX = escape(KeyCode.X); // hide/show help footer
    private static final DodoKey meta6 = either(alt(KeyCode.DIGIT6), meta(KeyCode.DIGIT6), alt(KeyCode.NUMPAD6), meta(KeyCode.NUMPAD6));
    private static final DodoKey ctrlA = ctrl(KeyCode.A); // sélectionner tout, not legacy
    private static final DodoKey metaA = either(alt(KeyCode.A), meta(KeyCode.A)); // mark
    private static final DodoKey ctrlC = ctrl(KeyCode.C); // report cursor position, annuler
    private static final DodoKey metaE = either(alt(KeyCode.E), meta(KeyCode.E)); // redo
    private static final DodoKey ctrlG = either(ctrl(KeyCode.G), keycode(KeyCode.F1)); // help
    private static final DodoKey ctrlK = either(ctrl(KeyCode.K), keycode(KeyCode.F9)); // cut
    private static final DodoKey metaN = either(alt(KeyCode.N), meta(KeyCode.N)); // hide/show line numbers
    private static final DodoKey ctrlO = either(ctrl(KeyCode.O), keycode(KeyCode.F3)); // save
    private static final DodoKey ctrlQ = ctrl(KeyCode.Q); // empêche le unfreeze si preserve
    private static final DodoKey ctrlS = ctrl(KeyCode.S); // empêche le freeze si preserve
    private static final DodoKey ctrlU = either(ctrl(KeyCode.U), keycode(KeyCode.F10)); // paste
    private static final DodoKey metaU = either(alt(KeyCode.U), meta(KeyCode.U)); // undo
    private static final DodoKey ctrlX = either(ctrl(KeyCode.X), keycode(KeyCode.F2)); // exit (and sometimes also save)
    private int ctrlNPsavedIndex = -1; // offset pour la navigation up/down
    private int mark = -1; // index marqué

    // Obligé de redéfinir le comportement des codes des flèches aussi pour faire exactement la même chose, sinon y'a une erreur javafx incompréhensible qui arrive aléatoirement avec up/down
    private static final DodoKey arrowUp = either(keycode(KeyCode.UP), ctrl(KeyCode.P));
    private static final DodoKey arrowDown = either(keycode(KeyCode.DOWN), ctrl(KeyCode.N));
    private static final DodoKey arrowLeft = either(keycode(KeyCode.LEFT), ctrl(KeyCode.B));
    private static final DodoKey arrowRight = either(keycode(KeyCode.RIGHT), ctrl(KeyCode.F));

    private static final Pattern firstNonWhitespaceIndex = Pattern.compile("[^\\s]");

    public static final String NANO_HELP_RESOURCE = "/Texts/nano_help.txt";
    private static String DEFAULT_NANO_HELP = "No help found.";

    private NanoState state = NanoState.EDIT_FILE;
    private final NanoConfig config;
    private String filename;

    private final ReadOnlyBooleanWrapper textChanged;

    public NanoMode(Terminal terminal, String name, String contents, NanoConfig config){
        super(terminal);
        this.config = config;

        terminal.getBody().setWrapText(!config.nowrap);
        terminal.getBody().getStyleClass().add(".nano-terminal");
        terminal.getBody().print(contents, DodoStyle.EMPTY); //TODO: option dans nano

        // alt+6 est utilisé pour copier, donc on veut empêcher l'écriture du caractère unicode U+2660 correspondant sur windows
        terminal.getBody().addEventFilter(KeyEvent.KEY_TYPED, e -> { // DOIT être un keyTyped
            if(e.getCharacter().equals("\u2660")){ // caractère obtenu avec alt+6 sur windows
                e.consume();
            }
        });

        // TODO tabsize : créer autre stylesheet pour le mettre avec un id css sur ce body du terminal

        if(config.line <= 0) config.line = 1;
        if(config.column <= 0) config.column = 1;

        try {
            terminal.getBody().moveTo(config.line-1, config.column-1); // /!\ début à (1,1)
        } catch(IndexOutOfBoundsException ignored){
            // l'option +line,column indique une ligne ou une colonne invalide
            // exception ignorée : on va au début du fichier par défaut.
            terminal.getBody().moveTo(0, 0);
        }

        if(config.view) terminal.getBody().setEditable(false);
        if(config.linenumbers) terminal.getBody().showLineNumbers();

        terminal.addNanoHeader(config.emptyline);
        NanoHeader header = terminal.getNanoHeader();
        header.getLeft().print(SGR.reverse(" GNU Nano"));

        filename = name;
        writeHeader();
        header.getCenter().setParagraphStyle(0, DodoStyle.makeCssStyle("-fx-text-alignment: center;")); // devra être fait après chaque clear (ou alors astuce : replace)

        header.getRight().print(SGR.reverse("Modifié").toString() + SGR.reverse("x").hide().toString());
        header.getRight().setVisible(false); // tout simplement

        textChanged = new ReadOnlyBooleanWrapper();
        terminal.getBody().textProperty().addListener(this::listenBodyTextProp);
        textChanged.addListener(this::updateModifiedLabel);

        terminal.addNanoFooter();
        NanoFooter footer = terminal.getNanoFooter();
        if(name.isEmpty())
            footer.getTop().print(SGR.reverse("[ Nouveau Fichier ]"));
        else
            footer.getTop().print(SGR.reverse(String.format("[ Lecture de %d lignes ]", 1+contents.chars().filter(c -> c == '\n').count())));
        footer.getTop().setParagraphStyle(0, DodoStyle.makeCssStyle("-fx-text-alignment: center;"));

        if(!config.nohelp){
            // runLater pour bien gérer la couleur de fond. Ne peut pas être dans NanoFooter.addHelper car sinon flickering
            Platform.runLater(this::showHelpFooter);
        }
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

        DEFAULT_NANO_HELP = bs.toString();
    }

    private void writeHeader(){
        DodoTextArea title = terminal.getNanoHeader().getCenter();
        title.clear();
        if(filename.isEmpty())
            title.print(SGR.reverse("Nouvel Espace"));
        else
            title.print(SGR.reverse(String.format("Fichier: %s", filename)));
    }

    private void showHelpFooter(){
        NanoFooter footer = terminal.getNanoFooter();
        footer.addHelper("\\e[7m^G\\e[0m Aide\n\\e[7m^X\\e[0m Quitter");
        //footer.addHelper("\\e[7m^O\\e[0m Écrire\n\\e[7m^R\\e[0m Lire");
        footer.addHelper("\\e[7m^O\\e[0m Écrire\n");
        footer.addHelper("\\e[7mM-A\\e[0m Marquer\n\\e[7mM-6\\e[0m Copier");
        footer.addHelper("\\e[7m^K\\e[0m Couper\n\\e[7m^U\\e[0m Coller");
    }

    private void listenBodyTextProp(Observable observable) {
        // dummy call à getText afin de s'assurer que listenBodyTextProp est bien appelé à chaque modification de texte
        // Oui, les listeners sont bizarres
        terminal.getBody().getText();

        textChanged.set(true); // déclenche l'appel à updateModifiedLabel si valait false précédemment
    }

    private void updateModifiedLabel(ObservableValue<? extends Boolean> observableValue, boolean oldValue, boolean newValue){
        if(state != NanoState.EDIT_FILE)
            return;

        terminal.getNanoHeader().getRight().setVisible(newValue);
    }

    //Vérifie s'il y a déjà un fichier associé à nano à sauvegarder, sinon demande d'en créer un.
    private void prepareSave(boolean saveas){
        if(!textChanged.get()) // Si le texte n'a pas été modifié, alors ne rien faire.
            return;

        if(saveas){ // TODO || si fichier inexistant
            // TODO le demander
        }

        save();
    }

    private void save(){
        unmark();

        if(!config.preserve && config.backup){ // TODO && le fichier associé existe/existait/est non vide, un truc du genre
            // TODO backup du fichier non sauvegardé en rajoutant un tilde
        }

        String data = terminal.getBody().getText();

        // rajoute un \n à la fin s'il n'y en a pas, à moins que la config ne l'interdise
        if(!config.nonewlines && !data.isEmpty() && !data.endsWith("\n")){
            data += "\n";
            terminal.getBody().print("\n");
        }

        MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(DaemonMessage.NANO_SAVE,data));
        textChanged.set(false);
        // TODO attention : garde le style précédent (voulu : reverse+texte centré)
        terminal.getNanoFooter().getTop().replaceText(String.format("[ Écriture de %d lignes ]", terminal.getBody().getParagraphs().size()));
    }

    private void caretListener(Observable o, int old, int pos){
        if(mark != -1){
            terminal.getBody().selectRange(mark, pos); // 1er arg : anchor, 2nd arg : caret position
        }
    }

    private void unmark(){
        if(mark != -1){
            terminal.getBody().caretPositionProperty().removeListener(this::caretListener);
            mark = -1;
            terminal.getNanoFooter().removeTop();
        }
    }

    private void enterHelp(){
        state = NanoState.HELP;
        terminal.pushBody();
        DodoTextArea body = terminal.getBody();
        body.setEditable(false);
        body.getStyleClass().add(".nano-terminal");
        body.print(DEFAULT_NANO_HELP);
        body.moveTo(0);
        body.requestFollowCaret();
        body.requestFocus();

        terminal.getNanoHeader().getLeft().setVisible(false);
        terminal.getNanoHeader().getCenter().clear();
        terminal.getNanoHeader().getCenter().print(SGR.reverse("Aide de nano"));
        terminal.getNanoHeader().getRight().setVisible(false);

        NanoFooter footer = terminal.getNanoFooter();
        footer.removeTop();
        footer.removeHelpers();
        footer.addHelper("\\e[7m^X\\e[0m Fermer");

    }

    private void exitHelp(){
        state = NanoState.EDIT_FILE;
        terminal.popBody();
        terminal.getBody().requestFocus();

        terminal.getNanoHeader().getLeft().setVisible(true);
        writeHeader();
        terminal.getNanoHeader().getRight().setVisible(textChanged.getValue());


        NanoFooter footer = terminal.getNanoFooter();
        footer.getTop(); // le fait réapparaitre
        footer.removeHelpers();
        showHelpFooter();
    }

    @Override
    public boolean filterTypedKey(KeyEvent e, TermArea a) {
        return false;
    }

    // controles liés aux mouvements
    private boolean filterBodyMovementsPressedKey(KeyEvent e){
        DodoTextArea body = terminal.getBody();

        // /!\ rien ici, mettre les autres conditions après (à cause du reste de ctrlNPsavedIndex )

        if(arrowUp.match(e)){ // moveTo previous line
            int lineIndex = body.getCurrentParagraph();

            if(lineIndex == 0)
                return false; // pas de previous line

            if(ctrlNPsavedIndex == -1){ // on sauvegarde le vrai index
                ctrlNPsavedIndex = body.getCaretPosition() - body.getParagraphPosition(lineIndex);
            }
            int newCaretPositionInLine = Math.min(ctrlNPsavedIndex, body.getParagraphLength(lineIndex-1));

            body.moveTo(lineIndex-1, newCaretPositionInLine);
            body.requestFollowCaret();
            e.consume();
            return true;
        }
        else if(arrowDown.match(e)){ // moveTo next line
            int lineIndex = body.getCurrentParagraph();

            if(lineIndex == body.getParagraphs().size()-1)
                return false; // pas de next line

            if(ctrlNPsavedIndex == -1){ // on sauvegarde le vrai index
                ctrlNPsavedIndex = body.getCaretPosition() - body.getParagraphPosition(lineIndex);
            }
            int newCaretPositionInLine = Math.min(ctrlNPsavedIndex, body.getParagraphLength(lineIndex+1));

            body.moveTo(lineIndex+1, newCaretPositionInLine);
            body.requestFollowCaret();
            e.consume();
            return true;
        }
        else {
            ctrlNPsavedIndex = -1;
        }

        // smarthome : déplace le curseur au premier caractère non vide de la ligne, ou s'il y est déjà : au tout début de la ligne
        if(home.match(e)){
            if(config.smarthome && body.getSelection().getLength() == 0){
                int lineIndex = body.getCurrentParagraph();
                int linePosition = body.getParagraphPosition(lineIndex);
                int caretPosition = body.getCaretPosition();
                String line = body.getParagraph(lineIndex).getText();
                int firstNonWhitespacePositionInLine = 0;

                for(int i=0 ; i<line.length() ; i++){
                    if(!Character.isWhitespace(line.charAt(i))){
                        firstNonWhitespacePositionInLine = i;
                        break;
                    }
                }

                if(caretPosition == linePosition + firstNonWhitespacePositionInLine){
                    body.moveTo(lineIndex, 0); // si caret déjà au premier char non whitespace, alors moveTo début de ligne
                }
                else {
                    body.moveTo(lineIndex, firstNonWhitespacePositionInLine); // sinon, moveTo le premier char non whitespace
                }
            }
            else {
                body.moveTo(body.getCurrentParagraph(), 0);
                body.requestFollowCaret();
            }
            e.consume();
            return true;
        }
        else if(end.match(e)){ // fin de ligne
            int lineIndex = body.getCurrentParagraph();
            body.moveTo(lineIndex, body.getParagraphLength(lineIndex));
            body.requestFollowCaret();
            e.consume();
            return true;
        }
        else if(arrowLeft.match(e)){ // move back
            body.moveTo(Math.max(body.getCaretPosition()-1, 0));
            body.requestFollowCaret();
            e.consume();
            return true;
        }
        else if(arrowRight.match(e)){ // move forward
            body.moveTo(Math.min(body.getCaretPosition()+1, body.getLength()));
            body.requestFollowCaret();
            e.consume();
            return true;
        }
        else if(pageup.match(e)){ // previous page
            int offset = Math.max(body.getVisibleParagraphs().size()-2, 0);
            int newLine = Math.max(body.firstVisibleLineNumberProperty().get()-1 - offset, 0);
            body.moveTo(newLine, 0);
            body.requestFollowCaret();
            e.consume();
            return true;
        }
        else if(pagedown.match(e)){ // next page
            int offset = Math.max(body.getVisibleParagraphs().size()-2, 0);
            int newLine = Math.min(body.lastVisibleLineNumberProperty().get()-1 + offset, body.getParagraphs().size()-1);
            body.moveTo(newLine, 0);
            body.requestFollowCaret();
            /*Platform.runLater(() -> {
                body.moveTo(body.firstVisibleLineNumberProperty().get(), 0);
                body.requestFollowCaret();
            });*/
            e.consume();
            return true;
        }

        return false;
    }

    // controles qui doivent annuler le mode mark
    private boolean filterBodyUnmarkPressedKey(KeyEvent e){
        DodoTextArea body = terminal.getBody();

        if(tab.match(e) || shifttab.match(e)){
            String replacement = (config.tabtospaces) ? " ".repeat(config.tabsize) : "\t";

            IndexRange selection = body.getSelection();
            if(shifttab.match(e) || selection.getLength() != 0){
                // Sauvegarde du début et de la fin de la sélection, afin de la remettre
                CaretNode beginCaret = body.addExtraCaret();
                beginCaret.moveTo(selection.getStart());

                CaretNode endCaret = body.addExtraCaret();
                endCaret.moveTo(selection.getEnd());

                boolean caretAtBeginning = (body.getCaretPosition() == selection.getStart());

                // /!\ ordre inverse car insertion d'un string au fur et à mesure de la lecture des deux listes
                // liste des indexes des paragraphes sélectionnés (même partiellement)
                List<Integer> indexes = body.getSelectedParagraphIndexes();
                // liste des positions des paragraphes sélectionnés (même partiellement)
                List<Integer> positions = Arrays.asList(indexes.stream().map(body::getParagraphPosition).toArray(Integer[]::new));

                if(tab.match(e)){ // indentation
                    positions.forEach(i -> body.replaceText(i, i, replacement)); // ajout au début du paragraphe
                }
                else { // shift+tab : dés-indentation
                    for(int i=0 ; i<indexes.size() ; i++){
                        String line = body.getText(indexes.get(i));
                        Matcher matcher = firstNonWhitespaceIndex.matcher(line);
                        int end = line.length();
                        if(matcher.find())
                            end = matcher.start();

                        String prefix = line.substring(0, end); // copie tous les whitespace characters le précédant
                        int j = prefix.indexOf("\t");
                        if(j != -1){ // tabulation trouvée
                            body.deleteText(positions.get(i)+j, positions.get(i)+j+1);
                        }
                        else {
                            int len = (int) Math.min(prefix.chars().filter(c -> c == ' ').count(), config.tabsize);
                            body.replaceText(positions.get(i), positions.get(i)+len, "");
                        }
                    }
                }

                if(caretAtBeginning)
                    body.selectRange(endCaret.getPosition(), beginCaret.getPosition());
                else
                    body.selectRange(beginCaret.getPosition(), endCaret.getPosition());
                body.removeCaret(beginCaret);
                body.removeCaret(endCaret);
            }
            else {
                body.replaceSelection(replacement);
            }

            e.consume();
            return true;
        }
        else if(meta6.match(e)){ // copy
            body.copy();
            e.consume();
            return true;
        }
        else if(ctrlK.match(e)){ // cut
            System.out.println("cut");
            body.cut();
            e.consume();
            return true;
        }
        else if(ctrlU.match(e)){ // paste
            body.paste();
            e.consume();
            return true;
        }
        else if(metaU.match(e)){ // undo
            body.undo();
            e.consume();
            return true;
        }
        else if(metaE.match(e)){ // redo
            body.redo();
            e.consume();
            return true;
        }
        else if(backspace.match(e)){
            // ne pas consommer : comportement par défaut, juste spécifié pour unmark
            return true;
        }
        else if(delete.match(e)){
            // ne pas consommer : comportement par défaut, juste spécifié pour unmark
            return true;
        }

        return false;
    }

    private boolean filterBodyPressedKey(KeyEvent e){
        DodoTextArea body = terminal.getBody();

        if(filterBodyMovementsPressedKey(e)) // Si le caret a bougé
            return true;

        if(escapeX.match(e)) { // hide/show help
            config.nohelp = !config.nohelp;
            if(config.nohelp)
                terminal.getNanoFooter().removeHelpers();
            else
                showHelpFooter();
            e.consume();
            return true;
        }
        else if(metaN.match(e)){ // hide/show line numbers
            config.linenumbers = !config.linenumbers;
            if(config.linenumbers)
                body.showLineNumbers();
            else
                body.hideLineNumbers();
            e.consume();
            return true;
        }
        else if(metaA.match(e)){ // mark / unmark
            if(mark == -1){
                mark = body.getCaretPosition();

                // change la sélection pour toujours être entre mark et la position du caret
                body.caretPositionProperty().addListener(this::caretListener);

                // TODO attention : garde le style précédent (voulu : reverse+texte centré)
                terminal.getNanoFooter().getTop().replaceText("[ Marque Mise ]");
            }
            else {
                unmark();
            }
            e.consume();
            return true;
        }
        else if(ctrlX.match(e)){ // quitter
            if(config.saveonexit)
                prepareSave(false); // TODO le sinon

            body.caretPositionProperty().removeListener(this::caretListener); // ne fait rien si n'existe pas

            MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(DaemonMessage.NANO_EXIT));
            terminal.getBody().textProperty().removeListener(this::listenBodyTextProp);
            terminal.enterCmdMode();
            e.consume();
            return true;
        }
        else if(ctrlC.match(e)){ // report cursor position
            // TODO
            e.consume();
            return true;
        }
        else if(ctrlG.match(e)){ // help
            unmark();
            enterHelp();
            e.consume();
            return true;
        }
        else if(ctrlQ.match(e) && !config.preserve){
            e.consume();
            return true;
        }
        else if(ctrlS.match(e) && !config.preserve){
            prepareSave(false); // save
            e.consume();
            return true;
        }
        else if(ctrlO.match(e)){
            prepareSave(true);
            e.consume();
            return true;
        }
        else if(ctrlA.match(e)){
            terminal.getBody().selectAll();
            e.consume();
            return true;
        }

        boolean fil = filterBodyUnmarkPressedKey(e);
        if(fil || !e.getText().isEmpty()){
            unmark();
            return fil;
        }

        return false;
    }

    @Override
    public boolean filterPressedKey(KeyEvent e, TermArea a) {
        switch(state){
            case HELP:
                if(ctrlX.match(e)){ // exit help
                    exitHelp();
                    e.consume();
                    return true;
                }
                break;
            case EDIT_FILE:
                if(a == TermArea.BODY && filterBodyPressedKey(e))
                    return true;
                break;
        }

        return false;
    }

    @Override
    public boolean onKeyPressed(KeyEvent e, TermArea a) {
        switch(state){
            case HELP:
                break;
            case EDIT_FILE:
                if(enter.match(e) && a == TermArea.BODY && config.autoindent){
                    DodoTextArea body = terminal.getBody();
                    String previousLine = body.getText(body.getCurrentParagraph()-1);
                    Matcher matcher = firstNonWhitespaceIndex.matcher(previousLine);
                    if(matcher.find()){ // s'il existe un non-whitespace character
                        String indent = previousLine.substring(0, matcher.start()); // copie tous les whitespace characters le précédant
                        if(config.tabtospaces)
                            indent = indent.replace("\t", " ".repeat(config.tabsize));
                        body.print(indent);
                    }
                    // TODO ??? si la ligne précédente est vide, regarder celle encore au dessus ? puis celle au dessus à nouveau, et etc ?
                }
                break;
        }

        return false;
    }
}
