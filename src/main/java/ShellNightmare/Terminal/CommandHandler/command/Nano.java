package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.*;
import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.DaemonMessage;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.FileSystem.*;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.TerminalFX.nano.NanoConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * commande voir man pour le détail
 * @author Louri Noël
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.Write)
public class Nano extends Command {
    private static final Pattern LINE_COLUMN_PATTERN = Pattern.compile("\\+\\d+,\\d+");
    private static final Pattern LINE_PATTERN = Pattern.compile("\\+\\d+");
    private static final Pattern EXTRACT = Pattern.compile("\\d+"); // pour extraire des nombres

    NanoConfig config = new NanoConfig();
    String tabsize = null;

    // Override afin d'interpréter l'option +line,column
    @Override
    public boolean prepareCommand(List<String> args){
        ArrayList<String> otherArgs = new ArrayList<>();
        otherArgs.add(args.get(0));
        Matcher matcher;

        // remarque : s'il y a plusieurs fois l'option +line,column alors seule la dernière est prise en compte
        for(int i=1 ; i<args.size() ; i++){
            matcher = LINE_COLUMN_PATTERN.matcher(args.get(i));
            if(matcher.find()){ // args.get(i) est l'option +line,column
                Matcher nbMatcher = EXTRACT.matcher(matcher.group());
                nbMatcher.find();
                config.line = Integer.parseInt(nbMatcher.group());
                nbMatcher.find();
                config.column = Integer.parseInt(nbMatcher.group());
            }
            else {
                matcher = LINE_PATTERN.matcher(args.get(i));
                if(matcher.find()) { // args.get(i) est l'option +line
                    Matcher nbMatcher = EXTRACT.matcher(matcher.group());
                    nbMatcher.find();
                    config.line = Integer.parseInt(nbMatcher.group());
                    config.column = 1; // attention, commence à 1
                } else {
                    otherArgs.add(args.get(i)); // sinon, c'est une autre option
                }
            }
        }

        return super.prepareCommand(otherArgs);
    }

    @Override
    public void processCommand() {
        String name = "temp";
        String content = "";
        PseudoPath p;
        File<Data> data;

        if (tabsize!=null){
            int n;
            try {
                n = Integer.parseInt(tabsize);
            }
            catch (NumberFormatException ignore){
                stderr.add("Le paramètre " + tabsize + " est invalide pour l'option -T de la commande nano. Un entier strictement positif est attendu.");
                return;
            }
            config.tabsize = n;
        }

        if (InputPseudoPath.size()==0) {
            p = new PseudoPath(context.currentPath); //On récupère le chemin du fichier
            p.newFile = name;
        }
        else{
            p = InputPseudoPath.get(0);
            name = p.newFile;
        }

        if (!p.isFileExist()) {
            data = new File<>(name, Data.class, context.currentUser);
            addErrorMessages(data.addToFolder(p.getFolder(), context.fs, context.currentUser));
        }
        else {
            if (p.getChildFile().getType()!= Type.DATA){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.IS_NOT_DATA));
                return;
            }
            data = File.ConvertFile(p.getChildFile(), Data.class);
            content = data.getInodeData().getData(context.currentUser);
            if (content==null) {
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.PERMISSION, "lecture "+data.getName()));
                return;
            }

        }
        MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(DaemonMessage.NANO,new Object[]{name,content,data, config}));
        MetaContext.mainDaemon.getMessageNano();
    }

    // Reset la config à chaque nouvelle commande nano crée
    @Override
    public Command clone(Context c){
        Nano clone = (Nano) super.clone(c);
        clone.config = new NanoConfig();
        return clone;
    }

    public void LONGOPT_A_smarthome(){config.smarthome = true;}
    //public void LONGOPT_B_backup(){config.backup = true;}
    public void LONGOPT_E_tabstospaces(){config.tabtospaces = true;}
    public void LONGOPT_L_nonewlines(){config.nonewlines = true;}
    //public void LONGOPT_R_restricted(){config.restricted = true;}

    public void LONGOPT_T_tabsize(String arg){
        tabsize=arg;
    }

    //public void LONGOPT_Y_syntax(String arg){config.syntax = arg;}
    //public void LONGOPT_c_constantshow(){config.constantshow = true;}
    public void LONGOPT_e_emptyline(){config.emptyline = true;}
    public void LONGOPT_i_autoindent(){config.autoindent = true;}
    public void LONGOPT_l_linenumbers(){config.linenumbers = true;}
    public void LONGOPT_p_preserve(){config.preserve = true;}
    public void LONGOPT_t_saveonexit(){config.saveonexit = true;}
    public void LONGOPT_v_view(){config.view = true;}
    public void LONGOPT_w_nowrap(){config.nowrap = true;}
    public void LONGOPT_x_nohelp(){config.nohelp = true;}
    //public void LONGOPT_y_afterends(){config.afterends = true;}
}
