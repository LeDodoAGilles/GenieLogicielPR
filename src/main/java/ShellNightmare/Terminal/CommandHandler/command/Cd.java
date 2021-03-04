package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.FileSystem.Path;
import ShellNightmare.Terminal.FileSystem.Permission;

import static ShellNightmare.Terminal.FileSystem.E_IOStatus.NOT_A_FOLDER;
import static ShellNightmare.Terminal.FileSystem.E_IOStatus.PERMISSION;
import static ShellNightmare.Terminal.FileSystem.IOStack.interpreterStack;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Read)
public class Cd extends Command {
    @Override
    public void processCommand() {
        if (InputFiles.isEmpty()){
            return;

        }
        if (InputFiles.size() > 1) {
            stderr.add("too many arguments");
            return;
        }
        String path = InputFiles.get(0).getPath();
        if (!InputFiles.get(0).isAFolder()){
            addErrorMessages(interpreterStack(NOT_A_FOLDER,path));
            return;
        }

        if (!InputFiles.get(0).getInode().getPermission(context.currentUser).contains(Permission.EXECUTION)) { // pas de regex sans la permission de lecture
            addErrorMessages(interpreterStack(PERMISSION, "execution"));
            return;
        }

        context.currentPath.setFilePath(path, context.currentUser);

    }


}


