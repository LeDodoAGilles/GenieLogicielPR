package ShellNightmare.Terminal.TerminalFX;

import ShellNightmare.Terminal.TerminalFX.color.IllegalColorSequenceException;
import ShellNightmare.Terminal.TerminalFX.color.SGR;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.event.Event;
import javafx.scene.control.IndexRange;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.SimpleEditableStyledDocument;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ShellNightmare.Terminal.MetaContext.COLOR_CODE;

// TODO escape sequences https://misc.flogisoft.com/bash/tip_colors_and_formatting

/* Fusion de StyleClassedTextArea et InlineCssTextArea afin de pouvoir définir un style pour des classes css,
 * mais se donner aussi la possibilité de le faire directement sur une sélection, sans passer par les classes css.
 *
 * Ajoute aussi un header et un footer. */
public class DodoTextArea extends StyledTextArea<DodoStyle, DodoStyle> {
    private final ReadOnlyIntegerWrapper firstVisibleLineNumber; // numéro de la première ligne visible. Commence à 1.
    private final ReadOnlyIntegerWrapper lastVisibleLineNumber; // numéro de la dernière ligne visible
    public final Val<Integer> linesCount; // nombre total de lignes. Read-only
    private final ReadOnlyBooleanWrapper eofReached; // si la dernière ligne est visible

    private static final double minimumHeaderHeight = 2.0; // aussi pour Footer
    private static final double maximumHeaderHeight = 10*18.0; // aussi pour Footer
    private final ReadOnlyDoubleWrapper minimumHeightEstimate;


    private static final Pattern CSS_CLASS_CODE = Pattern.compile("\\\\c\\[\\d*m");
    private static final Pattern EXTRACT = Pattern.compile("\\d+"); // pour extraire des nombres

    public static final DodoStyle DEFAULT_STYLE = DodoStyle.EMPTY.setBackground("terminal-background").setForeground("terminal-foreground");
    public DodoStyle currentStyle = DEFAULT_STYLE;

    private static int nextCaretId = 1; // pour la création de carets supplémentaires

    // Position limite indiquant la fin de la zone non-éditable et le début de la zone éditable.
    // Seulement utile lorsque qu'une partie du TextArea doit être non-éditable.
    private int editableLimit = 0;

    private final Object blinkLock;
    private final ArrayList<BlinkingSelection> blinkSelections;
    private Timeline blinkingTimeline;

    private DodoTextArea(BiConsumer<TextFlow, DodoStyle> applyParagraphStyle,
                         BiConsumer<? super TextExt, DodoStyle> applyStyle) {

        // preserveStyle=true : notamment historique undo/redo illimité
        super(DodoStyle.EMPTY, applyParagraphStyle,
                DodoStyle.EMPTY, applyStyle,
                new SimpleEditableStyledDocument<>(DodoStyle.EMPTY, DodoStyle.EMPTY), true);

        this.getStyleClass().add("terminal");
        //this.setUseInitialStyleForInsertion(true); // TODO a remettre ?

        firstVisibleLineNumber = new ReadOnlyIntegerWrapper();
        lastVisibleLineNumber = new ReadOnlyIntegerWrapper();

        // Mise à jour automatique des propriétés de numéro de première et dernière ligne.
        getVisibleParagraphs().addModificationObserver((e) -> {
            try {
                // firstVisibleParToAllParIndex() est l'index du 1er "paragraphe" visible, et il y a un paragraphe par ligne.
                firstVisibleLineNumber.set(firstVisibleParToAllParIndex() + 1);
                lastVisibleLineNumber.set(lastVisibleParToAllParIndex() + 1); // analogue, pour la dernière ligne visible
            } catch(IllegalArgumentException ignore){} // arrive quand il n'existe pas encore de lignes.
        });

        linesCount = LiveList.sizeOf(getParagraphs()); // Met à jour automatiquement le nombre de lignes

        eofReached = new ReadOnlyBooleanWrapper();
        lastVisibleLineNumber.addListener((observable, oldValue, newValue) -> eofReached.set(newValue.intValue() == linesCount.getValue())); // Met à jour eofReached automatiquement

        minimumHeightEstimate = new ReadOnlyDoubleWrapper();
        // totalHeightEstimateProperty : cumul des hauteurs en pixels de chaque ligne
        totalHeightEstimateProperty().addListener((observable, oldValue, newValue) -> {
            // Pour éviter de se retrouver avec une hauteur de 0, ou trop grande pour un header/footer
            if(newValue != null && minimumHeaderHeight <= newValue && newValue <= maximumHeaderHeight) {
                minimumHeightEstimate.set(newValue);
            }
        });

        blinkLock = new Object();
        blinkSelections = new ArrayList<>();
    }

    public DodoTextArea() {
        this(
                (paragraph, dodoStyle) -> {
                    paragraph.getStyleClass().addAll(dodoStyle.classStyles);
                    paragraph.setStyle(dodoStyle.getCssStyle());
                    },
                (text, dodoStyle) -> {
                    text.getStyleClass().addAll(dodoStyle.classStyles);
                    text.setStyle(dodoStyle.getCssStyle());
                }
        );
    }

    private void createTimelineIfNotExists(){
        if(blinkingTimeline != null) return;

        blinkingTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> {
                    synchronized (blinkLock){
                        blinkSelections.forEach(sel -> {
                            IndexRange range = sel.getRange();
                            if(range.getStart() != range.getEnd()){
                                this.setStyle(range.getStart(), range.getEnd(), sel.hiddenStyle);
                            }
                        });
                    }
                }),
                new KeyFrame(Duration.seconds(1), e -> {
                    synchronized (blinkLock){
                        blinkSelections.forEach(sel -> {
                            IndexRange range = sel.getRange();
                            if(range.getStart() != range.getEnd()){
                                this.setStyle(range.getStart(), range.getEnd(), sel.originalStyle);
                            }
                        });
                    }
                }));

        blinkingTimeline.setCycleCount(Animation.INDEFINITE);
        blinkingTimeline.play();
    }

    public void deactivateDragAndDrop(){
        // Désactive le drag-and-drop, notamment pour les sélections.
        getChildrenUnmodifiable().get(0).setOnDragDetected(Event::consume);
        //area.setOnSelectionDropped(Event::consume); // spécifiquement pour la sélection
    }

    public void useMinimumHeight(){
        prefHeightProperty().bind(minimumHeightEstimate.getReadOnlyProperty()); // Utilise le minimum de hauteur possible, automatiquement mis à jour
    }

    public void useMinimumWidth(){
        prefWidthProperty().bind(totalWidthEstimateProperty());
    }

    public void showLineNumbers(){
        setParagraphGraphicFactory(DodoGraphicFactory.get(this));
    }

    public void hideLineNumbers(){
        setParagraphGraphicFactory(i -> null);
    }

    public CaretNode addExtraCaret(){
        CaretNode extraCaret = new CaretNode(String.format("DodoTextArea CaretNode %d", nextCaretId++), this);
        extraCaret.getStyleClass().remove("caret");
        extraCaret.setStrokeWidth(0.0); // Rend ce caret invisible.

        if (!addCaret(extraCaret)) {
            throw new RuntimeException("Impossible de créer un nouveau caret");
        }

        return extraCaret;
    }

    public int getParagraphPosition(int index){
        // Utilise un autre caret pour déterminer la position du 1er caractère de la ligne (en fait du paragraphe)
        CaretNode extraCaret = addExtraCaret();

        extraCaret.moveTo(index, 0);
        int pos = extraCaret.getPosition();
        removeCaret(extraCaret);

        return pos;
    }

    @Override
    public void clear(){
        super.clear();

        if(blinkingTimeline != null){
            blinkingTimeline.stop();
            blinkSelections.clear();
            blinkingTimeline = null;
        }

        markEditableLimit();
    }

    public void clearEditable(){
        deleteText(editableLimit, getLength());
    }

    /** Écrit à la fin en utilisant le style par défaut. Pas de retour à la ligne automatique.
     * Le style actuel est utilisé et sera modifié par les balises de couleur.
     * Renvoie l'intervalle de sélection afin de pouvoir y appliquer un style plus tard. */
    public IndexRange print(String s){
        int begin = getLength();
        Matcher matcherSequence = COLOR_CODE.matcher(s);
        int previousEnd = 0;
        int[] lastIndex = {0};
        while(matcherSequence.find()){
            lastIndex[0] = getLength();
            append(s.substring(previousEnd, matcherSequence.start()), currentStyle); // écrit la partie d'avant le match

            currentStyle.blink.ifPresent(b -> {
                if(b){
                    selectBlink(lastIndex[0], getLength());
                }
            });

            previousEnd = matcherSequence.end();

            String sequence = matcherSequence.group(); // le substring qui a matché
            Matcher numberMatcher = EXTRACT.matcher(sequence);
            if(!sequence.startsWith("\\e"))
                numberMatcher.find(); // consomme le chiffre du pattern x1B ou 033

            // Java 9
            Integer[] codes = numberMatcher.results().map(MatchResult::group).map(Integer::parseInt).toArray(Integer[]::new);
            try{
                currentStyle = SGR.modifyStyleFromSequence(currentStyle, DEFAULT_STYLE, codes);
            } catch(IllegalColorSequenceException ignored){
                ignored.printStackTrace();
            }
        }
        append(s.substring(previousEnd), currentStyle); // jusqu'à la fin

        currentStyle.blink.ifPresent(b -> {
            if(b){
                selectBlink(lastIndex[0], getLength());
            }
        });

        requestFollowCaret();

        int end = getLength();

        searchClassCodes(s, begin);
        return new IndexRange(begin, end);
    }

    // TODO
    private void searchClassCodes(String s, int begin){
        Matcher matcherClass = CSS_CLASS_CODE.matcher(s);
        boolean classEntered = false;
        int start = 0; // sauvegarde de la position du style (dans s, une fois la balise supprimée)
        int classcode = 0; // sauvegarde du code de class css
        while(matcherClass.find()){
            String sequence = matcherClass.group(); // le substring qui a matché
            Matcher codeMatcher = EXTRACT.matcher(sequence);
            codeMatcher.find(); // pour que group() marche
            int code = Integer.parseInt(codeMatcher.group());
            if(code == 127){
                if(classEntered){
                    // TODO apply code
                    setStyle(begin+start, begin+matcherClass.start(), DodoStyle.makeSingleClassStyle("ls-folder"));
                    classEntered = false;
                }
                else {
                    System.err.println("Warning : balises de classe css incorrectes ?");
                    continue; // ignored
                }
            }
            else if(classEntered){
                System.err.println("Warning : balises de classe css incorrectes ?");
                continue; // ignored
            }
            else {
                classcode = code;
                start = matcherClass.start();
                classEntered = true;
            }

            // on supprime la balise dans s et dans l'area
            s = s.substring(0, matcherClass.start()) + s.substring(matcherClass.end());
            deleteText(begin+matcherClass.start(), begin+matcherClass.end());
            matcherClass = CSS_CLASS_CODE.matcher(s);
        }
    }

    public IndexRange print(Object o){
        return print(o.toString());
    }

    /** Écrit à la fin en utilisant le style fourni. Pas de retour à la ligne automatique.
     * Renvoie l'intervalle de sélection afin de pouvoir y appliquer un style plus tard. */
    public IndexRange print(String s, DodoStyle style){
        int begin = getLength();

        append(s, style);
        style.blink.ifPresent(b -> {
            if(b){
                selectBlink(begin, getLength());
            }
        });

        requestFollowCaret();
        int end = getLength();
        return new IndexRange(begin, end);
    }

    public IndexRange print(Object o, DodoStyle style){
        return print(o.toString(), style);
    }

    @Override
    public void copy(){
        String selected = this.getSelectedText();
        if(selected.isEmpty())
            return;

        StringSelection selection = new StringSelection(selected);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    @Override
    public void cut(){
        String selected = this.getSelectedText();
        if(selected.isEmpty())
            return;

        this.replaceText(this.getSelection(), "");

        StringSelection selection = new StringSelection(selected);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        requestFollowCaret();
    }

    @Override
    public void paste(){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            String s = (String) clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor);
            this.replaceText(this.getSelection(), s);
            requestFollowCaret();
        } catch (UnsupportedFlavorException | IOException ignored) {}
    }

    private void makeSelectionBlink(int begin, int end, DodoStyle style){
        BlinkingSelection sel = new BlinkingSelection(this, style);
        if (!addSelection(sel)) {
            throw new IllegalStateException("Impossible d'ajouter la sélection à l'area.");
        }

        sel.selectRange(begin, end);
        sel.selectedTextProperty().addListener((e, o, n) -> {
            if(n.equals("")){ // si la sélection devient vide
                synchronized (blinkLock){
                    blinkSelections.remove(sel);
                    if(blinkSelections.isEmpty()){
                        blinkingTimeline.stop();
                        blinkingTimeline = null;
                    }
                }
            }
        });

        synchronized (blinkLock){
            blinkSelections.add(sel);
        }
    }

    public void selectBlink(int begin, int end){
        if(begin < 0 || end > getLength() || begin >= end)
            throw new IllegalArgumentException("sélection invalide");

        createTimelineIfNotExists();

        int lastIndex = begin;
        DodoStyle style = getStyleAtPosition(begin+1);
        if(style.foreground.isEmpty() || style.background.isEmpty())
            style = DEFAULT_STYLE;

        for(int i=begin ; i<end ; i++){
            DodoStyle otherStyle = getStyleAtPosition(i+1);
            if(otherStyle.foreground.isEmpty() || otherStyle.background.isEmpty())
                otherStyle = DEFAULT_STYLE;

            if(!otherStyle.equals(style)){
                makeSelectionBlink(lastIndex, i, style);
                style = otherStyle;
                lastIndex = i;
            }
        }
        makeSelectionBlink(lastIndex, end, style);
    }

    public int getEditableLimit(){
        return editableLimit;
    }

    public TermSelection getSelectionInfo(){
        return new TermSelection(editableLimit, getCaretPosition(), getSelection(), getSelectedText());
    }

    /** /!\ retourne une liste dans l'ordre décroissant
     * si aucune sélection, renvoit l'index du paragraphe actuel comme seul élément de la liste */
    public List<Integer> getSelectedParagraphIndexes(){
        ArrayList<Integer> indexes = new ArrayList<>();

        int cur = getCurrentParagraph();
        indexes.add(cur);

        IndexRange selection = getSelection();
        int n = getParagraphs().size();
        if(selection.getLength() != 0){
            for(int i = cur-1; 0<=i && selection.getStart() < getParagraphPosition(i+1) ; i--) // avant
                indexes.add(i);
            for(int i = cur+1; i<n && selection.getEnd() >= getParagraphPosition(i) ; i++) // après
                indexes.add(0, i);
        }

        return indexes;
    }

    public void selectEditable(){
        selectRange(editableLimit, getLength());
    }

    /** Propriété du numéro de la première ligne visible. */
    public ReadOnlyIntegerProperty firstVisibleLineNumberProperty(){
        return firstVisibleLineNumber.getReadOnlyProperty();
    }

    /** Propriété du numéro de la dernière ligne visible. */
    public ReadOnlyIntegerProperty lastVisibleLineNumberProperty(){
        return lastVisibleLineNumber.getReadOnlyProperty();
    }

    /** Propriété indiquant si la dernière ligne est visible. */
    public ReadOnlyBooleanProperty eofReachedProperty(){
        return eofReached.getReadOnlyProperty();
    }

    /** Étend la zone non-éditable à tout ce qui a été affiché jusqu'à maintenant. */
    public void markEditableLimit(){
        editableLimit = getLength();
    }

    public String getEditableText(){
        if(isEditable())
            return getText(editableLimit, getLength());
        else
            return "";
    }

    /** À chaque fois qu'un string est ajouté d'une quelconque manière avec un style,
     * il est split selon le caractère '\n' et chaque morceau se voit attribuer ce style.
     * Les '\n' sont ajoutés sans le style.
     * Ceci est nécéssaire afin que chaque "paragraphe" prenne au maximum 1 ligne,
     * et ainsi que les numéros de lignes soient corrects (si on ne prend pas en compte le linewrap). */

    @Override
    public void replace(int start, int end, String seg, DodoStyle style) {
        String[] subs = seg.split("\n", -1); // -1 : ne pas ignorer les empty strings
        super.replace(start, end, subs[0], style);
        int index = start + subs[0].length();
        for(int i=1 ; i<subs.length ; i++){
            replaceText(index, index, "\n");
            index += 1;
            super.replace(index, index, subs[i], style);
            index += subs[i].length();
        }
    }
}
