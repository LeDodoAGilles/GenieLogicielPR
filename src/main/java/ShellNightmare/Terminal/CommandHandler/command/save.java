package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.Permission;
import ShellNightmare.Terminal.Context;

import java.io.*;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@Permission
public class save extends Command {
    @Override
    public void processCommand() {
        try {
            File doc = new File("context.bin");
            FileOutputStream file = new FileOutputStream(doc);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(context);
            out.close();
            file.close();
        }

    catch (Exception e){
        stderr.add(e.getMessage());
    }
}
}
