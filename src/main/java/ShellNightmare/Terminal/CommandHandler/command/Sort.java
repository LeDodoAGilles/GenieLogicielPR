package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Read)
public class Sort extends Command {
    @Override
    public void processCommand() {
        for (File<?> f : InputFiles) {
            if (f.getType() != Type.DATA) {
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.IS_NOT_DATA));
                continue;
            }
            File<Data> data = File.ConvertFile(f, Data.class);
            String dataS = data.getInodeData().getData(context.currentUser);
            List<String> ls = Arrays.stream(dataS.split("\n")).sorted().collect(Collectors.toList());
            stdout.add(String.join("\n", ls));
        }
    }
}
