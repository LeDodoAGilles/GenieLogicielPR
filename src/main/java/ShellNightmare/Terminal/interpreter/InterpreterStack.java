package ShellNightmare.Terminal.interpreter;

/**
 * objet représentant une erreur
 * @author Gaëtan Lounes
 */
public class InterpreterStack {
    public E_InterpreterStatus status;
    public String message;


    InterpreterStack(E_InterpreterStatus status,String message){
        this.status = status;
        this.message = message;
    }

    public static InterpreterStack interpreterStack(E_InterpreterStatus status,String message){
        return new InterpreterStack(status,message);
    }
}
