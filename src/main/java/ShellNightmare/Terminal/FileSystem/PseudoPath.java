package ShellNightmare.Terminal.FileSystem;


import utils.Clonage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * représente le chemin d'un fichier (pas encore) créé --> permet d'accéder directement au fichier répertoire
 * @author Gaëtan Lounes
 */

public class PseudoPath {
    public Path p;
    public String newFile=".";
    public PseudoPath(FileSystem fs,User u){
        p = new Path(fs,u);
    }

    public PseudoPath(Path p){
        this.p = new Path(p);
    }

    public IOStack setPseudoPath(String inputPath){ // absolutePath: est un chemin absolu

        if (inputPath.isEmpty())
            return IOStack.interpreterStack(E_IOStatus.OK);


        //if (inputPath.isEmpty()){
        //    newFile=".";
        //    return p.setPath("/");
        //}

        if (inputPath.contains("/")) {
            String lastFolderPath = inputPath.substring(0, inputPath.lastIndexOf('/'));
            if (lastFolderPath.equals("")) //on est a la racine du répertoire
            {
                newFile = (inputPath.length()>1)?inputPath.substring(inputPath.lastIndexOf('/')+1):".";
                p.setPath("/");
                return IOStack.interpreterStack(E_IOStatus.OK); // par definition de la racine, elle existe forcement
            }
            newFile = inputPath.substring(inputPath.lastIndexOf('/')+1);

                return p.setPath(lastFolderPath);
        }
        else{
            newFile=inputPath;
        }

        return IOStack.interpreterStack(E_IOStatus.OK);
    }




        public File<Folder> getFolder(){
        return p.getFolder();
    }

    public boolean isFileExist(){
        File<Folder> f = getFolder();
        if (f ==null)
            return false;
        return f.getInodeData().isFileNameInDirectory(newFile);
    }

    public File<Folder> getChildFolder(){
        if (isFileExist())
            return getFolder().getInodeData().getFileByName(newFile,Folder.class);
        return null;
    }

    public File<?> getChildFile(){
        if (isFileExist()){
            File<?> f = getFolder().getInodeData().getFileByName(newFile);
            if (f.getType()==Type.SOFTLINK){
                File<Symbolic> fs = File.ConvertFile(f,Symbolic.class);
                List<IOStack> error = new ArrayList<>();
                File<?> f2 = fs.getInodeData().getFile(p.fs,error);
                if (error.isEmpty())
                    return f2;
                return null;
            }
            return f;
        }

        return null;
    }

    @Override
    public String toString() {
        return p.toString()+(p.toString().equals("/")?"":'/')+newFile;
    }
}
