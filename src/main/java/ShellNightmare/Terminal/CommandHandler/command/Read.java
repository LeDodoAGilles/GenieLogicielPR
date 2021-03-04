package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.MetaContext;

import static ShellNightmare.Terminal.DaemonMessage.READ_INPUT;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@NeededParameters(value = "1")
public class Read extends Command {
    @Override
    public void processCommand() {
        String variable = neededParameters.get(0);
        MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(READ_INPUT));
        DaemonStack e = MetaContext.mainDaemon.getMessage();
        String message = (String) e.getMessage();
        message=message.substring(message.indexOf("\n")+1);
        context.setEnvar(variable, message);
    }
}
