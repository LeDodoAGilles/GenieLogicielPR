package Interface;

import javafx.scene.control.*;

import javafx.fxml.FXML;
import utils.config.*;

import static Interface.EscapeTheShellConfig.*;

public class OptionGUI extends GUI {
    public static final String FXML = "/FXML/Option.fxml";

    @FXML private CheckBox fullscreenCheck;
    @FXML private TextField widthTF;
    @FXML private TextField heightTF;
    @FXML private TextField portTF;
    @FXML private ComboBox<String> terminalStyleCB;
    @FXML private TextField caretTF;
    @FXML private Label caretLabel;
    @FXML private TextField foregroundTF;
    @FXML private Label foregroundLabel;
    @FXML private TextField backgroundTF;
    @FXML private Label backgroundLabel;

    @FXML private TextField blackTF;
    @FXML private Label blackLabel;
    @FXML private TextField redTF;
    @FXML private Label redLabel;
    @FXML private TextField greenTF;
    @FXML private Label greenLabel;
    @FXML private TextField yellowTF;
    @FXML private Label yellowLabel;
    @FXML private TextField blueTF;
    @FXML private Label blueLabel;
    @FXML private TextField magentaTF;
    @FXML private Label magentaLabel;
    @FXML private TextField cyanTF;
    @FXML private Label cyanLabel;
    @FXML private TextField whiteTF;
    @FXML private Label whiteLabel;

    @FXML private TextField BblackTF; // bright
    @FXML private Label BblackLabel;
    @FXML private TextField BredTF;
    @FXML private Label BredLabel;
    @FXML private TextField BgreenTF;
    @FXML private Label BgreenLabel;
    @FXML private TextField ByellowTF;
    @FXML private Label ByellowLabel;
    @FXML private TextField BblueTF;
    @FXML private Label BblueLabel;
    @FXML private TextField BmagentaTF;
    @FXML private Label BmagentaLabel;
    @FXML private TextField BcyanTF;
    @FXML private Label BcyanLabel;
    @FXML private TextField BwhiteTF;
    @FXML private Label BwhiteLabel;

    @Override
    public void reset(Object[] args) {

    }

    private void bind(TextField tf, Label label, String key, Config config){
        tf.setText((String) config.getValue(key).get());

        try{
            label.setStyle("-fx-background-color: " + ColorConfigItem.transform(tf.getText()) + ";");
        } catch(Exception ignored) {
            return;
        }

        tf.textProperty().addListener((e, o, n) -> {
            String c;
            try{
                c = ColorConfigItem.transform(n);
            } catch(Exception ex){
                return;
            }

            config.setItem(new ColorConfigItem(key, c, ""));
            label.setStyle("-fx-background-color: " + c + ";");
        });
    }

    @Override
    public void init(Object[] args) {
        assert args.length == 1;
        EscapeTheShellConfig config = (EscapeTheShellConfig) args[0];

        terminalStyleCB.getItems().addAll(TERMINAL_STYLESHEETS);
        terminalStyleCB.getSelectionModel().select((String) config.getValue(TERMINAL).get());
        terminalStyleCB.getSelectionModel().selectedItemProperty().addListener((e, o, n) -> {
            config.setItem(new EnumConfigItem(TERMINAL, TERMINAL_STYLESHEETS, n, ""));
            setTerminalStyle(n);
        });

        fullscreenCheck.setSelected((Boolean) config.getValue(FULLSCREEN).get());
        fullscreenCheck.selectedProperty().addListener((e, o, n) -> config.setItem(new BooleanConfigItem(FULLSCREEN, n, "")));

        widthTF.setText(String.valueOf((Integer) config.getValue(WIDTH).get()));
        widthTF.textProperty().addListener((e, o, n) -> {
            try{
                config.setItem(new IntegerConfigItem(WIDTH, Integer.parseInt(n), ""));
            } catch(Exception ignored){

            }
        });

        heightTF.setText(String.valueOf((Integer) config.getValue(HEIGHT).get()));
        heightTF.textProperty().addListener((e, o, n) -> {
            try{
                config.setItem(new IntegerConfigItem(HEIGHT, Integer.parseInt(n), ""));
            } catch(Exception ignored){

            }
        });

        portTF.setText(String.valueOf((Integer) config.getValue(PORT).get()));
        portTF.textProperty().addListener((e, o, n) -> {
            try{
                config.setItem(new IntegerConfigItem(PORT, Integer.parseInt(n), ""));
            } catch(Exception ignored){

            }
        });

        bind(caretTF, caretLabel, CARET, config);
        bind(foregroundTF, foregroundLabel, TERMINAL_FOREGROUND, config);
        bind(backgroundTF, backgroundLabel, TERMINAL_BACKGROUND, config);

        bind(blackTF, blackLabel, TERMINAL_BLACK, config);
        bind(redTF, redLabel, TERMINAL_RED, config);
        bind(greenTF, greenLabel, TERMINAL_GREEN, config);
        bind(yellowTF, yellowLabel, TERMINAL_YELLOW, config);
        bind(blueTF, blueLabel, TERMINAL_BLUE, config);
        bind(magentaTF, magentaLabel, TERMINAL_MAGENTA, config);
        bind(cyanTF, cyanLabel, TERMINAL_CYAN, config);
        bind(whiteTF, whiteLabel, TERMINAL_WHITE, config);

        bind(BblackTF, BblackLabel, TERMINAL_BRIGHT_BLACK, config);
        bind(BredTF, BredLabel, TERMINAL_BRIGHT_RED, config);
        bind(BgreenTF, BgreenLabel, TERMINAL_BRIGHT_GREEN, config);
        bind(ByellowTF, ByellowLabel, TERMINAL_BRIGHT_YELLOW, config);
        bind(BblueTF, BblueLabel, TERMINAL_BRIGHT_BLUE, config);
        bind(BmagentaTF, BmagentaLabel, TERMINAL_BRIGHT_MAGENTA, config);
        bind(BcyanTF, BcyanLabel, TERMINAL_BRIGHT_CYAN, config);
        bind(BwhiteTF, BwhiteLabel, TERMINAL_BRIGHT_WHITE, config);
    }

    private void setTerminalStyle(String style){ // plus le temps de faire propre
        if(style.contains("arch")){
            foregroundTF.setText("#17a88b");
            backgroundTF.setText("#000");
            blackTF.setText("#000");
            redTF.setText("205,0,0");
            greenTF.setText("0,205,0");
            yellowTF.setText("205,205,0");
            blueTF.setText("0,0,238");
            magentaTF.setText("205,0,205");
            cyanTF.setText("0,205,205");
            whiteTF.setText("229,229,229");

            BblackTF.setText("127,127,127");
            BredTF.setText("#f00");
            BgreenTF.setText("#0f0");
            ByellowTF.setText("#ff0");
            BblueTF.setText("92,92,255");
            BmagentaTF.setText("#f0f");
            BcyanTF.setText("#0ff");
            BwhiteTF.setText("#fff");
        }
        else { // ubuntu
            foregroundTF.setText("#fff");
            backgroundTF.setText("38,4,32");
            blackTF.setText("1,1,1");
            redTF.setText("222,56,43");
            greenTF.setText("57,181,74");
            yellowTF.setText("255,199,6");
            blueTF.setText("0,111,184");
            magentaTF.setText("118,38,113");
            cyanTF.setText("44,181,233");
            whiteTF.setText("204,204,204");

            BblackTF.setText("128,128,128");
            BredTF.setText("#f00");
            BgreenTF.setText("#0f0");
            ByellowTF.setText("#ff0");
            BblueTF.setText("#00f");
            BmagentaTF.setText("#f0f");
            BcyanTF.setText("#0ff");
            BwhiteTF.setText("#fff");
        }
    }

    @Override
    public void dispose() {

    }

    /** Revient au menu précédent.
     * Appelé lors de l'appui sur le bouton Quitter . */
    @FXML
    private void selectOK(){
        framework.loadSettings();

        stage.hide();
    }

    @FXML
    private void selectReturn(){ // Annuler
        stage.hide();
    }
}
