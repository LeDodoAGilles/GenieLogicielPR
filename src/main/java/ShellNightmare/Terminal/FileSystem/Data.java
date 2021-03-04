package ShellNightmare.Terminal.FileSystem;


import static ShellNightmare.Terminal.FileSystem.E_IOStatus.PERMISSION;
import static ShellNightmare.Terminal.FileSystem.IOStack.interpreterStack;

/**
 * représente les inodes de fichiers stockant de l'information
 * @author Gaëtan Lounes
 */
public class Data extends Inode<Data> {
    private static final long serialVersionUID = 306586921437585L;

    String data = "";
    public Data(User u,Group g){
        super(u,g);
        permission=744;
        type = Type.DATA;
        color=34;
    }

    @Override
    public int getSize() {
        return data.length();
    }


    public void setData(String data) { //TODO: permission user ici?
        this.data = data;
    }

    public String getData(User u) {
        if (getPermission(u).contains(Permission.READ))
            return data;
        return null;
    }
}
