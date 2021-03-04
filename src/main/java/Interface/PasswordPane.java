package Interface;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;

/** Contenu du dialog d'entrée de mot de passe Root ou Master d'un challenge, montré avant l'édition de ce dernier.
 *
 * @author Louri */
public class PasswordPane extends BorderPane {
    private final PasswordField textfield;
    private String password = null; // annulation par défaut

    public PasswordPane(){
        Label label = new Label("Rentrez le mot de passe Root ou Master :");
        BorderPane.setMargin(label, new Insets(20, 10, 10, 10));
        this.setTop(label);

        // zone d'entrée du pseudo
        textfield = new PasswordField();
        textfield.setOnAction(event -> this.onOK());

        BorderPane.setAlignment(textfield, Pos.CENTER);
        BorderPane.setMargin(textfield, new Insets(10, 30, 10, 30));
        this.setCenter(textfield);

        BorderPane buttonPane = new BorderPane();

        // bouton de validation.
        Button ok = new Button("OK");
        ok.setOnAction(event -> this.onOK());
        BorderPane.setAlignment(ok, Pos.CENTER);
        BorderPane.setMargin(ok, new Insets(10, 20, 10, 10));
        buttonPane.setLeft(ok);

        // bouton d'annulation
        Button cancel = new Button("Annuler");
        cancel.setOnAction(event -> this.getScene().getWindow().hide());
        BorderPane.setAlignment(cancel, Pos.CENTER);
        BorderPane.setMargin(cancel, new Insets(10, 20, 10, 10));
        buttonPane.setRight(cancel);

        this.setBottom(buttonPane);

        // met le focus sur le textfield dès l'affichage du dialog, afin de pouvoir écrire dedans directement
        Platform.runLater(textfield::requestFocus);
    }

    /** Appelé lorsque le mot de passe saisi doit être interprété (il n'y a pas annulation). */
    private void onOK(){
        password = textfield.getText();
        this.getScene().getWindow().hide();
    }

    /** Renvoie le mot de passe saisi par l'utilisateur.
     * Ne doit être appelé qu'une fois le dialog refermé.
     *
     * @return le mot de passe saisi
     *         null si l'utilisateur a annulé la saisie */
    public String getPassword(){
        return password;
    }
}
