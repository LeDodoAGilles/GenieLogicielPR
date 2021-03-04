package ShellNightmare.Terminal.FileSystem;


import ShellNightmare.Terminal.MetaContext;
import utils.Clonage;

import java.io.Serializable;

import static ShellNightmare.Terminal.FileSystem.E_IOStatus.*;
import static ShellNightmare.Terminal.FileSystem.IOStack.interpreterStack;

/**
 * classe correspondant à un fichier unix
 * @author Gaëtan Lounes
 */
public class File<E extends Inode<E>> implements Serializable,Comparable<File<E>> {
    private static final long serialVersionUID = 832926312281L;

    protected String name;
    String path = "//unlinked";
    Inode<E> data = null;

    @SuppressWarnings("unchecked")
    public static File<Folder> FileRoot(User u){
        File<Folder> f = new File<>("", Folder.class,u,u.mainGroup);
        f.path="/";
        return f;
    }

    public File(String name, Class<E> type,User u) {
        this(name,type,u,u.mainGroup);
    }
    @SuppressWarnings("unchecked")
    public File(String name, Class<E> type,User u,Group g){
        this.name = name;
        try {
            data = (Inode<E>) type.getConstructors()[0].newInstance(u,g); //safe behavior
            data.nbReference++;
        }
        catch (Exception ignored){
            ignored.printStackTrace();

        }
    }


    public File(String name, Inode<E> inode,boolean copy){
        this.name = name;
        data = copy? Clonage.cloneObject(inode):inode;
        if (copy)
            data.nbReference=1; //l'objet est différent donc pas le même id
        else
            data.nbReference++;
    }

    public IOStack removeFile(FileSystem fs, User u){
        if (path.equals("//unlinked"))
            return interpreterStack(CRITICAL_BUG);
        PseudoPath p = new PseudoPath(fs,new User(0));
        p.setPseudoPath(path);
        return p.getFolder().getInodeData().removeFileNameFromFolder(p.newFile,fs,u);

    }

    public IOStack removeFolder(FileSystem fs,boolean recursive, User u){
        if (path.equals("//unlinked"))
            return interpreterStack(CRITICAL_BUG);
        PseudoPath p = new PseudoPath(fs,new User(0));
        p.setPseudoPath(path);
        return p.getFolder().getInodeData().removeFolderNameFromFolder(p.newFile,recursive,fs,u);

    }

    public boolean isInternFile(){
        return name.equals("..") || name.equals(".");
    }


    public IOStack copyFile(File<Folder> destination, String newName,FileSystem fs, User u){
        String ancienNom;
        ancienNom = name;
        name=newName;
        IOStack status = copyFile(destination, fs, u);
        name=ancienNom;
        return status;
    }


    /**
     * copie le fichier dans le dossier destination.
     * @param destination
     * @param fs
     * @param u
     * @return
     */
        public IOStack copyFile(File<Folder> destination,FileSystem fs, User u){ //TODO: afficher toute les erreurs
        if (isAFolder()){ //copy récursive d'un répertoire
            File<Folder> copy = new File<>(name,Folder.class,u);
            File<Folder> original = ConvertFile(this,Folder.class);
            for (File<?> f: original.getInodeData().getExistantFiles(true,u))
                if (!f.isInternFile())
                    f.copyFile(copy,fs,u);

                return copy.addToFolder(destination,fs,u);
        }


        File<?> f = new File<>(getName(),getInode(),true);
        return f.addToFolder(destination,fs,u);
    }

    public void removeFileFromFS(FileSystem fs){
        data.nbReference--;
        if (data.nbReference==0){
            fs.register.remove(this.data);
        }
    }

    @SuppressWarnings("unchecked")
    public static <X extends Inode<X>> File<X> ConvertFile(File<?> file, Class<X> type){
        return (File<X>) file;

    }

    public IOStack addToFolder(File<Folder> folder, FileSystem fs,User u){
        if (!MetaContext.VALID_FILE_NAME.matcher(name).matches())
            return IOStack.interpreterStack(INVALID_NAME,name);
        path=folder.path+(folder.path.equals("/")?"":"/")+name;
        IOStack state =  folder.data.getFile().addFileToFolder(this,u);
        if (state.status == OK)
            fs.register.add(this.data);
        return state;
    }



    public Type getType(){
        if (data!=null){
            return data.type;
        }
        return null;
    }

    public String getName(){
        return name.equals("")?"/":name;
    }



    @SuppressWarnings("unchecked")
    public String fancyNameColor(boolean advancedData, FileSystem fs){
        if (data.type == Type.SOFTLINK && advancedData){
            Inode<Symbolic> s = (Inode<Symbolic>)data;
            if (s.getFile().isFileExist(fs))
                return String.format("%s-->%s",getName(),s.getFile().path);
            return String.format("\\e[41m\\e[5m%s-->%s\\e[25m\\e[49m",getName(),s.getFile().path);
        }
            return String.format("\\e[%dm%s\\e[0m",data.color,getName());
    }

    public Inode<E> getInode(){
        return data;
    }

    public E getInodeData(){
        return data.getFile();
    }




    public boolean isAFolder(){
        return data.isAFolder();
    }



    @Override
    public String toString() {
        return path;
    }

    public File<E> setPath(String path) {
        this.path = path;
        return this;
    }

    public String getPath() {
        return path;
    }



    @Override
    public int compareTo(File<E> o) {
        return name.compareTo(o.name);
    }
}
