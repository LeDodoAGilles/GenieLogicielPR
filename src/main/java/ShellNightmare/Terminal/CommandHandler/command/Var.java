package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.SuperRaw;
/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(SuperRaw)
public class Var extends Command {
    @Override
    public void processCommand() {
        for (var e: context.Envarset())
            if (!e.getValue().isEmpty())
                stdout.add(String.format("$%s:%s",e.getKey(),e.getValue()));
    }


}
