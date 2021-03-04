package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.FileSystem.User;
import org.apache.commons.codec.digest.DigestUtils;

import static ShellNightmare.Terminal.FileSystem.E_IOStatus.INVALID_PASSWORD;
import static ShellNightmare.Terminal.FileSystem.E_IOStatus.INVALID_USER;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.Raw)
public class Su extends Command {
    String user="root";
    @Override
    public void processCommand() {
        String pass = stdin.isEmpty()? "":stdin.get(0);
        var ou  = context.getUser(user);
        if (ou.isEmpty()){
            addErrorMessages(IOStack.interpreterStack(INVALID_USER,user));
            return;
        }
        User u = ou.get();
        if (u.isPasswordValid(pass)||context.currentUser==context.rootUser){
            context.currentUser=u;
        }else
        {
            addErrorMessages(IOStack.interpreterStack(INVALID_PASSWORD,user));
            return;
        }

    }

    public void OARG_l(String user) {
        this.user = user;
    }
}
