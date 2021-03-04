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
@FileMode(CommandFileMode.Read)
public class Ls extends Command {
    Boolean hidden = false;
    Boolean verbose = false;
    @Override
    public void processCommand() {
        if (InputPseudoPath.isEmpty()){
            InputPseudoPath.add(new PseudoPath(context.currentPath));
        }
        for(PseudoPath p : InputPseudoPath){
            File<Folder> f = p.getChildFolder();

            if (!f.isAFolder())
                f =p.getFolder();

            if (f==null)
                continue;
            if (!verbose)
                for (File<?> f2 :f.getInodeData().getExistantFiles(hidden, context.currentUser))
                    stdout.add(f2.fancyNameColor(false, context.fs));
            else{ //TODO: alignement de l'affichage
                int[] a = new int[4];
                for (File<?> f2 :f.getInodeData().getExistantFiles(hidden, context.currentUser)){
                    a[0]= (int) Math.max(a[0],Math.ceil(Math.log10(f2.getInode().nbReference))+1);
                    a[1]=Math.max(a[1],f2.getInode().getOwner().name.length());
                    a[2]=Math.max(a[2],f2.getInode().getGroup().name.length());
                    a[3]=(int) Math.max(a[3],Math.floor(Math.log10(1+f2.getInode().getSize()))+1);
                }
                String regex ="%-9s %-"+a[0]+"d %-"+a[1]+"s %-"+a[2]+"s %-"+a[3]+"d %s %s";
                for (File<?> f2 :f.getInodeData().getExistantFiles(hidden, context.currentUser)){
                    stdout.add(String.format(regex,f2.getInode().getPermissionInformationFancy(),f2.getInode().nbReference,f2.getInode().getOwner().name,f2.getInode().getGroup().name,f2.getInode().getSize(), f2.getType().shortName,f2.fancyNameColor(true, context.fs)));

                }
            }
        }

    }

    public void OARG_l() {
        verbose=true;
    }

    public void OARG_a() { hidden=true; }


}
