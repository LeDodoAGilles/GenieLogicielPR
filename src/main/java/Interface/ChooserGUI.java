package Interface;

import ShellNightmare.Terminal.challenge.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.codec.digest.DigestUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

/** Menu de sélection de challenge.
 * Utilisé dans les modes Soloplayer, Multiplayer (à l'upload) et Editor.
 *
 * Un challenge ne peut pas être joué ou uploadé tant qu'il n'a pas été validé dans l'éditeur.
 *
 * Un challenge nécessite parfois un mot de passe pour pouvoir être édité.
 * Un nouveau challenge peut être créé et ensuite initialisé dans l'éditeur.
 *
 * Il existe une <i>race condition</i> entre le moment ou les challenges sont affichés
 * et celui où l'un deux est sélectionné. En effet, l'utilisateur a pu supprimer ou renommer un challenge entre-temps.
 * Ce souci débouche systématiquement sur l'affichage d'une erreur, et non pas un crash de l'application.
 *
 * @author Marc
 * @author Louri */
public class ChooserGUI extends GUI {
    public static final String FXML = "/FXML/Chooser.fxml";
    private static final String HTML_HELP = "help/index.html";

    private static final String CREATION_THUMBNAIL_PATH = "/Images/create_challenge.png";
    private static final int GAP = 30;

    /** Définit deux comportements différents selon que l'on veuille jouer/uploader ou éditer un challenge. */
    enum ChooserState {PLAY, UPLOAD, EDIT}

    @FXML
    private ScrollPane scrollpane;

    @FXML
    private GridPane grid;
    private int nbColumns; // même nombre de colonnes que défini dans le FXML

    // mode de sélection
    private ChooserState state;

    private ChallengeTile tileClicked; // dernier tile cliqué, utilisé lors du retour à ce GUI
    private Challenge challengeToUpload; // challenge (chargé) à uploader (reste null si state != UPLOAD)

    // TODO settings
    /*public static Settings updateSettings(Stage stage) throws IOException {
        Settings settings = new Settings();
        settings.initFile();
        if(settings.getValueOf("ConsoleStyle").equals("0"))
        {
            stage.getScene().getStylesheets().add(stage.getClass().getResource("/ubuntu.css").toExternalForm());
        }
        else
        {
            stage.getScene().getStylesheets().add(stage.getClass().getResource("/arch.css").toExternalForm());
        }

        return settings;
    }*/

    @Override
    public void reset(Object[] args) {
        refresh();

        // Faire ce qui est ci-dessous si on ne veut rafraîchir que le challenge qui a été cliqué.
        // Cependant, on préfère tous les recharger, au cas où certains auraient été supprimés entre-temps.

        /*if(tileClicked == null)
            return;

        if(tileClicked.getFile() == null) // "dummy challlenge" => rien à recharger
            return;

        int x = GridPane.getColumnIndex(tileClicked);
        int y = GridPane.getRowIndex(tileClicked);

        grid.getChildren().remove(tileClicked); // supprime le tile de la grille, l'emplacement (x,y) est vide

        try{
            // crée un nouveau tile et le place au même endroit
            loadFileInfos(tileClicked.getFile(), x, y);
        } catch(IOException e){
            // Impossible de recharger le fichier. Problème : on ne veut pas le garder, et l'emplacement reste vide.
            // Solution : on déplace tous les tiles qui venaient après pour combler ce vide.
            grid.getChildren().stream()
                    // pour tous les tiles venant après le tile cliqué
                    .filter(tile -> compareCoords(x, y, GridPane.getColumnIndex(tile), GridPane.getRowIndex(tile)) < 0)
                    // les mettre dans l'ordre
                    .sorted((u, v) -> compareTiles((ChallengeTile) u, (ChallengeTile) v))
                    // du plus proche au plus loin en terme d'indices
                    .forEach(tile -> {
                        // on le rapproche de 1 case
                        Map.Entry<Integer, Integer> less = decrementCoords(GridPane.getColumnIndex(tile), GridPane.getRowIndex(tile), nbColumns);
                        GridPane.setConstraints(tile, less.getKey(), less.getValue());
                    });
        }

        tileClicked = null;*/
    }

    @Override
    public void init(Object[] args) {
        assert args.length == 1;
        state = (ChooserState) args[0];

        nbColumns = grid.getColumnCount();

        // Dimensionne correctement la grille et la scrollpane, calcule la taille minimale

        for(var constraint : grid.getColumnConstraints()){
            constraint.setPercentWidth(100.0);
            constraint.setMinWidth(ChallengeTile.SIZE);
            constraint.setHalignment(HPos.CENTER);
        }

        grid.getRowConstraints().clear();

        grid.setHgap(GAP);
        grid.setVgap(GAP);
        grid.setPadding(new Insets(GAP, GAP, GAP, GAP));

        scrollpane.setMinWidth(nbColumns*(ChallengeTile.SIZE + GAP) + GAP);
        scrollpane.setMinHeight(ChallengeTile.SIZE + 2*GAP);

        loadFilesInfos();
    }

    @Override
    public void dispose(){}

    /** Recharge les informations de tous les challenges une fois de plus.
     * Appelé lors du clic sur le bouton Rafraîchir. */
    @FXML
    private void refresh(){
        grid.getChildren().clear();
        tileClicked = null;
        loadFilesInfos();
    }

    /** Charge les informations de tous les challenges et les met sous forme de tiles.
     * N'est appelé que lorsque la grille est vide / a été vidée. */
    private void loadFilesInfos(){
        // Prochain index à remplir dans la grille. Essentiel de faire ainsi pour gérer les fichiers introuvables.
        int nextIndex = 0;

        // Pour l'édition : ajout d'un "dummy challenge" pour la création de challenges.
        if(state == ChooserState.EDIT){
            Image creationThumbnail = new Image(getClass().getResource(CREATION_THUMBNAIL_PATH).toExternalForm());
            ChallengeTile creationTile = new ChallengeTile(null, creationThumbnail, null);
            creationTile.setOnMouseClicked(this::onTileClicked);
            grid.add(creationTile, 0, 0);
            nextIndex++;
        }

        // Récupération de tous les fichiers .zip du dossier des challenges
        java.io.File folder = new java.io.File(Challenge.CHALLENGES_FOLDER);
        java.io.File[] files = folder.listFiles((f, name) -> name.endsWith(".zip")); // ne prend que les .zip du dossier
        if(files == null){ // arrive si le dossier a été supprimé, renommé, est introuvable...
            Framework.showError("Challenges introuvables", "Impossible de lire le dossier /"+ Challenge.CHALLENGES_FOLDER);
            return;
        }

        // Chargement du header et de la vignette de tous les challenges, affichage sous forme de tile.
        for(var file : files){ // aucun des challenges.get(i) ne sera null
            try{
                loadFileInfos(file, nextIndex % nbColumns, nextIndex / nbColumns);
            } catch(Exception e){
                // Le fichier a été supprimé entre temps, n'a pas pu être lu... Temps pis, on ne l'affiche pas.
                // Ou si 'est un .zip qui n'est pas un challenge
                continue; // ne pas faire nextIndex++
            }

            nextIndex++; // incrémenté uniquement s'il n'y a pas eu d'erreur, et donc qu'un tile a été ajouté
        }
    }

    /** Charge les informations d'un challenge particulier, les met sous forme de tile et ajoute ce dernier à la grille.
     * Aussi utilisé pour mettre à jour un challenge déjà existant.
     *
     * @param file le fichier du challenge dont on veut charger les informations
     * @param x numéro de colonne du challenge à rajouter dans la grille
     * @param y numéro de ligne du challenge à rajouter dans le grille
     * @exception IOException le fichier n'a pas pu être chargé */
    private void loadFileInfos(java.io.File file, int x, int y) throws IOException {
        ChallengeHeader header;
        BufferedImage bufimg;

        // Attention : race condition, le fichier a pu être supprimé entre-temps.
        // chargement du header
        header = Challenge.readSimpleField(ChallengeHeader.class, file.getPath(), Challenge.HEADER);

        // Attention : race condition
        // chargement de la vignette
        bufimg = Challenge.readImageField(file.getPath());
        Image thumbnail = SwingFXUtils.toFXImage(bufimg, null);

        // Mise en forme d'un tile et affichage dans la grille
        ChallengeTile tile = new ChallengeTile(header, thumbnail, file);
        tile.setOnMouseClicked(this::onTileClicked);
        grid.add(tile, x, y); // remplissage ligne par ligne, de gauche à droite
    }

    /** Appelé lorsqu'un tile et cliqué.
     * Selon qu'il s'agit du clic gauche ou droit, ouvre ou montre les scores du challenge.
     * Si le challenge doit être ouvert, le prépare pour être joué ou édité.
     *
     * @param event l'événement associé au clic de souris */
    private void onTileClicked(MouseEvent event){
        tileClicked = (ChallengeTile) event.getSource();

        if(event.getButton().equals(MouseButton.PRIMARY)){ // clic gauche
            switch(state){
                case PLAY:
                    playChallenge(tileClicked);
                    break;
                case UPLOAD:
                    uploadChallenge(tileClicked);
                    break;
                case EDIT:
                    editChallenge(tileClicked);
                    break;
            }
        }
        else if(event.getButton().equals(MouseButton.SECONDARY)){ // clic droit
            framework.showScores(tileClicked);
        }
    }

    /** Détermine si un challenge peut être joué.
     * Un challenge peut être joué s'il a été validé au préalable.
     *
     * @param tile le tile du challenge que l'on veut jouer
     * @return true si le challenge peut être joué (il a été validé) */
    private boolean canPlayChallenge(ChallengeTile tile){
        assert tile.getFile() != null;

        ChallengeHeader header;
        try {
            header = Challenge.readSimpleField(ChallengeHeader.class, tile.getFile().getPath(), Challenge.HEADER);
        } catch (IOException e) {
            Framework.showError("Impossible de charger le challenge.", "Le challenge ne peut pas être chargé.");
            return false;
        }

        if(!header.validated){
            Framework.showError("Impossible de jouer à ce challenge.", "Le challenge n'a pas encore été validé dans l'éditeur.");
            return false;
        }

        return true;
    }

    /** Ouvre le challenge associé au tile donné dans l'objectif de le jouer.
     *
     * @param tile le tile du challenge que l'on doit jouer */
    private void playChallenge(ChallengeTile tile){
        if(!canPlayChallenge(tileClicked))
            return;

        framework.launchGameFromFile(tile.getFile().getName());
    }

    /** Ouvre le challenge associé au tile donné dans l'objectif de l'uploader.
     *
     * @param tile le tile du challenge que l'on doit uploader */
    private void uploadChallenge(ChallengeTile tile){
        if(!canPlayChallenge(tileClicked)) // mêmes conditions pour l'upload
            return;

        Challenge challenge = new Challenge();
        if(challenge.loadFromDisk(tile.getFile().getPath()) == null){
            Framework.showError("Impossible d'uploader ce challenge.", "Le challenge ne peut pas être chargé.");
            return;
        }

        challenge.filename = tile.getFile().getName();
        challengeToUpload = challenge;
        stage.hide(); // ferme le dialog
    }

    /** Renvoie le challenge (chargé) qui doit être uploadé.
     *
     * @return le challenge chargé à uploader
     *         null si l'utilisateur n'a sélectionné aucun challenge à uploader */
    public Challenge getChallengeToUpload(){
        return challengeToUpload;
    }

    /** Demande de rentrer le mot de passe Master ou Root du challenge.
     *
     * @return le mot de passe saisi
     *         null si l'utilisateur a annulé la saisie */
    private String askPasswordForEdition(){
        PasswordPane dialog = new PasswordPane();
        Stage stage = framework.makeDialog(dialog, "mot de passe");

        stage.showAndWait();

        return dialog.getPassword();
    }

    /** Détermine si un challenge peut être édité.
     * Un challenge peut être créé sans conditions.
     * Un challenge peut être édité s'il n'a pas de mot de passe Master,
     * ou si le mot de passe rentré par l'utilisateur correspond à celui-ci
     * ou au dernier mot de passe Root utilisé pour ce challenge.
     *
     * @param tile le tile du challenge que l'on veut éditer
     * @return true si le challenge peut être édité (accès non restreint ou débloqué) */
    private boolean canEditChallenge(ChallengeTile tile){
        if(tile.getFile() == null)
            return true;

        ChallengeHeader header;
        try {
            header = Challenge.readSimpleField(ChallengeHeader.class, tile.getFile().getPath(), Challenge.HEADER);
        } catch (IOException e) {
            Framework.showError("Impossible de charger le challenge.", "Le challenge ne peut pas être chargé.");
            return false;
        }

        // s'il n'y a pas de mot de passe Master, n'importe qui peut éditer le challenge
        if(header.hashedMasterPassword.equals(""))
            return true;

        String guess = askPasswordForEdition();
        if(guess == null) // cancel
            return false;

        String hashed = DigestUtils.sha512Hex(guess);
        if(!hashed.equals(header.hashedMasterPassword) && !hashed.equals(header.hashedRootPassword)){
            Framework.showError("Accès refusé", "Le mot de passe rentré est incorrect.");
            return false;
        }

        return true;
    }

    /** Ouvre le challenge associé au tile donné dans l'objectif de l'éditer.
     *
     * @param tile le tile du challenge que l'on doit éditer */
    private void editChallenge(ChallengeTile tile){
        if(!canEditChallenge(tile))
            return;

        Challenge challenge = null;

        // si le challenge existe : il doit être édité, pas créé
        if(tile.getFile() != null){
            challenge = new Challenge();
            if(challenge.loadFromDisk(tile.getFile().getPath()) == null){
                Framework.showError("Impossible d'uploader ce challenge.", "Le challenge ne peut pas être chargé.");
                return;
            }

            challenge.filename = tile.getFile().getName();
        }

        try {
            framework.next(EditorGUI.FXML, new Object[]{challenge});
        } catch (IOException e) { // ressource FXML non trouvée
            e.printStackTrace();
        }
    }

    /** Affiche l'aide de l'application.
     * Appelé lors de l'appui sur le bouton Information . */
    @FXML
    private void selectInfo(){
        framework.showInfo(HTML_HELP);
    }

    /** Affiche les options.
     * Appelé lors de l'appui sur le bouton Option . */
    @FXML
    private void selectOption() throws IOException {
        framework.showOption();
    }

    /** Revient au menu principal.
     * Appelé lors de l'appui sur le bouton Menu Principal . */
    @FXML
    private void selectReturn(){
        if(state == ChooserState.UPLOAD)
            stage.hide(); // ferme le dialog
        else
            framework.previous(); // retourne au GUI précédent (on était dans la fenêtre principale)
    }



    /* Méthodes à conserver dans l'alternative où seul le challenge cliqué doit être rafraîchit lors du retour. */

    /** Compare deux tiles sur leurs coordonnées selon l'ordre lexicographique sur (Y,X) .
     *
     * @param u le 1er tile
     * @param v le 2nd tile
     * @return selon l'ordre lexicographique sur leurs coordonnées :
     *         -1 si u < v
     *          0 si u = v
     *         +1 si u > v */
    private static int compareTiles(ChallengeTile u, ChallengeTile v){
        Integer x1 = GridPane.getColumnIndex(u);
        Integer y1 = GridPane.getRowIndex(u);

        Integer x2 = GridPane.getColumnIndex(v);
        Integer y2 = GridPane.getRowIndex(v);

        return compareCoords(x1, y1, x2, y2);
    }

    /** Compare les coordonnées selon l'ordre lexicographique sur (Y,X) .
     *
     * @param x1 numéro de colonne du 1er couple
     * @param y1 numéro de ligne du 1er couple
     * @param x2 numéro de colonne du 2nd couple
     * @param y2 numéro de ligne du 2nd couple
     * @return selon l'ordre lexicographique :
     *         -1 si (y1,x1) < (y2,x2)
     *          0 si (y1,x1) = (y2,x2)
     *         +1 si (y1,x1) > (y2,x2) */
    private static int compareCoords(Integer x1, Integer y1, Integer x2, Integer y2){
        assert x1 != null && y1 != null && x2 != null && y2 != null;

        if(y1 < y2 || (y1.equals(y2) && x1 < x2))
            return -1;
        else if(y1.equals(y2) && x1.equals(x2))
            return 0;
        else
            return +1;
    }

    /** Décrémente les coordonnées (x,y) de 1.
     *
     * Équivalent à :
     * <code>
     *     x' = x-1;
     *     if(x'<0){
     *         y' = y-1;
     *         x' = n-1;
     *     }
     *     return (x',y')
     * </code>
     *
     * @param x numéro de colonne
     * @param y numéro de ligne
     * @param n nombre total de colonnes
     * @return le couple (x', y') résultant de la décrémentation */
    private static Map.Entry<Integer, Integer> decrementCoords(int x, int y, int n){
        // attention à l'ordre, donc il vaut mieux les laisser en séquentiel comme ça
        y -= (x == 0) ? 1 : 0;
        x = (x-1+n) % n;

        return new AbstractMap.SimpleEntry<>(x, y);
    }
}
