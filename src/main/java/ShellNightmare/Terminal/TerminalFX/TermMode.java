package ShellNightmare.Terminal.TerminalFX;

import javafx.scene.input.KeyEvent;

public abstract class TermMode {
    protected final Terminal terminal;

    public TermMode(Terminal terminal){
        this.terminal = terminal;
    }

    /** Empêche de modifier la zone non-editable en tapant un caractère imprimable.
     * Renvoie true si l'event a été utilisé (mais pas forcément consommé).
     * Le TermArea indique si lequel des header/body/footer est affecté. */
    public abstract boolean filterTypedKey(KeyEvent e, TermArea a);

    /** Empêche de modifier la zone non-editable en tapant un caractère non-imprimable.
     * Renvoie true si l'event a été utilisé (mais pas forcément consommé).
     * Le TermArea indique si lequel des header/body/footer est affecté. */
    public abstract boolean filterPressedKey(KeyEvent e, TermArea a);

    /** Renvoie true si l'event a été utilisé.
     * Le TermArea indique si lequel des header/body/footer est affecté. */
    public abstract boolean onKeyPressed(KeyEvent e, TermArea a);
}
