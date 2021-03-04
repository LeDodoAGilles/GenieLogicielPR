package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.Group;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.FileSystem.User;
/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

public class Groups extends Command {
    User user;
    @Override
    public void processCommand() {
        if (stdin.isEmpty())
            user = context.currentUser;
        for (String std : stdin){
            var ou = context.getUser(std);
            if (ou.isPresent()){
                StringBuilder sb = new StringBuilder();
                sb.append(std).append(" : ");
                for (Group g :context.getGroups())
                    if (g.getMembers().contains(user.uid))
                        sb.append(g.name);
                    stdout.add(sb.toString());
            }
            else
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_USER,std));
        }
    }
}
