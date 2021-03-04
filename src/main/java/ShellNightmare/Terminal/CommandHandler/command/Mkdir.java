package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.Folder;
import ShellNightmare.Terminal.FileSystem.PseudoPath;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.Write)
public class Mkdir extends Command {
    @Override
    public void processCommand() {

        for (PseudoPath p : InputPseudoPath){
            File<Folder> v = new File<>(p.newFile, Folder.class, context.currentUser);
            addErrorMessages(v.addToFolder(p.getFolder(),context.fs,context.currentUser));
        }
    }

}
