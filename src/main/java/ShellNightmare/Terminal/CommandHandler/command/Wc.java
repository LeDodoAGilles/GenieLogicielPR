package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.*;

import java.util.HashSet;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.Read)
public class Wc extends Command {
    HashSet<Character> s = new HashSet<>();
    @Override
    public void processCommand() {
        if (s.isEmpty()){
            s.add('m');
            s.add('l');
            s.add('c');
            s.add('w');
        }

        StringBuilder sb = new StringBuilder();
        for (File<?> f : InputFiles){
            if (f.getType()!= Type.DATA){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.IS_NOT_DATA));
                return;}

                String data = File.ConvertFile(f, Data.class).getInodeData().getData(context.currentUser);
                if (data==null){
                    addErrorMessages(IOStack.interpreterStack(E_IOStatus.PERMISSION,f.getName()));
                    continue;
                }
                if (s.contains('m'))
                    sb.append(String.format("%d\t",data.length()));
                if (s.contains('w'))
                    sb.append(String.format("%d\t",data.trim().split(" ").length));//TODO: corriger
                if (s.contains('c'))
                    sb.append(String.format("%d\t",data.length()+144)); //TODO: faire un truc plus propre
                if (s.contains('l')){
                    if (data.length()==0)
                        sb.append("0\t");
                    else
                        sb.append(data.split("\n").length).append("\t");
                }
                sb.append(f.getPath()).append("\n");



            }
        s.clear();
        if (sb.length()==0)
            return;
        sb.deleteCharAt(sb.length()-1);

        stdout.add(sb.toString());

    }


    public void OARG_m() {
        s.add('m');
    }
    public void OARG_l() {
        s.add('l');
    }
    public void OARG_c() {
        s.add('c');
    }
    public void OARG_w() {
        s.add('w');
    }
}
