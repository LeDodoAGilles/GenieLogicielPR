package Interface;

import ShellNightmare.Terminal.challenge.ChallengeHeader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/** Regroupe les informations principales d'un challenge dans un carré pour l'affichage.
 *
 * @author Louri */
public class ChallengeTile extends StackPane {
    public static final int SIZE = 256; // largeur et hauteur de l'image finale
    private static final Background INFO_BACKGROUND;
    private static final String INVALIDATED_IMAGE_PATH = "/Images/warning.png";
    private static final int INVALIDATED_SIZE = 32;
    private static final String NAME_STYLE = "-fx-font-weight: bold; -fx-font-size: 14pt;";
    private static final String DIFFICULTY_STYLE = "-fx-font-style: italic;";
    private static final String DESCRIPTION_STYLE = "";

    static {
        INFO_BACKGROUND = new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
    }

    private final java.io.File file; // sauvegarde du fichier associé au challenge de ce tile

    /** Création d'un nouveau tile à partir du header (pour les informations) et de la vignette.
     *
     * @param header    le header du challenge regroupant ses informations principales
     *                  null pour le "dummy challenge" de création
     * @param thumbnail la vignette du challenge
     * @param file      fichier zip associé
     *                  null pour le "dummy challenge" de création */
    public ChallengeTile(ChallengeHeader header, Image thumbnail, java.io.File file){
        this.file = file;

        // composant d'affichage de la vignette, avec une taille fixe.
        ImageView view = new ImageView(thumbnail);
        view.setPreserveRatio(true);
        view.setFitWidth(SIZE);
        view.setFitHeight(SIZE);
        this.getChildren().add(view);

        // si ce n'est pas le tile du "dummy challenge" de création, alors afficher les informations
        if(header != null){
            // layout des informations
            BorderPane borderPane = new BorderPane();
            borderPane.setMouseTransparent(true);
            borderPane.setMaxSize(SIZE, SIZE);

            // indique par une étoile si le challenge n'a pas encore été validé
            if(!header.validated){
                ImageView invalidated = new ImageView(new Image(getClass().getResource(INVALIDATED_IMAGE_PATH).toExternalForm()));
                invalidated.setPreserveRatio(true);
                invalidated.setFitWidth(INVALIDATED_SIZE);
                invalidated.setFitHeight(INVALIDATED_SIZE);
                BorderPane.setAlignment(invalidated, Pos.TOP_RIGHT);
                BorderPane.setMargin(invalidated, new Insets(5, 5, 5, 5));
                borderPane.setTop(invalidated);
            }

            // affichage vertical des informations textuelles
            VBox infoList = new VBox();
            infoList.setBackground(INFO_BACKGROUND);

            Label name = new Label(header.name);
            name.setStyle(NAME_STYLE);

            Label difficulty = new Label(header.difficulty);
            difficulty.setStyle(DIFFICULTY_STYLE);

            Label description = new Label(header.description);
            description.setStyle(DESCRIPTION_STYLE);
            description.setWrapText(true);

            infoList.getChildren().addAll(name, difficulty, description); // ajout de haut en bas

            borderPane.setBottom(infoList); // informations affichées en bas
            this.getChildren().add(borderPane); // affiché au dessus de la vignette

            infoList.setVisible(false);
            this.setOnMouseEntered(event -> infoList.setVisible(true)); // TODO animation jolie : fondu, ou déplacement depuis le bas (jouer sur maxsize ?)
            this.setOnMouseExited(event -> infoList.setVisible(false));
        }
    }

    /** Récupère le fichier .zip de ce challenge.
     * @return Le fichier du challenge associé à ce tile. Peut être null. */
    public java.io.File getFile(){
        return file;
    }
}
