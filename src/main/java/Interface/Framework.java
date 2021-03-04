package Interface;

import Monitoring.Monitor;
import ShellNightmare.Terminal.challenge.Challenge;
import ShellNightmare.Terminal.challenge.ScoreList;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SystemUtils;
import utils.config.Config;

import java.awt.Desktop;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static Interface.EscapeTheShellConfig.*;

/** Gestionnaire de {@link GUI}s permettant de les créer et de naviguer de l'un à l'autre.
 * S'occupe aussi de créer les dialogs, afin d'uniformiser leur apparence à travers l'application.
 *
 * La création des GUIs se fait depuis leur fichier FXML associé.
 *
 * REMARQUE IMPORTANTE
 * Notre implémentation oblige que l'objet à la racine du FXML soit un {@link Pane}, ce qui est plus restrictif
 * qu'un {@link Parent} ou qu'une {@link Region} (par exemple, cela ne peut pas être un {@link SplitPane}
 * qui hérite de {@link Control}. L'astuce est de mettre le composant problématique au centre d'un BorderPane.
 *
 * @author Louri */
public class Framework {
    /** Stylesheet d'initialisation du style de l'application. */
    private static final String MAIN_STYLESHEET = "/main.css";

    /** Configuration de l'application. */
    public Config config;

    /** Stage de la fenêtre principale. */
    public final Stage primaryStage;

    /** Icône de toutes les fenêtres, afin de garder une cohérence au niveau de l'application.
     * N'a besoin d'être initialisée qu'une seule fois. */
    private final Image icon;

    /** Background appliqué à toutes les fenêtres, afin de garder une cohérence au niveau de l'application. */
    private final Paint paint;

    /** Ressource du stylesheet principal des fenêtres, déjà en forme externalisée. */
    private final String stylesheetResource;

    /** Container principal prenant toute la fenêtre.
     * C'est un AnchorPane afin de pouvoir étendre n'importe quel contenu jusqu'à ses bords.
     * Il ne possède qu'un seul enfant. */
    private final AnchorPane root;

    /** Pile LIFO de GUIs s'étant succédés par la méthode next.
     * Le dernier GUI inséré est le GUI actuel.
     * La sauvegarde successive des GUIs permet de retourner au GUI précédent facilement. */
    private final Deque<GUI> guiHistory;

    /** Gestionnaire de la partie Client-Serveur. */
    public final Monitor monitor;

    /**
     * Configure la fenêtre principale.
     *
     * @param primaryStage Stage principal
     * @param icon         chemin vers la ressource de l'icône des fenêtres
     * @param title        titre de la fenêtre principale
     * @param fullscreen   si la fenêtre doit être lancée en fullscreen
     * @param width        largeur par défaut de la fenêtre principale (lorsqu'elle n'est pas en fullscreen)
     * @param height       hauteur par défaut de la fenêtre principale (lorsqu'elle n'est pas en fullscreen)
     * @param paint        couleur de background des fenêtres */
    public Framework(Stage primaryStage, String icon, String title, boolean fullscreen, int width, int height, boolean resizeable, Paint paint, Config config){
        this.primaryStage = primaryStage;
        this.icon = new Image(getClass().getResource(icon).toExternalForm()); // On n'a besoin de créer l'Image qu'une fois car peut être partagée entre plusieurs ImageViewer
        this.paint = paint;
        this.stylesheetResource = getClass().getResource(MAIN_STYLESHEET).toExternalForm();

        this.config = config;

        primaryStage.getIcons().add(this.icon);
        primaryStage.setTitle(title);
        primaryStage.setFullScreen(fullscreen);

        root = new AnchorPane();
        Scene scene = new Scene(root, width, height, paint);
        scene.getStylesheets().add(stylesheetResource);

        primaryStage.setScene(scene);
        primaryStage.setResizable(resizeable);

        // https://stackoverflow.com/questions/20732100/additional-margins
        if(!resizeable)
            primaryStage.sizeToScene(); // à utiliser si resizable(false) pour régler un bug

        guiHistory = new ArrayDeque<>();

        monitor = new Monitor();
    }

    public void setConfig(Config config){
        this.config = config;
    }

    /** Navigue vers le précédent GUI.
     *
     * @exception java.util.NoSuchElementException il n'y a pas de précédent GUI */
    public void previous(){
        previous(new Object[]{});
    }

    /** Navigue vers le précédent GUI.
     *
     * @param args une liste d'arguments nécessaires à l'interprétation du retour
     * @exception java.util.NoSuchElementException il n'y a pas de précédent GUI */
    public void previous(Object[] args){
        if(guiHistory.isEmpty())
            throw new NoSuchElementException("Aucun GUI n'a encore été chargé.");

        GUI gui = guiHistory.pop(); // supprime le GUI actuel de l'historique
        if(guiHistory.isEmpty()){
            guiHistory.push(gui); // on le remet
            throw new NoSuchElementException("Le GUI actuel est le premier : il n'a pas de précédent.");
        }

        gui.dispose();
        root.getChildren().remove(0); // enlève le contenu actuel
        //noinspection ConstantConditions
        setStageContent(primaryStage, guiHistory.peek().localRoot); // met le contenu du GUI précédent
        //noinspection ConstantConditions
        guiHistory.peek().reset(args);
    }

    /** Navigue vers le prochain GUI stocké dans le fichier FXML donné.
     * Est aussi appelé pour charger le premier GUI de l'application.
     *
     * @param fxml chemin vers la ressource FXML */
    public void next(String fxml) throws IOException {
        next(fxml, new Object[]{});
    }

    /** Navigue vers le prochain GUI stocké dans le fichier FXML donné.
     * Est aussi appelé pour charger le premier GUI de l'application.
     *
     * @param fxml chemin vers la ressource FXML
     * @param args une liste d'arguments nécessaires à l'initialisation */
    public <T extends GUI> void next(String fxml, Object[] args) throws IOException {
        GUILoader<T> loader = new GUILoader<>(getClass().getResource(fxml)); // Charge le fichier FXML
        Pane content = loader.load(); // Noeud à l'origine de la hiérarchie du FXML

        // Si ce n'est pas le premier GUI, alors on doit remplacer l'ancien
        if(!guiHistory.isEmpty())
            root.getChildren().remove(0); // note: le noeud retiré n'est pas perdu car il est stocké dans le GUI
        setStageContent(primaryStage, content);

        T gui = loader.getGUI(); // Crée le controller du FXML
        gui.setLocals(this, primaryStage, content);
        gui.init(args);

        guiHistory.push(gui);
    }

    /** Remplace le contenu de la Scene du Stage sans changer son noeud root.
     * Étend le nouveau contenu jusqu'aux bords de la Scene.
     *
     * @param stage   le Stage dont on veut remplacer le contenu de la Scene
     * @param content le nouveau contenu de la Scene */
    private static void setStageContent(Stage stage, Pane content){
        /* Étend le contenu aux 4 bords de la *Scene*, pour être précis. */
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
        ((Pane) stage.getScene().getRoot()).getChildren().add(content);

        // Calcule la taille minimale de la fenêtre (pour le resize : sur le Stage), à partir du nouveau contenu.
        Platform.runLater(() -> { // runLater car on doit attendre que la fenêtre soit affichée
            try{
                setMinimalStageSize(stage, content);
            } catch(IllegalStateException e){
                showError("Impossible de déterminer la taille minimale de la fenêtre", "Raison : La fenêtre n'est pas visible.");
            }
        });
    }

    /** Calcule et applique la taille minimale du Stage à partir de son contenu.
     * On doit attribuer la taille minimale du contenu à la taille minimale de la Scene,
     * donc il faut prendre en compte les bordures du Stage pour calculer correctement sa taille minimale.
     *
     * @param stage   le Stage à qui appliquer la taille minimale calculée
     * @param content le contenu du Stage
     * @exception IllegalStateException le Stage n'a ni largeur ni hauteur (peut arriver s'il n'est pas visible) */
    private static void setMinimalStageSize(Stage stage, Pane content){
        Insets insets = getStageInsets(stage); // taille des bords de la fenêtre
        double w = content.minWidth(-1) + insets.getLeft() + insets.getRight();
        double h = content.minHeight(-1) + insets.getTop() + insets.getBottom();
        stage.setMinWidth(w);
        stage.setMinHeight(h);
    }

    /** Crée un dialog à partir de son contenu.
     * Utilise la même méthode que pour créer des GUIs.
     *
     * @param content le contenu du dialog à créer
     * @param title   le titre de la fenêtre */
    public Stage makeDialog(Pane content, String title){
        return makeDialog(content, title, false, StageStyle.DECORATED); // dialog décoré par défaut
    }

    /** Crée un dialog à partir de son contenu.
     * Utilise la même méthode que pour créer des GUIs.
     *
     * @param content    le contenu du dialog à créer
     * @param title      le titre de la fenêtre
     * @param resizeable si le dialog doit être redimensionnable
     * @param style      le style de la fenêtre (decorated ou non, notamment) */
    public Stage makeDialog(Pane content, String title, boolean resizeable, StageStyle style){
        Stage stage = new Stage(style); // fenêtre du dialog à créer
        stage.initModality(Modality.WINDOW_MODAL); // empêche l'édition des autres fenêtres
        stage.initOwner(primaryStage);

        if(style.equals(StageStyle.DECORATED)){
            stage.setTitle(title);
            stage.getIcons().add(icon);
        }

        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root, paint); // la taille sera automatique selon le contenu
        scene.getStylesheets().add(stylesheetResource);

        stage.setScene(scene);

        if(!resizeable){
            stage.setResizable(false);
            stage.sizeToScene(); // toujours pour éviter le bug avec setResizeable(false)
        }

        setStageContent(stage, content);

        return stage;
    }

    /** Crée un dialog à partir d'un GUI
     *
     * @param fxml       chemin vers la ressource FXML
     * @param title      le titre de la fenêtre
     * @param resizeable si le dialog doit être redimensionnable
     * @param style      le style de la fenêtre (decorated ou non, notamment)
     * @param args       les arguments à passer au GUI lors de sa création */
    public <T extends GUI> T makeGUIdialog(String fxml, String title, boolean resizeable, StageStyle style, Object[] args) throws IOException {
        Stage stage = new Stage(style); // fenêtre du dialog à créer
        stage.initModality(Modality.WINDOW_MODAL); // empêche l'édition des autres fenêtres
        stage.initOwner(primaryStage);

        if(style.equals(StageStyle.DECORATED)){
            stage.setTitle(title);
            stage.getIcons().add(icon);
        }

        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root, paint); // la taille sera automatique selon le contenu
        scene.getStylesheets().add(stylesheetResource);

        stage.setScene(scene);

        if(!resizeable){
            stage.setResizable(false);
            stage.sizeToScene(); // toujours pour éviter le bug avec setResizeable(false)
        }

        GUILoader<T> loader = new GUILoader<>(getClass().getResource(fxml)); // Charge le fichier FXML
        Pane content = loader.load(); // Noeud à l'origine de la hiérarchie du FXML

        setStageContent(stage, content);

        T gui = loader.getGUI(); // Crée le controller du FXML
        gui.setLocals(this, stage, content);
        gui.init(args);

        return gui;
    }

    /** Montre un dialog d'erreur.
     * Il devra être fermé avant de pouvoir interagir à nouveau avec les autres fenêtres.
     *
     * @param message message d'erreur
     * @param details détails de l'erreur */
    public static void showError(String message, String details){
        showAlert("Erreur", message, details, Alert.AlertType.ERROR, true);
    }

    /** Montre un dialog.
     *
     * @param title   titre du dialog
     * @param message message du dialog
     * @param details contenu détaillé du dialog
     * @param type    le type de dialog à montrer (erreur, information, ...)
     * @param wait    si l'apparition du dialog doit bloquer l'interaction avec les autres fenêtres de l'application */
    public static void showAlert(String title, String message, String details, Alert.AlertType type, boolean wait){
        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(details);

        if(wait)
            alert.showAndWait();
        else
            alert.show();
    }

    /** Calcule et renvoie la taille des bords d'un Stage.
     * Il s'agit là de la différence de taille sur chaque bord entre le Stage et sa Scene.
     * Si le Stage est une fenêtre décorée, cela renverra la taille de la décoration = des bords de la fenêtre.
     *
     * Utilisé notamment pour calculer correctement la taille minimale d'un Stage à partir de son contenu.
     *
     * @param stage le Stage dont on veut déterminer la taille des bords.
     * @return      la taille des bords du Stage
     * @exception IllegalStateException le Stage n'a ni largeur ni hauteur (peut arriver s'il n'est pas visible) */
    public static Insets getStageInsets(Stage stage){
        Scene scene = stage.getScene();
        // note : si le paramètre était la Scene, on accéderait au Stage avec getWindow

        if(Double.isNaN(stage.getWidth()))
            throw new IllegalStateException("Le stage n'a ni largeur ni hauteur.");

        return new Insets(
                scene.getY(), /* top */
                scene.getX(), /* left */
                stage.getHeight() - scene.getY() - scene.getHeight(), /* bottom */
                stage.getWidth() - scene.getX() - scene.getWidth() /* right */
        );
    }

    /** Charge un challenge depuis le disque et lance le jeu.
     * Appelé depuis ChooserGUI.playChallenge et EditorGUI.validate . (mais pas en multijoueur)
     *
     * @param filename le nom du challenge en .zip, sans la mention du dossier des challenges */
    public void launchGameFromFile(String filename){
        Challenge challenge = new Challenge();
        if(challenge.loadFromDisk(Challenge.CHALLENGES_FOLDER + "/" + filename) == null) {
            showError("Impossible de jouer à ce challenge.", "Le challenge ne peut pas être chargé.");
            return;
        }

        challenge.filename = filename;
        // détermine le mot de passe ici, évite de le faire depui l'écran de jeu (sinon problème avec multijoueur)
        String password = challenge.header.generatePassword().getKey();
        challenge.header.hashedRootPassword = DigestUtils.sha512Hex(password);
        challenge.context.rootUser.hash = challenge.header.hashedRootPassword;

        try {
            next(GameGUI.FXML, new Object[]{challenge});
        } catch (IOException e) { // ressource FXML non trouvée
            e.printStackTrace();
        }
    }

    /** Affiche les scores du challenge associé au tile donné.
     *
     * @param tile le tile du challenge dont on doit afficher les scores */
    public void showScores(ChallengeTile tile){
        if(tile.getFile() == null)
            return;

        ScoreList scores;
        try {
            scores = Challenge.readSimpleField(ScoreList.class, tile.getFile().getPath(), Challenge.SCORES);
        } catch (IOException e) {
            Framework.showError("Impossible de récupérer les scores", "Le challenge ne peut pas être chargé.");
            return;
        }

        ScorePane table = new ScorePane(scores);

        Stage stage = this.makeDialog(table, "Scores", true, StageStyle.DECORATED);
        stage.showAndWait();
    }

    /** Ouvre la page d'aide HTML dans le navigateur. Selon le GUI, différents points d'entrée sont possibles.
     *
     * @param html chemin vers le fichier HTML de l'aide à afficher */
    public void showInfo(String html){
        if(SystemUtils.IS_OS_WINDOWS){
            try {
                Desktop.getDesktop().browse(new File(html).toURI());
            } catch (IOException e) {
                showError("L'aide ne peut pas être affichée.", "La page d'aide n'a pas été trouvée à l'adresse " + html);
            }
        }
        else {
            try {
                if (Runtime.getRuntime().exec(new String[] { "which", "xdg-open" }).getInputStream().read() != -1) {
                    Runtime.getRuntime().exec(new String[] { "xdg-open", new File(html).toURI().toString() });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Affiche le menu des options. */
    public void showOption() throws IOException {
        OptionGUI dialog = this.makeGUIdialog(OptionGUI.FXML, "Options", true, StageStyle.DECORATED, new Object[]{config});
        dialog.stage.showAndWait();
    }

    /** Ajoute le style des couleurs nommées (terminal-XXX) au StringBuilder du stylesheet.
     *
     * @param sb  le StringBuilder du css du .root
     * @param key la clé dans la configuration dont on doit écrire le style */
    private void writeColorConfigToStringBuilder(StringBuilder sb, String key){
        config.getValue(key).ifPresent(s -> sb.append("\t").append(key).append(": ").append(s).append(";\n"));
    }

    /** Charge la configuration de l'application.
     * Met notamment à jour les couleurs dans toute l'application en rechargeant le stylesheet.
     *
     * Pour mettre à jour le stylesheet, il faut d'abord supprimer les stylesheets de la Scene qui pourraient
     * être embêtant. Il faut ensuite créer un nouveau fichier, y mettre tout le contenu du style voulu,
     * puis l'ajouter ayx stylesheets de la Scene. */
    public void loadSettings(){
        try {
            config.save(EscapeTheShellConfig.CONFIG);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //String m = getClass().getResource(MAIN_STYLESHEET).toExternalForm();
        //primaryStage.getScene().getStylesheets().removeAll(primaryStage.getScene().getStylesheets().stream().filter(s -> !s.equals(m)).collect(Collectors.toList()));

        primaryStage.getScene().getStylesheets().add(getClass().getResource((String) config.getValue(TERMINAL).get()).toExternalForm());

        try{
            File temp = new File(Files.createTempFile("EscapeTheShell", ".css").toUri());

            StringBuilder sb = new StringBuilder();

            sb.append("\n.root {\n");

            writeColorConfigToStringBuilder(sb, TERMINAL_BACKGROUND);
            writeColorConfigToStringBuilder(sb, TERMINAL_FOREGROUND);
            writeColorConfigToStringBuilder(sb, TERMINAL_BLACK);
            writeColorConfigToStringBuilder(sb, TERMINAL_RED);
            writeColorConfigToStringBuilder(sb, TERMINAL_GREEN);
            writeColorConfigToStringBuilder(sb, TERMINAL_YELLOW);
            writeColorConfigToStringBuilder(sb, TERMINAL_BLUE);
            writeColorConfigToStringBuilder(sb, TERMINAL_MAGENTA);
            writeColorConfigToStringBuilder(sb, TERMINAL_CYAN);
            writeColorConfigToStringBuilder(sb, TERMINAL_WHITE);
            writeColorConfigToStringBuilder(sb, TERMINAL_BRIGHT_BLACK);
            writeColorConfigToStringBuilder(sb, TERMINAL_BRIGHT_RED);
            writeColorConfigToStringBuilder(sb, TERMINAL_BRIGHT_GREEN);
            writeColorConfigToStringBuilder(sb, TERMINAL_BRIGHT_YELLOW);
            writeColorConfigToStringBuilder(sb, TERMINAL_BRIGHT_BLUE);
            writeColorConfigToStringBuilder(sb, TERMINAL_BRIGHT_MAGENTA);
            writeColorConfigToStringBuilder(sb, TERMINAL_BRIGHT_CYAN);
            writeColorConfigToStringBuilder(sb, TERMINAL_BRIGHT_WHITE);

            sb.append("}\n");

            /*try {
                sb.append(Files.readString(Path.of(getClass().getResource((String) config.getValue(TERMINAL).get()).toURI()), StandardCharsets.UTF_8));
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }*/

            sb.append("\n.caret {\t-fx-stroke: ").append(config.getValue(CARET).get()).append(";\n}\n");

            Files.writeString(Path.of(temp.getPath()), sb.toString(), StandardCharsets.UTF_8);

            primaryStage.getScene().getStylesheets().add("file:///" + temp.getAbsolutePath().replace("\\", "/"));
        } catch(IOException e){
            e.printStackTrace();
        }

        primaryStage.setFullScreen((Boolean) config.getValue(FULLSCREEN).get());
        Monitor.PORT = (Integer) config.getValue(PORT).get();
    }
}
