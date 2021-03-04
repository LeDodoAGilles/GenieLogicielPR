package ShellNightmare.Terminal.TerminalFX;

import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.MetaContext;
import javafx.scene.input.*;
import utils.keycode.DodoKey;

import java.util.function.Supplier;

import static ShellNightmare.Terminal.DaemonMessage.COMMAND;
import static utils.keycode.DodoKeyFactory.*;

public class CmdMode extends TermMode {
    private static final DodoKey enter = keycode(KeyCode.ENTER);
    private static final DodoKey home = keycode(KeyCode.HOME);
    private static final DodoKey delete = keycode(KeyCode.DELETE);
    private static final DodoKey backspace = keycode(KeyCode.BACK_SPACE);
    private static final DodoKey ctrlBackspace = either(ctrl(KeyCode.BACK_SPACE), shift(KeyCode.BACK_SPACE));
    private static final DodoKey leftArrow = keycode(KeyCode.LEFT);
    private static final DodoKey downArrow = keycode(KeyCode.DOWN);
    private static final DodoKey rightArrow = keycode(KeyCode.RIGHT);
    private static final DodoKey upArrow = keycode(KeyCode.UP);
    private static final DodoKey ctrlA = ctrl(KeyCode.A);
    private static final DodoKey ctrlC = ctrl(KeyCode.C);
    private static final DodoKey ctrlShiftC = ctrl_shift(KeyCode.C);
    private static final DodoKey ctrlN = ctrl(KeyCode.N);
    private static final DodoKey ctrlP = ctrl(KeyCode.P);
    private static final DodoKey altR = alt(KeyCode.R);
    private static final DodoKey ctrlV = ctrl(KeyCode.V);
    private static final DodoKey ctrlShiftV = ctrl_shift(KeyCode.V);
    private static final DodoKey ctrlX = ctrl(KeyCode.X);
    private static final DodoKey ctrlZ = ctrl(KeyCode.Z);

    // true : ajoutera ^C et passera à la ligne suivante lors d'un Ctrl+C.
    // false : effacera la commande rentrée. Moins fidèle mais plus propre.
    public static final boolean SHOW_CTRL_C = false;

    // Historique des commandes.
    // Normalement stocké dans ~/.bash_history donc il faudrait peut-être le stocker dans User, mais le faire ici
    // permet de ne pas sauvegarder les commandes entre les challenges (cela permet d'éviter de rusher un challenge
    // déjà fait et d'obtenir un meilleur score au temps).
    private final CommandHistory commandHistory;
    private CommandHistory.PseudoListIterator<String> histIt; // Sauvegard de l'itérateur quand on commence à parcourir l'historique des commandes.

    public Boolean readMode = false;
    /** Entrée dans un challenge. Affiche le message de bienvenue et les instructions. */
    public CmdMode(Terminal terminal){
        super(terminal);

        commandHistory = new CommandHistory(MetaContext.mainDaemon.c.history);

        DodoTextArea body = terminal.getBody();
    }

    public void waitNewCommand(){
        waitNewCommand(false);

    }

    public void waitNewCommand(Boolean force){
        if (!(terminal.mode instanceof CmdMode)) // dû aux threads
            return;

        if (MetaContext.win)
            return;

        var body = terminal.getBody();

        if (!force && body.getLength()==body.getEditableLimit())
            return;

        // TODO rendre le pattern customizable
        body.print("(" +MetaContext.mainDaemon.c.currentUser.name+"  "+ MetaContext.mainDaemon.c.currentPath.getFile().getName()+")>>", DodoStyle.makeSingleClassStyle("input-prefix"));
        body.print(" ");
        body.markEditableLimit();
        histIt = null;

        terminal.waitingUserCommand.set(true);
    }

    // TODO text listener : get le dernier char en mode read

    @Override
    public boolean filterTypedKey(KeyEvent e, TermArea a) {
        return false;
    }

    @Override
    public boolean filterPressedKey(KeyEvent e, TermArea a) {
        if(a != TermArea.BODY) return false; // Le mode des commandes ne porte pas sur l'header ou le footer.

        DodoTextArea body = terminal.getBody();
        TermSelection selection = body.getSelectionInfo();

        if(ctrlA.match(e)) { // Empêche de tout sélectionner, mais seulement la partie éditable.
            body.selectEditable();
            e.consume();
            return true;
        }
        else if(ctrlC.match(e)){ // Empêche le comportement de 'copie' par Ctrl+C
            if(SHOW_CTRL_C){ // Pour être fidèle, mais c'est moins propre.
                body.print("^C\n");
                waitNewCommand();
            }
            else {
                body.clearEditable();
            }
            e.consume(); // Obligatoire pour éviter de copier
            return false; // Le terminal doit pouvoir unfreeze avec Ctrl+C.
        }
        else if(ctrlShiftC.match(e)){ // Copie la sélection dans le presse-papier.
            body.copy();
            e.consume();
            return true;
        }
        else if(ctrlV.match(e)){ // Empêche de coller avec Ctrl+V
            e.consume();
            return true;
        }
        else if(ctrlShiftV.match(e)){ // Empêche de coller si dans la zone non-éditable
            if(selection.isEditable()){
                body.paste();
            }
            e.consume();
            return true;
        }
        else if(ctrlX.match(e)){ // Empêche de couper si dans la zone non-éditable
            if(selection.isEditable()){
                body.cut();
            }
            e.consume();
            return true;
        }
        else if(ctrlZ.match(e)){ // Empêche le undo
            e.consume();
            return true;
        }
        else if(ctrlBackspace.match(e)){ // effacemet bizarre : on enlève tout court
            e.consume();
        }
        else if(backspace.match(e)){ // Empêche l'effacement dans la partie non-éditable (sélection y compris).
            if(!selection.isEmpty()){
                if(selection.start < selection.limit){
                    int n = body.getLength();
                    if(Terminal.MOVE_ON_READONLY_EDITION){
                        body.moveTo(n);
                        body.selectRange(n, n); // enlève la sélection
                        if(n == selection.limit){
                            e.consume();
                        }
                    }
                    else {
                        if(selection.end >= selection.limit){
                            body.moveTo(selection.end);
                            body.selectRange(selection.end, selection.end);
                        }
                        else {
                            body.moveTo(n);
                            body.selectRange(n, n); // enlève la sélection
                        }
                        e.consume();
                    }
                    return true;
                }
            }
            else if(selection.caret < selection.limit){ // Pas de sélection et curseur dans la zone read-only.
                if(Terminal.MOVE_ON_READONLY_EDITION && body.getLength() > selection.limit){
                    body.moveTo(body.getLength());
                }
                else {
                    body.moveTo(selection.limit);
                    e.consume();
                }
                return true;
            }
            else if(selection.caret == selection.limit){ // Pas de sélection et curseur en limite de la zone read-only.
                e.consume();
                return true;
            }
        }
        else if(delete.match(e)){ // Empêche l'effacement (à droite) dans la partie non-éditable (sélection y compris).
            if(!selection.isEmpty()){
                if(selection.start < selection.limit){
                    if(Terminal.MOVE_ON_READONLY_EDITION){
                        body.moveTo(selection.limit);
                        body.selectRange(selection.limit, selection.limit); // enlève la sélection
                    }
                    else {
                        if(selection.end >= selection.limit){
                            body.moveTo(selection.end);
                            body.selectRange(selection.end, selection.end); // enlève la sélection
                        }
                        else {
                            body.moveTo(selection.limit);
                            body.selectRange(selection.limit, selection.limit);
                        }
                        e.consume();
                    }
                }
            }
            else if(selection.caret < selection.limit){ // Pas de sélection et curseur dans la zone read-only.
                if(Terminal.MOVE_ON_READONLY_EDITION && body.getLength() > selection.limit){
                    body.moveTo(selection.limit);
                }
                else {
                    body.moveTo(selection.limit);
                    e.consume();
                }
            }
            //else if(caret == readonlyLimit) : pas besoin car supprime à droite
        }
        else if(home.match(e)){ // Empêche de revenir au début de la ligne, mais au début de la partie éditable
            body.moveTo(selection.limit);
            e.consume();
            return true;
        }
        else if(enter.match(e)){
            // Peu importe où est le curseur, on le déplace à la fin afin d'insérer un saut de ligne automatique.
            body.moveTo(body.getLength());
            // ne pas consommer e car il devra être utilisé dans onKeyPressed()
            return true;
        }
        else if(leftArrow.match(e)){ // Limite le déplacement gauche à la zone de commande.
            if(selection.start <= selection.limit){
                body.moveTo(selection.limit);
                e.consume();
                return true;
            }
        }
        else if(rightArrow.match(e)){ // Limite le déplacement droit à la zone de commande.
            // sélection non vide : fin dans/limitrophe à la partie non-éditable
            // sélection vide : curseur strictement dans la partie non-éditable
            if((!selection.isEmpty() && selection.end <= selection.limit) || (selection.isEmpty() && selection.caret < selection.limit)){
                body.moveTo(selection.limit);
                e.consume();
                return true;
            }
        }
        else if(upArrow.match(e) || ctrlP.match(e)){ // Remplace l'entrée par la précédente commande de l'historique.
            if(histIt == null) histIt = commandHistory.iterator();

            if(histIt.hasPrevious()){
                body.clearEditable();
                body.print(histIt.previous(),DodoStyle.EMPTY);
            }
            e.consume();
            return true;
        }
        else if(downArrow.match(e) || ctrlN.match(e)){ // Remplace l'entrée par la prochaine commande de l'historique.
            if(histIt != null && histIt.hasNext()){
                body.clearEditable();
                body.print(histIt.next());
            }
            else
                body.clearEditable();
            e.consume();
            return true;
        }
        else if(altR.match(e)){ // Si l'entrée est une commande modifiée de l'historique, annule les modifications faites.
            if(histIt != null){
                body.clearEditable();
                body.print(histIt.peek());
            }
        }

        return false;
    }

    @Override
    public boolean onKeyPressed(KeyEvent e, TermArea a) {
        if(a != TermArea.BODY) return false; // Le mode des commandes ne porte pas sur l'header ou le footer.
        if(enter.match(e)){
            String cmd = terminal.getBody().getEditableText();
            cmd = cmd.substring(0, cmd.length()-1); // enlève le tout dernier '\n'
            if (readMode){
                cmd=cmd.substring(cmd.lastIndexOf("\n")+1);
                MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(COMMAND,cmd));
                readMode=false; // TODO mettre ça avant au cas où il y a à nouveau un read mode ? pas possible normalement
                return true;
            }

            if(!cmd.isBlank()){
                commandHistory.add(cmd);
                MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(COMMAND,cmd));
                return true;
            }
            else {
                waitNewCommand();
            }
        }

        return false;
    }
}
