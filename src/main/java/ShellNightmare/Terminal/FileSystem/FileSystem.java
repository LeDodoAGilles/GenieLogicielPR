package ShellNightmare.Terminal.FileSystem;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * classe correspondant au système de fichier Unix
 * @author Gaëtan Lounes
 */
public class FileSystem implements Serializable {
    private static final long serialVersionUID = 1459398L;

    public Set<Inode<?>> register = new HashSet<>();

    public File<Folder> root;

    public FileSystem(User owner){
        root = File.FileRoot(owner);
        root.data.getFile().addInternFiles(null,"/");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Inode<?> i : register){
            sb.append(String.format("%d\n",i.nbReference));
        }
        return sb.toString();
    }

    public boolean checkRegisterIntegrity(){
        return register.parallelStream().noneMatch(i -> i.nbReference<=0); //pas d'inode inutilisée
    }
}
