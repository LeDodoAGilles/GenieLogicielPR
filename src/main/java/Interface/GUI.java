package Interface;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/** Un Controller associé à un GUI chargé depuis un fichier FXML.
 * Suivant le pattern MVC, le fichier FXML contient le Modèle,
 * cette classe GUI est le Contrôleur, et le Stage et la Scene sont la Vue.
 *
 * @author Louri */
public abstract class GUI {
    /** Framework auquel appartient le GUI. */
    protected Framework framework;

    /** Stage contenant la Scene qui contient dans sa descendance le localRoot de ce GUI. */
    protected Stage stage;

    /** Premier noeud dans la hiérarchie chargée depuis le FXML. */
    protected Pane localRoot;

    /** Initialise le Stage et le localRoot du GUI.
     * Il est en effet nécessaire de les sauvegarder afin d'y ré-accéder plus tard.
     *
     * @param framework le framework auquel appartient le GUI
     * @param stage     le Stage du GUI
     * @param localRoot le premier noeud dans la hiérarchie FXML de ce GUI */
    public void setLocals(Framework framework, Stage stage, Pane localRoot){
        this.framework = framework;
        this.stage = stage;
        this.localRoot = localRoot;
    }

    /** Traitements à faire lors du retour à ce GUI.
     * Appelé par le {@link Framework} lors du retour à ce GUI.
     *
     * @param args une liste d'arguments nécessaires à l'interprétation du retour */
    public abstract void reset(Object[] args);

    /** Traitements à faire lors de l'entrée dans ce GUI.
     * Appelé par le {@link Framework} à la création du GUI, après setLocals.
     *
     * @param args une liste d'arguments nécessaires à l'initialisation */
    public abstract void init(Object[] args);

    /** Libération des ressources de ce GUI.
     * Appelé par le {@link Framework} lorsque le GUI est définitivement quitté. */
    public abstract void dispose();
}
