package ShellNightmare.Terminal.CommandHandler;

import gnu.getopt.LongOpt;
import utils.Reflexion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

// longopt https://www.gnu.org/software/gnuprologjava/api/gnu/getopt/Getopt.html

/**
 * Classe gérant le chargement des commandes.
 * @author Gaëtan Lounes
 * @author Louri Noël
 */
public class CommandRegister {
    private ConcurrentHashMap<String, Command> register = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, ConcurrentHashMap<Character,Method>> optionnalArgs = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> getOptarg = new ConcurrentHashMap<>();

    public final StringBuffer longOptSB = new StringBuffer();
    private ConcurrentHashMap<String, LongOpt[]> longOpts = new ConcurrentHashMap<>();

    private CommandRegister(){}

    private static class InstanceHolder{
        private static final CommandRegister instance = new CommandRegister();
    }

    public static CommandRegister getInstance(){
        return InstanceHolder.instance;
    }

    public void registerCommand(Command c){
        register.put(c.name,c);
    }

    public boolean registerCommand(){
        try {
            ArrayList<Class<?>> classes = Reflexion.getClassesForPackage("ShellNightmare.Terminal.CommandHandler.command");
            for (Class<?> c : classes){
                //enregistrement des commandes
                Constructor<?>[] constructors = c.getDeclaredConstructors();
                if (constructors.length==0)
                    return false;
                Command command = (Command) constructors[0].newInstance();
                String[] pname = c.getCanonicalName().split("\\.");
                command.name =pname[pname.length-1].toLowerCase();
                register.put(command.name,command);

                FileMode fm = c.getAnnotation(FileMode.class);
                command.mode=(fm!=null)?fm.value():CommandFileMode.Raw;

                Permission p = c.getAnnotation(Permission.class);
                    command.needSudoPermission= p != null;


                NeededParameters np = c.getAnnotation(NeededParameters.class);
                command.parameterNumber = (np!=null)? Integer.parseInt(np.value()): 0;



                //gestion des commandes avec arguments optionnels
                ConcurrentHashMap<Character,Method> optionnalMethods = new ConcurrentHashMap<>();
                Method[] ma = c.getMethods();
                StringBuilder goptArg = new StringBuilder();

                ArrayList<LongOpt> longoptList = new ArrayList<>();
                ConcurrentHashMap<Character,Method> longOptMethods = new ConcurrentHashMap<>();

                for (Method m:ma){
                    String methodName = m.getName();
                    if (methodName.length()>4 && methodName.subSequence(0,5).equals("OARG_")){
                        boolean isOptionnal = m.isAnnotationPresent(isOptional.class);
                        boolean hasParameter = m.getParameterCount() ==1;
                        Character letter = methodName.charAt(5);
                        goptArg.append(letter);
                        goptArg.append(hasParameter?isOptionnal?"::":":":"");
                        optionnalMethods.put(letter,m);
                    }
                    else if(methodName.length()>11 && methodName.subSequence(0,8).equals("LONGOPT_")){
                        Character shortname = methodName.charAt(8);
                        String longname = methodName.substring(10);
                        int argflag = -1;
                        if(m.getParameterCount() == 0){
                            argflag = LongOpt.NO_ARGUMENT;
                        }
                        else if(m.isAnnotationPresent(isOptional.class)){
                            argflag = LongOpt.OPTIONAL_ARGUMENT;
                        }
                        else {
                            argflag = LongOpt.REQUIRED_ARGUMENT;
                        }

                        longoptList.add(new LongOpt(longname, argflag, longOptSB, shortname));
                        longOptMethods.put(shortname, m);

                        // mais ne gère pas le shortname tout seul : faut le faire soi-même
                        boolean isOptionnal = (argflag == LongOpt.OPTIONAL_ARGUMENT);
                        boolean hasParameter = m.getParameterCount() == 1;
                        goptArg.append(shortname);
                        goptArg.append(hasParameter?isOptionnal?"::":":":"");
                        optionnalMethods.put(shortname,m);
                    }
                }
                getOptarg.put(command.name,goptArg.toString());
                optionnalArgs.put(command.name,optionnalMethods);

                longOpts.put(command.name, longoptList.toArray(new LongOpt[0]));
            }
        }
        catch(Exception ignore){
            return false;
        }
        return true;

    }

    public String getOpt(String s){
        return getOptarg.get(s);

    }

    public LongOpt[] getLongOpt(String s){
        return longOpts.get(s);
    }

    public Command getCommand(String n){
        return register.getOrDefault(n, null);
    }

    public Collection<String> getCommandsString(){
        return Collections.list(register.keys()).stream().sorted().collect(Collectors.toCollection(ArrayList::new));
    }

    public Collection<Command> getCommands(){
        return register.values();
    }

    public void invokeMethod(Command co, Character c, String args) {
        Method m = optionnalArgs.get(co.name).get(c);
        try {
            if (m.getParameterCount()==1)
                m.invoke(co, args);
            else
                m.invoke(co);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
