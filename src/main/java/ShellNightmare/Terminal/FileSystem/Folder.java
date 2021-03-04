package ShellNightmare.Terminal.FileSystem;

import java.util.*;
import java.util.stream.Collectors;

import static ShellNightmare.Terminal.FileSystem.E_IOStatus.*;
import static ShellNightmare.Terminal.FileSystem.IOStack.interpreterStack;

/**
 * classe correspondant à l'information des inodes de dossier
 * @author Gaëtan Lounes
 */

public class Folder extends Inode<Folder> {
    private static final long serialVersionUID = 5248217262L;

    private final HashMap<String, File<?>> directoryEntry = new HashMap<>();

    public Folder(User u,Group g){
        super(u,g);
        type = Type.FOLDER;
        permission = 737;
        color = 37;

    }

    @Override
    public int getSize() {
        return directoryEntry.size()*8;
    }

    public void addInternFiles(Folder parent,String childPath){
        directoryEntry.put(".",new File<>(".",this,false).setPath(childPath));
        String parentPath = childPath;
        if (!childPath.equals("/")) {
            String[] parentPaths = childPath.split("/");
            parentPath=parentPath.substring(0,parentPath.length()-parentPaths[parentPaths.length-1].length()-1);
            if (parentPath.length()==0)
                parentPath="/";

        }
        directoryEntry.put("..",new File<>("..",parent==null?this:parent,false).setPath(parentPath));
    }

    public void removeInternFiles(FileSystem fs, User u){
        removeFileNameFromFolder("..",fs,u,true);
        removeFileNameFromFolder(".",fs,u,true);
    }


    protected IOStack addFileToFolder(File<?> f, User u){
        if (directoryEntry.containsKey(f.name)){
            return interpreterStack(ALREADY_EXIST,f.name);
        }

        if (!getPermission(u).contains(Permission.WRITE))
            return interpreterStack(PERMISSION,f.name);

        if (f.data instanceof Folder){
            ((Folder) f.data).addInternFiles(this,f.path);
        }
        directoryEntry.put(f.name,f);
        return interpreterStack(OK);
    }

    private void recursiveCleaner(File<Folder> f,FileSystem fs, User u){
        for (File<?> fr : f.getInodeData().getExistantFiles(true,u)) {
            if (fr.isInternFile())
                continue;
            if (fr.isAFolder())
                recursiveCleaner(File.ConvertFile(fr,Folder.class),fs,u);
            else
                f.getInodeData().removeFileNameFromFolder(fr.name,fs,u);
        }

    }



    public IOStack removeFolderNameFromFolder(String name,boolean recursive, FileSystem fs, User u){
        if (!directoryEntry.containsKey(name))
            return interpreterStack(NOT_EXIST);
        File<?> f = directoryEntry.get(name);
        if (!f.isAFolder())
            return interpreterStack(NOT_A_FOLDER);

        if (f.path.equals("/"))
            return interpreterStack(TRY_REMOVING_ROOT);
        File<Folder> folder = File.ConvertFile(f,Folder.class);
        if (folder.getInodeData().getExistantFiles(true,u).size()!=2)
        {
            if (!recursive)
                return interpreterStack(FOLDER_IS_NOT_EMPTY);
            recursiveCleaner(folder,fs,u);
            }



        if (!getPermission(u).contains(Permission.WRITE))
            return interpreterStack(PERMISSION);

        folder.getInodeData().removeInternFiles(fs,u);
        folder.removeFileFromFS(fs);
        directoryEntry.remove(name);
        return interpreterStack(OK);
    }
    public IOStack removeFileNameFromFolder(String name,FileSystem fs, User u){
        return removeFileNameFromFolder(name, fs, u,false);
    }

    public IOStack removeFileNameFromFolder(String name,FileSystem fs, User u, boolean ignoreType ){
        if (!directoryEntry.containsKey(name))
            return interpreterStack(NOT_EXIST);
        File<?> f = directoryEntry.get(name);
        if (!ignoreType && f.isAFolder())
            return interpreterStack(TRY_REMOVING_FOLDER);

        if (!f.data.getPermission(u).contains(Permission.WRITE))
            return interpreterStack(PERMISSION);

        f.removeFileFromFS(fs);
        directoryEntry.remove(name);
        return interpreterStack(OK);

    }



    public File<?> getFileByName(String name){
        return directoryEntry.get(name);
    }

    @SuppressWarnings("unchecked")
    public <X extends Inode<X>> File<X> getFileByName(String name, Class<X> ignore){
        return (File<X>) directoryEntry.get(name);
    }

    public ArrayList<String> getExistantFilesNames(boolean hidden,User u){
        if (getPermission(u).contains(Permission.READ))
            return directoryEntry.keySet().stream().filter(x->hidden ||x.charAt(0)!='.').sorted().collect(Collectors.toCollection(ArrayList::new));
        return new ArrayList<>();
    }

    public ArrayList<File<?>> getExistantFiles(Boolean hidden,User u){
        if (getPermission(u).contains(Permission.EXECUTION))
            return directoryEntry.values().stream().filter(x->hidden ||x.name.charAt(0)!='.').sorted().collect(Collectors.toCollection(ArrayList::new));
        return new ArrayList<>();
    }


    public boolean isFileNameInDirectory(String s){
        return directoryEntry.containsKey(s);
    }


}
