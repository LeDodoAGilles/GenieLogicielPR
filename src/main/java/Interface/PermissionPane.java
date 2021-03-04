package Interface;

import ShellNightmare.Terminal.Context;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;
/** Contenu du dialog permettant de mettre en place les permissions  d'un fichier.
 *
 * @author Louri */
public class PermissionPane extends VBox {
    public String user;
    public String group;
    public String perm;

    public boolean isOK = false;

    public PermissionPane(String user, String group, String perm, Context c){
        this.user = user;
        this.group = group;
        this.perm = perm;

        BorderPane b1 = new BorderPane();
        BorderPane b2 = new BorderPane();
        BorderPane b3 = new BorderPane();
        BorderPane buttonPane = new BorderPane();
        this.getChildren().addAll(b1, b2, b3, buttonPane);

        Label userLabel = new Label("User");
        BorderPane.setAlignment(userLabel, Pos.CENTER_LEFT);
        BorderPane.setMargin(userLabel, new Insets(10, 10, 10, 10));
        b1.setLeft(userLabel);

        ComboBox<String> userCB = new ComboBox<>();
        userCB.getItems().addAll(c.getUsers().stream().map(u -> u.name).collect(Collectors.toList()));
        userCB.getSelectionModel().select(user);
        userCB.getSelectionModel().selectedItemProperty().addListener((e, o, n) -> this.user = n);
        BorderPane.setAlignment(userCB, Pos.CENTER_RIGHT);
        BorderPane.setMargin(userCB, new Insets(10, 10, 10, 10));
        b1.setCenter(userCB);

        Label groupLabel = new Label("Group");
        BorderPane.setAlignment(groupLabel, Pos.CENTER_LEFT);
        BorderPane.setMargin(groupLabel, new Insets(10, 10, 10, 10));
        b2.setLeft(groupLabel);

        ComboBox<String> groupCB = new ComboBox<>();
        groupCB.getItems().addAll(c.getGroups().stream().map(g -> g.name).collect(Collectors.toList()));
        groupCB.getSelectionModel().select(group);
        groupCB.getSelectionModel().selectedItemProperty().addListener((e, o, n) -> this.group = n);
        BorderPane.setAlignment(groupCB, Pos.CENTER_RIGHT);
        BorderPane.setMargin(groupCB, new Insets(10, 10, 10, 10));
        b2.setCenter(groupCB);

        Label permLabel = new Label("Permission (octal)");
        BorderPane.setAlignment(permLabel, Pos.CENTER_LEFT);
        BorderPane.setMargin(permLabel, new Insets(10, 10, 10, 10));
        b3.setLeft(permLabel);

        TextField permTF = new TextField(perm);
        permTF.setMinWidth(50);
        permTF.setPrefWidth(50);
        permTF.setMaxWidth(50);
        permTF.textProperty().addListener((e, o, n) -> this.perm = n);
        BorderPane.setAlignment(permTF, Pos.CENTER_RIGHT);
        b3.setCenter(permTF);

        VBox descBox = new VBox();
        BorderPane.setAlignment(descBox, Pos.CENTER);
        b3.setBottom(descBox);

        BorderPane b31 = new BorderPane();
        b31.setCenter(new Label("user - group - other"));

        BorderPane b32 = new BorderPane();
        b32.setCenter(new Label("rwx - rwx - rwx"));

        descBox.getChildren().addAll(b31, b32);

        Button ok = new Button("OK");
        ok.setOnAction(e -> {
            isOK = true;
            this.getScene().getWindow().hide();
        });
        buttonPane.setLeft(ok);

        Button cancel = new Button("Annuler");
        cancel.setOnAction(e -> {
            this.getScene().getWindow().hide();
        });
        buttonPane.setRight(cancel);

        this.setMinWidth(240);
        this.setMinHeight(200);
        this.setPadding(new Insets(10, 10, 10, 10));
    }
}
