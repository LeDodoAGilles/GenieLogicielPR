package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.*;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.IOStack;

@FileMode(CommandFileMode.Read)
@Permission
@NeededParameters(value = "1")
public class Chown extends Command {
    @Override
    public void processCommand() {
        var ou = context.getUser(neededParameters.get(0));
        if (ou.isEmpty()){
            addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_USER,neededParameters.get(0)));
            return;
        }
        for (File<?> f : InputFiles)
            f.getInode().setOwner(ou.get());
    }


}
