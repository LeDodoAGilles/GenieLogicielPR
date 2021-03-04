package ShellNightmare.Terminal.FileSystem;

import java.util.List;

/**
 * représente une inode d'un fichier de lien symbolique
 * @author Gaëtan Lounes
 */
public class Symbolic extends Inode<Symbolic> {
    private static final long serialVersionUID = 301236921437585L;

    public String path;

    public Symbolic(User u,Group g) {
        super(u,g);
        type = Type.SOFTLINK;
        permission = 777;
        color=36;
    }

    @Override
    public int getSize() {
        return path.length();
    }

    public boolean isFileExist(FileSystem fs){
        User u = new User(0); //fake root
        Path p = new Path(fs, u);
        IOStack is = p.setPath(path);
        return is.status == E_IOStatus.OK;
    }

    public File<?> getFile(FileSystem fs, List<IOStack> error){
        User u = new User(0); //fake root
        Path p = new Path(fs, u);
        IOStack is = p.setPath(path);

        if (is.status!=E_IOStatus.OK){
            error.add(is);
            return null;
        }
        return p.getFile();

    }

}
