package Interface;

import javafx.fxml.FXMLLoader;

import java.net.URL;

/** Parseur de fichier FXML spécialisé dans la création des controllers GUI.
 * Cela permet d'éviter un cast lorsque l'on veut appeler les méthodes définies dans la classe abstraite {@link GUI}.
 *
 * @author Louri */
public class GUILoader<T extends GUI> extends FXMLLoader {
    /** Crée un loader pour la ressource FXML donnée.
     *
     * @param fxml l'URL de la ressource FXML */
    public GUILoader(URL fxml){
        super(fxml);
    }

    /** Crée et renvoie un nouveau GUI associé au FXML chargé.
     *
     * @return un nouveau GUI correspondant au FXML chargé */
    public T getGUI(){
        return super.getController();
    }
}
