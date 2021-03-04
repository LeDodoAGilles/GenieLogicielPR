package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.FileSystem.*;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Read)
@NeededParameters("1")
public class Chmod extends Command {
    @Override
    public void processCommand() {
        String perm = neededParameters.get(0);
        short i;
        try {
             i = Short.parseShort(perm);
        }
        catch(Exception e){
            stderr.add("mode invalide: "+perm);
            return;
        }
        for(PseudoPath p : InputPseudoPath){
            File<?> f = p.getChildFile();
            if (f.getInodeData().getPermission(context.currentUser).contains(Permission.EXECUTION))
                f.getInode().setPermission(i);
            else
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.PERMISSION,"execution"));
    }

    }
}
