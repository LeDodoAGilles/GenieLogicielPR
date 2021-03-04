package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.InterruptionException;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.NullInput;
/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(NullInput)
@NeededParameters(value = "1")
public class Exit extends Command {
    @Override
    public void processCommand() {
        String id = neededParameters.get(0);
        short i;
        try {
            i = Short.parseShort(id);
        }
        catch(Exception e){
            stderr.add("id invalide: "+id);
            return;
        }

        if (i>255 || i<0)
        {
            stderr.add("id invalide: "+id);
            return;
        }
        context.setEnvar("?",id);
        throw new InterruptionException("exit");
    }
}
