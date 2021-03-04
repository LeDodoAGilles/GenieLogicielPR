package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.*;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.Read_Write;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(Read_Write)
public class Mv extends Command {
    private boolean recursive = false;

    @Override
    public void processCommand() {
        int indexMove = InputPseudoPath.size()-1;
        PseudoPath destination = InputPseudoPath.get(indexMove);

        File<?> dest = destination.getChildFile();
        if (destination.isFileExist() && dest.isAFolder()){
            for (int k=0;k<indexMove;k++) {
                File<?> source = InputFiles.get(k);
                if (source.isAFolder()){
                    if (!recursive)
                        stderr.add(String.format("%s est un dossier (pas de mode récursive)",source.getName()));
                    else {
                        addErrorMessages(source.copyFile(destination.getChildFolder(), context.fs, context.currentUser));
                        source.removeFolder(context.fs,true, context.currentUser); //TODO: fix  déplacement d'un dossier dans un autre

                    }
                }
                else {
                    addErrorMessages(source.copyFile(destination.getChildFolder(), context.fs, context.currentUser));
                    source.removeFile(context.fs,context.currentUser);
                }
            }
        }
        else
        if (indexMove==1)
        {
            if (destination.isFileExist())
                if (addErrorMessages(dest.removeFile(context.fs, context.currentUser)))
                    return;
            addErrorMessages(InputFiles.get(0).copyFile(destination.getFolder(), destination.newFile, context.fs, context.currentUser));
            File<?> f = InputFiles.get(0);
            if (f.isAFolder())
                addErrorMessages(f.removeFolder(context.fs,true,context.currentUser));
            else
                addErrorMessages(f.removeFile(context.fs,context.currentUser));
        }
        else
        {
            stderr.add("ne peut déplacer plusieurs fichiers sur un fichier");
        }
    }


    public void OARG_r() {
        recursive=true;
    }
}
