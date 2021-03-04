package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.CommandHandler.Permission;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.Group;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.FileSystem.User;

import static ShellNightmare.Terminal.FileSystem.E_IOStatus.*;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@Permission
@NeededParameters(value = "1")
public class Useradd extends Command {
    String password="";
    String group=null;
    boolean remove = false;
    @Override
    public void processCommand() {
        String newUser = neededParameters.get(0);

        if (remove){
            context.removeUser(newUser);
            return;
        }

        if (group!=null)
        {
            var og = context.getGroup(group);
            if (og.isEmpty()){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_GROUP));
                return;
            }
            Group g =og.get();

            var ou = context.getUser(newUser);
            if (ou.isEmpty()) {
                addErrorMessages(IOStack.interpreterStack(INVALID_USER, newUser));
                return;
            }
            g.addMember(ou.get());
            return;
        }


        if (context.getUser(newUser).isPresent()) {
            addErrorMessages(IOStack.interpreterStack(ALREADY_EXIST_USER, newUser));
            return;
        }

        context.addNewUser(newUser,password);

    }

    public void OARG_p(String pass) {
        password=pass;
    }

    public void OARG_G(String g) {
        group=g;
    }

    public void OARG_r() {
        remove=true;
    }
}
