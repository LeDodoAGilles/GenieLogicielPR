package ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree;

/**
 * utilisé pour arreter l'exécution d'un AST
 * @author Gaëtan Lounes
 */
public class InterruptionException extends RuntimeException {
    public InterruptionException(String msg) {
        super(msg);
    }

    {
    }
}
