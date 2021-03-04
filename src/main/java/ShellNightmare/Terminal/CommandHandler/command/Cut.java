package ShellNightmare.Terminal.CommandHandler.command;

import ShellNightmare.Terminal.CommandHandler.Command;
import ShellNightmare.Terminal.CommandHandler.CommandFileMode;
import ShellNightmare.Terminal.CommandHandler.FileMode;
import ShellNightmare.Terminal.FileSystem.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * commande voir man pour le détail
 * @author Gaëtan Lounes
 */

@FileMode(CommandFileMode.Read)
public class Cut extends Command {
    String delimiter;
    String index;
    @Override
    public void processCommand() {
        ArrayList<Integer> whitelist = new ArrayList<>();
        if (delimiter==null){
            stderr.add("pas de délimiteur");
        }

        if (index != null){
            try {
                if (index.contains(":")){
                    int indexI,indexF;
                    indexI=Short.parseShort(index.substring(0,index.lastIndexOf(":")));
                    indexF=Short.parseShort(index.substring(index.lastIndexOf(":")+1));

                    if (indexF<=indexI) {
                        addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_NUMBER, index));
                        return;
                    }
                    for (int i=indexI;i<indexF;i++)
                        whitelist.add(i);
                }
                else
                    if(index.contains(",")){
                        var array = index.split(",");
                        for (String s: array){
                            whitelist.add(Integer.parseInt(s));
                        }

                    }

                else
                    whitelist.add(Integer.parseInt(index));

            }
            catch(Exception e){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.INVALID_NUMBER));
                return;
            }
        }


        for (File<?> f : InputFiles){
            if (f.getType() != Type.DATA){
                addErrorMessages(IOStack.interpreterStack(E_IOStatus.IS_NOT_DATA));
                continue;
            }
            File<Data> data = File.ConvertFile(f,Data.class);
            String dataS = data.getInodeData().getData(context.currentUser);
            for (String lines : dataS.split("\n")){
                ArrayList<String> arrayS = Arrays.stream(lines.split(delimiter)).filter(e->!e.equals("")).collect(Collectors.toCollection(ArrayList::new));
                if (whitelist.isEmpty()){
                    stdout.addAll(arrayS);
                }
                else {
                   stdout.add(whitelist.stream().filter(e->e<arrayS.size()).map(arrayS::get).collect(Collectors.joining(delimiter)));
                }
            }



        }
    }



    public void OARG_f(String index) {
        this.index = index;
    }

    public void OARG_d(String delimiter) {
        this.delimiter = delimiter;
    }
}
