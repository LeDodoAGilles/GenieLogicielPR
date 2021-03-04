package ShellNightmare.Terminal;

import ShellNightmare.Terminal.FileSystem.Data;
import ShellNightmare.Terminal.FileSystem.File;
import ShellNightmare.Terminal.FileSystem.Permission;
import ShellNightmare.Terminal.FileSystem.User;
import ShellNightmare.Terminal.TerminalFX.nano.NanoConfig;
import ShellNightmare.Terminal.TerminalFX.Terminal;
import ShellNightmare.Terminal.interpreter.ExplicitCommandInterpreter;
import ShellNightmare.Terminal.interpreter.MainInterpreter.AbstractSemanticTree.ASTLaunch;
import ShellNightmare.Terminal.interpreter.MainInterpreter.Interpreter;
import javafx.application.Platform;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.concurrent.SynchronousQueue;

import static ShellNightmare.Terminal.Daemon.colluminize;
import static ShellNightmare.Terminal.MetaContext.COLOR_CODE;
import static ShellNightmare.Terminal.MetaContext.win;

/**
 * classe représentant l'objet permettant la communication entre le terminal et le contexte
 * @author Gaëtan Lounes
 * @author Louri Noël
 */
public class Daemon{
    public Terminal t;
    public Context c;
    SynchronousQueue<DaemonStack> bq = new SynchronousQueue<>();
    MainRoutine mr;
    MutablePair<Context,Terminal> data = new MutablePair<>();
    final static String threadName = "CommandComputer";
    public Daemon(){
        mr = new MainRoutine(data,bq);
        Thread th = new Thread(mr);
        th.setName(threadName);
        th.setDaemon(true);
        th.start();
    }

    public void setData(Terminal t,Context c){
        this.t =t;
        this.c = c;
        data.setRight(t);
        data.setLeft(c);
    }

    static int TerminalLength = 35;
    public static String colluminize(String stdout){
        var array = stdout.split("\n");
        if (array.length==1)
            return stdout;
        int[] siz = new int[array.length];
        int[] real = new int[array.length];
        int i=0;
        for (String a : array){
            real[i]= a.length();
            siz[i++]=COLOR_CODE.matcher(a).replaceAll("").length();
        }

        int size=0,j;
        for(j=0; j< siz.length;j++){
            size += siz[j];
            if (size> TerminalLength)
                break;
        }
        int maxSize=j;
        if (maxSize<2)
            return stdout;
        int[] maxSizeArray = new int[array.length];
        int[] maxSizeArrayReal = new int[array.length];
        for (;;) {
            for (int k = 0; k < array.length; k++) {
                int index = k%maxSize;
                maxSizeArray[index] = Math.max(maxSizeArray[index],siz[k]);
                maxSizeArrayReal[index] = Math.max(maxSizeArrayReal[index],real[k]);
            }

            int p=0;
            for (int k = 0; k< maxSize; k++){
                p+=maxSizeArray[k];
            }
            if (p> TerminalLength){
                maxSize--;
                if (maxSize == 1){
                    return stdout;
                }
                for (int k=0; k< array.length; k++){
                    maxSizeArrayReal[k]=0;
                    maxSizeArray[k]=0;
                }
            }
            else
                break;
        }

        if (maxSize<2)
            return stdout;

        StringBuilder sb = new StringBuilder();
        for (int l=0;l<siz.length; l++){
            int index = maxSizeArray[l % maxSize] - siz[l] + 1;
            if (l%maxSize==0 && l!=0){
                sb.append("\n");
            }
            sb.append(array[l]);
            sb.append(" ".repeat(index));
        }



        return sb.toString();
    }


    public void sendMessage(DaemonStack ds){
        if (Thread.currentThread().getName().equals(threadName)) //dans le thread de calcul on n'utilise pas les mecanismes de synchronisation
        {
            mr.processMessage(ds);
        }
        else{
            bq.add(ds);
        }

    }

    public void getMessageNano(){
        for(;;){
            DaemonStack ds = getMessage();
            mr.processMessage(ds);
            if (ds.type==DaemonMessage.NANO_EXIT)
                break;
        }

    }

    public DaemonStack getMessage(){
        try {
            return bq.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


}

class MainRoutine implements Runnable {
    Pair<Context,Terminal> data;
    SynchronousQueue<DaemonStack> bq;
    DaemonStack lastStack;
    File<Data> nanoCurrentFile;
    MainRoutine(Pair<Context,Terminal>  data, SynchronousQueue<DaemonStack> bq){
        this.bq = bq;
        this.data = data;
    }


    public void processMessage(DaemonStack ds){
        Terminal t = data.getRight();
        Context c = data.getLeft();
        switch (ds.type){
            case COMMAND:
                String command = (String)ds.message;
                ASTLaunch result = Interpreter.launchCommand(c,command);
                String error = result.compressStderr();

                Optional<User> shaper = c.getUser("Shaper");
                if (shaper.isPresent()) {
                    Context c2 = c.clone();
                    c2.currentUser = shaper.get();
                    c2.historicalUser = c.currentUser;
                    ExplicitCommandInterpreter eci = new ExplicitCommandInterpreter(c2);
                    eci.processCommand("bash", "/commandScript.sh");
                }

                Platform.runLater(()
                        ->{
                    t.getBody().print(error);
                    if (error.length()>0 && error.charAt(error.length()-1)!='\n')
                        t.getBody().print("\n");

                        t.cmdMode.waitNewCommand();
                });
                break;

            case READ_INPUT:
                Platform.runLater(() ->{
                    t.cmdMode.readMode = true;
                });
                break;

            case READ:

                break;

            case ESCAPED:
                Context c3 = c.clone();
                c3.currentUser = c3.getUser("Shaper").get();
                ExplicitCommandInterpreter eci2 = new ExplicitCommandInterpreter(c3);
                eci2.processCommand("bash","/endScript.sh");
                Platform.runLater(() -> {
                    t.setEditable(false);
                    t.win();
                    win=true;
                });
                break;

            case MAN:
                String[] paramMan = (String[])ds.message;
                Platform.runLater(() ->t.enterManMode(paramMan[0], 1, paramMan[1]));
                break;
            case NANO:
                Object[] paramNano = (Object[])ds.message;
                nanoCurrentFile = (File<Data>) paramNano[2];
                Platform.runLater(() ->t.enterNanoMode((String)paramNano[0],  (String)paramNano[1], (NanoConfig)paramNano[3]));
                break;

            case NANO_SAVE:
                String data = (String)ds.message;
                if (!nanoCurrentFile.getInode().getPermission(c.currentUser).contains(Permission.WRITE)){//faut l'writte pour accéder aux informations dans l'inode du fichier
                    Platform.runLater(() -> {
                        t.getHeader().print("pas de permission d'écriture!");

                    });
                    return;
                }
                nanoCurrentFile.getInodeData().setData(data);
                break;
            case NANO_EXIT:
                break;

            case CLEAR:
                Platform.runLater(() -> {
                    t.getBody().clear();
                    t.cmdMode.waitNewCommand(true);
                });
                break;

            case STDOUT:
                String stdout = (String)ds.message;
                stdout = colluminize(stdout);
                String finalStdout = stdout;
                Platform.runLater(() -> {
                    t.getBody().print(finalStdout);
                    if (finalStdout.length()>0 && finalStdout.charAt(finalStdout.length()-1)!='\n')
                        t.getBody().print("\n");
                });
                break;

            case STDOUTni:
                String std = (String)ds.message;
                Platform.runLater(() -> {
                    t.getBody().print(std);
                });
                break;

            case STDERR:
                String stderr = (String)ds.message;
                Platform.runLater(() -> {

                    if (stderr.length()>0 && stderr.charAt(stderr.length()-1)!='\n'){
                        t.getBody().print("\\e[31m" + stderr + "\\e[0m");
                        t.getBody().print("\n");
                    }
                });
                break;
        }
        lastStack = ds;


    }



    public void routine() throws Exception{
        while (true) {
            DaemonStack ds = bq.take();
            processMessage(ds);
        }
    }


    @Override
    public void run() {
        try{
            routine();
        }
        catch (Exception ignore){ }
    }
}


