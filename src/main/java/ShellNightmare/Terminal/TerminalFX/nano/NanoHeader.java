package ShellNightmare.Terminal.TerminalFX.nano;

import ShellNightmare.Terminal.TerminalFX.DodoTextArea;
import javafx.event.Event;
import javafx.geometry.NodeOrientation;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class NanoHeader extends VBox {
    private DodoTextArea left;
    private DodoTextArea center;
    private DodoTextArea right;
    private DodoTextArea bottom;

    public NanoHeader(boolean emptyline){
        StackPane stack = new StackPane();
        BorderPane titlebar = new BorderPane();

        left = new DodoTextArea();
        left.setStyle("-fx-background-color: terminal-foreground;");
        left.setEditable(false);
        left.addEventFilter(MouseEvent.ANY, Event::consume);
        left.deactivateDragAndDrop();
        left.useMinimumHeight();
        left.useMinimumWidth();
        titlebar.setLeft(left);

        right = new DodoTextArea();
        right.setStyle("-fx-background-color: terminal-foreground;");
        right.setEditable(false);
        right.addEventFilter(MouseEvent.ANY, Event::consume); // Empêche la sélection (et donc de révéler le caractère jouant le rôle de l'espace à droite)
        right.deactivateDragAndDrop();
        right.useMinimumHeight();
        right.useMinimumWidth();
        right.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        titlebar.setRight(right);

        // grâce au stackpane, sera parfaitement centré sans dépendre de la taille  de 'left' et 'right'
        center = new DodoTextArea();
        center.setStyle("-fx-background-color: terminal-foreground;");
        center.setEditable(false);
        center.addEventFilter(MouseEvent.ANY, Event::consume);
        center.deactivateDragAndDrop();
        center.useMinimumHeight();

        stack.getChildren().addAll(center, titlebar); // mais 'center' sera caché par 'left' et 'right' si resize de la largeur trop petit
        this.getChildren().add(stack);

        if(emptyline){
            bottom = new DodoTextArea();
            bottom.setEditable(false);
            bottom.deactivateDragAndDrop();
            bottom.useMinimumHeight();
            this.getChildren().add(bottom);
        }
    }

    public void clear(){
        if(left != null) left.clear();
        if(center != null) center.clear();
        if(right != null) right.clear();
        if(bottom != null) bottom.clear();
    }

    public DodoTextArea getListenedArea(){
        return bottom;
    }

    public DodoTextArea getLeft(){
        return left;
    }

    public DodoTextArea getCenter(){
        return center;
    }

    public DodoTextArea getRight(){
        return right;
    }

    public DodoTextArea getBottom(){
        return bottom;
    }
}
