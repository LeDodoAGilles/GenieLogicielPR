package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

public class Functions extends Command {
    String name=null;
    @Override
    public void processCommand() {
        if (name!=null){
            context.setEnvar("?",context.getFunctions().contains(name)?"0":"1");
            return;
        }
        stdout.addAll(context.getFunctions());
    }


    public void OARG_c() {
        for (var c : context.getFunctions())
            context.removeFunction(c);
    }

    public void OARG_e(String name) {
        this.name = name;
    }
}
