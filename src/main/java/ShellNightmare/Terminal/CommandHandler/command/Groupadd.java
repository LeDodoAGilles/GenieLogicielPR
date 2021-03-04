package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.CommandHandler.Permission;
import ShellNightmare.Terminal.FileSystem.Group;
import ShellNightmare.Terminal.FileSystem.IOStack;

import static ShellNightmare.Terminal.FileSystem.E_IOStatus.ALREADY_EXIST_GROUP;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@Permission
@NeededParameters(value = "1")
public class Groupadd extends Command {
    boolean remove = false;
    @Override
    public void processCommand() {
        String newGroup = neededParameters.get(0);

        if (remove){
            context.removeGroup(newGroup);
            return;
        }
        var og = context.getGroup(newGroup);
        if (og.isEmpty()) {
            addErrorMessages(IOStack.interpreterStack(ALREADY_EXIST_GROUP, newGroup));
            return;
        }
        context.addNewGroup(newGroup);
    }

    public void OARG_r() {
        remove=true;
    }
}