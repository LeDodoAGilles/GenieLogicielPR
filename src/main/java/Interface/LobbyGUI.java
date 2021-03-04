package Interface;

import Monitoring.Message;
import Monitoring.message.*;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.challenge.Challenge;
import ShellNightmare.Terminal.challenge.Score;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.StageStyle;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.time.Duration;

public class LobbyGUI extends GUI {
    public static final String FXML = "/FXML/Lobby.fxml";
    private static final String HTML_HELP = "help/index.html";

    @FXML
    private BorderPane challengePane;
    @FXML
    private Button closeConnectionButton;
    @FXML
    private ListView<String> clientsLV;
    @FXML
    private Button chooseButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Button launchButton;
    @FXML
    private Label ipLabel;

    private Challenge challenge;

    private boolean connectionsClosed = false; // ne sert que pour le serveur

    private Thread listeningTheServerThread;
    private Thread uploadThread;

    @Override
    public void reset(Object[] args) { // côté client, après avoir fini de jouer
        stage.setTitle(EscapeTheShell.TITLE);

        try{
            if(MetaContext.win)
                framework.monitor.clientSideHandler.send(new ScoreMessage(MetaContext.scoreProp.get()));
            else
                framework.monitor.clientSideHandler.send(new ScoreMessage(new Score(MetaContext.username, 0L)));
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void init(Object[] args) {
        assert args.length == 2;
        Boolean isServer = (Boolean) args[0];
        String ip = (String) args[1];

        if(isServer)
            initServerSide(ip);
        else
            initClientSide();
    }

    private void initServerSide(String ip){
        stage.setTitle(EscapeTheShell.TITLE + " : Serveur");

        ipLabel.setText(String.format("ip : %s", ip));

        framework.monitor.setupServer();
        framework.monitor.server.lobby = this;

        try {
            framework.monitor.listenEnteringConnections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initClientSide(){
        stage.setTitle(EscapeTheShell.TITLE + " : Client");

        chooseButton.setDisable(true);
        uploadButton.setDisable(true);
        launchButton.setDisable(true);
        closeConnectionButton.setDisable(true);
        ipLabel.setVisible(false);

        framework.monitor.clientSideHandler.lobby = this;
        framework.monitor.clientSideHandler.setupBehaviours();
        listeningTheServerThread = new Thread(framework.monitor.clientSideHandler);
        listeningTheServerThread.start();

        try {
            framework.monitor.clientSideHandler.send(new PresentezVousMessage(MetaContext.username));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {

    }

    @FXML
    private void selectCloseConnection(){
        closeConnectionButton.setDisable(true);
        try {
            framework.monitor.closeEnteringConnections();
            connectionsClosed = true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void selectChooseChallenge() throws IOException {
        ChooserGUI dialog = framework.makeGUIdialog(ChooserGUI.FXML, "Challenge à uploader", true, StageStyle.DECORATED, new Object[]{ChooserGUI.ChooserState.UPLOAD});;
        dialog.stage.showAndWait();

        challenge = dialog.getChallengeToUpload();

        if(challenge == null) // annulé
            return;

        ChallengeTile tile = new ChallengeTile(challenge.header, SwingFXUtils.toFXImage(challenge.image, null), null);
        challengePane.setCenter(tile);
        tile.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.SECONDARY)){ // clic droit
                framework.showScores(tile);
            }
        });
    }

    @FXML
    private void selectUpload(){
        if(!connectionsClosed){
            Framework.showError("Transfert impossible", "Il est demandé de fermer les connexions entrantes avant.");
            return;
        }

        if(uploadThread != null && uploadThread.isAlive())
            return;

        String password = challenge.header.generatePassword().getKey();
        challenge.header.hashedRootPassword = DigestUtils.sha512Hex(password);
        challenge.context.rootUser.hash = challenge.header.hashedRootPassword;
        System.out.printf("password=%s, hash=%s\n", password, challenge.context.rootUser.hash);

        String savedFilename = challenge.filename;
        challenge.filename = savedFilename;

        // in another thread parce que c'est long
        uploadThread = new Thread(() -> {
            framework.monitor.server.broadcast(new DownloadMessage(challenge));
        });
        uploadThread.start();

        Framework.showAlert("Session démarrée", "Le mot de passe pour cette session est :", password, Alert.AlertType.INFORMATION, false);
    }

    @FXML
    private void selectLaunch(){
        if(!connectionsClosed){
            Framework.showError("Transfert impossible", "Il est demandé de fermer les connexions entrantes avant.");
            return;
        }

        if(!framework.monitor.server.isEveryoneReadyToPlay()){
            Framework.showError("Lancement impossible", "Certains joueurs ne sont pas encore prêt.");
            return;
        }

        framework.monitor.server.broadcast(new LetsBeginMessage());
    }

    public void receivePresentezVous(Message<?> msg){
        Platform.runLater(() -> clientsLV.getItems().add((String) msg.data));
    }

    public void anotherDisconnected(String pseudo){
        Platform.runLater(() -> clientsLV.getItems().remove((String) pseudo));
    }

    public void receiveDownload(Message<?> msg){
        challenge = (Challenge) msg.data;

        ChallengeTile tile = new ChallengeTile(challenge.header, SwingFXUtils.toFXImage(challenge.image, null), null);
        Platform.runLater(() -> challengePane.setCenter(tile));

        try {
            framework.monitor.clientSideHandler.send(new ReadyMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveLetsBegin(Message<?> msg){
        Platform.runLater(() -> {
            try {
                framework.next(GameGUI.FXML, new Object[]{challenge});
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void receiveScore(Message<?> msg){
        Score score = (Score) msg.data;
        if(score.time.time.equals(Duration.ofNanos(0))){
            System.out.printf("%d a abandonné\n", msg.srcId);
        }
        else {
            System.out.printf("%d scored %s\n", msg.srcId, score);
            challenge.scores.add(score);
            challenge.updateScoresOnDisk();
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

    /** Quitte l'application.
     * Appelé lors de l'appui sur le bouton Quitter . */
    @FXML
    private void selectExit(){
        stage.setTitle(EscapeTheShell.TITLE);

        if(uploadThread != null)
            uploadThread.interrupt();
        framework.monitor.dispose();
        framework.previous();
    }
}
