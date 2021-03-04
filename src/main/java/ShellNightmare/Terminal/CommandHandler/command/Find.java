package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.FnMatch;
import ShellNightmare.Terminal.FileSystem.Folder;
import ShellNightmare.Terminal.FileSystem.PseudoPath;

import java.util.HashMap;
import java.util.HashSet;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Read)
public class Find extends Command {
    HashMap<String,String> filterMap = new HashMap<>();
    @Override
    public void processCommand() {
        if (InputPseudoPath.size()==0){
            InputPseudoPath.add(new PseudoPath(context.currentPath));
        }

        for(PseudoPath p : InputPseudoPath){
            File<Folder> f = p.getChildFolder();
            if (f==null|| !f.isAFolder())
                continue;
            HashSet<String> s = new HashSet<>();
            recursiveAux(f,s);
            stdout.addAll(s);
        }
        filterMap.clear();

    }

    private void recursiveAux(File<Folder> repertory, HashSet<String> result){
            for (File<?> f: repertory.getInodeData().getExistantFiles(true, context.currentUser)) {
                if (f.isAFolder() && !f.isInternFile())
                    recursiveAux(File.ConvertFile(f,Folder.class),result);

                if (!matchFilterMap(f))
                    continue;

                if (f.isInternFile() && f.getName().equals("..")) //seulement le fichier .
                    continue;

                result.add(f.getPath());
            }
    }

    private boolean matchFilterMap(File<?> f){
        if (filterMap.containsKey("name")){
            if (!FnMatch.fnmatch(filterMap.get("name"),f.getName())) //regex
                return false;
        }

        if (filterMap.containsKey("type")){
            if (!f.getType().shortName.equals(filterMap.get("type")))
                return false;
        }

        if (filterMap.containsKey("user")){
            if (!f.getInode().getOwner().name.equals(filterMap.get("user")))
                return false;
        }

        if (filterMap.containsKey("link")){
            if (!String.valueOf(f.getInode().nbReference).equals(filterMap.get("link")))
                return false;
        }


        return true;
    }



    public void OARG_n(String arg) {
        filterMap.put("name",arg);
    }

    public void OARG_t(String arg) {
        filterMap.put("type",arg);
    }

    public void OARG_u(String arg) {
        filterMap.put("user",arg);
    }

    public void OARG_l(String arg) {

        try {
            Integer.parseInt(arg);
            filterMap.put("link",arg);
        }
        catch (Exception e){
            stderr.add(String.format("invalide paramètre type: %s",arg));
        }

    }

}
