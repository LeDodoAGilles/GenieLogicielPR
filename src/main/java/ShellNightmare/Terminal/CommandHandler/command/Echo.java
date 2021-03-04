package ShellNightmare.Terminal.CommandHandler.command;


import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.DaemonMessage;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.MetaContext;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Raw)
public class Echo extends Command {
boolean noIndent = false;
    @Override
    public void processCommand() {
        if (noIndent)
            for (String std : stdin)
                MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(DaemonMessage.STDOUTni,std));
            else
                stdout.addAll(stdin);
    }

    public void OARG_i() {
        noIndent = true;
    }

}
