package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.*;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Read)
public class Cat extends Command {
    @Override
    public void processCommand() {
        for(File<?> f : InputFiles){
            if (f.getType()!= Type.DATA){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.IS_NOT_DATA));
                continue;
            }
            String data = File.ConvertFile(f, Data.class).getInodeData().getData(context.currentUser);
            if (data==null){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.PERMISSION,f.getName()));
            }
            else
            stdout.add(data);
        }
    }
}
