package Interface;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MultiModeGUI extends GUI {
    public static final String FXML = "/FXML/MultiMode.fxml";
    private static final String HTML_HELP = "help/index.html";

    @Override
    public void reset(Object[] args) {

    }

    @Override
    public void init(Object[] args) {

    }

    @Override
    public void dispose() {

    }

    @FXML
    private void selectJoin() throws IOException {
        JoinPane pane = new JoinPane();
        Stage stage = framework.makeDialog(pane, "Connexion");
        stage.showAndWait();

        if(pane.socket.isConnected()){
            try {
                framework.monitor.setOpenedClientSocket(pane.socket);
            } catch (IOException e) {
                Framework.showError("IOException", e.getMessage());
                e.printStackTrace();
                return;
            }

            framework.next(LobbyGUI.FXML, new Object[]{Boolean.FALSE, ""});
        }
    }

    @FXML
    private void selectHost() throws IOException {
        String ip = framework.monitor.acquireAutoIp();

        if(ip == null){
            Framework.showError("Impossible de créer un serveur", "Pas de connexion internet");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Host");
        alert.setHeaderText("Votre adresse ip est :");
        alert.setContentText(ip);

        alert.showAndWait();

        framework.next(LobbyGUI.FXML, new Object[]{Boolean.TRUE, ip});
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

    /** Quitte l'application.
     * Appelé lors de l'appui sur le bouton Quitter . */
    @FXML
    private void selectExit(){
        framework.previous();
    }
}
