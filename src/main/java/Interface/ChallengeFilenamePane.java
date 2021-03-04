package Interface;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/** Contenu du dialog de choix de nom de fichier à attribuer à un challenge.
 *
 * @author Louri */
public class ChallengeFilenamePane extends BorderPane {
    private final TextField textfield;
    private String filename = null; // annulation par défaut

    public ChallengeFilenamePane(){
        Label label = new Label("Sous quel nom enregistrer le challenge ?");
        BorderPane.setMargin(label, new Insets(20, 10, 10, 10));
        this.setTop(label);

        BorderPane fieldPane = new BorderPane();

        // zone d'entrée du pseudo
        textfield = new TextField();
        textfield.setOnAction(event -> this.onOK());

        BorderPane.setAlignment(textfield, Pos.CENTER);
        BorderPane.setMargin(textfield, new Insets(10, 30, 10, 0));
        fieldPane.setCenter(textfield);

        Label extension = new Label(".zip");
        BorderPane.setAlignment(extension, Pos.CENTER_LEFT);
        BorderPane.setMargin(extension, new Insets(10, 10, 10, 10));
        fieldPane.setRight(extension);

        this.setCenter(fieldPane);

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

    /** Appelé lorsque le nom de fichier saisi doit être utilisé (il n'y a pas annulation). */
    private void onOK(){
        filename = textfield.getText() + ".zip";
        this.getScene().getWindow().hide();
    }

    /** Renvoie le nom de fichier saisi par l'utilisateur.
     * Ne doit être appelé qu'une fois le dialog refermé.
     *
     * @return le nom de fichier saisi
     *         null si l'utilisateur a annulé la saisie */
    public String getFilename(){
        return filename;
    }
}
