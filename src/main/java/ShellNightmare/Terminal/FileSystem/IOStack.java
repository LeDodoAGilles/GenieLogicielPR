package ShellNightmare.Terminal.FileSystem;

/**
 * classe représentant un message d'erreur
 * @author Gaëtan Lounes
 */
public class IOStack {
    public E_IOStatus status;
    public String message;


    IOStack(E_IOStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public static IOStack interpreterStack(E_IOStatus status, String message) {
        return new IOStack(status, message);
    }

    public static IOStack interpreterStack(E_IOStatus status) {
        return new IOStack(status, null);
    }
}
