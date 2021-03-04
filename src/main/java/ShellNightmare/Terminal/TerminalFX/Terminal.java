package ShellNightmare.Terminal.TerminalFX;

import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.TerminalFX.nano.NanoConfig;
import ShellNightmare.Terminal.TerminalFX.nano.NanoFooter;
import ShellNightmare.Terminal.TerminalFX.nano.NanoHeader;
import ShellNightmare.Terminal.TerminalFX.nano.NanoMode;
import ShellNightmare.Terminal.challenge.Score;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import utils.CssToColorHelper;
import utils.keycode.DodoKey;
import utils.snapshot.Snapshot;
import utils.snapshot.SnapshotCanvas;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static utils.keycode.DodoKeyFactory.*;

// TODO raccourcis mac

// TODO complex keyboard shortcuts https://stackoverflow.com/questions/29064225/how-to-create-a-javafx-keycombination-with-three-or-more-keys

// TODO touche 'inser'

// area.addCaret(new CaretNode("test caret", area, 2)); extends CaretNode et l'élargir, ou idem qu'avec blinking : new selection

/** Simule un terminal unix. */
public class Terminal extends StackPane {
    private static final DodoKey ctrlC = ctrl(KeyCode.C);
    private static final DodoKey ctrlQ = ctrl(KeyCode.Q);
    private static final DodoKey ctrlS = ctrl(KeyCode.S);

    /* Message de bienvenue à afficher à chaque début de challenge. */
    // Generated with  http://patorjk.com/software/taag/#p=display&f=Doh&t=Type%20Something%20
    private static final String WELCOME =
                    " __        __   _                          \n" +
                    " \\ \\      / /__| | ___ ___  _ __ ___   ___ \n" +
                    "  \\ \\ /\\ / / _ \\ |/ __/ _ \\| '_ ` _ \\ / _ \\\n" +
                    "   \\ V  V /  __/ | (_| (_) | | | | | |  __/\n" +
                    "    \\_/\\_/ \\___|_|\\___\\___/|_| |_| |_|\\___|\n\n";

    private static final String GAMEOVER =
                    "   _____                         ____                 \n" +
                    "  / ____|                       / __ \\                \n" +
                    " | |  __  __ _ _ __ ___   ___  | |  | |_   _____ _ __ \n" +
                    " | | |_ |/ _` | '_ ` _ \\ / _ \\ | |  | \\ \\ / / _ \\ '__|\n" +
                    " | |__| | (_| | | | | | |  __/ | |__| |\\ V /  __/ |   \n" +
                    "  \\_____|\\__,_|_| |_| |_|\\___|  \\____/  \\_/ \\___|_|   \n";


    // Comportement à adopter lorsque l'utilisateur tente de modifier la zone read-only.
    // true : replace le curseur à la fin de la zone de commande et y apporte la modification.
    // false : consomme la modification, la faisant disparaître.
    public static final boolean MOVE_ON_READONLY_EDITION = true;

    public  CmdMode cmdMode; // sauvegardé à part
    protected TermMode mode;

    private final BorderPane areaPane;

    // Composants dérivés de JavaFX.
    private DodoTextArea header;
    private DodoTextArea body; // Zone principale
    private DodoTextArea footer;

    private NanoHeader nanoHeader;
    private NanoFooter nanoFooter;

    private ArrayList<DodoTextArea> savedBodies = new ArrayList<>(); // sauvegarde de 'body' lors du changement de mode

    public BooleanProperty isFrozen = new SimpleBooleanProperty(false); // Si le terminal est gelé (freeze)

    public final boolean debug;

    public long startTime;

    public final BooleanProperty waitingUserCommand; // utilisé dans l'éditeur


    public Terminal(boolean debug){
        // TODO si debug==true alors ne pas suivre le challenge, mais l'éditer
        this.debug = debug;

        getChildren().add(CssToColorHelper.HELPER); // pour déterminer la valeur des couleurs css nommées, utilisé pour le code SGR 2 dim

        waitingUserCommand = new SimpleBooleanProperty(false);
        waitingUserCommand.addListener((e, o, isWaiting) -> {
            if(isWaiting)
                Platform.runLater(() -> waitingUserCommand.set(false)); // reset automatique, juste le temps d'informer les autres listeners
        });

        areaPane = new BorderPane();
        getChildren().add(areaPane);

        pushBody(); // crée le body
    }

    public void init(){
        cmdMode = new CmdMode(this);
        mode = cmdMode;
        body.print(WELCOME);
        body.print("lancer le script pour démarrer le challenge: bash /home/launch \n");
        cmdMode.waitNewCommand(true);
        startTime = System.nanoTime();
        MetaContext.win=false;
    }

    public Terminal(){
        this(false);
    }

    public DodoTextArea getArea(TermArea a){
        switch(a){
            case HEADER: return header;
            case BODY: return body;
            case FOOTER: return footer;
            default: return null;
        }
    }

    public DodoTextArea getHeader(){
        return header;
    }

    public DodoTextArea getBody(){
        return body;
    }

    public DodoTextArea getFooter(){
        return footer;
    }

    public boolean hasHeader(){
        return header != null;
    }

    public boolean hasFooter(){
        return footer != null;
    }

    /** Crée un nouveau body et sauvegarde l'ancien. */
    public void pushBody(){
        if (body != null) {
            savedBodies.add(body);
        }

        // Création d'un nouveau body
        body = new DodoTextArea();

        // Utiliser le style par défaut à l'insertion au lieu du style précédent.
        // TODO à désactiver temporairement pour les codes couleur (?)

        body.setWrapText(true); // temps pis si pas fidèle, VirtualFlow en est incapable. (wrap au mot et pas au caractère)

        body.addEventFilter(KeyEvent.KEY_TYPED, e -> filterTypedKey(e, TermArea.BODY));
        body.addEventFilter(KeyEvent.KEY_PRESSED, e -> filterPressedKey(e, TermArea.BODY));
        body.setOnKeyPressed(e -> onKeyPressed(e, TermArea.BODY));

        body.deactivateDragAndDrop();

        areaPane.setCenter(body); // On remplace

        body.requestFocus();
    }

    /** Remplace le body actuel par le moins ancien. */
    public void popBody(){
        if(savedBodies.isEmpty()) return;

        int lastIndex = savedBodies.size()-1;
        body.clear();
        body = savedBodies.get(lastIndex);
        savedBodies.remove(lastIndex);
        areaPane.setCenter(body); // On remplace

        body.requestFocus();
    }

    /** Ajoute un header ou un footer au terminal. Cela ne change pas la taille du terminal. */
    private void addHeaderOrFooter(DodoTextArea area, TermArea a, Consumer<Node> add){
        area.setBackground(new Background(new BackgroundFill(getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY)));
        area.deactivateDragAndDrop();
        area.useMinimumHeight(); // resize automatique du header/footer selon la hauteur de toutes les lignes.

        area.addEventFilter(KeyEvent.KEY_TYPED, e -> filterTypedKey(e, a));
        area.addEventFilter(KeyEvent.KEY_PRESSED, e -> filterPressedKey(e, a));
        area.setOnKeyPressed(e -> onKeyPressed(e, a));

        add.accept(area);
    }

    public void addHeader(){
        if(!hasHeader()){
            header = new DodoTextArea();
            addHeaderOrFooter(header, TermArea.HEADER, areaPane::setTop);
        }
    }

    public void addFooter(){
        if(!hasFooter()){
            footer = new DodoTextArea();
            addHeaderOrFooter(footer, TermArea.FOOTER, areaPane::setBottom);
        }
    }

    public void removeHeader(){
        if(hasHeader()){
            header.clear();
            areaPane.setTop(null); // remove
            header = null;
        }
    }

    public void removeFooter(){
        if(hasFooter()){
            footer.clear();
            areaPane.setBottom(null); // remove
            footer = null;
        }
    }

    // Pour nano

    public void addNanoHeader(boolean emptyline){
        nanoHeader = new NanoHeader(emptyline);

        DodoTextArea area = nanoHeader.getListenedArea();
        if(area != null){
            area.addEventFilter(KeyEvent.KEY_TYPED, e -> filterTypedKey(e, TermArea.HEADER));
            area.addEventFilter(KeyEvent.KEY_PRESSED, e -> filterPressedKey(e, TermArea.HEADER));
            area.setOnKeyPressed(e -> onKeyPressed(e, TermArea.HEADER));
        }

        areaPane.setTop(nanoHeader);
    }

    public void removeNanoHeader(){
        if(nanoHeader == null) return;

        nanoHeader.clear();
        areaPane.setTop(null);
        nanoHeader = null;
    }

    public NanoHeader getNanoHeader(){
        return nanoHeader;
    }

    public void addNanoFooter(){
        nanoFooter = new NanoFooter(this);

        DodoTextArea area = nanoFooter.getListenedArea();
        area.addEventFilter(KeyEvent.KEY_TYPED, e -> filterTypedKey(e, TermArea.FOOTER));
        area.addEventFilter(KeyEvent.KEY_PRESSED, e -> filterPressedKey(e, TermArea.FOOTER));
        area.setOnKeyPressed(e -> onKeyPressed(e, TermArea.FOOTER));

        areaPane.setBottom(nanoFooter);
    }

    public void removeNanoFooter(){
        if(nanoFooter == null) return;

        nanoFooter.clear();
        areaPane.setBottom(null);
        nanoFooter = null;
    }

    public NanoFooter getNanoFooter(){
        return nanoFooter;
    }

    /** Donne l'impression que le terminal est gelé (toutes les modifications se font, y compris les clics souris, mais ne s'affichent pas).
     * Pour cela, une capture d'écran du terminal est placée au dessus afin de le cacher. */
    public void freeze(){
        if(isFrozen.get())
            return;

        // dimensions (faussées) du terminal, et donc que devra avoir la capture.
        Bounds bounds = areaPane.localToScreen(areaPane.getBoundsInLocal());
        double w = bounds.getWidth();
        double h = bounds.getHeight();

        Color color = getBackgroundColor(); // couleur de fond du terminal

        Image snapshot = Snapshot.capture(areaPane);
        SnapshotCanvas canvas = new SnapshotCanvas(w, h, snapshot, color);
        canvas.setMouseTransparent(true); // Les MouseEvents n'iront pas sur le canvas, mais sur les éléments en dessous.
        getChildren().add(canvas); // Ajoute le canvas avec la capture d'écran par dessus l'areaPane pour le cacher.
        isFrozen.set(true);
    }

    /** Dégêle le terminal.
     * Enlève le canvas avec la capture d'écran qui cache le terminal. */
    public void unfreeze(){
        if(!isFrozen.get())
            return;

        getChildren().remove(getChildren().size()-1); // Enlève le canvas qui cache l'area.
        isFrozen.set(false);
    }

    public void enterCmdMode(){
        popBody();

        removeHeader();
        removeFooter();
        removeNanoHeader();
        removeNanoFooter();

        mode = cmdMode;
        cmdMode.waitNewCommand(); // note: ne pas mettre dans le print du DodoTextArea
    }

    public void enterManMode(String name, int section, String content){
        pushBody();
        mode = new ManMode(this, name, section, content);
    }

    public void enterNanoMode(String name, String content, NanoConfig config){
        removeHeader();
        removeFooter();

        pushBody();

        mode = new NanoMode(this, name, content, config);
    }

    /** Empêche de modifier la zone non-editable en tapant un caractère imprimable. */
    private void filterTypedKey(KeyEvent e, TermArea a){
        DodoTextArea area = getArea(a);
        TermSelection selection = body.getSelectionInfo();

        if(!mode.filterTypedKey(e, a)){ // Si l'event n'a pas été utilisé
            if(e.isControlDown() || e.isShiftDown() || e.isAltDown()){} // Laisser passer si c'est une commande spéciale.
            else if(!selection.isEmpty() && !selection.isEditable()) {
                if(MOVE_ON_READONLY_EDITION){
                    area.moveTo(area.getLength());
                }
                else {
                    area.moveTo(selection.end);
                    e.consume();
                }
            }
            else if(selection.caret < selection.limit){ // pas de sélection et curseur strictement dans la zone read-only
                // Empêche l'édition de la zone read-only.
                if(MOVE_ON_READONLY_EDITION){
                    area.moveTo(area.getLength());
                }
                else {
                    e.consume();
                }
            }
        }
    }

    /** Empêche de modifier la zone non-editable en tapant un caractère non-imprimable. */
    private void filterPressedKey(KeyEvent e, TermArea a){
        if(!mode.filterPressedKey(e, a)){ // Si l'event n'a pas été utilisé
            if(ctrlC.match(e)){ // unfreeze
                unfreeze();
            }
        }
    }

    /** Gestion normale de l'appui des touches. */
    private void onKeyPressed(KeyEvent e, TermArea a){
        if(!mode.onKeyPressed(e, a)){ // Si l'event n'a pas été utilisé
            if(ctrlS.match(e)){ // freeze
                freeze();
            }
            else if(ctrlQ.match(e) || ctrlC.match(e)){ // unfreeze
                unfreeze();
            }
        }
    }

    public Color getBackgroundColor(){
        Paint paint = body.getBackground().getFills().get(0).getFill();
        if(paint instanceof Color){
            return (Color) paint;
        }
        else {
            System.err.println("Background color cannot be retrieved.");

            return Color.rgb(0,0,0);
        }
    }

    /*public void setBackgroundColor(Color color){
        var bkg = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));

        // TODO nano header/footer
        // TODO reverseStyle
        if(hasHeader()) header.setBackground(bkg);
        body.setBackground(bkg);
        if(hasFooter()) footer.setBackground(bkg);
    }*/

    // /!\ a n'utiliser qu'avec false
    public void setEditable(boolean state){
        if(state){
            removeEventFilter(KeyEvent.KEY_TYPED, KeyEvent::consume);
            removeEventFilter(KeyEvent.KEY_PRESSED, KeyEvent::consume);
            removeEventFilter(MouseEvent.ANY, MouseEvent::consume);
        }
        else {
            addEventFilter(KeyEvent.KEY_TYPED, KeyEvent::consume);
            addEventFilter(KeyEvent.KEY_PRESSED, KeyEvent::consume);
            addEventFilter(MouseEvent.ANY,MouseEvent::consume);
        }
    }

    // TODO
    public void win(){
        // Account.CURRENT.username car le challenge a pu mettre un nom custom pour l'utilisateur
        Score score = new Score(MetaContext.username, System.nanoTime() - startTime);
        MetaContext.scoreProp.set(score);
        body.print(GAMEOVER);
        body.print("Votre temps est " + score.time + "\n");
    }
}
