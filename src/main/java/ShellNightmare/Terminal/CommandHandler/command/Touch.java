package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.*;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.Write)
public class Touch extends Command {
    private boolean binary = false;

    @Override
    public void processCommand() {
        if (InputPseudoPath.isEmpty())
            return;

        Class c = binary ? BinaryData.class : Data.class;

        for(PseudoPath p: InputPseudoPath){
            File<?> v = new File<>(p.newFile, c, context.currentUser);
            addErrorMessages(v.addToFolder(p.getFolder(), context.fs,context.currentUser));
        }
    }

    public void LONGOPT_b_binary(){binary = true;} // juste pour l'éditeur
}
