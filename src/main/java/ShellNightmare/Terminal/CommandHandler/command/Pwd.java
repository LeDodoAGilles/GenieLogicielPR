package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.NullInput;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(NullInput)
public class Pwd extends Command {
    @Override
    public void processCommand() {
        stdout.add(context.currentPath.toString());
    }


}
