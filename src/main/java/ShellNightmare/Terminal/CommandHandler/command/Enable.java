package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.Permission;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.FileSystem.User;

import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.Raw;
import static ShellNightmare.Terminal.CommandHandler.CommandFileMode.SuperRaw;
/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@Permission
@FileMode(Raw)
public class Enable extends Command {
    String user=null;
    @Override
    public void processCommand() {
        User u = null;
        if (user!=null){
            var ou = context.getUser(user);
            if (ou.isEmpty()) {
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_USER, user));
                return;
            }
            u=ou.get();
        }

        for (String s: stdin){
            if (!context.getCommandsName().contains(s)){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_COMMAND,s));
            }
            else
                context.removeBlackListCommand(s,u);
        }
    }

    public void OARG_u(String user) {
        this.user = user;
    }
}
