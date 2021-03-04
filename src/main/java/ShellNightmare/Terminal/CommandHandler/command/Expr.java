package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.IOStack;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.SuperRaw)
public class Expr extends Command {
    @Override
    public void processCommand() {
        if (stdin.size()!=3){
            addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_OPTION,String.join(" ",stdin)));
            return;
        }
        int n1,n2;
        try {
            n1 = Integer.parseInt(stdin.get(0));
            n2 = Integer.parseInt(stdin.get(2));
        }
        catch(NumberFormatException ignored){
            return;
        }
        int result;
        switch (stdin.get(1)) {


            case "%":
                result = n1 % n2;
                break;

            case "+":
                result = n1 + n2;
                break;
            case "-":
                result = n1 - n2;
                break;
            default:
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_OPTION,stdin.get(1)));
                return;
        }
        stdout.add(String.valueOf(result));
        
    }
}
