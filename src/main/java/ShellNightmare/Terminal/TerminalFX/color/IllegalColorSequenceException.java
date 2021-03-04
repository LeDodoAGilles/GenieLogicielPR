package ShellNightmare.Terminal.TerminalFX.color;

public class IllegalColorSequenceException extends RuntimeException {
    public IllegalColorSequenceException(String msg, Throwable err){
        super(msg, err);
    }

    public IllegalColorSequenceException(String msg){
        super(msg);
    }
}
