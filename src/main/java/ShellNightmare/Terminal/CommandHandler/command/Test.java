package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.FileSystem.PseudoPath;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.SuperRaw)
public class Test extends Command { //TODO : les options les plus simples genre existences fichiers/ arithmetiques facile
    @Override
    public void processCommand() {
        boolean result = false;
        if (stdin.size()==2){
            switch(stdin.get(0)){
                case "-n":
                    result = stdin.get(1).isEmpty();
                    break;
                case "-z":
                    result = !stdin.get(1).isEmpty();
                    break;
                case "-f":
                    String fileName = stdin.get(1);
                    PseudoPath p = new PseudoPath(context.currentPath);
                    p.setPseudoPath(fileName);
                    result=p.isFileExist();
                    break;

                default:
                    stderr.add("invalide option");
                    return;
            }
        }
        if (stdin.size()==3){
            try {
                switch (stdin.get(1)) {
                    case "=":
                        result = stdin.get(0).replace("\n","").equals(stdin.get(2).replace("\n",""));
                        break;
                    case "!=":
                        result = !stdin.get(0).replace("\n","").equals(stdin.get(2).replace("\n",""));
                        break;
                    case "-gt":
                        int e1 = Integer.parseInt(stdin.get(0));
                        int e2 = Integer.parseInt(stdin.get(2));
                        result = e1 > e2;
                        break;

                    case "-ge":
                        e1 = Integer.parseInt(stdin.get(0));
                        e2 = Integer.parseInt(stdin.get(2));
                        result = e1 >= e2;
                        break;

                    case "-eq":
                        e1 = Integer.parseInt(stdin.get(0));
                        e2 = Integer.parseInt(stdin.get(2));
                        result = e1 == e2;
                        break;
                    default:
                        stderr.add("option invalide");
                        return;
                }
            }
            catch(NumberFormatException ignored){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_NUMBER));
            }
        }




        context.setEnvar("?",result?"0":"1");

    }

}
