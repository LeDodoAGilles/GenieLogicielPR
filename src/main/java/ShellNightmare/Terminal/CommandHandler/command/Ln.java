package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.isOptional;
import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.PseudoPath;
import ShellNightmare.Terminal.FileSystem.Symbolic;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.Read_Write;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(Read_Write)
public class Ln extends Command {
    private boolean symbolic=false;

    @Override
    public void processCommand() {
        if (InputPseudoPath.size()>2)
        {
            stderr.add("trop de paramètres");
            return;
        }

        if (InputPseudoPath.isEmpty() || InputFiles.isEmpty())
        {
            stderr.add("pas assez de paramètres");
            return;
        }

        if (symbolic){
            PseudoPath p = InputPseudoPath.get(1);
            File<Symbolic> v = new File<>(p.newFile, Symbolic.class, context.currentUser);
            v.getInodeData().path = InputPseudoPath.get(0).toString();
            addErrorMessages(v.addToFolder(p.getFolder(), context.fs, context.currentUser));
        }
        else{
            PseudoPath p = InputPseudoPath.get(1);
            File<?> f =InputFiles.get(0);
            if (f.isAFolder()){
                stderr.add("pas de hardlink avec un dossier!");
                return;
            }
            File<?> f2 = new File<>(p.newFile,f.getInode(),false);
            f2.addToFolder(p.getFolder(), context.fs, context.currentUser);
        }

    }


    public void OARG_s() {
        symbolic = true;
    }
}
