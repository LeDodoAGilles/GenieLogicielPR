package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;

public class Strlen extends Command {
    @Override
    public void processCommand() {
        int i =0;
        for (String std : stdin){
            i+= std.length();
        }
        stdout.add(Integer.toString(i));
    }
}
