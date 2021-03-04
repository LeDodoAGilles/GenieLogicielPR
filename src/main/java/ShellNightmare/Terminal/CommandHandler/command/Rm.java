package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.PseudoPath;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.Read)
public class Rm extends Command {
    @Override
    public void processCommand() {

        for (PseudoPath pseudoPath : InputPseudoPath){
            addErrorMessages(pseudoPath.p.getFolder().getInodeData().removeFileNameFromFolder(pseudoPath.newFile, context.fs,context.currentUser));
        }

    }


}
