package Interface;

import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.Type;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/** Élément d'un TreeView qui contient les informations principales d'un {@link File}.
 *
 * @author Louri */
public class FileLabel extends Label {
    public static final int ICON_SIZE = 16;
    public static final String FOLDER_ICON_PATH = "/Images/folder.png";
    public static Image folder_icon = null; // set à la première instanciation de EditorGUI

    public static Duration tooltipDelay = Duration.seconds(1);

    public File<?> file; // sauvegarde du fichier associé
    public String name; // nom de la cellule à afficher = du File
    public Type type;

    public final boolean required;
    public boolean visited = false; // utilisé lors du parcours de l'arbre pour sa mise à jour

    /** Crée une cellule renfermant le fichier donné.
     * La cellule aura le même nom et type que le fichier, n'aura pas de tooltip et ne sera pas required.
     *
     * @param file le fichier à sauvegarder dans la cellule */
    public FileLabel(File<?> file){
        this(file, false, file.getName(), file.getType(), "");
    }

    /** Crée une cellule renfermant le fichier donné.
     *
     * @param file        le fichier à sauvegarder dans la cellule
     *                    null s'il ne peut pas encore être récupéré
     * @param required    si le fichier en question est requis dans tous les challenges
     * @param name        le nom affiché dans la cellule, identique au nom du fichier qui est/sera stocké
     * @param type        le type de fichier qui est/sera stocké
     * @param description le court texte explicatif à afficher dans le tooltip */
    public FileLabel(File<?> file, boolean required, String name, Type type, String description){
        this.file = file;
        this.required = required;

        this.name = name;
        this.type = type;

        if(type == null)
            return;

        // contenu et style

        setText(name);
        getStyleClass().add("filecell");

        if(required)
            getStyleClass().add("required");

        switch(type){
            case FOLDER:
                // ajout d'une icône "dossier"

                ImageView iconView = new ImageView(folder_icon);
                iconView.setPreserveRatio(true);
                iconView.setFitWidth(ICON_SIZE);
                iconView.setFitHeight(ICON_SIZE);

                setGraphic(iconView);
                getStyleClass().add("folder");
                break;
            case SOFTLINK:
                getStyleClass().add("softlink");
                break;
            case BINARY:
                getStyleClass().add("binary");
                break;
            case DATA:
                getStyleClass().add("data");
                break;
        }

        // s'il y a une description, alors créer un tooltip
        if(description != null && !description.isEmpty()){
            Tooltip tooltip = new Tooltip(description);
            tooltip.setShowDelay(tooltipDelay);
            setTooltip(tooltip);
        }
    }

    /** Initialise les icônes si elles sont nulles.
     * Comme on a besoin d'aller chercher les ressources, une instance d'un objet quelconque est passé en paramètre.
     *
     * @param obj instance d'un objet quelconque que l'on puisse utilisé pour récupérer les ressources */
    protected static void Init(Object obj){
        if(folder_icon == null){
            FileLabel.folder_icon = new Image(obj.getClass().getResource(FOLDER_ICON_PATH).toExternalForm());
        }
    }

    /** Vérifie si cette cellule représente le fichier donné.
     *
     * @param file le fichier auquel comparer
     * @return true si la cellule représente correctement le fichier */
    public boolean is(File<?> file){
        // comparaison sur les éléments d'affichage et sur le fichier pointé
        // le type aussi, c'est important, si c'est différent vaut mieux tout refaire (to folder, styles, ...)
        return name.equals(file.getName()) && type == file.getType();
    }
}
