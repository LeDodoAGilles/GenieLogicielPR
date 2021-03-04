package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.NullInput;
import static ShellNightmare.Terminal.TypeContext.SCRIPT;
import static ShellNightmare.Terminal.TypeContext.STANDARD;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(NullInput)
public class Whoami extends Command {

    @Override
    public void processCommand() {

        if (context.type==STANDARD)
            stdout.add(context.currentUser.name);
        else
            stdout.add(context.historicalUser.name);
    }
}
