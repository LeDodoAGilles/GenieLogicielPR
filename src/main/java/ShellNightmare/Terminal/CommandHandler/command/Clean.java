package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.*;


import static ShellNightmare.Terminal.MetaContext.COLOR_CODE;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Read)
public class Clean extends Command {
    @Override
    public void processCommand() {
        for (File<?> f : InputFiles) {
            if (f.getType() != Type.DATA) {
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.IS_NOT_DATA));
                continue;
            }
            File<Data> data = File.ConvertFile(f, Data.class);
            String dataText = data.getInodeData().getData(context.currentUser);

            String replacement = COLOR_CODE.matcher(dataText).replaceAll("");
            stdout.add(replacement);
        }
    }
}
