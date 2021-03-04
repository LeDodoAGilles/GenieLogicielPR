package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.Permission;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.MetaContext;

import static ShellNightmare.Terminal.DaemonMessage.ESCAPED;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@Permission
public class Escape extends Command {
    @Override
    public void processCommand() {
        MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(ESCAPED));
    }
}
