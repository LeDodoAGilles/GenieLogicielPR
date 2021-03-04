package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@NeededParameters(value = "1")
public class Sleep extends Command {
    @Override
    public void processCommand() {
        String time = neededParameters.get(0);
        short i;
        try {
            i = Short.parseShort(time);
        }
        catch(Exception e){
            stderr.add("temps invalide: "+time);
            return;
        }
        try {
            Thread.sleep(i);
        } catch (InterruptedException ignore) {
        }
    }
}
