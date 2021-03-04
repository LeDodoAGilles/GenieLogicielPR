package Interface;

import ShellNightmare.Terminal.MetaContext;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.util.List;

/** Contenu du dialog de login montré au lancement de l'application.
 *
 * @author Louri */
public class LoginPane extends BorderPane {
    private final TextField textfield;
    private final List<String> blacklist;
    private final Button ok;

    /** Crée et setup le contenu d'un dialog de login.
     *
     * @param defaultUsername pseudo par défaut
     * @param blacklist liste de pseudos interdits, notamment celui de l'utilisateur Root */
    public LoginPane(String defaultUsername, List<String> blacklist){
        Label label = new Label("Rentrez votre pseudo :");
        BorderPane.setMargin(label, new Insets(20, 10, 10, 10));
        this.setTop(label);

        // zone d'entrée du pseudo
        textfield = new TextField(defaultUsername);
        textfield.setOnAction(event -> this.onAction());
        this.blacklist = blacklist;

        BorderPane.setAlignment(textfield, Pos.CENTER);
        BorderPane.setMargin(textfield, new Insets(10, 30, 10, 30));
        this.setCenter(textfield);

        // bouton de validation. C'est le seul bouton du dialog.
        ok = new Button("Login");
        ok.setOnAction(event -> this.onAction());

        BorderPane.setAlignment(ok, Pos.CENTER);
        BorderPane.setMargin(ok, new Insets(10, 20, 10, 10));
        this.setBottom(ok);

        // désactivation du bouton tant que le pseudo choisi est invalide
        textfield.textProperty().addListener((e, o, pseudo) -> ok.setDisable(invalidate(pseudo)));

        // met le focus sur le textfield dès l'affichage du dialog, afin de pouvoir écrire dedans directement
        Platform.runLater(textfield::requestFocus);

        // assure que si le pseudo est invalide en fermant la fenêtre, celui par défaut sera retenu.
        Platform.runLater(() -> this.getScene().getWindow().setOnCloseRequest(e -> {
            if(invalidate(textfield.getText()))
                textfield.setText(defaultUsername);
        }));
    }

    /** Ferme le dialog si le nom d'utilisateur rentré est valide.
     * Exécuté lors de l'appui sur le bouton OK ou sur la touche Entrée. */
    private void onAction(){
        if(!invalidate(textfield.getText()))
            this.getScene().getWindow().hide();
    }

    /** Vérifie si le pseudo choisi est invalide.
     *
     * @param input le pseudo choisi
     * @return true si le pseudo est invalide */
    private boolean invalidate(String input){
        return input.isBlank() || blacklist.contains(input) || !MetaContext.VALID_FILE_NAME.matcher(input).matches();
    }

    /** Renvoie le pseudo choisi (ou laissé) par l'utilisateur.
     * Ne doit être appelé qu'une fois le dialog refermé.
     *
     * @return le pseudo choisi */
    public String getUsername(){
        return textfield.getText().strip().replace(" ", "_"); // enlève les leading et trailing spaces, remplace ceux restants par des underscores
    }
}
