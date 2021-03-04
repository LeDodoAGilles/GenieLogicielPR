package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.NullInput;
/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(NullInput)
public class History extends Command {
    Boolean simple = false;
    @Override
    public void processCommand() {
        stdout.addAll(simple?context.simplehistory:context.history);
    }

    public void OARG_c() {
        context.history.clear();
        context.simplehistory.clear();
    }

    public void OARG_s() {
        simple=true;
    }
}
