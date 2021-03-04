package Interface;

import Monitoring.Monitor;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.Socket;

public class JoinPane extends BorderPane {
    private static final String LOADING_ICON = "/Images/load.png";
    private static final int ICON_SIZE = 32;

    private Thread lookForServerThread;
    private volatile String ipAdress = "";
    public Socket socket = null;

    private RotateTransition rt;
    private ImageView spinningWheel;

    public JoinPane(){
        Label ipLabel = new Label("Rentrez l'adresse IP du serveur à rejoindre :");
        ipLabel.setPadding(new Insets(10, 10, 10, 10));
        this.setTop(ipLabel);

        TextField ipTF = new TextField();
        ipTF.setMinWidth(50);
        ipTF.setPrefWidth(50);
        ipTF.setMaxWidth(200);
        ipTF.setPromptText("exemple : 192.168.1.83");
        ipTF.textProperty().addListener((e, o, n) -> ipAdress = n);
        this.setCenter(ipTF);

        StackPane footerS = new StackPane();
        this.setBottom(footerS);

        GridPane footer = new GridPane();
        footer.setPadding(new Insets(10, 10, 10, 10));
        footer.setHgap(10);
        this.setBottom(footer);

        Button cancel = new Button("Annuler");
        cancel.setOnAction(e -> doCancel());
        GridPane.setHalignment(cancel, HPos.LEFT);
        footer.add(cancel, 0, 0);

        spinningWheel = new ImageView(new Image(getClass().getResource(LOADING_ICON).toExternalForm()));
        spinningWheel.setVisible(false);
        spinningWheel.setPreserveRatio(true);
        spinningWheel.setFitWidth(ICON_SIZE);
        spinningWheel.setFitHeight(ICON_SIZE);
        footer.add(spinningWheel, 1, 0);

        rt = new RotateTransition(Duration.millis(1000), spinningWheel);
        rt.setByAngle(180);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setAutoReverse(false);
        rt.play();

        Button ok = new Button("Se connecter");
        ok.setOnAction(e -> doOK());
        GridPane.setHalignment(ok, HPos.RIGHT);
        footer.add(ok, 2, 0);

        Platform.runLater(() -> this.getScene().getWindow().setOnCloseRequest(e -> doCancel()));
    }

    private void doOK(){
        if(lookForServerThread == null || !lookForServerThread.isAlive()){
            lookForServerThread = new Thread(() -> {
                String savedIpAdress = ipAdress;
                try {
                    socket = new Socket(savedIpAdress, Monitor.PORT);
                } catch (IOException e) {
                    Platform.runLater(() -> Framework.showError("Connexion échouée", "Impossible de se connecter à l'adresse " + savedIpAdress));
                    Platform.runLater(() -> spinningWheel.setVisible(false));
                    e.printStackTrace();
                    socket = null;
                    return;
                }
                Platform.runLater(() -> {
                    rt.stop();
                    getScene().getWindow().hide();
                });
            });

            lookForServerThread.start();
            spinningWheel.setVisible(true);
        }
    }

    private void doCancel(){
        if(lookForServerThread != null && lookForServerThread.isAlive()){
            if(socket != null) {
                try {
                    socket.close();
                    lookForServerThread.interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            spinningWheel.setVisible(false);
        }
        else{
            rt.stop();
            this.getScene().getWindow().hide();
        }
    }
}
