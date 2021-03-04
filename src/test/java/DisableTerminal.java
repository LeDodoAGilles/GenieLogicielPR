
// https://medium.com/information-and-technology/test-driven-development-in-javafx-with-testfx-66a84cd561e0
// https://github.com/TestFX/TestFX/issues/222
// https://stackoverflow.com/questions/52605298/simple-testfx-example-fails

// NE PAS RETIRER : autre solution de l'exécution séquentielle
// https://stackoverflow.com/questions/29984775/surefire-run-certain-tests-in-sequence-others-in-parallel
// https://maven.apache.org/surefire/maven-surefire-plugin/examples/inclusion-exclusion.html

import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.Daemon;
import ShellNightmare.Terminal.MetaContext;
import utils.CssToColorHelper;
import ShellNightmare.Terminal.TerminalFX.DodoTextArea;
import ShellNightmare.Terminal.TerminalFX.Terminal;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.function.BiConsumer;

import javafx.scene.Scene;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import net.jcip.annotations.NotThreadSafe;
import org.fxmisc.richtext.CaretNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactfx.util.TriConsumer;
import org.testfx.api.FxToolkit;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit.ApplicationTest;

import static ShellNightmare.Terminal.MetaContext.mainDaemon;

/** Regroupe les tests unitaires du Terminal.
 *
 * Ne pas toucher à son clavier ou à sa souris lorsque les tests sont lancés : cela perturbera FxRobot
 * qui en a aussi le contrôle.
 *
 * Hérite de ApplicationTest qui hérite lui-même de FxRobot, donc les méthodes de FxRobot sont accessibles directement. */
@NotThreadSafe // Exécution séquentielle
public class DisableTerminal extends ApplicationTest {
    protected Terminal terminal;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getChildren().add(CssToColorHelper.HELPER);

        MetaContext.Init();
        Context c = new Context();

        terminal = new Terminal();
        mainDaemon.setData(terminal,c);
        terminal.init();
        root.setCenter(terminal);

        Scene scene = new Scene(root, 740, 480); // La taille DOIT être précisée, sinon FxRobot ne fonctionne pas correctement.
        scene.getStylesheets().add("arch.css"); // TODO charger un style propre aux Tests qui n'est pas susceptible de changer.
        primaryStage.setScene(scene);

        primaryStage.show();
        primaryStage.toFront();
    }

    @Before
    public void setUp() {
        /*interact(() -> {
            terminal.clear();
            terminal.waitUserInput();
        });*/
    }

    @After
    public void tearDown() throws Exception{
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    /** Renvoie la position absolue en pixels du coin haut-gauche du DodoTextArea du Terminal
     * (par rapport au coin haut-gauche de l'écran). */
    protected Point2D getTerminalCoordinates(){
        double x = terminal.getScene().getX() + terminal.getScene().getWindow().getX();
        double y = terminal.getScene().getY() + terminal.getScene().getWindow().getY();
        return new Point2D.Double(x, y);
    }

    /** Renvoie la position absolue en pixels du caret du Terminal (par rapport au coin haut-gauche de l'écran).
     * Renvoie en fait la position centrale du caret.
     * Renvoie (-1,-1) si le caret n'est pas visible, et donc n'a pas de position. */
    protected Point2D getCaretCoordinates(DodoTextArea area){
        Point2D p = new Point2D.Double(-1,-1);
        area.getCaretBounds().ifPresent(bounds -> p.setLocation(bounds.getCenterX(), bounds.getCenterY()));
        return p;
    }

    /** Utilise le robot pour sélectionner une zone de texte, de <start> vers <end>. */
    protected void select(DodoTextArea area, int start, int end){
        // Utilise un nouveau caret afin de déterminer les coordonnées.
        CaretNode extraCaret = new CaretNode("another caret", area);
        extraCaret.getStyleClass().remove("caret");
        extraCaret.setStrokeWidth(0.0); // Rend ce caret invisible.

        interact(() -> {
            if (!area.addCaret(extraCaret)) {
                throw new IllegalStateException("Impossible de créer un nouveau caret.");
            }
        });

        Point2D startCoords = new Point2D.Double(-1,-1);
        interact(() -> extraCaret.moveTo(start));
        extraCaret.getCaretBounds().ifPresent(bounds -> startCoords.setLocation(bounds.getCenterX(), bounds.getCenterY()));

        Point2D endCoords = new Point2D.Double(-1,-1);
        interact(() -> extraCaret.moveTo(end));
        extraCaret.getCaretBounds().ifPresent(bounds -> endCoords.setLocation(bounds.getCenterX(), bounds.getCenterY()));

        interact(() -> area.removeCaret(extraCaret));

        // selection
        moveTo(startCoords.getX(), startCoords.getY());
        press(MouseButton.PRIMARY);
        moveTo(endCoords.getX(), endCoords.getY());
        release(MouseButton.PRIMARY);

        IndexRange range = (start <= end) ? new IndexRange(start, end) : new IndexRange(end, start); // dans l'ordre
        Assertions.assertThat(area.getSelection()).isEqualTo(range); // sinon le test ne peut pas continuer
    }

    /** Utilise le robot pour cliquer à la position donnée dans la zone de texte. */
    protected void moveToAndClick(DodoTextArea area, int pos){
        // Utilise un nouveau caret afin de déterminer les coordonnées.
        CaretNode extraCaret = new CaretNode("another caret", area);
        extraCaret.getStyleClass().remove("caret");
        extraCaret.setStrokeWidth(0.0); // Rend ce caret invisible.

        interact(() -> {
            if (!area.addCaret(extraCaret)) {
                throw new IllegalStateException("Impossible de créer un nouveau caret.");
            }
        });

        Point2D coords = new Point2D.Double(-1,-1);
        interact(() -> extraCaret.moveTo(pos));
        extraCaret.getCaretBounds().ifPresent(bounds -> coords.setLocation(bounds.getCenterX(), bounds.getCenterY()));

        interact(() -> area.removeCaret(extraCaret));

        Assertions.assertThat(coords.getX()).isNotEqualTo(-1);

        // selection
        moveTo(coords.getX(), coords.getY());
        clickOn();

        Assertions.assertThat(area.getCaretPosition()).isEqualTo(pos); // sinon le test ne peut pas continuer
    }

    public String getStringFromClipboard(){
        try {
            // N'utilise pas le Clipboard de JavaFx afin d'éviter l'appel à interact() qui donne un résultat bizarre pour le clipboard.
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /*public void testTest(){
        write("nano");
        press(KeyCode.ENTER);
        sleep(100);
        write("some text");
        Point2D pos = getCaretCoordinates(terminal.getBody());
        moveTo(pos.getX(), pos.getY());
        sleep(3000);

        var area = terminal.getBody();
        select(area, area.getLength()-20, area.getLength()-10);
        sleep(3000);
    }*/

    @Test
    public void testWriteInEditableArea(){
        DodoTextArea area = terminal.getBody();

        String o = area.getText(); // texte avant modification (partie non-éditable)
        // On vérifie sur tout le texte et pas uniquement la sélection, au cas où la zone non-éditable aurait été modifiée.

        int start = area.getLength(); // début de la zone de commande
        interact(() -> area.moveTo(start));

        moveToAndClick(area, 1);

        write("his ");
        Assertions.assertThat(area.getText()).isEqualTo(o+"his "); // écriture en début/fin de zone éditable vide

        int middle = area.getLength();
        write(" Sparta");
        Assertions.assertThat(area.getText()).isEqualTo(o+"his "+" Sparta"); // écriture en fin de zone éditable non vide

        moveToAndClick(area, middle);
        Assertions.assertThat(area.getCaretPosition()).isEqualTo(middle); // sinon le test ne peut pas continuer
        write("is");
        Assertions.assertThat(area.getText()).isEqualTo(o+"his "+"is"+" Sparta"); // écriture en millieu de zone éditable non vide

        moveToAndClick(area, start);
        Assertions.assertThat(area.getCaretPosition()).isEqualTo(start); // sinon le test ne peut pas continuer
        write("t");
        Assertions.assertThat(area.getText()).isEqualTo(o+"t"+"his "+"is"+" Sparta"); // écriture en début de zone éditable non vide

        // Exemples :
        //FxAssert.verifyThat("#terminal-area", ... ?); // idem que assertThat, mais avec un screenshot en plus
        //Assertions.assertThat(area.getText(start, end)).isEqualTo(s); // ordre actual -> expected, + meilleurs messages d'erreur
        //assertEquals(s, area.getText(start, end)); // JUnit
    }

    @Test
    public void testWriteInUneditableArea(){
        DodoTextArea area = terminal.getBody();

        String o = area.getText(); // texte avant modification (partie non-éditable)

        int end = area.getLength(); // début de la zone de commande
        moveToAndClick(area, end-10);
        int caretPos = area.getCaretPosition();
        write("denied");
        if(Terminal.MOVE_ON_READONLY_EDITION){
            Assertions.assertThat(area.getText()).isEqualTo(o+"denied");
            Assertions.assertThat(area.getCaretPosition()).isEqualTo(area.getLength());
        }
        else {
            Assertions.assertThat(area.getText()).isEqualTo(o);
            Assertions.assertThat(area.getCaretPosition()).isEqualTo(caretPos); // unchanged
        }
    }

    @Test
    public void testWriteInEditableAreaSelection(){
        DodoTextArea area = terminal.getBody();

        int start = area.getLength(); // début de la zone de commande
        String o = area.getText(); // texte avant modification (partie non-éditable)
        moveToAndClick(area, start);
        final String[] s = {"Ce texte est changé"}; // tableau pour être modifiable par le lambda

        write(s[0]);
        Assertions.assertThat(area.getText()).isEqualTo(o+s[0]); // sinon le test ne peut pas continuer

        TriConsumer<String, String, Boolean> testme = (old, replacement, leftToRight) -> {
            int pos = start+s[0].indexOf(old);
            if(leftToRight)
                select(area, pos, pos+old.length());
            else
                select(area, pos+old.length(), pos);
            write(replacement);
            s[0] = s[0].replaceFirst(old, replacement);
            Assertions.assertThat(area.getText()).isEqualTo(o+s[0]);
        };

        // sélection gauche-droite en milieu de zone éditable
        testme.accept("est", "était", true);
        // sélection droite-gauche en milieu de zone éditable
        testme.accept("était", "a été", false);

        // sélection gauche-droite en fin de zone éditable
        testme.accept("changé", "revu", true);
        // sélection droite-gauche en fin de zone éditable
        testme.accept("revu", "modifié", false);

        // sélection gauche-droite en début de zone éditable
        testme.accept("Ce", "Mon", true);
        // sélection droite-gauche en début de zone éditable
        testme.accept("Mon", "Le", false);
    }

    @Test
    public void testWriteInUneditableAreaSelection(){
        DodoTextArea area = terminal.getBody();
        moveToAndClick(area, area.getLength());
        write("clear");
        press(KeyCode.ENTER);
        release(KeyCode.ENTER);

        int limit = area.getLength(); // début de la zone de commande
        Assertions.assertThat(limit).isGreaterThanOrEqualTo(3); // sinon le test ne peut pas continuer

        String o = area.getText(); // texte avant modification (partie non-éditable)
        moveToAndClick(area, limit);
        final String[] s = {"denied"}; // tableau pour être modifiable par le lambda

        write(s[0]);
        Assertions.assertThat(area.getText()).isEqualTo(o+ s[0]); // sinon le test ne peut pas continuer

        String add = "X"; // 1 seul caractère
        BiConsumer<Integer, Integer> testme = (start, end) -> {
            select(area, start, end);
            write(add);
            if(Terminal.MOVE_ON_READONLY_EDITION)
                s[0] += add;
            else
                Assertions.assertThat(area.getCaretPosition()).isEqualTo(Math.max(start, end));
            Assertions.assertThat(area.getText()).isEqualTo(o+s[0]);
        };

        // sélection gauche-droite en milieu de zone non-éditable
        testme.accept(1, limit-1);
        // sélection droite-gauche en milieu de zone non-éditable
        testme.accept(limit-1, 1);

        // sélection gauche-droite en début de zone non-éditable
        testme.accept(0, limit-1);
        // sélection droite-gauche en début de zone non-éditable
        testme.accept(limit-1, 0);

        // sélection gauche-droite en fin de zone non-éditable
        testme.accept(1, limit);
        // sélection droite-gauche en fin de zone non-éditable
        testme.accept(limit, 1);

        // sélection gauche-droite à cheval sur la zone non-éditable et éditable
        testme.accept(1, area.getLength()-1);
        // sélection droite-gauche à cheval sur la zone non-éditable et éditable
        testme.accept(area.getLength()-1, 1);
    }

}
