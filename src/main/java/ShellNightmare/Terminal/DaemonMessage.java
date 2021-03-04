package ShellNightmare.Terminal;

/**
 * classe représentant les messages que le Daemon peut envoyer/recevoir
 * @author Gaëtan Lounes
 */
public enum DaemonMessage {
    COMMAND,
    READ, READ_INPUT,
    MAN, MAN_EXIT,
    NANO, NANO_SAVE, NANO_EXIT,
    CLEAR,
    STDOUT, STDOUTni, STDERR,
    ESCAPED,

}
