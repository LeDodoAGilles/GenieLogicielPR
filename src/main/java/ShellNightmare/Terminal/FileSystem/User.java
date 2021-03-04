package ShellNightmare.Terminal.FileSystem;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
/**
 * classe représentant un utilisateur Unix
 * @author Gaëtan Lounes
 */
public class User implements Serializable {
    private static final long serialVersionUID = 8714053L;

    public static final String ROOT_USERNAME = "root";


    public String name;
    public String hash;
    public int uid;
    public Group mainGroup;


     public User(Group g,String name, String password){
        this(password);
        this.name = name;
        mainGroup = g;

    }

    public boolean isPasswordValid(String pass){
        return hash.equals(DigestUtils.sha512Hex(pass));
    }

    User(String password){
        this.hash = DigestUtils.sha512Hex(password);
    }

    User(int uid){
         this.uid = uid;
    }


    @Override
    public String toString() {
        return name;
    }
}
