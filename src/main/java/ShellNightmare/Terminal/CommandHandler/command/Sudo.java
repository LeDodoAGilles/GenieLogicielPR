package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.DaemonStack;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.MetaContext;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.ASTLaunch;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;

import static ShellNightmare.Terminal.DaemonMessage.READ_INPUT;
import static ShellNightmare.Terminal.DaemonMessage.STDOUT;
import static ShellNightmare.Terminal.FileSystem.E_IOStatus.INVALID_PASSWORD;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.Interpreter.launchCommand;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.SuperRaw)
@NeededParameters(value="1")
public class Sudo extends Command {
    @Override
    public void processCommand() {
        boolean rootlog = false;
        if (context.rootAccessDeadline.before(new Date())) {
            MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(STDOUT, "root Password >: "));
            MetaContext.mainDaemon.sendMessage(DaemonStack.DaemonStack(READ_INPUT));
            DaemonStack e = MetaContext.mainDaemon.getMessage();
            String message = (String) e.getMessage();
            rootlog = context.rootUser.hash.equals(DigestUtils.sha512Hex(message));
            if (rootlog)
                context.rootAccessDeadline.setTime(new Date().getTime()+36000); // TODO: mettre un vrai machin pour le log
        }
        else
            rootlog =true;

        if (rootlog){
            Context croot = context.clone();
            croot.currentUser=croot.rootUser;
            ASTLaunch astL = launchCommand(croot,neededParameters.get(0)+" "+String.join(" ",stdin),false);
            stdout.addAll(astL.getStdout());
            stderr.addAll(astL.getStderr());
        }
        else
        {
            addErrorMessages(IOStack.interpreterStack(INVALID_PASSWORD,"root"));
            return;
        }

    }
}
