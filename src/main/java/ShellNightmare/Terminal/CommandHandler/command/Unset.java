package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.IOStack;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.SuperRaw)
public class Unset extends Command {
    @Override
    public void processCommand() {
        for (String evar : stdin){
            if (context.getEnvar(evar).equals("")){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_VAR,evar));
                continue;
            }
            context.removeEnvar(evar);
        }
    }

    public void OARG_c() {
        context.sweepVar();
    }
}
