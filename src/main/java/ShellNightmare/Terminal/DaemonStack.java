package ShellNightmare.Terminal;

/**
 * classe représentant les messages du Daemon
 * @author Gaëtan Lounes
 */
public class DaemonStack {
    Object message;
    DaemonMessage type;

    DaemonStack(DaemonMessage type,Object message) {
        this.message = message;
        this.type = type;
    }

    public static DaemonStack DaemonStack(DaemonMessage type, Object message) {
        return new DaemonStack(type,message);
    }
    public static DaemonStack DaemonStack(DaemonMessage type) {
        return new DaemonStack(type, null);
    }

    public Object getMessage() {
        return message;
    }
}
