package Interface;

import ShellNightmare.Terminal.challenge.Score;
import ShellNightmare.Terminal.challenge.ScoreTable;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import java.util.List;

/** Contenu du dialog des scores.
 *
 * @author Louri */
public class ScorePane extends BorderPane {

    /** Crée et setup le contenu d'un dialog de login.
     *
     * @param scores liste des scores à afficher */
    public ScorePane(List<Score> scores){
        TableView<Score> table = new ScoreTable();
        table.getItems().addAll(scores);
        BorderPane.setAlignment(table, Pos.CENTER);
        BorderPane.setMargin(table, new Insets(20, 20, 20, 20));
        this.setCenter(table);

        Button ok = new Button("Retour");
        ok.setOnAction(event -> this.getScene().getWindow().hide());
        BorderPane.setAlignment(ok, Pos.CENTER);
        BorderPane.setMargin(ok, new Insets(10, 20, 20, 20));
        this.setBottom(ok);

        // il suffira d'appuyer sur Entrée pour activer le bouton OK
        Platform.runLater(ok::requestFocus);
    }
}
