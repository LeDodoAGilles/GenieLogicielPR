package ShellNightmare.Terminal;


import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.FileSystem.*;
import ShellNightmare.Terminal.challenge.Score;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Tree;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * classe représentant le contexte d'un terminal
 * @author Gaëtan Lounes
 * @author Louri Noël
 */
//contexte de la session : chemin, utilisateur en cours, variables d'environnement, mode user/root
public class Context implements Serializable, Cloneable {
    private static final long serialVersionUID = 781227L;

    public  List<String> history = new ArrayList<>();
    public List<String> simplehistory = new ArrayList<>();
    public TypeContext type = TypeContext.STANDARD;

    private final Set<Group> registeredGroup = new HashSet<>();
    private final Set<User> registeredUser = new HashSet<>();
    public User rootUser;
    public Group rootGroup;
    public User currentUser,historicalUser;
    public FileSystem fs;
    public Path currentPath;
    private final Map<String,Tree> functions = new HashMap<>();
    private final Map<User,Map<String,Tree>> privateFunctions = new HashMap<>();
    private final Map<String,String> envar = new HashMap<>();
    private final Map<User,Map<String,String>> privateEnvar = new HashMap<>();
    public Map<String, Tree> alias = new HashMap<>();
    public Date rootAccessDeadline = new Date();
    public Set<String> blackListCommand = new HashSet<>();
    private Map<User,Set<String>> blackListCommandPersonnal = new HashMap<>();



    public Context(){
        initEnvar();
        rootUser=addNewUser(User.ROOT_USERNAME,"password",0);
        rootGroup = rootUser.mainGroup;
        currentUser=addNewUser("Charles_Henry","1234");
        fs = new FileSystem(rootUser);
        currentPath = new Path(fs, rootUser);
    }

    public void sweepVar(){
        privateEnvar.clear();
        envar.clear();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        if (blackListCommandPersonnal==null)
            blackListCommandPersonnal = new HashMap<>();

    }

    public void addBlackListCommand(String c, User u){
        if (u != null){
            if (!blackListCommandPersonnal.containsKey(u))
                blackListCommandPersonnal.put(u,new HashSet<>());
            blackListCommandPersonnal.get(u).add(c);
        }
        else
            blackListCommand.add(c);
    }

    public void removeBlackListCommand(String c){
        removeBlackListCommand(c,null);
    }

    public void removeBlackListCommand(String c, User u){
        if (u != null){
            if (blackListCommandPersonnal.containsKey(u))
                blackListCommandPersonnal.get(u).remove(c);
        }
        else
            blackListCommand.remove(c);
    }

    public Set<String>  getBlacklistedCommand(){
        if (blackListCommandPersonnal.containsKey(currentUser)){
            Set<String> e = new HashSet<>(blackListCommandPersonnal.get(currentUser));
            e.addAll(blackListCommand);
            return e;
        }
        else
            return blackListCommand;
    }

    public Collection<Command> getCommands(){
        if (getUserGroups(currentUser).contains(rootGroup))
            return MetaContext.registerC.getCommands();
        return MetaContext.registerC.getCommands().stream().filter(s->!getBlacklistedCommand().contains(s.name)).collect(Collectors.toCollection(ArrayList::new));
    }

    public Collection<String> getCommandsName(){
        if (getUserGroups(currentUser).contains(rootGroup))
            return MetaContext.registerC.getCommandsString();
        return MetaContext.registerC.getCommandsString().stream().filter(s->!getBlacklistedCommand().contains(s)).collect(Collectors.toCollection(ArrayList::new));
    }

    public void setFunction(String name,Tree t){
        if (name.charAt(0)=='_'){
            if (!privateFunctions.containsKey(currentUser))
                privateFunctions.put(currentUser,new HashMap<>());
            privateFunctions.get(currentUser).put(name,t);
        }
        else
            functions.put(name,t);
    }

    public Tree getFunction(String name){
        if (name.charAt(0)=='_'){
            if (privateFunctions.containsKey(currentUser))
                if (privateFunctions.get(currentUser).containsKey(name))
                    return privateFunctions.get(currentUser).get(name);
                return null;
        }
        else
            return functions.get(name);

    }

    public Collection<String> getFunctions(){
        HashSet<String> part = new HashSet<>(functions.keySet());
        if (privateFunctions.containsKey(currentUser))
            part.addAll(privateFunctions.get(currentUser).keySet());
        return part;
    }


    public void setEnvar(String name,String data){
        if (name.charAt(0)=='_'){
            if (!privateEnvar.containsKey(currentUser))
                privateEnvar.put(currentUser,new HashMap<>());
            privateEnvar.get(currentUser).put(name,data);
        }
        else
            envar.put(name,data);
    }

    public String getEnvar(String name){
        if (name.charAt(0)=='_'){
            if (privateEnvar.containsKey(currentUser))
                if (privateEnvar.get(currentUser).containsKey(name))
                    return privateEnvar.get(currentUser).get(name);
            return "";
        }
        else
            return envar.getOrDefault(name, "");
    }
    public void removeEnvar(String name){
        if (name.charAt(0)=='_'){
            if (privateEnvar.containsKey(currentUser))
                privateEnvar.get(currentUser).remove(name);
        }
        else
            envar.remove(name);
    }

    public void removeFunction(String name){
        if (name.charAt(0)=='_'){
            if (privateFunctions.containsKey(currentUser))
                privateFunctions.get(currentUser).remove(name);
        }
        else
            functions.remove(name);
    }

    public Set<Map.Entry<String, String>> Envarset(){
        HashMap<String,String> result = new HashMap<>(envar);
        if (privateEnvar.containsKey(currentUser))
            result.putAll(privateEnvar.get(currentUser));
        return result.entrySet();
    }

    public Set<String> listEnvar(){
        return envar.keySet();
    }

    private void initEnvar(){
        envar.put("?","0");
    }

    public Context clone(){
        Context clone = null;
        try {
            clone = (Context) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }


    public User addNewUser(String name,String password) {
        return addNewUser(name,password,getUserNewId());
    }

    public void removeUser(String name){
        Optional<User> ou = getUser(name);
        ou.ifPresent(registeredUser::remove);
    }

    public void removeGroup(String name){
        Optional<Group> ou = getGroup(name);
        ou.ifPresent(registeredGroup::remove);
    }

    public User addNewUser(String name,String password,int uid){
        Group g =addNewGroup(name);
        User u = new User(g,name,password);
        boolean present = registeredUser.stream().anyMatch(us-> us.uid ==uid);
        if (!present)
            u.uid = uid;
        else
            u.uid=getUserNewId();

        registeredUser.add(u);
        addUserToGroup(u,g);
        return u;
    }

    public Group addNewGroup(String name){
        Group g = new Group(name);
        g.gid=getGroupNewId();
        registeredGroup.add(g);
        return g;
    }


    public void addUserToGroup(User u, Group g){
        g.addMember(u);
    }

    public void addUserToAdminGroup(User u){
        rootUser.mainGroup.addMember(u);
        u.mainGroup=rootUser.mainGroup;
    }

    public Optional<Group> getGroup(String group){
        return registeredGroup.stream().filter(g-> g.name.equals(group)).findFirst();
    }

    public int getGroupNewId(){
        var og = registeredGroup.stream().mapToInt(g->g.gid).max();
        if (og.isEmpty())
            return 0;
        return og.getAsInt()+1;

    }

    public int getUserNewId(){
        var ou = registeredUser.stream().mapToInt(u->u.uid).max();
        if (ou.isEmpty())
            return 1;
        return ou.getAsInt()+1;

    }

    public Optional<Group> getGroup(int gid){
        return registeredGroup.stream().filter(g-> g.gid==gid).findFirst();
    }

    public Optional<User>  getUser(String user){
        return registeredUser.stream().filter(u-> u.name.equals(user)).findFirst();
    }
    public Optional<User> getUser(int gid){
        return registeredUser.stream().filter(u-> u.uid==gid).findFirst();
    }

    public Set<Group> getGroups() {
        return registeredGroup;
    }

    public Set<User> getUsers() {
        return registeredUser;
    }

    public List<Group> getUserGroups(User u){
        return  registeredGroup.stream().filter(g -> g.memberList.contains(u.uid)).collect(Collectors.toCollection(ArrayList::new));
    }
}

