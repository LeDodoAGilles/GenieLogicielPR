package ShellNightmare.Terminal.TerminalFX.nano;

import ShellNightmare.Terminal.TerminalFX.DodoTextArea;
import ShellNightmare.Terminal.TerminalFX.Terminal;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.util.ArrayList;

public class NanoFooter extends VBox {
    private final Terminal terminal;

    private DodoTextArea top;
    private StackPane bottomStack;
    private HBox bottom;
    private ArrayList<DodoTextArea> helpers;

    private Label dummyLabel;

    public NanoFooter(Terminal terminal){
        this.terminal = terminal;

        top = new DodoTextArea();
        top.setEditable(false);
        top.addEventFilter(MouseEvent.ANY, this::filterMouse);
        top.deactivateDragAndDrop();
        top.useMinimumHeight();
        this.getChildren().add(top);

        helpers = new ArrayList<>();
    }

    public void clear(){
        if(top != null) top.clear();

        for(DodoTextArea area : helpers){
            area.clear();
        }
    }

    public void addHelper(String help){
        if(bottom == null){
            bottomStack = new StackPane(); // stack+back+dummyLabel : pour avoir une couleur de fond
            BorderPane back = new BorderPane();
            dummyLabel = new Label();
            dummyLabel.setBackground(new Background(new BackgroundFill(terminal.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY)));
            dummyLabel.setMaxWidth(Double.MAX_VALUE);
            dummyLabel.setMaxHeight(Double.MAX_VALUE);
            back.setCenter(dummyLabel);

            bottom = new HBox();
            bottom.prefWidthProperty().bind(top.prefWidthProperty());
            bottomStack.getChildren().addAll(back, bottom);
            this.getChildren().add(bottomStack);
        }

        DodoTextArea area = new DodoTextArea();
        area.getStyleClass().add("terminal");
        area.setEditable(false);
        area.addEventFilter(MouseEvent.ANY, Event::consume);
        area.deactivateDragAndDrop();
        area.useMinimumHeight();

        HBox.setHgrow(area, Priority.ALWAYS);
        area.setMaxWidth(Double.MAX_VALUE);

        area.print(help);

        bottom.getChildren().add(area);
        helpers.add(area);
    }

    public void removeHelpers(){
        if(bottom == null)
            return;

        bottom.getChildren().clear();
        helpers.clear();

        bottom.prefWidthProperty().unbind();
        this.getChildren().remove(bottomStack);

        bottom = null;
    }

    public DodoTextArea getListenedArea(){
        return top;
    }

    public DodoTextArea getTop(){
        if(!this.getChildren().contains(top))
            this.getChildren().add(0, top);

        return top;
    }

    public void removeTop(){
        //top.clear();
        this.getChildren().remove(top);
    }

    private void filterMouse(MouseEvent e){
        if(!top.isEditable())
            e.consume();
    }
}
