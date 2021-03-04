package ShellNightmare.Terminal.CommandHandler.command;


import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.isOptional;
import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.PseudoPath;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.Read_Write;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(Read_Write)
public class Cp extends Command {
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

                    }
                }
                else {
                    addErrorMessages(source.copyFile(destination.getChildFolder(), context.fs, context.currentUser));
                }
            }
        }
        else
        if (indexMove==1)
        {
            if (destination.isFileExist())
                if (!addErrorMessages(dest.removeFile(context.fs, context.currentUser)))
                    return;
            addErrorMessages(InputFiles.get(0).copyFile(destination.getFolder(), destination.newFile, context.fs, context.currentUser));
        }
        else
        {
            stderr.add("ne peut copier plusieurs fichiers sur un fichier");
        }
    }


    public void OARG_r() {
        recursive=true;
    }
}