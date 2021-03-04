package ShellNightmare.Terminal;

import ShellNightmare.Terminal.CommandHandler.CommandRegister;
import ShellNightmare.Terminal.challenge.Score;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.regex.Pattern;

/**
 * classe stockant les données qui sont communes à tous les terminaux
 *@author Gaëtan Lounes
 */
public class MetaContext {
    public static final Pattern VALID_FILE_NAME = Pattern.compile("[A-z._0-9~()éàè]*");
    public static final Pattern COLOR_CODE = Pattern.compile("\\\\(e|033|x1B)\\[(\\d;?)*m");
    public static final  CommandRegister  registerC = CommandRegister.getInstance();
    public static Daemon mainDaemon = new Daemon(); //TODO: l'integrer potentionnellement dans le challenge
    public static DocTemplate doc;
    public static boolean win = false;
    public static ObjectProperty<Score> scoreProp;
    public static String username = "Invité"; // login
    public static void Init(){
        if (!registerC.getCommands().isEmpty())
            return;
        registerC.registerCommand();
        doc = DocTemplate.loadFromJson();;
        scoreProp = new SimpleObjectProperty<>(null);

    }

}
