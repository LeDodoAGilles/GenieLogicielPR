package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.FileSystem.PseudoPath;

import static ShellNightmare.Terminal.FileSystem.E_IOStatus.NOT_A_FOLDER;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.Read)
public class Rmdir extends Command {
    private Boolean recursive=false;
    @Override
    public void processCommand() {
        for (PseudoPath p: InputPseudoPath){
            File<?> f = p.getChildFile();
            if (f.isAFolder()) {
                if (context.currentPath.equals(f.getPath()))
                {
                    stderr.add("On ne peut supprimer le dossier courant");
                    return;
                }
                addErrorMessages(p.getFolder().getInodeData().removeFolderNameFromFolder(p.newFile, recursive, context.fs, context.currentUser));
            }
            else
                addErrorMessages(IOStack.interpreterStack(NOT_A_FOLDER));
        }
    }



    public void OARG_r() {
        recursive=true;
    }

}
