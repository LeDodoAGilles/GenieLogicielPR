package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.FileSystem.E_IOStatus;
import ShellNightmare.Terminal.FileSystem.IOStack;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.ASTbuilder;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Tree;
import ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.LexicalAnalysis;

import static ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Pattern.SEQCOMMAND;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.Interpreter.analysis;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@NeededParameters(value = "2")
public class Seq extends Command {

    @Override
    public void processCommand() {
        int n1=0,n2=0;
        try {
            n1 = Integer.parseInt(neededParameters.get(0));
            n2 = Integer.parseInt(neededParameters.get(1));
        }
        catch(NumberFormatException ignore){
            addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_NUMBER));
            return;
        }

        for (int i=n1;i<n2+1;i++)
            stdout.add(Integer.toString(i));
    }

}
