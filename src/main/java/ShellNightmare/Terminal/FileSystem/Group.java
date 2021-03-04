package ShellNightmare.Terminal.FileSystem;

import java.io.Serializable;
import java.util.*;

/**
 * classe représentant les groupes Unix
 * @author Gaëtan Lounes
 */
public class Group implements Serializable {
    private static final long serialVersionUID = 359978L;

    public String name;
    public int gid;
    public Set<Integer> memberList = new HashSet<>();

    public Group(String name){
        this.name = name;
    }

    public void addMember(User u){
        if (memberList.contains(u.uid)){
            return;
        }
        memberList.add(u.uid);
    }
    public void removeMember(User u){
        memberList.remove(u.uid);
    }

    public Collection<Integer> getMembers(){
        return memberList;
    }

}
