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
@NeededParameters(value = "1")
public class Chgrp extends Command {
    @Override
    public void processCommand() {
        String groupe = neededParameters.get(0);
        var og = context.getGroup(groupe);
        if (og.isEmpty()){
            addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_GROUP));
            return;
        }
        Group g = og.get();
        if (context.rootUser!=context.currentUser && !g.getMembers().contains(context.currentUser.uid)){
            addErrorMessages(IOStack.interpreterStack(E_IOStatus.USER_NOT_IN_GROUP,context.currentUser.name));
            return;
        }
        for(PseudoPath p : InputPseudoPath){
            File<?> f = p.getChildFile();
            if (f.getInodeData().getPermission(context.currentUser).contains(Permission.WRITE))
                f.getInode().setGroup(g);
            else
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.PERMISSION,f.getName()));
    }

    }
}
