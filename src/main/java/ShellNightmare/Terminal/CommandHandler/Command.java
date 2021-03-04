package ShellNightmare.Terminal.CommandHandler;

import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.FileSystem.PseudoPath;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.interpreter.InterpreterStack;
import gnu.getopt.Getopt;

import java.util.ArrayList;
import java.util.List;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.SuperRaw;
import static ShellNightmare.Terminal.FileSystem.E_IOStatus.*;
import static ShellNightmare.Terminal.FileSystem.IOStack.interpreterStack;
import static java.lang.String.format;

/**
 * classe représentant une commande
 * @author Gaëtan Lounes
 */
public abstract class Command implements Cloneable {
    public String name;
    protected ArrayList<String>  stdin, stdout, stderr;
    protected ArrayList<File<?>> InputFiles;
    protected ArrayList<PseudoPath> InputPseudoPath;
    protected ArrayList<String> neededParameters;
    protected CommandFileMode mode;
    protected int parameterNumber;
    protected Context context;
    protected boolean needSudoPermission;

    public Command(){
        stdout = new ArrayList<>();
        stdin = new ArrayList<>();
        stderr = new ArrayList<>();
        neededParameters = new ArrayList<>();
        InputFiles = new ArrayList<>();
        InputPseudoPath = new ArrayList<>();
    }

    public abstract void processCommand();


    public boolean prepareCommand(List<String> args){
        String commandName = args.get(0);
        Getopt getopt1 = new Getopt(commandName,args.toArray(new String[0]), MetaContext.registerC.getOpt(commandName), MetaContext.registerC.getLongOpt(commandName));
        getopt1.setOpterr(false);
        int c;
        if (mode!=SuperRaw) {
            while ((c = getopt1.getopt()) != -1){
                if(c == '?') { // error
                    addErrorMessages(interpreterStack(INVALID_OPTION, args.get(getopt1.getOptind() - 1)));
                }
                else if(c == 0) { // longopt
                    char shortname = (char)(Integer.valueOf(MetaContext.registerC.longOptSB.toString())).intValue();
                    MetaContext.registerC.invokeMethod(this, shortname, getopt1.getOptarg());
                }
                else {
                    MetaContext.registerC.invokeMethod(this, (char) c, getopt1.getOptarg());
                }

            }
            if (getopt1.getOptind() > 0) {
                args.subList(0, getopt1.getOptind()).clear();
            }
        }

        for (int k=0;k<parameterNumber;k++){
            if (args.size()==1){
                addErrorMessages(interpreterStack(MISSING_NEEDED_PARAMETERS,String.valueOf(parameterNumber-k)));
                return false;
            }
            neededParameters.add(args.get(1));
            args.remove(1);
        }
        setStdin(args);

        if (needSudoPermission){
            if (context.currentUser.uid==0 || context.currentUser.mainGroup.gid == 0)
                return true;
                else
            {
                addErrorMessages(interpreterStack(PERMISSION,"mode root"));
                return false;
            }
        }
        return true;
    }

    private void setStdin(List<String> args){ //TODO: option (annotation?) pour désactiver l'interpretation de softLink
        args.remove(0);
        stdin.addAll(args);
        ArrayList<IOStack> errorCatch = new ArrayList<>();
        context.currentPath.u= context.currentUser;

        switch (mode){
            case SuperRaw:
            case NullInput:
            case Raw:
                    break;

            case Read_Write: //n-1 premiers read et n en write
                if (args.size()<2)
                    return;
                String write = args.remove(args.size()-1);
                for (String arg : args)
                    for(String s : context.currentPath.showRelatives(arg, true,false,errorCatch)){
                        PseudoPath p = new PseudoPath(context.currentPath); //pseudoPath pas nécessaire mais simplification de la gestion des flux avec les autres modes d'accès aux fichiers.
                        if (addErrorMessages(p.setPseudoPath(s)))
                            continue;
                        InputPseudoPath.add(p);
                        if (!p.isFileExist())
                            addErrorMessages(interpreterStack(NOT_EXIST,p.newFile));
                        else
                            InputFiles.add(p.getChildFile());
                    }

                PseudoPath pp = new PseudoPath(context.currentPath);
                    if (addErrorMessages(pp.setPseudoPath(write)))
                        break;
                InputPseudoPath.add(pp);
                break;


            case Read:
                for (String arg : args)
                    for(String s : context.currentPath.showRelatives(arg,true,false,errorCatch)){
                        PseudoPath p = new PseudoPath(context.currentPath); //pseudoPath pas nécessaire mais simplification de la gestion des flux avec les autres modes d'accès aux fichiers.
                        if (addErrorMessages(p.setPseudoPath(s)))
                            continue;
                        InputPseudoPath.add(p);
                        if (!p.isFileExist())
                            addErrorMessages(interpreterStack(NOT_EXIST));
                        else
                            InputFiles.add(p.getChildFile());
                }

                break;

            case Write: // les fichiers en mode write n'existent pas encore... mais le chemin père doit être bon
                for (String arg : args){
                    PseudoPath p = new PseudoPath(context.currentPath);
                    if (!addErrorMessages(p.setPseudoPath(arg)))
                        InputPseudoPath.add(p);
                }


        }
        addErrorMessages(errorCatch);

    }

    public void clearCommandArray(){
            stdin.clear();
            stdout.clear();
            stderr.clear();
            InputPseudoPath.clear();
            InputFiles.clear();
            neededParameters.clear();
    }

    public List<String> getStdout(){
        return stdout;
    }

    public List<String> getStderr(){
        return stderr;
    }

    public boolean getNeedSudoPermission() {
        return needSudoPermission;
    }

    public Command clone(Context c){
        context = c;
        Command clone = null;
        try {
            clone = (Command) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }

    protected boolean addErrorMessages(ArrayList<IOStack> aio){
        boolean result = false;
        for(IOStack io : aio){
            result = result || addErrorMessages(io);
        }
        return result;
    }

    protected boolean addErrorMessages(IOStack io){
        if (io.status==OK)
            return false;
        stderr.add(getErrorMessage(io));
        return true;
    }


    public static String getErrorMessage(IOStack aio){
        String message = aio.message==null? "": aio.message;
        switch(aio.status){
            case NOT_EXIST: return format("Le fichier %s n'existe pas",message);
            case NOT_A_FOLDER:return format("Le fichier %s n'est pas un dossier",message);
            case MISSING_NEEDED_PARAMETERS: return format("Cette commande require %s paramètre",message);
            case PERMISSION: return format("La permission %s est nécessaire pour cette action",message);
            case FOLDER_IS_NOT_EMPTY: return format("Le dossier %s n'est pas vide",message);
            case IS_NOT_DATA: return format("Le fichier %s n'est pas de la donnée",message);
            case CRITICAL_BUG: return "Oups erreur critique -> merci d'envoyer un ticket";
            case INVALID_VAR: return format("La variable %s n'est pas valide",message);
            case INVALID_NAME: return format("Le nom %s n'est pas valide",message);
            case INVALID_GROUP:return format("Le groupe %s n'est pas valide",message);
            case INVALID_NUMBER: return format("Ce n'est pas un nombre %s",message);
            case INVALID_PASSWORD: return format("Le mot de passe %s n'est pas valide",message);
            case INVALID_OPTION: return format("L'option %s n'est pas valide",message);
            case INVALID_COMMAND: return format("Ce n'est pas une commande %s",message);
            case ALREADY_EXIST: return format("Le fichier %s existe déjà!",message);
            case USER_NOT_IN_GROUP: return format("Cette utilisateur %s n'est pas dans le groupe",message);
            case INVALID_USER: return format("Ce n'est pas un utilisateur %s",message);
            case TRY_REMOVING_ROOT:return "On ne peut pas retirer le dossier racine";
            case ALREADY_EXIST_USER: return format("Cet utilisateur %s existe déjà",message);
            case ALREADY_EXIST_GROUP: return format("Ce groupe %s existe déjà",message);
            case TRY_REMOVING_FOLDER: return format("On ne peut retirer ce dossier %s",message);
            default: return aio.status.name() + message;
        }
    }


}
