package ShellNightmare.Terminal.interpreter;
import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.MetaContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ShellNightmare.Terminal.TypeContext.SCRIPT;
import static ShellNightmare.Terminal.interpreter.E_InterpreterStatus.OK;
import static ShellNightmare.Terminal.interpreter.E_InterpreterStatus.UNKNOWN_INPUT;
import static ShellNightmare.Terminal.interpreter.InterpreterStack.interpreterStack;

/**
 * classe représentant un interpréteur de commande basique : commande + paramètre
 * @author Gaëtan Lounes
 */
public class ExplicitCommandInterpreter {
    Context context;
    public ExplicitCommandInterpreter(Context c){
    this.context = c;
    }


    public List<String> stdout,stderr;


    public InterpreterStack processCommand(String ...a){
        return processCommand(Arrays.stream(a).collect(Collectors.toList()));
    }


    public InterpreterStack processCommand(List<String> commandArgument){
        if (commandArgument.isEmpty())
            return interpreterStack(E_InterpreterStatus.WRONG_INPUT,"\ninputvide\n");
        String coreCommand = commandArgument.get(0);
        if (!MetaContext.registerC.getCommandsString().contains(coreCommand))
            return interpreterStack(E_InterpreterStatus.WRONG_INPUT,"commande \\e[31minconnue\\e[0m : "+ coreCommand);
        Optional<Command> cc = context.getCommands().stream().filter(c->c.name.equals(coreCommand)).findAny();
        if (cc.isEmpty())
            return interpreterStack(E_InterpreterStatus.WRONG_INPUT,"commande \\e[31minterdite\\e[0m : "+ coreCommand);
        Command cCheck= cc.get();
        E_InterpreterStatus d = UNKNOWN_INPUT;
        Command c = cCheck.clone(context);
        c.clearCommandArray();
        context.setEnvar("?","-1");
        if (c.prepareCommand(new ArrayList<>(commandArgument))) {
            c.processCommand();
            d = OK;
        }

        stdout = c.getStdout();
        stderr = c.getStderr();

        if (context.type!=SCRIPT || context.currentUser != context.rootUser)
            context.simplehistory.add(String.join(" ",commandArgument));


        //envar
        if (context.getEnvar("?").equals("-1"))
            context.setEnvar("?",stderr.isEmpty()?"0":"1");

        return interpreterStack(d,null);
    }
}
