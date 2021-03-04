package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.MetaContext;

import static ShellNightmare.Terminal.DaemonMessage.CLEAR;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

public class Clear extends Command {
    @Override
    public void processCommand() {
        MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(CLEAR));
    }
}
