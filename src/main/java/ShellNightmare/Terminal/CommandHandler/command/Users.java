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
public class Users extends Command {
    @Override
    public void processCommand() { //TODO: affichier UID
                for (User u :context.getUsers())
                    if (u.mainGroup.gid==0)//root
                        stdout.add(String.format("\\e[31m%s\\e[0m",u.name));
                    else
                        stdout.add(u.name);
            }

}
