package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.ASTbuilder;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Tree;
import ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.LexicalAnalysis;

import static ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Pattern.SEQCOMMAND;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.Interpreter.analysis;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@NeededParameters(value = "1")
public class Alias extends Command {

    @Override
    public void processCommand() {
        String param = neededParameters.get(0);
        var sa = param.split("=");
        if (sa.length==1)
        {
            stderr.add("invalid alias");
            return;
        }
        String commandName = sa[0];
        String command=sa[1];

        LexicalAnalysis la = analysis(command);
        String error = null;
        ASTbuilder ast = new ASTbuilder();
        Tree t = new Tree(SEQCOMMAND);
        try{
            ast.build(t,la.tokened3);
        }
        catch (Exception e){
            error = e.getMessage();
        }
        if (error!=null) {
            stderr.add(error);
            return;
        }
        context.alias.put(commandName,t);
    }

}
