package ShellNightmare.Terminal.FileSystem;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * classe correspondant à une inode
 * @author Gaëtan Lounes
 */

public abstract class Inode<E> implements Serializable{
    private static final long serialVersionUID = 48268629L;

    public  int nbReference = 0;
    protected short permission;
    private Date creationTime= null;
    private Date lastModification= null;
    private Group group;
    private User owner;
    protected Type type;
    protected int color;

    public Inode(User u, Group g){
        owner = u;
        group = g;
        creationTime = lastModification = new Date();
    }

    public abstract int getSize();

    public void updateInode(){
        lastModification = new Date();
    }


    public String getPermissionInformationFancy(){
        StringBuilder result = new StringBuilder();
        short digit;
        short temp = permission, temp2;
        for (short k=0; k<3; k++){
            temp2 = (short) (temp / 10);
            digit = (short) (temp % 10);
            temp = temp2;

            short i=0;
            for (String c : new String[]{"x","w","r"})
                result.insert(0, ((digit >> i++) % 2 == 1 ? c : '-'));
        }
        return result.toString();
    }

    public void setPermission(short permission) {
        if (permission>777 || permission<0)
            return;
        this.permission = permission;
    }

    public HashSet<Permission> getPermission(UserType ut){
        short digit;
        short temp = permission, temp2;
        HashSet<Permission> perms = new HashSet<>();
        for (short k=2; k>-1; k--){
            temp2 = (short) (temp / 10);
            digit = (short) (temp % 10);
            temp = temp2;
            if (k==ut.ordinal()){
                int j=0;
                for (Permission c : Permission.values())
                    if ((digit >> j++) % 2 == 1){
                        perms.add(c);
                    }

            }

        }
        return perms;
    }


    public Set<Permission> getPermission(User u){
        if (u.uid==0 || u.mainGroup.gid==0)
        {
            return Arrays.stream(Permission.values()).collect(Collectors.toSet());
        }
        if (u == owner)
            return getPermission(UserType.OWNER);
        if (group!= null && group.memberList.contains(u.uid))
            return getPermission(UserType.GROUP);
        return getPermission(UserType.OTHER);
    }

    public short getPermission(){ // pour l'éditeur
        return permission;
    }

    @SuppressWarnings("unchecked")
    public E getFile(){
        return (E)this;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner){ // utilisé dans l'éditeur, pour se simplifier la tâche
        this.owner = owner;
    }

    public Type getType() {
        return type;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    public boolean isAFolder(){
        return type == Type.FOLDER;
    }
}
