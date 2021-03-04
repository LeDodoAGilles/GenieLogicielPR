package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.FileSystem.*;
import ShellNightmare.Terminal.TypeContext;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.ASTLaunch;

import static ShellNightmare.Terminal.FileSystem.E_IOStatus.PERMISSION;
import static ShellNightmare.Terminal.FileSystem.IOStack.interpreterStack;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.Interpreter.launchCommand;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Read)
public class Bash extends Command {
    Boolean silent = false;
    @Override
    public void processCommand() {
        //TODO: execution avec les permissions de son créateur
        for(File<?> f : InputFiles) {
            if (f.getType() != Type.DATA) {
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.IS_NOT_DATA));
                continue;
            }

            if (!f.getInode().getPermission(context.currentUser).contains(Permission.EXECUTION)) { // pas de regex sans la permission de lecture
                addErrorMessages(interpreterStack(PERMISSION, "execution"));
                return;
            }
            Context c2 = context.clone();
            c2.type = TypeContext.SCRIPT;
            c2.historicalUser=(c2.historicalUser==null)?c2.currentUser:c2.historicalUser;
            c2.currentUser = f.getInodeData().getOwner();//on lance le script en tant que l'owner
            String data = File.ConvertFile(f, Data.class).getInodeData().getData(c2.currentUser);
            if (data==null)
            {
                addErrorMessages(IOStack.interpreterStack(PERMISSION)); //degueulasse
                return;
            }



            ASTLaunch astL = launchCommand(c2,data,silent);
            stdout.addAll(astL.getStdout());
            stderr.addAll(astL.getStderr());
        }
        silent = false;
    }

    public void OARG_i() {
        silent =true;
    }
}
