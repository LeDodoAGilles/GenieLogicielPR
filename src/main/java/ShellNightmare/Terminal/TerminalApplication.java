package ShellNightmare.Terminal;

import ShellNightmare.Terminal.TerminalFX.ManMode;
import ShellNightmare.Terminal.TerminalFX.audio.AudioRegister;
import ShellNightmare.Terminal.TerminalFX.nano.NanoMode;
import javafx.application.Platform;
import utils.CssToColorHelper;
import ShellNightmare.Terminal.TerminalFX.Terminal;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.keycode.KeyCodeWatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static ShellNightmare.Terminal.MetaContext.mainDaemon;

public class TerminalApplication extends Application{
    private static final int WINDOW_WIDTH = 720;
    private static final int WINDOW_HEIGHT = 480;
    private static final String WINDOW_TITLE = "TerminalFx : Test";

    // exemple https://stackoverflow.com/questions/13227809/displaying-changing-values-in-javafx-label
    private void bindToTime(StringProperty prop) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.S");
        Calendar origin = Calendar.getInstance();
        int hour = origin.get(Calendar.HOUR_OF_DAY);
        int minute = origin.get(Calendar.MINUTE);
        int second = origin.get(Calendar.SECOND);
        int milli = origin.get(Calendar.MILLISECOND);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        actionEvent -> {
                            Calendar time = Calendar.getInstance();
                            time.add(Calendar.HOUR_OF_DAY, -hour);
                            time.add(Calendar.MINUTE, -minute);
                            time.add(Calendar.SECOND, -second);
                            time.add(Calendar.MILLISECOND, -milli);
                            prop.set(simpleDateFormat.format(time.getTime()));
                        }
                ),
                new KeyFrame(Duration.millis(100))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        //timeline.play();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        root.getChildren().add(CssToColorHelper.HELPER);

        NanoMode.Init(getClass().getResource("/Texts/nano_help.txt"));
        ManMode.Init(getClass().getResource("/Texts/man_help.txt"));

        Label label = new Label();
        StringProperty prop = new SimpleStringProperty();
        prop.set("0");
        bindToTime(prop);
        label.textProperty().bind(prop);
        root.setTop(label);

        MetaContext.Init(); // a placer ici
        Context c;
        File doc = new File("context.bin");
        if (!doc.exists()) {
            c = new Context();
        }
        else{
            FileInputStream file = new FileInputStream(doc);
            ObjectInputStream in = new ObjectInputStream(file);
            c = (Context) in.readObject();
            in.close();
            file.close();
        }


        Terminal terminal = new Terminal();
        mainDaemon.setData(terminal,c);
        terminal.init();
        root.setCenter(terminal);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add("arch.css");
        //scene.getStylesheets().add("ubuntu.css");

        KeyCodeWatcher.WATCHER.init(scene);

        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(scene);

        // Faire des traitement avant de quitter (croix rouge, alt+F4)
        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(e -> {
            AudioRegister.INSTANCE.removeAll();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void submain(String[] args){
        Application.launch();
    }
}
