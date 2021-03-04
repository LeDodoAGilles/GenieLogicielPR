package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.MetaContext;

import static ShellNightmare.Terminal.DaemonMessage.MAN;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@NeededParameters(value = "1")
public class Man extends Command {
    @Override
    public void processCommand() {
        String command = neededParameters.get(0);
        if (!MetaContext.registerC.getCommandsString().contains(command)){
            addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_COMMAND));
            return;
        }
        String data = MetaContext.doc.formatCommand(command);
        MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(MAN,new String[]{command,data}));
        MetaContext.mainDaemon.getMessage();

    }
}
