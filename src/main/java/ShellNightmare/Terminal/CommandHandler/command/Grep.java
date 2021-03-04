package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.FileSystem.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Read)
@NeededParameters(value = "1")
public class Grep extends Command { //que le remplacement
    @Override
    public void processCommand() {
        final Pattern p = Pattern.compile(neededParameters.get(0));
        for (File<?> f : InputFiles){
            if (f.getType() != Type.DATA){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.IS_NOT_DATA));
                continue;
            }
            File<Data> data = File.ConvertFile(f,Data.class);
            String dataText = data.getInodeData().getData(context.currentUser);
            if (dataText==null){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.PERMISSION,f.getName()));
                continue;
            }
            var lines = dataText.split("\n");
            for (String line : lines)
            {
                final Matcher m = p.matcher(line);
                int counter = 0;
                while (m.find()) {
                    counter++;
                }
                if (counter>0)
                    stdout.add(line);
            }
        }

    }
}
