package ShellNightmare.Terminal.interpreter;

import ShellNightmare.Terminal.Context;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.ASTLaunch;

import static ShellNightmare.Terminal.interpreter.MainInterpreter.Interpreter.launchCommand;
/**
 * classe utilisée pour lancer les tests unitaires
 * @author Gaëtan Lounes
 */
public class Tester {
    private Context c;
    private ASTLaunch astL;
    public Tester(Context c){
        this.c = c;
    }

    public void launch(String command) throws AssertionError{
         astL = launchCommand(c,command);
    }

    public void noError(String command) throws AssertionError{
        launch(command);
        if (!astL.getStderr().isEmpty())
            throw new AssertionError(astL.compressStderr());
    }

    public void errorExpected(String command){
        launch(command);
        if (astL.getStderr().isEmpty())
            throw new AssertionError(command);
    }

    public void expectedResult(String command, String result){
        expectedResult(command, result,true);
    }
    public void expectedResult(String command,String result,boolean noError){
        if (noError)
            noError(command);
        else
            launch(command);

        if (!astL.compressStdout().equals(result))
            throw new AssertionError(String.format("sortie :<|%s|>; expected :<|%s|>",astL.compressStdout(),result));
    }



}
