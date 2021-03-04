package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.CommandHandler.NeededParameters;
import ShellNightmare.Terminal.FileSystem.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */
@FileMode(CommandFileMode.Read)
@NeededParameters(value = "1")
public class Sed extends Command { //seulement le remplacement et la suppression regex pour le moment
    @Override
    public void processCommand() {
        var instructions = neededParameters.get(0).split("[^\\\\](;)");
        for (String instruction : instructions){
            switch(instruction.charAt(0)){
                case 's':{
                    if (!instruction.matches("(.*[^\\\\]/){3}")){
                        stderr.add("invalid pattern");
                        break;
                    }
                    boolean greedy = instruction.charAt(instructions.length-1)=='g';
                    var command = instruction.split("(?<!\\\\)" + Pattern.quote("/"));

                    Pattern p = Pattern.compile(command[1]);

                    for (File<?> f : InputFiles){
                        if (f.getType() != Type.DATA){
                            addErrorMessages(IOStack.interpreterStack(E_IOStatus.IS_NOT_DATA));
                            continue;
                        }
                        File<Data> data = File.ConvertFile(f,Data.class);
                        StringBuilder sb = new StringBuilder();
                        String dataFile = data.getInodeData().getData(context.currentUser);
                        if (dataFile == null) {
                            addErrorMessages(IOStack.interpreterStack(E_IOStatus.PERMISSION, f.getName()));
                            continue;
                        }

                        var lines = dataFile.split("\n");


                        for (String line : lines){
                            Matcher m = p.matcher(line);
                            if (!m.find())
                                continue;
                            String lineAltered = line.replaceFirst(command[1],command[2]);

                            if (greedy)
                                lineAltered = lineAltered.replaceAll(command[1],command[2]);
                            sb.append(lineAltered);
                        }
                        stdout.add(sb.toString());
                    }
                    break;
                }

            }

        }


    }
}
