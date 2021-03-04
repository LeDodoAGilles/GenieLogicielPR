package ShellNightmare.Terminal.interpreter.MainInterpreter;

import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.Daemon;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.ASTLaunch;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.ASTbuilder;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.InterruptionException;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Tree;
import ShellNightmare.Terminal.interpreter.MainInterpreter.LexicalAnalysis.LexicalAnalysis;

import java.util.ArrayList;

import static ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Pattern.SEQCOMMAND;
import static ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.Pattern.SILENTSEQCOMMAND;
/**
 * classe permettant de lancer l'interpréteur
 * @author Gaëtan Lounes
 */
public class Interpreter {
    public static LexicalAnalysis analysis(String command){
        LexicalAnalysis la = new LexicalAnalysis(new StringBuilder(command));
        la.Structuralanalyze();
        la.subStructureAnalysis();
        la.flatten();
        return la;
    }
    public static ASTLaunch launchCommand(Context c, String command){
        return launchCommand(c,command,false);
    }


    public static ASTLaunch launchCommand(Context c, String command, boolean silent){
        var la = analysis(command);
        String error = null;
        ASTbuilder ast = new ASTbuilder();
        Tree t = new Tree(silent?SILENTSEQCOMMAND:SEQCOMMAND);
        try{
            ast.build(t,la.tokened3);
        }
        catch (Exception e){
            error = e.getMessage();
        }

        ASTLaunch astL = new ASTLaunch(t,c);
        if (error!=null)
        {
            astL.setStdout(new ArrayList<>());
            astL.setStderr(new ArrayList<>());
            astL.getStderr().add(error);
            return astL;
        }
        try {
            astL.run();
        }
        catch (Exception e){
            if (e instanceof InterruptionException)
                return astL;
            astL.getStderr().add(e.getMessage());
        }
        return astL;
    }

}
