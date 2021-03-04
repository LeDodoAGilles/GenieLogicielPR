package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.IOStack;

@NeededParameters("1")
public class CutString extends Command {
    @Override
    public void processCommand() {
        int e1;
        try {
             e1= Integer.parseInt(neededParameters.get(0));
        }
        catch (Exception ignore){
            addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_NUMBER));
            return;
        }
        for (String input : stdin){
            if (input.isEmpty())
                continue;
            stdout.add(String.valueOf(input.charAt(input.length()<=e1-1?0:e1-1)));
        }
    }
}
