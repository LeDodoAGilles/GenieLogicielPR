package ShellNightmare.Terminal.challenge;

import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Comparator;

public class ScoreTable extends TableView<Score> {
    public ScoreTable(){
        Label prompt = new Label("Il n'y a aucun score enregistré pour ce challenge.");
        prompt.setWrapText(true);
        this.setPlaceholder(prompt);

        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // espace redistribué entre les colonnes existantes, sinon y'en a une en trop qui prendrait l'espace restant
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setEditable(false); // par sécurité

        TableColumn<Score, String> usernameColumn = new TableColumn<>("Utilisateur");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Score, ScoreTime> timeColumn = new TableColumn<>("Temps");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setComparator(Comparator.comparing((ScoreTime v) -> v.time));

        this.getColumns().add(usernameColumn);
        this.getColumns().add(timeColumn);
    }
}
