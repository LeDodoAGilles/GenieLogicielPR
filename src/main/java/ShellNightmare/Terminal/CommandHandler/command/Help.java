package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.MetaContext;

import java.util.concurrent.ConcurrentHashMap;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.NullInput;
/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(NullInput)
public class Help extends Command {
    @Override
    public void processCommand() {
        String color="";
         var command = MetaContext.registerC.getCommands().iterator();
        while(command.hasNext()){
            Command c = command.next();
            if (context.getBlacklistedCommand().contains(c.name)){
                color = "\\e[31m";
            }
            else {
                if (c.getNeedSudoPermission())
                    color = "\\e[34m";
                else
                    color = "\\e[32m";
            }
            stdout.add(String.format("%s%s%s",color,c.name,command.hasNext()?"":"\\e[0m"));
        }

    }


}
